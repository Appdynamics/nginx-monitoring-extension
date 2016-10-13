package com.appdynamics.monitors.nginx;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by adityajagtiani on 8/31/16.
 */
public class NGinXMonitor extends AManagedMonitor {
    public static final Logger logger = LoggerFactory.getLogger(NGinXMonitor.class);
    private MonitorConfiguration configuration;

    public NGinXMonitor() {
        logger.info(String.format("Using NGinXMonitor Version [%s]", getImplementationVersion()));
    }

    private static String getImplementationVersion() {
        return NGinXMonitor.class.getPackage().getImplementationTitle();
    }

    protected void initialize(Map<String, String> argsMap) {
        if (configuration == null) {
            MetricWriteHelper metricWriter = MetricWriteHelperFactory.create(this);
            MonitorConfiguration conf = new MonitorConfiguration("Custom Metrics|WebServer|NGinX", new TaskRunner(),metricWriter);
            final String configFilePath = argsMap.get("config-file");
            conf.setConfigYml(configFilePath);
            conf.checkIfInitialized(MonitorConfiguration.ConfItem.METRIC_PREFIX, MonitorConfiguration.ConfItem.CONFIG_YML, MonitorConfiguration.ConfItem.HTTP_CLIENT
                    , MonitorConfiguration.ConfItem.EXECUTOR_SERVICE);
            this.configuration = conf;
        }
    }

    private class TaskRunner implements Runnable{

        @Override
        public void run () {
            Map<String, ?> config = configuration.getConfigYml();
            List<Map> servers = (List) config.get("servers");
            if (servers != null && !servers.isEmpty()) {
                for (Map server : servers) {
                    NGinXMonitorTask task = new NGinXMonitorTask(configuration, server);
                    configuration.getExecutorService().execute(task);
                }
            } else {
                logger.error("The stats read from the metric xml is empty. Please make sure that the metrics xml is correct");
            }
        }
    }

    public TaskOutput execute(Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        logger.debug("The raw arguments are {}", map);
        try {
            initialize(map);
            configuration.executeTask();
        }
        catch(Exception e){
            if(configuration != null && configuration.getMetricWriter() != null) {
                configuration.getMetricWriter().registerError(e.getMessage(), e);
            }
        }
        return null;
    }
}
