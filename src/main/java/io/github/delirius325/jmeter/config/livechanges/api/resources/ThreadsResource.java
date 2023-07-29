package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.utils.JSONHelper;
import io.github.delirius325.jmeter.config.livechanges.utils.ThreadGroupHelper;
import org.apache.jmeter.threads.ThreadGroup;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Base endpoint for the ThreadResource class
 */
@Path("/threads")
public class ThreadsResource {
    /**
     * POST Endpoint allowing the user to change a specific ThreadGroup values
     * @param request - The user's request
     * @param threadGroupName - The ThreadGroup specified in the URL
     * @return A stringified JSONObject as the Response
     * @throws IOException
     */
    @POST
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifySpecificThread(String request, @PathParam("name") String threadGroupName) throws IOException {
        JSONObject json = new JSONObject(request);
        ThreadGroup threadGroup = ThreadGroupHelper.getThreadGroup(threadGroupName);
        if(threadGroup == null) {
            json.remove("threadNum");
            JSONHelper.jsonSetInfo(json, "error", String.format("Thread Group %s does not exist.", threadGroupName));
            return Response.ok(json.toString()).build();
        }
        LiveChanges.setActiveThreads(Integer.parseInt(json.get("threadNum").toString()));
        LiveChanges.setActiveThreadGroup(threadGroup);
        JSONHelper.jsonSetInfo(json, "success", "Changed number of active threads.");
        return Response.ok(json.toString()).build();
    }

    /**
     * GET Endpoint allowing the user to retrieve all thread groups information
     * @return A stringified JSONArray as the Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThreads() {
        return Response.ok(ThreadGroupHelper.getAllThreadGroupsAsJSON().toString()).build();
    }

    /**
     * GET Endpoint allowing the user to retrieve information for a specific thread group
     * @param threadGroupName - the thread group to retrieve information for
     * @return A stringified JSONObject as the Response
     */
    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThread(@PathParam("name") String threadGroupName) {
        JSONObject jsonObject = ThreadGroupHelper.getThreadGroupAsJSON(threadGroupName);
        if(jsonObject.keySet().size() < 1) {
            JSONHelper.jsonSetInfo(jsonObject, "error", String.format("Thread Group %s does not exist.", threadGroupName));
        }
        return Response.ok(jsonObject.toString()).build();
    }
}