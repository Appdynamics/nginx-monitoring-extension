#AppDynamics Nginx Monitoring Extension

* [Use Case](#use-case)
* [Installation](#installation)
* [Rebuilding the Project](#Rebuilding-the-project)
* [Files and Folders](#files-and-folders)
* [Metrics](#metrics)
* [Contributing](#contributing)

##Use Case

Nginx is an open-source HTTP server and reverse proxy server. (Nginx is pronounced "engine-x".)

The Nginx monitoring extension gets metrics from the Nginx server and displays them in the AppDynamics Metric Browser.

Metrics include:
* Active connections
* Total number of requests
* Accepted and handled requests
* Reading, writing, and waiting


##Installation

For the following steps to work, Nginx should be running with &quot;--with-http_stub_status_module&quot; enabled as well as a stub status on in the Nginx.conf. 
See the <a href="http://wiki.Nginx.org/HttpStubStatusModule">Nginx wiki</a> for more information</td>.


1. In <machine-agent-home>/monitors create a new subdirectory for the Nginx monitoring extension.   
2. Copy the contents in the 'dist' folder to the subdirectory created in step 1.  
3. Restart the Machine Agent.  
4. Look for the metrics in the Metric Browser at: Application Infrastructure Performance|\<Node\>|Custom Metrics|WebServer|Nginx|Status.

#####Rebuilding the Project

1. From the command line, go to root directory (where all the files are located).
2. Type "ant" (without the quotes).  
3. 'dist' will be updated with the monitor.xml and the compiled Nginx.jar.

##Files 

|Files/Directory | Description |
| --- | --- |
|conf | Contains the monitor.xml |
|src | Contains source code to Nginx Custom Monitor|
|build.xml | Ant build script to package the project (only required if changing java code) |
|extensions-commons | Submodule that includes vendor dependencies shared by all extensions|

##Metrics

| Metric Name | Description |
| --- | --- |
| Active Connections | Number of all open active connections |
| Server: Accepts | Number of accepted requests |
| Server: Handled | Number of handled requests |
| Server: Requests | Total number of requests  |
| Reading | Nginx reads request header  |
| Writing | Nginx reads request body, processes request, or writes response to a client  |
| Waiting | Nginx keep-alive connections or currently active |
  

##Contributing

Always feel free to fork and contribute any changes directly via GitHub.


##Support

For any support questions, please contact ace@appdynamics.com.
