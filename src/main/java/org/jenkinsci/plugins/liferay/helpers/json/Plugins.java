package org.jenkinsci.plugins.liferay.helpers.json;

import java.util.Arrays;


public class Plugins {

	private String error;
	private int status;
	private String[] output;
	
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
	
	public String getOutputAsString() {
		String result = "[";
		boolean isFirst = true;
		if (getOutput().length > 0) {
			for (String outputElement : getOutput()) {
				if (isFirst) {
					isFirst = false;
				}
				else {
					result += ", ";
				}
				result += "'" + outputElement + "'";
			}
		}
		result += "]";
		return result;
	}

	
	public String[] getOutput() {
	
		return output;
	}

	
	public void setOutput(String[] output) {
	
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
		builder.append(Arrays.toString(output));
		builder.append("]");
		return builder.toString();
	}
	
	
}
