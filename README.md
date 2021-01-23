# SonarQube PlasticSCM plugin

## Description
This plugin implements SCM dependent features of SonarQube for [PlasticSCM](https://www.plasticscm.com/) projects.

## Requirements
* The PlasticSCM command line tool (cm) must be available in the path, but you may configure the full path in SonarQubes settings.

## Installation
Download the latest release and place the jar file in extensions/plugins folder of your SonarQube directory.

## Usage
Auto-detection will work if there is a .plastic folder in the project root directory. Otherwise you can force the provider using -Dsonar.scm.provider=PlasticSCM.

## Known Issues
The annotate/blame command of the PlasticSCM cli can easily timeout. If SonarQube is warning you that it misses blame information for some files, you may have to increase the socket timeout value in the client.conf and server.conf files. 
Add this to both files:
```
<SocketConnectTimeoutMillisec>10000</SocketConnectTimeoutMillisec>
```