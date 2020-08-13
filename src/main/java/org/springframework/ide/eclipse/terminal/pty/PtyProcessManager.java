package org.springframework.ide.eclipse.terminal.pty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.eclipse.terminal.PtyProperties;
import org.springframework.ide.eclipse.terminal.model.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.pty4j.PtyProcess;

public class PtyProcessManager {
	
	private static final Logger log = LoggerFactory.getLogger(PtyProcessManager.class);
	
	private Map<String, PtyProcessInfo> processes = new HashMap<>();
	private ThreadPoolTaskScheduler taskExecutor;

	private PtyProperties ptyProperties;
		
	public PtyProcessManager(PtyProperties ptyProperties, ThreadPoolTaskScheduler taskExecutor) {
		this.ptyProperties = ptyProperties;
		this.taskExecutor = taskExecutor;
		
	}
	
	private PtyProcessInfo create(String id, List<String> cmd, String cwd) throws IOException {
		PtyProcess pty = PtyProcess.exec(cmd.toArray(new String[cmd.size()]), System.getenv(), cwd, false, false, null);
		PtyProcessInfo processInfo = new PtyProcessInfo(pty, id, taskExecutor);
		
		taskExecutor.createThread(() -> {
			byte[] buffer = new byte[4096];
			try {
				int length = 0;
				while ((length = pty.getInputStream().read(buffer)) != -1) {
					if (length > 0) {
						try {
							broadcastData(processInfo, Arrays.copyOf(buffer, length));
						} catch (Exception e) {
							log.error("", e);
						}
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
	
	public synchronized PtyProcessInfo createOrConnect(WebSocketSession ws, String id, List<String> cmd, String cwd) throws IOException {
		PtyProcessInfo processInfo = this.processes.get(id);
		if (processInfo == null) {
			processInfo = create(id, cmd, cwd);
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
				
				switch (ptyProperties.getShutdown()) {
				case IMMEDIATELY:
					terminatePty(ptyProcessInfo.getId());
					break;
				case DELAY:
					ptyProcessInfo.terminationFuture = taskExecutor.getScheduledExecutor().schedule(
							() -> this.terminatePty(e.getKey()), ptyProperties.getShutdownDelay(), TimeUnit.SECONDS);
					break;
				default:
					// Nothing to do - no shutdown
				}
				break;
			}
		}
	}
	
	private synchronized boolean terminatePty(String id) {
		PtyProcessInfo ptyProcessInfo = processes.get(id);
		if (ptyProcessInfo != null && ptyProcessInfo.sockets.isEmpty()) {
			log.info("Terminating pty process for id=" + id);
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

	public boolean isEmpty() {
		return processes.isEmpty();
	}

}
