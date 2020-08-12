package org.springframework.ide.eclipse.terminal.model;

import java.util.List;

public class InitParams {
	
	private String cwd;
	private List<String> ptyParams;
	public String getCwd() {
		return cwd;
	}
	public void setCwd(String cwd) {
		this.cwd = cwd;
	}
	public List<String> getPtyParams() {
		return ptyParams;
	}
	public void setPtyParams(List<String> ptyParams) {
		this.ptyParams = ptyParams;
	}

}
