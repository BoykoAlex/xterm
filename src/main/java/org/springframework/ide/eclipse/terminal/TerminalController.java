package org.springframework.ide.eclipse.terminal;

import java.io.IOException;

import org.springframework.ide.eclipse.terminal.model.Theme;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TerminalController {
	
	private static final String TERMINAL = "terminal";
	
	private Shutdown shutdown;
	
	public TerminalController(Shutdown shutdown) {
		this.shutdown = shutdown;
	}
	
	@GetMapping("/terminal/{id}")
	public String terminal(
			@PathVariable() String id,
			@RequestParam(required = false) String cmd,
			@RequestParam(required = false) String cwd,
			@RequestParam(required = false) String bg,
			@RequestParam(required = false) String fg,
			@RequestParam(required = false) String selection,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false) String cursorAccent,
			@RequestParam(required = false) String fontFamily,
			@RequestParam(required = false) Integer fontSize,
			Model model) {
		String[] cmdArray;
		Assert.hasText(id, "Id for the terminal must be provided");
		if (cmd != null && !cmd.trim().isEmpty()) {
			cmdArray = cmd.trim().split("\\s+");
		} else {
			cmdArray = defaultShellCommand();
		}
		
		Theme theme = new Theme();
		if (bg != null) {
			theme.background = bg;
		}
		if (fg != null) {
			theme.foreground = fg;
		}
		if (selection != null) {
			theme.selection = selection;
		}
		if (cursor != null) {
			theme.cursor = cursor;
		}
		if (cursorAccent != null) {
			theme.cursorAccent = cursorAccent;
		}
		if (fontFamily != null) {
			theme.fontFamily = fontFamily;
		}
		if (fontSize != null) {
			theme.fontSize = fontSize;
		}
		
		model.addAttribute("cwd", cwd);
		model.addAttribute("id", id);
		model.addAttribute("cmd", cmdArray);
		model.addAttribute("theme", theme);
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

	@PostMapping("/shutdown")
	public void shutdown() throws IOException {
		shutdown.shutdown();
	}

}
