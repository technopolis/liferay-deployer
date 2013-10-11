liferay-deployer
================

A jenkins plugin that deploys liferay plugins into a liferay server. Requires the app "Remote IDE Connector" to be installed on the liferay server.

Remote IDE Connector
====================
This liferay app is required to be installed in the liferay portal. Only through this app can the liferay-deployer plugin retrieve the information of the installed plugins and upload a new build of a specific plugin.
You can find it in https://www.liferay.com/marketplace. It is free.

Install plugin
==============
Download form https://github.com/technopolis/liferay-deployer/blob/master/target/liferay-deployer.hpi?raw=true, place it in the plugins directory of your jenkins installation, or upload it manually.
