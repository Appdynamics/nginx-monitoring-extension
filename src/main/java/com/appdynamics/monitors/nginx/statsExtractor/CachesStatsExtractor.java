/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.nginx.statsExtractor;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CachesStatsExtractor implements StatsExtractor {

    @Override
    public Map<String, String> extractStats(JSONObject respJson) {
        JSONObject caches = respJson.getJSONObject("caches");
        Map<String, String> cachesStats = getCachesStats(caches);
        return cachesStats;
    }

    private Map<String, String> getCachesStats(JSONObject caches) {
        Map<String, String> cachesStats = new HashMap<String, String>();
        Set<String> cacheNames = caches.keySet();

        for (String cacheName : cacheNames) {
            JSONObject cache = caches.getJSONObject(cacheName);

            long size = cache.getLong("size");
            cachesStats.put("caches|" + cacheName + "|size", String.valueOf(size));

            long max_size = cache.getLong("max_size");
            cachesStats.put("caches|" + cacheName + "|max_size", String.valueOf(max_size));

            boolean cold = cache.getBoolean("cold");
            cachesStats.put("caches|" + cacheName + "|cold", String.valueOf(cold ? 0 : 1));

            for (String s : new String[]{"hit", "stale", "updating", "revalidated"}) {
                JSONObject jsonObject = cache.getJSONObject(s);
                long responses = jsonObject.getLong("responses");
                cachesStats.put("caches|" + cacheName + "|" + s + "|responses", String.valueOf(responses));

                long bytes = jsonObject.getLong("bytes");
                cachesStats.put("caches|" + cacheName + "|" + s + "|bytes", String.valueOf(bytes));
            }

            for (String s : new String[]{"miss", "expired", "bypass"}) {
                JSONObject jsonObject = cache.getJSONObject(s);
                long responses = jsonObject.getLong("responses");
                cachesStats.put("caches|" + cacheName + "|" + s + "|responses", String.valueOf(responses));

                long bytes = jsonObject.getLong("bytes");
                cachesStats.put("caches|" + cacheName + "|" + s + "|bytes", String.valueOf(bytes));

                long responses_written = jsonObject.getLong("responses_written");
                cachesStats.put("caches|" + cacheName + "|" + s + "|responses_written", String.valueOf(responses_written));

                long bytes_written = jsonObject.getLong("bytes_written");
                cachesStats.put("caches|" + cacheName + "|" + s + "|bytes_written", String.valueOf(bytes_written));
            }
        }
        return cachesStats;
    }
}
