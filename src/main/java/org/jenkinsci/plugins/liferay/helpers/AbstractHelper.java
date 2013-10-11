package org.jenkinsci.plugins.liferay.helpers;

import hudson.model.BuildListener;


public class AbstractHelper {

	private BuildListener listener;
	private boolean verbose;
	
	public AbstractHelper(BuildListener listener, boolean verbose) {

		this.listener = listener;
	}
	
	void log(String message) {
		if (listener != null && verbose) {
			listener.getLogger().println(message);
		}
	}
	
	void log(Object message) {
		if (listener != null && verbose) {
			listener.getLogger().println(message.toString());
		}
	}
}
