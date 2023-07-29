package io.github.delirius325.jmeter.config.livechanges.api;

import org.apache.jmeter.threads.JMeterContext;
import org.webbitserver.*;
import org.webbitserver.netty.NettyWebServer;
import org.webbitserver.rest.Rest;

public class App {
    private WebServer instance;
    private Rest rest;
    private int port;
    private JMeterContext ctx;

    public App(int p) {
        this.port = p;
        this.instance = new NettyWebServer(this.port);
        this.rest = new Rest(this.instance);
    }

    public void setupRoutes() {
        /**
         * Test route
         */
        this.rest.GET("/test/connectivity", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content("You are connected!").end();
            }
        });

        /**
         * Other routes
         */
        Threads.addRoutes(rest);
        Variables.addRoutes(rest);
        Properties.addRoutes(rest);
    }

    /**
     *  Methods to start or stop the API
     */
    public void start() {
        this.instance.start();
        this.setupRoutes();
    }

    public void stop() {
        this.instance.stop();
    }
}