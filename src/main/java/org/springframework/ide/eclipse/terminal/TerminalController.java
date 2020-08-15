package org.springframework.ide.eclipse.terminal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.eclipse.terminal.theme.Theme;
import org.springframework.ide.eclipse.terminal.theme.ThemeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TerminalController {
	
	private static final String TERMINAL = "terminal";
	
	@Autowired
	private ThemeRepository themeRepository;
	
	@GetMapping("/terminal/{id}")
	public String terminal(
			@PathVariable() String id,
			@RequestParam(required = false) String cmd,
			@RequestParam(required = false) String cwd,
			@RequestParam(required = false) String theme,
			Model model) {
		String[] cmdArray;
		Assert.hasText(id, "Id for the terminal must be provided");
		if (cmd != null && !cmd.trim().isEmpty()) {
			cmdArray = cmd.trim().split("\\s+");
		} else {
			cmdArray = defaultShellCommand();
		}
		model.addAttribute("cwd", cwd);
		model.addAttribute("id", id);
		model.addAttribute("cmd", cmdArray);
		Theme themeObj = theme == null ? null : themeRepository.findTheme(theme);
		model.addAttribute("theme", themeObj == null ? new Theme() : themeObj);
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
