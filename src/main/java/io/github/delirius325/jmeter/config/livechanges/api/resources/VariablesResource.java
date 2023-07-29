package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.utils.JSONUtils;
import org.apache.jmeter.threads.JMeterVariables;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/variables")
public class VariablesResource {
    @GET
    @Path("/{param}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariables(@PathParam("param") String name) {
        JSONObject json = new JSONObject();
        LiveChanges.getjMeterVariables().entrySet().forEach(entry -> {
            json.put(entry.getKey(), entry.getValue().toString());
        });
        return Response.ok(json.toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postVariables(String request) {
        JSONObject json = new JSONObject(request);
        JMeterVariables vars = LiveChanges.getjMeterVariables();
        vars.entrySet().forEach(entry -> {
            if(json.has(entry.getKey()) && (json.get(entry.getKey()) != entry.getValue().toString())) {
                json.put(entry.getKey(), json.get(entry.getKey()));
                vars.put(entry.getKey(), json.get(entry.getKey()).toString());
            }
        });
        LiveChanges.setjMeterVariables(vars);
        JSONUtils.jsonSetInfo(json, "success", "VariablesResource were changed.");
        return Response.ok(json.toString()).build();
    }
}


