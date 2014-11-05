/**
 * Copyright 2014 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
