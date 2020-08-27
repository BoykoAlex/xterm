package org.springframework.ide.eclipse.terminal;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ide.eclipse.terminal.pty.PtyProcessManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class Shutdown implements ApplicationContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(Shutdown.class);
	
	private static int PREFERRED_CHECK_PERIOD = 5000;
	
	private long timeMark;
	private long delay;
	private Closeable closeableApp;
	
	public Shutdown(PtyProcessManager processManager, ThreadPoolTaskScheduler taskExecutor, long delay) {
		this.timeMark = System.currentTimeMillis();
		this.delay = delay;
		int period = delay < PREFERRED_CHECK_PERIOD ? 1000 : PREFERRED_CHECK_PERIOD;
		taskExecutor.getScheduledExecutor().scheduleWithFixedDelay(() -> evaluate(processManager), period, period, TimeUnit.MILLISECONDS);
	}
	
	private void evaluate(PtyProcessManager processManager) {
		long current = System.currentTimeMillis();
		if (processManager.isEmpty()) {
			if (current - timeMark > delay) {
				log.info("Auto shutting down due to no activity...");
				if (closeableApp == null) {
					System.exit(0);
				} else {
					try {
						closeableApp.close();
					} catch (IOException e) {
						log.error("", e);
					}
				}
			}
		} else {
			this.timeMark = current;
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (applicationContext instanceof Closeable) {
			this.closeableApp = (Closeable) applicationContext;
		}
	}
	
	public void shutdown() throws IOException {
		if (closeableApp != null) {
			closeableApp.close();
		} else {
			throw new IOException("Cannot shutdown app");
		}
	}

}
