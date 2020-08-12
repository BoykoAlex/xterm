package org.springframework.ide.eclipse.terminal.pty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.eclipse.terminal.model.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.pty4j.PtyProcess;

public class PtyProcessManager {
	
	private static final Logger log = LoggerFactory.getLogger(PtyProcessManager.class);
	
	private Map<String, PtyProcessInfo> processes = new HashMap<>();
	private ThreadPoolTaskScheduler taskExecutor;
		
	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}
	
	public PtyProcessManager(ThreadPoolTaskScheduler taskExecutor) {
		this.taskExecutor = taskExecutor;
		
	}
	
	private PtyProcessInfo create(String id, String cwd, Collection<String> ptyParams) throws IOException {
		String[] cmd = new String[1 + (ptyParams == null ? 0 : ptyParams.size())];
		cmd[0] = isWindows() ? "powershell.exe" : "/bin/bash";
		int index = 1;
		if (ptyParams != null) {
			for (String param : ptyParams) {
				cmd[index++] = param;
			}
		}
		
		PtyProcess pty = PtyProcess.exec(cmd, System.getenv(), cwd, false, true, null);
		PtyProcessInfo processInfo = new PtyProcessInfo(pty, id, taskExecutor);
		
		taskExecutor.createThread(() -> {
			byte[] buffer = new byte[4096];
			try {
				int length = 0;
				while ((length = pty.getInputStream().read(buffer)) != -1) {
					if (length > 0) {
						broadcastData(processInfo, Arrays.copyOf(buffer, length));
					}
				}
			} catch (IOException e) {
				log.error("", e);
			}
		}).start();
		
		return processInfo;
	}
	
	public void broadcastData(PtyProcessInfo processInfo, byte[] byteData) throws IOException {
		String data = new String(byteData, StandardCharsets.UTF_8);
		Message msg = Message.dataMessage(processInfo.getId(), data);
		
		for (WebSocketSession socket : processInfo.sockets) {
			if (socket.isOpen()) {
				socket.sendMessage(new TextMessage(msg.toString()));
			}
		}
		
        if (processInfo.isOverBufferSize()) {
        	processInfo.getBuffer().poll();
        }
        processInfo.getBuffer().add(msg.getData());
	}
	
	public synchronized PtyProcessInfo createOrConnect(WebSocketSession ws, String id, String cwd, Collection<String> ptyParams) throws IOException {
		PtyProcessInfo processInfo = this.processes.get(id);
		if (processInfo == null) {
			processInfo = create(id, cwd, ptyParams);
			this.processes.put(id, processInfo);
		}
		connectSocket(ws, processInfo);
		return processInfo;
	}
	
	public synchronized PtyProcessInfo get(String id) throws IOException {
		return this.processes.get(id);
	}
	
	public synchronized void disconnectSocket(WebSocketSession ws) {
		for (Map.Entry<String, PtyProcessInfo> e : processes.entrySet()) {
			PtyProcessInfo ptyProcessInfo = e.getValue();
			if (ptyProcessInfo.sockets.remove(ws)) {
				ptyProcessInfo.terminationFuture = taskExecutor.getScheduledExecutor().schedule(() -> this.terminatePty(e.getKey()), 60, TimeUnit.SECONDS);
				break;
			}
		}
	}
	
	private synchronized boolean terminatePty(String id) {
		log.info("Attempt to terminate pty process for id=" + id);
		PtyProcessInfo ptyProcessInfo = processes.get(id);
		if (ptyProcessInfo != null && ptyProcessInfo.sockets.isEmpty()) {
			ptyProcessInfo.getPty().destroy();
			processes.remove(id);
			return true;
		}
		return false;
	}
	
	private synchronized void connectSocket(WebSocketSession ws, PtyProcessInfo processInfo) {
		if (processInfo.terminationFuture != null) {
			processInfo.terminationFuture.cancel(false);
			processInfo.terminationFuture = null;
		}
		processInfo.sockets.add(ws);
	}

}
