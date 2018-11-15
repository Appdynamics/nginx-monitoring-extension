package com.appdynamics.monitors.nginx;

public class Constant {
    public static String URI;
    public static String PORT;
    public static String USER_NAME;
    public static String PASSWORD;
    public static String METRIC_SEPARATOR;
    public static String METRIC_PREFIX;
    public static String MonitorName;

    static {
        METRIC_PREFIX = "Custom Metrics|NGinX";
        MonitorName = "Nginx Monitor";
        URI = "uri";
        USER_NAME = "username";
        PASSWORD = "password";
        METRIC_SEPARATOR = "|";
        PORT = "80";

    }
}
