# AppDynamics Nginx Monitoring Extension

## Use Case
Nginx is a web server which can also be used as a reverse proxy, load balancer, mail proxy and HTTP cache. The Nginx monitoring extension gets metrics from the nginx server and displays them in the AppDynamics Metric Browser. This extension supports both NGinx and NGinx Plus.

## Prerequisites

1. Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.
2. The extension needs to be able to connect to the Nginx in order to collect and send metrics. To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.


## Installation

**Note**: For the following steps to work, nginx should be running with <a href="http://nginx.org/en/docs/http/ngx_http_stub_status_module.html">&quot;ngx_http_stub_status_module&quot;</a> or <a href="http://nginx.org/en/docs/http/ngx_http_api_module.html">&quot;ngx_http_api_module&quot;</a> enabled. Please make sure you have required changes in the nginx.conf.

1. Run 'mvn clean install' from "nginx-monitoring-extension"
2. Unzip the `nginx-monitoring-extension-<VERSION>.zip` from `target` directory into the "<MachineAgent_Dir>/monitors" directory.
3. Edit the file config.yml as described below in Configuration Section, located in <MachineAgent_Dir>/monitors/NginxMonitor and update the server(s) details.
4. All metrics to be reported are configured in metrics.xml. Users can remove entries from metrics.xml to stop the metric from reporting, or add new entries as well.
5. Restart the Machine Agent.

Please place the extension in the **"monitors"** directory of your **Machine Agent** installation directory. Do not place the extension in the **"extensions"** directory of your **Machine Agent** installation directory.

## Directory Structure

<table><tbody>
<tr>
<th align="left"> Directory/File </th>
<th align="left"> Description </th>
</tr>
<tr>
<td class='confluenceTd'> src/main/resources/conf </td>
<td class='confluenceTd'> Contains the config.yml and monitor.xml </td>
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

## Configuration
### Config.yml

Configure the extension by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/NginxMonitor/`.
  1. Configure the "COMPONENT_ID" under which the metrics need to be reported. This can be done by changing the value of `<COMPONENT_ID>` in   **metricPrefix: Server|Component:<TIER_ID>|Custom Metrics|Nginx|**.
       For example,
       ```
        metricPrefix:  "Server|Component:100>|Custom Metrics|Nginx|"
       ```
More details around metric prefix can be found [here](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695).

  2. The extension supports reporting metrics from multiple Nginx instances. The monitor provides an option to add Nginx server/s for monitoring the metrics provided by the particular end-point. Have a look at config.yml for more details.
      For example:
      ```
        metricPrefix:  "Server|Component:<TIER_ID>|Custom Metrics|Nginx|"

		servers:
		  - displayName: "Nginx Server" # mandatory
		    uri: "http://localhost/nginx_status" # append port if needed
      # uri: "http://demo.nginx.com/" # when nginx_plus equals true
		    username: ""
		    password: ""
		    encryptedPassword:
		    nginx_plus: "false"  # true for nginx plus else false

		encryptionKey: ""

		connection:
		  sslCertCheckEnabled: false
		  socketTimeout: 10000
		  connectTimeout: 10000

		 # For each server you monitor, you will need a total of 8(by default) thread.
		 # By default we want to support 5 servers, so it is 5 * 8 = 40 threads.
		numberOfThreads: 12
      ```
  3. If you want to monitor [nginx plus](https://www.nginx.com/products/nginx/) then put nginx_plus as true and make sure [ngx_http_api_module](http://nginx.org/en/docs/http/ngx_http_api_module.html) is configured.
        ```
             nginx_plus: "true"  # true for nginx plus else false
        ```
       **NOTE:** When you want to monitor Nginx Plus, then the uri should not have the nginx_status. uri should be the Hostname/IP as below.
        ```
             uri: "http://demo.nginx.com/"
        ```
  4. Configure the numberOfThreads.
     For example,
     If number of servers that need to be monitored is 5, then number of threads required is 5 * 12 = 60
     ```
     numberOfThreads: 60
     ```

### Metrics.xml
You can add/remove metrics of your choice by modifying the provided metrics.xml file. This file consists of all the metrics that will be monitored and sent to the controller. Please look how the metrics have been defined and follow the same convention, when adding new metrics. You do have the ability to chosoe your Rollup types as well as set an alias that you would like to be displayed on the metric browser.

   1. Stats Configuration
    Add the stats `url` which has api version(1/2/3) information as shown below.
        ```
		<stats url="/api/3">
        ```

   2. Metric Stat Configuration
    Add the `metric` to be monitored under the metric tag as shown below.
        ```
	    <stat suburl="processes" name="Processes-Status">
	        <metric attr="respawned" alias="Respawned" aggregationType = "AVERAGE" timeRollUpType = "AVERAGE" clusterRollUpType = "COLLECTIVE"/>
	    </stat>
        ```
For configuring the metrics, the following properties can be used:

 |     Property      |   Default value |         Possible values         |                                               Description                                                      |
 | ----------------- | --------------- | ------------------------------- | -------------------------------------------------------------------------------------------------------------- |
 | alias             | metric name     | Any string                      | The substitute name to be used in the metric browser instead of metric name.                                   |
 | aggregationType   | "AVERAGE"       | "AVERAGE", "SUM", "OBSERVATION" | [Aggregation qualifier](https://docs.appdynamics.com/display/latest/Build+a+Monitoring+Extension+Using+Java)    |
 | timeRollUpType    | "AVERAGE"       | "AVERAGE", "SUM", "CURRENT"     | [Time roll-up qualifier](https://docs.appdynamics.com/display/latest/Build+a+Monitoring+Extension+Using+Java)   |
 | clusterRollUpType | "INDIVIDUAL"    | "INDIVIDUAL", "COLLECTIVE"      | [Cluster roll-up qualifier](https://docs.appdynamics.com/display/latest/Build+a+Monitoring+Extension+Using+Java)|
 | multiplier        | 1               | Any number                      | Value with which the metric needs to be multiplied.                                                            |
 | convert           | null            | Any key value map               | Set of key value pairs that indicates the value to which the metrics need to be transformed. eg: UP:1, OPEN:1  |
 | delta             | false           | true, false                     | If enabled, gives the delta values of metrics instead of actual values.                                        |


 **All these metric properties are optional, and the default value shown in the table is applied to the metric (if a property has not been specified) by default.**



## Metrics
Nginx Monitoring Extension can collect metric by hitting the available [Endpoints](http://nginx.org/en/docs/http/ngx_http_api_module.html) which are configured in the metrics.xml.

### Nginx Metrics

| Metric Name | Description |
| --- | --- |
| Active Connections | Number of all open active connections |
| Server: Accepts | Number of accepted requests |
| Server: Handled | Number of handled requests |
| Server: Requests | Total number of requests  |
| Reading | Nginx reads request header  |
| Writing | Nginx reads request body, processes request, or writes response to a client  |
| Waiting | NginX keep-alive connections or currently active |

### Nginx Plus Metrics

#### Requests

| Metric Name | Description |
| --- | --- |
| total | The total number of client requests |
| current | The current number of client requests |

#### Server Zones
| Metric Name | Description |
| --- | --- |
| processing | The number of client requests that are currently being processed |
| requests | The total number of client requests received from clients |
| responses/total | The total number of responses sent to clients |
| responses/ 1xx, 2xx, 3xx, 4xx, 5xx  | The number of responses with status codes 1xx, 2xx, 3xx, 4xx, and 5xx |
| discarded | The total number of bytes discarded from clients  |
| received | The total number of bytes received from clients  |
| sent | The total number of bytes sent to clients |

#### Upstreams

| Metric Name | Description |
| --- | --- |
| active | The current number of active connections |
| backup | A boolean value indicating whether the server is a backup server |
| downtime  | Total time the server was in the “unavail” and “unhealthy” states |
| fails | The total number of unsuccessful attempts to communicate with the server |
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

#### Connections

| Metric Name | Description |
| --- | --- |
| accepted | The total number of accepted client connections |
| dropped | The total number of dropped client connections |
| active | The current number of active client connections  |
| idle | The current number of idle client connections |

#### Caches

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

## Credentials Encryption

Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130).

## Troubleshooting
Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension.

## Contributing
Always feel free to fork and contribute any changes directly here on [GitHub](https://github.com/Appdynamics/nginx-monitoring-extension).

## Version
|          Name            |  Version          |
|--------------------------|-------------------|
|Extension Version         |2.2.1              |
|Product Tested On         |1.13.3 and later   |
|Last Update               |21/12/2020         |
|Changes list              |[ChangeLog](https://github.com/Appdynamics/nginx-monitoring-extension/blob/master/CHANGELOG.md)|

**Note**: While extensions are maintained and supported by customers under the open-source licensing model, they interact with agents and Controllers that are subject to [AppDynamics’ maintenance and support policy](https://docs.appdynamics.com/latest/en/product-and-release-announcements/maintenance-support-for-software-versions). Some extensions have been tested with AppDynamics 4.5.13+ artifacts, but you are strongly recommended against using versions that are no longer supported.
