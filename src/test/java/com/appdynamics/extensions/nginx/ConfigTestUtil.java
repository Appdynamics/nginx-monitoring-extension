/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.nginx;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.nginx.Config.Stat;
import org.mockito.Mockito;

import java.io.File;

public class ConfigTestUtil {

    public static MonitorContextConfiguration getContextConfiguration(String xmlPath, String configPath) {
        MonitorContextConfiguration configuration = new MonitorContextConfiguration("NginxMonitor", "Custom Metrics|Nginx|", Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));
        if (configPath != null)
            configuration.setConfigYml(configPath);
        if (xmlPath != null)
            configuration.setMetricXml(xmlPath, Stat.Stats.class);
        return configuration;
    }

    public static Stat getAptStat(Stat[] stats, String statsKey, String subStatsKey) {
        for (Stat statItr : stats) {
            if (statItr.getName().equals(statsKey)) {
                if (statItr.getStats() != null)
                    return getAptSubStat(statItr.getStats(), subStatsKey);
                else
                    return statItr;
            }
        }
        return null;
    }

    public static Stat getAptSubStat(Stat[] stats, String subStatsKey) {
        for (Stat subStatItr : stats) {
            if (subStatItr.getName().equals(subStatsKey))
                return subStatItr;
        }
        return null;
    }
}
