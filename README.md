# AppDynamics Nginx - Monitoring Extension

This extension works only with the Java agent.

##Use Case

Nginx is an open-source HTTP server and reverse proxy, and an IMAP/POP3 proxy server. The Nginx monitoring extension gets metrics from the nginx server and displays them in the AppDynamics Metric Browser. This extension supports both NGinx and NGinx Plus.

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

###NGinx Metrics

| Metric Name | Description |
| --- | --- |
| Active Connections | Number of all open active connections |
| Server: Accepts | Number of accepted requests |
| Server: Handled | Number of handled requests |
| Server: Requests | Total number of requests  |
| Reading | Nginx reads request header  |
| Writing | Nginx reads request body, processes request, or writes response to a client  |
| Waiting | NGinX keep-alive connections or currently active |
  
###NGinx Plus Metrics

####Requests

| Metric Name | Description |
| --- | --- |
| total | The total number of client requests |
| current | The current number of client requests |

####Server Zones
| Metric Name | Description |
| --- | --- |
| processing | The number of client requests that are currently being processed |
| requests | The total number of client requests received from clients |
| responses/total | The total number of responses sent to clients |
| responses/ 1xx, 2xx, 3xx, 4xx, 5xx  | The number of responses with status codes 1xx, 2xx, 3xx, 4xx, and 5xx |
| received | The total number of bytes received from clients  |
| sent | The total number of bytes sent to clients |

####Upstreams

| Metric Name | Description |
| --- | --- |
| active | The current number of active connections |
| backup | A boolean value indicating whether the server is a backup server |
| downstart | The time (in milliseconds since Epoch) when the server became “unavail” or “unhealthy”  |
| downtime  | Total time the server was in the “unavail” and “unhealthy” states |
| fails | The total number of unsuccessful attempts to communicate with the server |
| keepalive | The current number of idle keepalive connections |
| received | The total number of bytes received from this server |
| requests | The total number of client requests forwarded to this server |
| sent | The total number of bytes sent to this server |
| state | urrent state, which may be one of “up”, “down”, “unavail”, or “unhealthy”.  |
| unavail | Times the server became unavailable |
| weight | Weight of the server |
| responses/total | The total number of responses obtained from this server |
| responses/ 1xx, 2xx, 3xx, 4xx, 5xx  | The number of responses with status codes 1xx, 2xx, 3xx, 4xx, and 5xx |
| health_checks/checks | The total number of health check requests made |
| health_checks/fails | The number of failed health checks  |
| health_checks/unhealthy | How many times the server became unhealthy (state “unhealthy”)  |
| health_checks/last_passed | Boolean indicating if the last health check request was successful and passed tests  |

####Connections

| Metric Name | Description |
| --- | --- |
| accepted | The total number of accepted client connections |
| dropped | The total number of dropped client connections |
| active | The current number of active client connections  |
| idle | The current number of idle client connections |

####Caches

| Metric Name | Description |
| --- | --- |
| size | The current size of the cache |
| max_size | The limit on the maximum size of the cache specified in the configuration |
| cold | A boolean value indicating whether the “cache loader” process is still loading data from disk into the cache |
| hit, stale, updating, revalidated/responses | The total number of responses read from the cache |
| hit, stale, updating, revalidated/bytes | The total number of bytes read from the cache |
| miss, expired, bypass/responses | The total number of responses not taken from the cache |
| miss, expired, bypass/bytes | The total number of bytes read from the proxied server |
| miss, expired, bypass/responses_written | The total number of responses written to the cache |
| miss, expired, bypass/bytes_written | The total number of bytes written to the cache |


##Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/nginx-monitoring-extension).

##Community

Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/Extensions/Nginx-Monitoring-Extension/idi-p/895) community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).
