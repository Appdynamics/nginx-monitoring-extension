package com.appdynamics.monitors.nginx;

public class Constant {
    public static String URI;
    public static String USER_NAME;
    public static String PASSWORD;
    public static String ENCRYPTED_PASSWORD;
    public static String ENCRYPTION_KEY;
    public static String METRIC_SEPARATOR;
    public static String METRIC_PREFIX;
    public static String MonitorName;

    static {
        // #TODO Please change the case of "Nginx" if you change the name of the monitor.
        METRIC_PREFIX = "Custom Metrics|NGinX";
        MonitorName = "Nginx Monitor";
        URI = "uri";
        USER_NAME = "username";
        ENCRYPTED_PASSWORD = "encryptedPassword";
        ENCRYPTION_KEY = "encryptionKey";
        PASSWORD = "password";
        METRIC_SEPARATOR = "|";
    }
}
