package org.springframework.ide.eclipse.terminal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("terminal.pty")
public class PtyProperties {
	
	public enum PtyShutdown {
		NEVER,
		IMMEDIATELY,
		DELAY
	}
	
	private PtyShutdown shutdown = PtyShutdown.NEVER;
	
	private int shutdownDelay = 60;

	public PtyShutdown getShutdown() {
		return shutdown;
	}

	public void setShutdown(PtyShutdown shutdown) {
		this.shutdown = shutdown;
	}

	public int getShutdownDelay() {
		return shutdownDelay;
	}

	public void setShutdownDelay(int shutdownDelay) {
		this.shutdownDelay = shutdownDelay;
	}

}
