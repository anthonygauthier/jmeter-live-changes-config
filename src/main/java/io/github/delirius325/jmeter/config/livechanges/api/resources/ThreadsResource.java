package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.utils.JSONUtils;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/threads")
public class ThreadsResource {
    @GET
    @Path("/{param}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariables(@PathParam("param") String name) {
        JSONObject json = new JSONObject();
        json.put("active", LiveChanges.getActiveThreads());
        return Response.ok(json.toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postVariables(String request) {
        JSONObject json = new JSONObject(request);
        LiveChanges.setActiveThreads(Integer.parseInt(json.get("threadNum").toString()));
        JSONUtils.jsonSetInfo(json, "success", "Changed number of active threads.");
        return Response.ok(json.toString()).build();
    }
}