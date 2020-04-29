/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.nginx.statsExtractor;


import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

public class StatsFactory {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(StatsFactory.class);

    public StatsExtractor getstatsExtractor(String statsUrl) {
        if (statsUrl.equals("server_zones")) {
            return new ServerZoneStatsExtractor();
        } else if (statsUrl.equals("caches")) {
            return new CacheStatsExtractor();
        } else if (statsUrl.equals("upstreams")) {
            return new UpstreamsStatsExtractor();
        } else if (statsUrl.equals("slabs")) {
            return new SlabStatsExtractor();
        } else {
            return null;
        }
    }
}
