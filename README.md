liferay-deployer
================

A jenkins plugin that deploys liferay plugins into a liferay server. Requires the app "Remote IDE Connector" to be installed on the liferay server.

Remote IDE Connector
====================
This liferay app is required to be installed in the liferay portal. Only through this app can the liferay-deployer plugin retrieve the information of the installed plugins and upload a new build of a specific plugin.
You can find it in [Liferay Marketplace](https://www.liferay.com/marketplace). It is free.

Install plugin
==============
Download from [here](https://github.com/technopolis/liferay-deployer/blob/master/target/liferay-deployer.hpi?raw=true), place it in the plugins directory of your jenkins installation, or upload it manually.

Using the plugin
================
## Liferay portal servers
First, you should go to the jenkins configuration and setup one or more liferay portal servers (which have the remote IDE connector app installed).
![jenkins configuration](https://raw.github.com/technopolis/liferay-deployer/master/doc/jenkins-configuration.png)
You can define as many servers as you want. Every server should have a unique name, a host, a port number, an administrative username and password. You can test the connection to the server by clicking the Test Connection button
## Job configuration
After defining the liferay portal servers, you can go to your job's configuration and in the build area define a deployment. 
![jenkins configuration](https://raw.github.com/technopolis/liferay-deployer/master/doc/job-configuration.png)
All you need to do is select "Deploy on Liferay Server", give the plugin name, give the war file directory and name and choose one of the predefined liferay portal servers. If you want to have a verbose log you can click also the verbose checkbox. Thats all.