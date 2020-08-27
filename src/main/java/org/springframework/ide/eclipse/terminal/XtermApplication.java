package org.springframework.ide.eclipse.terminal;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ide.eclipse.terminal.pty.PtyProcessManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
@EnableAsync
public class XtermApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(XtermApplication.class, args);
	}
	
	
	@Bean
	public ThreadPoolTaskScheduler taskExecutor() {
		ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
		return executor;
	}
	
	@Bean
	public PtyProcessManager ptyProcessManager(PtyProperties ptyProperties, ThreadPoolTaskScheduler taskExecutor) {
		return new PtyProcessManager(ptyProperties, taskExecutor);
	}
	
	@Bean
	public Shutdown shutdown(PtyProcessManager processManager, ThreadPoolTaskScheduler taskExecutor, ShutdownProperties ShutdownProperties) {
		return new Shutdown(processManager, taskExecutor,Duration.ofSeconds(ShutdownProperties.getDelay()).toMillis());
	}
	
}
