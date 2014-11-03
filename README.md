# AppDynamics Nginx - Monitoring Extension

This extension works only with the Java agent.

##Use Case

Nginx is an open-source HTTP server and reverse proxy, and an IMAP/POP3 proxy server. The Nginx monitoring extension gets metrics from the nginx server and displays them in the AppDynamics Metric Browser.

Metrics include:
* Active connections
* Total number of requests
* Accepted and handled requests
* Reading, writing, and waiting


##Installation

**Note**: For the following steps to work, nginx should be running with <a href="http://nginx.org/en/docs/http/ngx_http_stub_status_module.html">&quot;ngx_http_stub_status_module&quot;</a> or <a href="http://nginx.org/en/docs/http/ngx_http_status_module.html">&quot;ngx_http_status_module&quot;</a> enabled. Please make sure you have required changes in the nginx.conf.

1. Type 'mvn clean install' in the command line from the nginx-monitoring-extension directory
2. Deploy the file NginxMonitor.zip found in the 'target' directory into \<machineagent install dir\>/monitors/
3. Unzip the deployed file
4. Restart the machineagent
5. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | WebServer | NGinX.


##Directory Structure

<table><tbody>
<tr>
<th align="left"> Directory/File </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> src/main/resources/conf </td>
<td class='confluenceTd'> Contains the monitor.xml </td>
</tr>
<tr>
<td class='confluenceTd'> src/main/java </td>
<td class='confluenceTd'> Contains source code of the Nginx monitoring extension </td>
</tr>
<tr>
<td class='confluenceTd'> target </td>
<td class='confluenceTd'> Only obtained when using maven. Run 'mvn clean install' to get the distributable .zip file </td>
</tr>
<tr>
<td class='confluenceTd'> pom.xml </td>
<td class='confluenceTd'> Maven build script to package the project (required only if changing Java code) </td>
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
