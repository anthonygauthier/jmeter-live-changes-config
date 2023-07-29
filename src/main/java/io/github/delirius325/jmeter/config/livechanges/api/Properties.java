package io.github.delirius325.jmeter.config.livechanges.api;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.utils.JSONUtils;
import org.json.JSONObject;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.rest.Rest;

public class Properties {
    public static void addRoutes(Rest instance) {
        postProperties(instance);
        getProperties(instance);
    }

    private static void postProperties(Rest rest) {
        rest.POST("/properties", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject(request.body());
                java.util.Properties props = LiveChanges.getjMeterProperties();
                props.entrySet().forEach(entry -> {
                    if(json.has(entry.getKey().toString()) && (json.get(entry.getKey().toString()) != entry.getValue().toString()))  {
                        json.put(entry.getKey().toString(), json.get(entry.getKey().toString()));
                        props.put(entry.getKey(), json.get(entry.getKey().toString()));
                    }
                });
                JSONUtils.jsonSetInfo(json, "success", "Variables were changed.");
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
    }

    private static void getProperties(Rest rest) {
        rest.GET("/properties", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                JSONObject json = new JSONObject();
                LiveChanges.getjMeterProperties().entrySet().forEach(entry -> {
                    json.put(entry.getKey().toString(), entry.getValue().toString());
                });
                response.content(json.toString()).header("Content-Type", "application/json").end();
            }
        });
    }
}
