# AppDynamics Nginx - Monitoring Extension

##Use Case

Nginx is an open-source HTTP server and reverse proxy, and an IMAP/POP3 proxy server. (Nginx is pronounced "engine-x".)

The nginx monitoring extension gets metrics from the nginx server and displays them in the AppDynamics Metric Browser.

Metrics include:
* Active connections
* Total number of requests
* Accepted and handled requests
* Reading, writing, and waiting


##Installation

**Note**: For the following steps to work, nginx should be running with &quot;--with-http_stub_status_module&quot; enabled as well as a stub status on in the nginx.conf. 
See the <a href="http://wiki.nginx.org/HttpStubStatusModule">nginx wiki</a> for more information</td>.

1. Type 'ant package' in the command line from the nginx-monitoring-extension directory
2. Deploy the file NginxMonitor.zip found in the 'dist' directory into \<machineagent install dir\>/monitors/
3. Unzip the deployed file
4. Restart the machineagent
5. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | WebServer | NGinX | Status.


##Directory Structure

<table><tbody>
<tr>
<th align="left"> Directory/File </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> conf </td>
<td class='confluenceTd'> Contains the monitor.xml </td>
</tr>
<tr>
<td class='confluenceTd'> lib </td>
<td class='confluenceTd'> Contains third-party project references </td>
</tr>
<tr>
<td class='confluenceTd'> src </td>
<td class='confluenceTd'> Contains source code of the Nginx monitoring extension </td>
</tr>
<tr>
<td class='confluenceTd'> dist </td>
<td class='confluenceTd'> Only obtained when using ant. Run 'ant build' to get binaries. Run 'ant package' to get the distributable .zip file </td>
</tr>
<tr>
<td class='confluenceTd'> build.xml </td>
<td class='confluenceTd'> Ant build script to package the project (required only if changing Java code) </td>
</tr>
</tbody>
</table>

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

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/nginx-monitoring-extension).

##Community

Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/Extensions/Nginx-Monitoring-Extension/idi-p/895) community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).
