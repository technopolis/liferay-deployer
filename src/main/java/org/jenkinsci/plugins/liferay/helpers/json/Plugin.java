package org.jenkinsci.plugins.liferay.helpers.json;

import java.util.Arrays;


public class Plugin {

	private String error;
	private int status;
	private Output output;
	
	public String getError() {
	
		return error;
	}
	
	public void setError(String error) {
	
		this.error = error;
	}
	
	public int getStatus() {
	
		return status;
	}
	
	public void setStatus(int status) {
	
		this.status = status;
	}
	


	
	public Output getOutput() {
	
		return output;
	}


	
	public void setOutput(Output output) {
	
		this.output = output;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("Plugin [error=");
		builder.append(error);
		builder.append(", status=");
		builder.append(status);
		builder.append(", output=");
		builder.append(output);
		builder.append("]");
		return builder.toString();
	}
	
	
}
