/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.nginx.statsExtractor;


import org.json.JSONObject;

import java.util.Map;

public interface StatsExtractor {
    Map<String, String> extractStats(JSONObject respJson);
}