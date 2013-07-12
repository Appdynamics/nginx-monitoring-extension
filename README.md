# AppDynamics Nginx Monitoring Extension

* [Use Case](nginx-readme.md#use-case)
* [Installation](nginx-readme.md#installation)
* [Rebuilding the Project](nginx-readme.md#Rebuilding-the-project)
* [Files and Folders](nginx-readme.md#files-and-folders)
* [Metrics](nginx-readme.md#metrics)
* [Contributing](nginx-readme.md#contributing)

##Use Case

Nginx is an open-source HTTP server and reverse proxy, and an IMAP/POP3 proxy server. (Nginx is pronounced "engine-x".)

The nginx monitoring extension gets metrics from the nginx server and displays them in the AppDynamics Metric Browser.

Metrics include:
* Active connections
* Total number of requests
* Accepted and handled requests
* Reading, writing, and waiting


##Installation

![Warning](images/emoticons/warning.gif) For the following steps to work, nginx should be running with &quot;--with-http_stub_status_module&quot; enabled as well as a stub status on in the nginx.conf. 
See the <a href="http://wiki.nginx.org/HttpStubStatusModule">nginx wiki</a> for more information</td>.


1. In <machine-agent-home>/monitors create a new subdirectory for the nginx monitoring extension.
2. Copy the contents in the 'dist' folder to the subdirectory created in step 1.
3. Restart the Machine Agent.  
4. Look for the metrics in the Metric Browser at: Application Infrastructure Performance|\<Node\>|Custom Metrics|WebServer|NGinX|Status.

#####Rebuilding the Project

1. From the command line, go to root directory (where all the files are located).
2. Type "ant" (without the quotes).  
3. 'dist' will be updated with the monitor.xml and nginx.jar.

##Files 

|Files/Directory | Description |
| --- | --- |
|bin | Contains class files |
|conf | Contains the monitor.xml |
|lib | Contains third-party project references |
|src | Contains source code to NGinX Custom Monitor|
|dist | Contains the final distribution package (monitor.xml nginx.jar) |
|build.xml | Ant build script to package the project (only required if changing java code) |


##Metrics

| Metric Name | Description |
| --- | --- |
| Active Connections | Number of all open active connections |
| Server: Accepts | Number of accepted requests |
| Server: Handled | Number of handled requests |
| Server: Requests | Total number of requests  |
| Reading | Nginx reads request header  |
| Writing | Nginx reads request body, processes request, or writes response to a client  |
| Waiting | NGinX keep-alive connections or currently active |
  


##Contributing

Always feel free to fork and contribute any changes directly via GitHub.


##Support

For any support questions, please contact ace@appdynamics.com.
