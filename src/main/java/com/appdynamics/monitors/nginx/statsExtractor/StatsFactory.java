package com.appdynamics.monitors.nginx.statsExtractor;

import org.apache.log4j.Logger;

public class StatsFactory {
    public static final Logger logger = Logger.getLogger(StatsFactory.class);

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
            logger.warn("No matching class in the factory for" + statsUrl);
            return null;
        }
    }
}
