package org.springframework.ide.eclipse.terminal.theme;

import java.util.HashMap;
import java.util.Map;

public class ThemeRepository {
	
	private Map<String, Theme> idToTheme = new HashMap<>();
	
	{
		idToTheme.put("eclipse-dark", createEclipseDark());
		idToTheme.put("eclipse-light", createEclipseLight());
	}
	
	private Theme createEclipseDark() {
		Theme theme = new Theme();
		theme.background = "rgb(35, 35, 35)";
		theme.foreground = "rgb(255, 255, 255)";
		theme.selection = "rgba(235, 235, 235, 0.2)";
		theme.cursor = "rgb(255, 255, 255)";
		theme.cursorAccent = "rgb(255, 255, 255)";
		return theme;
	}

	private Theme createEclipseLight() {
		Theme theme = new Theme();
		theme.background = "rgb(255, 255, 255)";
		theme.foreground = "rgb(0, 0, 0)";
		theme.selection = "rgba(45, 45, 45, 0.2)";
		theme.cursor = "rgb(0, 0, 0)";
		theme.cursorAccent = "rgb(0, 0, 0)";
		return theme;
	}
	
	public synchronized Theme findTheme(String id) {
		return idToTheme.get(id);
	}
	
}
