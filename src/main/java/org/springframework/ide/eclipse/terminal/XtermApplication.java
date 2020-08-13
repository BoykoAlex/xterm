package org.springframework.ide.eclipse.terminal;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
	
	@ConditionalOnProperty(name = {"terminal.auto-shutdown.on"}, havingValue = "true")
	@Bean
	public AutoShutdown autoshutdown(PtyProcessManager processManager, ThreadPoolTaskScheduler taskExecutor, TerminalAutoShutdownProperties autoShutdownProperties) {
		return new AutoShutdown(processManager, taskExecutor,Duration.ofSeconds(autoShutdownProperties.getDelay()).toMillis());
	}

}
