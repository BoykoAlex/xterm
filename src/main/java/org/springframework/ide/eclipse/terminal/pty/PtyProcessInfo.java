package org.springframework.ide.eclipse.terminal.pty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketSession;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;

public class PtyProcessInfo {
	
	private static final int MAX_PAGE = 3;
	
	private String id;
	private PtyProcess pty;
	private Queue<String> buffer = new LinkedList<String>();
	List<WebSocketSession> sockets = new ArrayList<>();
	
	ScheduledFuture<?> terminationFuture;

	public PtyProcessInfo(PtyProcess pty, String id, ThreadPoolTaskScheduler taskExecutor) {
		this.pty = pty;
		this.id = id;		
	}
	
	boolean isOverBufferSize() throws IOException {
        WinSize winSize = this.pty.getWinSize();
		int max = winSize.ws_col * winSize.ws_row * MAX_PAGE;
        int totalLength = this.buffer.stream().map(s -> s.length()).reduce((s, c) -> s + c).orElse(0);
        return totalLength > max;
	}

	public String getId() {
		return id;
	}

	public PtyProcess getPty() {
		return pty;
	}

	public Queue<String> getBuffer() {
		return buffer;
	}
	
}