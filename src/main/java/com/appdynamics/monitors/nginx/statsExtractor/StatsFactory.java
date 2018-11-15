package com.appdynamics.monitors.nginx.statsExtractor;

public class StatsFactory {
    public StatsExtractor getstatsExtractor(String statsUrl) {
        if (statsUrl.equals("server_zones")) {
            return new ServerZoneStatsExtractor();
        } else if (statsUrl.equals("caches")) {
            return  new CacheStatsExtractor();
        } else if (statsUrl.equals("upstreams")) {
            return new UpstreamsStatsExtractor();
        } else if (statsUrl.equals("slabs")) {
            return new SlabStatsExtractor();
        } else
            return null;
    }
}
