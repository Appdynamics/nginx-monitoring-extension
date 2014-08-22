package com.appdynamics.monitors.nginx;



import java.io.IOException;

public class TestNGinXMonitor {

   // @Test
    public void parseResultsSuccessfully() throws IOException {
        String response = "Active connections: 37 \n" +
                "server accepts handled requests \n" +
                "10574 10574 10649 \n" +
                "Reading: 0 Writing: 1 Waiting: 36";

        NGinXMonitor nGinXMonitor = new NGinXMonitor();
        nGinXMonitor.parseResults(response);
    }


}
