package io.github.delirius325.jmeter.config.livechanges.api;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.utils.JSONUtils;
import org.apache.jmeter.threads.JMeterVariables;
import org.json.JSONObject;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.rest.Rest;

public class Threads {
    public static void addRoutes(Rest instance) {
        postThreads(instance);
        getThreads(instance);
    }

    private static void postThreads(Rest rest) {
        rest.POST("/threads", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject(request.body());
                LiveChanges.setActiveThreads(Integer.parseInt(json.get("threadNum").toString()));
                JSONUtils.jsonSetInfo(json, "success", "Changed number of active threads.");
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
    }

    private static void getThreads(Rest rest) {
        rest.GET("/threads", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject();
                json.put("active", LiveChanges.getActiveThreads());
                response.content(json.toString()).header("Content-Type","application/json").end();
            }
        });
    }
}