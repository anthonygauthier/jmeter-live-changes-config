package io.github.delirius325.jmeter.config.livechanges.api;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import org.apache.jmeter.threads.JMeterContext;
import org.json.JSONObject;
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
        this.rest.GET("/test/connectivity", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content("You are connected!").end();
            }
        });
        this.rest.GET("/threads", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject();
                json.put("active", LiveChanges.getActiveThreads());
                response.content(json.toString()).header("Content-Type","application/json").end();
            }
        });
        this.rest.POST("/threads", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject(request.body());
                LiveChanges.setActiveThreads(Integer.parseInt(json.get("threadNum").toString()));
                json.put("info", "success");
                json.put("description", "Changed number of active threads");
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
        this.rest.GET("/variables", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject();
//                Threads threads = new Threads();
//                threads.getVariables();
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
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