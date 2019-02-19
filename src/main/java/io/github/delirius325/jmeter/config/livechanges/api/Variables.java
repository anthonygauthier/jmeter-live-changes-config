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

public class Variables {
    public static void addRoutes(Rest instance) {
        postVariables(instance);
        getVariables(instance);
    }

    private static void postVariables(Rest rest) {
        rest.POST("/variables", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject(request.body());
                JMeterVariables vars = LiveChanges.getjMeterVariables();
                vars.entrySet().forEach(entry -> {
                    if(json.has(entry.getKey()) && (json.get(entry.getKey()) != entry.getValue().toString())) {
                        json.put(entry.getKey(), json.get(entry.getKey()));
                        vars.put(entry.getKey(), json.get(entry.getKey()).toString());
                    }
                });
                LiveChanges.setjMeterVariables(vars);
                JSONUtils.jsonSetInfo(json, "success", "Variables were changed.");
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
    }

    private static void getVariables(Rest rest) {
        rest.GET("/variables", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject();
                LiveChanges.getjMeterVariables().entrySet().forEach(entry -> {
                    json.put(entry.getKey(), entry.getValue().toString());
                });
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
    }
}


