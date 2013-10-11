package org.jenkinsci.plugins.liferay.helpers.json;

import java.util.Arrays;


public class Output {

	private boolean installed;
	private boolean started;
	private String[] types;
	
	public boolean isInstalled() {
	
		return installed;
	}
	
	public void setInstalled(boolean installed) {
	
		this.installed = installed;
	}
	
	public boolean isStarted() {
	
		return started;
	}
	
	public void setStarted(boolean started) {
	
		this.started = started;
	}
	
	public String[] getTypes() {
	
		return types;
	}
	
	public void setTypes(String[] types) {
	
		this.types = types;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("Output [installed=");
		builder.append(installed);
		builder.append(", started=");
		builder.append(started);
		builder.append(", types=");
		builder.append(Arrays.toString(types));
		builder.append("]");
		return builder.toString();
	}
	
	public String getTypesAsString() {
		String result = "[";
		boolean isFirst = true;
		if (getTypes().length > 0) {
			for (String typesElement : getTypes()) {
				if (isFirst) {
					isFirst = false;
				}
				else {
					result += ", ";
				}
				result += "'" + typesElement + "'";
			}
		}
		result += "]";
		return result;
	}
	
}
