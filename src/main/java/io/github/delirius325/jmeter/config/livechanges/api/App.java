package io.github.delirius325.jmeter.config.livechanges.api;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
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
        /**
         * GET ROUTES
         */
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
        this.rest.GET("/variables", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject();
                LiveChanges.getjMeterVariables().entrySet().forEach(entry -> {
                    json.put(entry.getKey(), entry.getValue().toString());
                });
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
        this.rest.GET("/properties", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject();
                LiveChanges.getjMeterProperties().entrySet().forEach(entry -> {
                    json.put(entry.getKey().toString(), entry.getValue().toString());
                });
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });


        /**
         * POST ROUTES
         */
        this.rest.POST("/threads", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject(request.body());
                LiveChanges.setActiveThreads(Integer.parseInt(json.get("threadNum").toString()));
                jsonSetInfo(json, "success", "Changed number of active threads.");
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
        this.rest.POST("/variables", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject(request.body());
                JMeterVariables originalVars = LiveChanges.getjMeterVariables();
                JMeterVariables newVars = new JMeterVariables();

                originalVars.entrySet().forEach(entry -> {
                    newVars.put(entry.getKey(), entry.getValue().toString());
                    if(json.has(entry.getKey()) && (json.get(entry.getKey()) != entry.getValue().toString())) {
                        json.put(entry.getKey(), json.get(entry.getKey()));
                    }
                });
                LiveChanges.setjMeterVariables(newVars);
                jsonSetInfo(json, "success", "Variables were changed.");
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
    }

    public void jsonSetInfo(JSONObject obj, String info, String description) {
        obj.put("info", info);
        obj.put("description", description);
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