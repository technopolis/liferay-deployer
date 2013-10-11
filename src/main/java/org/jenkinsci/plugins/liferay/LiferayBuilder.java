package org.jenkinsci.plugins.liferay;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormFieldValidator;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.apache.tools.ant.DirectoryScanner;
import org.jenkinsci.plugins.liferay.helpers.LiferayServerHelper;
import org.jenkinsci.plugins.liferay.helpers.json.Plugin;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link LiferayBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Charalampos Chrysikopoulos
 */
public class LiferayBuilder extends Builder {

	private static final Logger logger = Logger.getLogger(LiferayBuilder.class.getName()); 
	
    private final String pluginName;
    private final String warFileDirectory;
    private final String warFile;
    private final String liferayServer;
    private final boolean verbose;

    @DataBoundConstructor
    public LiferayBuilder(String pluginName, String warFileDirectory, String warFile, String liferayServer, boolean verbose) {
        this.pluginName = pluginName;
		if (warFileDirectory == null || warFileDirectory.isEmpty()) {
			warFileDirectory = ".";
		}
        this.warFileDirectory = warFileDirectory;
        this.warFile = warFile;
        this.liferayServer = liferayServer;
        this.verbose = verbose;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getPluginName() {
        return pluginName;
    }
    
	public String getLiferayServer() {
	
		return liferayServer;
	}
	
	public String getWarFile() {
	
		return warFile;
	}

	
	public String getWarFileDirectory() {
	
		return warFileDirectory;
	}

	
	public boolean isVerbose() {
	
		return verbose;
	}

	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		
		if (verbose) {
			listener.getLogger().println("Selected plugin is " + pluginName);
			listener.getLogger().println("Selected war file directory is " + warFileDirectory);
			listener.getLogger().println("Selected war file is " + warFile);
			listener.getLogger().println("Selected server name is " + liferayServer);
			listener.getLogger().println("Verbose: " + verbose);
		}
		
		LiferayServerMap liferayServerMap = getDescriptor().getLiferayServerMap(liferayServer);
		LiferayServerHelper serverHelper = new LiferayServerHelper(listener, liferayServerMap, verbose);
		
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[]{warFile});
		scanner.setBasedir(warFileDirectory);
		scanner.setCaseSensitive(false);
		scanner.scan();
		
		String[] files = scanner.getIncludedFiles();
		
		if (files.length < 0) {
			listener.getLogger().println("NO FILE FOUND FOR '" + warFile + "' IN '" + warFileDirectory + "'");
			build.setResult(Result.FAILURE);
			return false;
		}
		if (files.length > 1) {
			listener.getLogger().println("MORE THAN ONE FILE FOUND FOR '" + warFile + "' IN '" + warFileDirectory + "'");
			build.setResult(Result.FAILURE);
			return false;
		}
		
		String fileInDirectory = warFileDirectory + "/" + files[0];
		
		listener.getLogger().println("UPLOADING FILE: " + fileInDirectory);
		
		Plugin uploadPlugin = serverHelper.uploadPlugin(liferayServerMap, fileInDirectory, pluginName);
		if (verbose) {
			listener.getLogger().println("UPLOAD RESULT: " + uploadPlugin);
		}
		
		if (uploadPlugin == null || uploadPlugin.getStatus() != 0) {
			listener.getLogger().println("THERE WAS AN ERROR UPLOADING THE FILE '" + fileInDirectory + "'. PLEASE CHECK THE LIFERAY SERVER LOGS.");
			if (uploadPlugin != null) {
				listener.getLogger().println("  ERROR MESSAGE FROM LIFERAY SERVER:" + uploadPlugin.getError());
			}
			build.setResult(Result.UNSTABLE);
			return false;
		}
		
		if (verbose) {
			listener.getLogger().println("CHECKING DEPLOYMENT BY CALLING " + liferayServerMap.getLiferayServerUrlForPlugin(pluginName));
		}
		
		Plugin plugin = serverHelper.checkForPlugin(liferayServerMap, pluginName);
		
		if (plugin == null) {
			listener.getLogger().println("RESULT IS NULL...");
			build.setResult(Result.UNSTABLE);
			return false;
		}
		else if (plugin == null || !plugin.getError().equals("")) {
			listener.getLogger().println("There was an error while uploading: " + plugin.getError());
			build.setResult(Result.UNSTABLE);
			return false;
		}
		else if (!plugin.getOutput().isInstalled()) {
			listener.getLogger().println("The plugin " + pluginName + " doesn't seem to be installed...");
			build.setResult(Result.UNSTABLE);
		}
		else if (!plugin.getOutput().isStarted()) {
			listener.getLogger().println("The plugin " + pluginName + " doesn't seem to be started...");
			build.setResult(Result.UNSTABLE);
		}
		
		if (verbose) {
			listener.getLogger().println("Response from liferay server about " + pluginName + ": ");
			listener.getLogger().println("  Status: " + plugin.getStatus());
			listener.getLogger().println("  Error: " + plugin.getError());
			listener.getLogger().println("  Installed: " + plugin.getOutput().isInstalled());
			listener.getLogger().println("  Started: " + plugin.getOutput().isStarted());
			listener.getLogger().println("  Types: " + plugin.getOutput().getTypesAsString());
		}
        
		listener.getLogger().println("THE PLUGIN '" + pluginName + "' IS DEPLOYED WITH SUCCESS IN '" + liferayServerMap.getLiferayServerName() + "'!");
		
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link LiferayBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    	
		public DescriptorImpl() {
			load();
		}
    	
        private LiferayServerMap[] liferayServers;

        public LiferayServerMap getLiferayServerMap(String liferayServerName) {
        	logger.info("SEARCHING SERVER FOR NAME:" + liferayServerName);
        	for (LiferayServerMap liferayServerMap : getLiferayServers()) {
        		logger.info("  CHECKING " + liferayServerMap);
        		if (liferayServerMap != null 
        						&& liferayServerMap.getLiferayServerName() != null 
        						&& liferayServerMap.getLiferayServerName().equals(liferayServerName)) {
        			logger.info("  RETURNING:" + liferayServerMap);
        			return liferayServerMap;
        		}
        	}
        	logger.info("RETUNING NULL");
        	return null;
        }
        
        public ListBoxModel doFillLiferayServerItems() {
        	ListBoxModel items = new ListBoxModel();
            for (LiferayServerMap liferayServerMap : getLiferayServers()) {
            	
                items.add(liferayServerMap.getLiferayServerName(), liferayServerMap.getLiferayServerName());
            }
            return items;
        }

        public FormValidation doCheckPluginName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a plugin name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }
        
        @SuppressWarnings("deprecation")
		public void doTestLiferayConnection(StaplerRequest req, StaplerResponse rsp,
	            @QueryParameter("liferayServerHost") final String liferayServerHost, 
	            @QueryParameter("liferayServerPort") final String liferayServerPort,
	            @QueryParameter("liferayUserName") final String liferayUserName, 
	            @QueryParameter("liferayUserPassword") final String liferayUserPassword) throws IOException, ServletException {
        	
        	new FormFieldValidator(req,rsp,true) {
                protected void check() throws IOException, ServletException {
                    try {
                    	LiferayServerMap liferayServerMap = new LiferayServerMap(
                    		"",
                    		liferayServerHost,
                    		liferayServerPort,
                    		liferayUserName,
                    		liferayUserPassword
                    					);
                    	LiferayServerHelper helper = new LiferayServerHelper(liferayServerMap);
                    	
                    	if (helper.hasRemoteIdeConnectorInstalled(liferayServerMap)) {
                    		ok("Success");
                    	} 
                    	else {
                    		error("Failure...");
                    	}
                    	
                    } catch (Exception e) {
                        error("Client error : "+e.getMessage());
                    }
                }
            }.process();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }


        public String getDisplayName() {
            return "Deploy on Liferay Server";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        	try {
        		setLiferayServers(req.bindJSONToList(LiferayServerMap.class,
					req.getSubmittedForm().get("liferayServer")).toArray(new LiferayServerMap[0]));
				return true;
			} catch (ServletException e) {
				throw new FormException(e, "");
			}
        }
        
		public void setLiferayServers(LiferayServerMap[] maps) {
			liferayServers = maps;
			save();
		}


        public LiferayServerMap[] getLiferayServers() {
        	logger.info("    GOT SERVERS: " + liferayServers);
            return liferayServers;
        }
    }
}

