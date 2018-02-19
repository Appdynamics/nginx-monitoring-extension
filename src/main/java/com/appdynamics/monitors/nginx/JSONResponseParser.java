/*
 * Copyright 2013. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.nginx;

import com.appdynamics.monitors.nginx.statsExtractor.*;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adityajagtiani on 10/10/16.
 */
public class JSONResponseParser {

    public JSONResponseParser() {
    }

    public Map<String, String> parseResponse (String responseBody) {
        Map<String, String> resultMap = new HashMap<String, String>();
        JSONObject jsonObject = new JSONObject(responseBody);

        StatsExtractor connectionsStatsExtractor = new ConnectionsStatsExtractor();
        Map<String, String> connectionStats = connectionsStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(connectionStats);

        StatsExtractor requestsStatsExtractor = new RequestsStatsExtractor();
        Map<String, String> requestStats = requestsStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(requestStats);

        StatsExtractor serverZoneStatsExtractor = new ServerZoneStatsExtractor();
        Map<String, String> serverZonesStats = serverZoneStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(serverZonesStats);

        StatsExtractor upstreamsStatsExtractor = new UpstreamsStatsExtractor();
        Map<String, String> upstreamsStats = upstreamsStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(upstreamsStats);

        StatsExtractor cachesStatsExtractor = new CachesStatsExtractor();
        Map<String, String> cachesStats = cachesStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(cachesStats);
        return resultMap;
    }
}
