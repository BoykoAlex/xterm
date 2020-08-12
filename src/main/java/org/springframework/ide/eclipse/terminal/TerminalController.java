package org.springframework.ide.eclipse.terminal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TerminalController {
	
	private static final String TERMINAL = "terminal";
	
	@GetMapping("/terminal/{id}")
	public String terminal(
			@PathVariable() String id,
			@RequestParam(required = false) String shellCommand,
			@RequestParam(required = false) String cwd,
			Model model) {
		String[] shellCmdArray;
		Assert.hasText(id, "Id for the terminal must be provided");
		if (shellCommand != null && !shellCommand.trim().isEmpty()) {
			shellCmdArray = shellCommand.trim().split("\\s+");
		} else {
			shellCmdArray = defaultShellCommand();
		}
		model.addAttribute("cwd", cwd);
		model.addAttribute("id", id);
		model.addAttribute("shellCommand", shellCmdArray);
		return TERMINAL;
	}
	
	private String[] defaultShellCommand() {
		if (isWindows()) {
			return new String[] { "powershell.exe" };
		} else {
			return new String[] { "/bin/bash", "--login" };
		}
	}

	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}


}
