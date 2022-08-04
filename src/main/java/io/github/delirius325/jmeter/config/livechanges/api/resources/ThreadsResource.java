package io.github.delirius325.jmeter.config.livechanges.api.resources;

import java.io.IOException;
import java.util.HashMap;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.jmeter.threads.ThreadGroup;
import org.json.JSONObject;
import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.helpers.JSONHelper;
import io.github.delirius325.jmeter.config.livechanges.helpers.ThreadGroupHelper;

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
     * @throws IOException Throws an IOException if an error occurs during method execution
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

        // add incoming thread change request to queue
        HashMap<String, Object> changeMap = new HashMap<String, Object>();
        changeMap.put("threadNum", Integer.parseInt(json.get("threadNum").toString()));

        LiveChanges.getChangeQueueMap().get(threadGroupName).add(changeMap);

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