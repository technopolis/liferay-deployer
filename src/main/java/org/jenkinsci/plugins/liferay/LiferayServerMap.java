package org.jenkinsci.plugins.liferay;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;


public class LiferayServerMap implements Serializable {

	private static final long serialVersionUID = 1L;
	private String liferayServerName;
	private String liferayServerHost;
	private String liferayServerPort;
	private String liferayUserName;
	private String liferayUserPassword;
	
	@DataBoundConstructor
	public LiferayServerMap(String liferayServerName, String liferayServerHost, String liferayServerPort, String liferayUserName, String liferayUserPassword) {

		super();
		this.liferayServerName = liferayServerName;
		this.liferayServerHost = liferayServerHost;
		this.liferayServerPort = liferayServerPort;
		this.liferayUserName = liferayUserName;
		this.liferayUserPassword = liferayUserPassword;
	}

	public String getLiferayServerUrlForIDE() {
		return getLiferayServerUrl() + "/server-manager-web";
	}
	
	public String getLiferayServerUrlForPlugins() {
		return getLiferayServerUrlForIDE() + "/plugins";
	}
	
	public String getLiferayServerUrlForPlugin(String pluginName) {
		return getLiferayServerUrlForPlugins() + "/" + pluginName;
	}
	
	public String getLiferayServerUrl() {
		return "http://" + liferayServerHost + ":" + liferayServerPort;
	}
	
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((liferayServerName == null) ? 0 : liferayServerName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiferayServerMap other = (LiferayServerMap) obj;
		if (liferayServerName == null) {
			if (other.liferayServerName != null)
				return false;
		}
		else if (!liferayServerName.equals(other.liferayServerName))
			return false;
		return true;
	}



	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("LiferayServerMap [liferayServerName=");
		builder.append(liferayServerName);
		builder.append(", liferayServerHost=");
		builder.append(liferayServerHost);
		builder.append(", liferayServerPort=");
		builder.append(liferayServerPort);
		builder.append(", liferayUserName=");
		builder.append(liferayUserName);
		builder.append(", liferayUserPassword=");
		builder.append(liferayUserPassword);
		builder.append("]");
		return builder.toString();
	}

	public String getLiferayServerName() {
	
		return liferayServerName;
	}
	
	public void setLiferayServerName(String serverName) {
	
		this.liferayServerName = serverName;
	}
	


	
	public String getLiferayUserName() {
	
		return liferayUserName;
	}

	
	public void setLiferayUserName(String liferayUserName) {
	
		this.liferayUserName = liferayUserName;
	}

	
	public String getLiferayUserPassword() {
	
		return liferayUserPassword;
	}

	
	public void setLiferayUserPassword(String liferayUserPassword) {
	
		this.liferayUserPassword = liferayUserPassword;
	}

	
	public String getLiferayServerHost() {
	
		return liferayServerHost;
	}

	
	public void setLiferayServerHost(String liferayServerHost) {
	
		this.liferayServerHost = liferayServerHost;
	}

	
	public String getLiferayServerPort() {
	
		return liferayServerPort;
	}

	
	public void setLiferayServerPort(String liferayServerPort) {
	
		this.liferayServerPort = liferayServerPort;
	}
	
	
}
