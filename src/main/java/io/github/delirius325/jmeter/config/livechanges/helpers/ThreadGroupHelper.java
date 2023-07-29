package io.github.delirius325.jmeter.config.livechanges.helpers;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Class that contains static methods to manipulate Thread Groups and retrieve information
 */
public class ThreadGroupHelper {
    /**
     * Static method that retrieves Thread Groups from the test tree
     * @return Set of ThreadGroup objects
     */
    private static HashMap<String, ThreadGroup> getThreadGroups() {
        HashMap<String, ThreadGroup> threadGroups = new HashMap<>();
        for(ThreadGroup threadGroup : LiveChanges.getTestThreadGroups()) {
            threadGroups.put(threadGroup.getName(), threadGroup);
        }
        return threadGroups;
    }

    /**
     * Static method that retrieves a specific Thread Group from the test tree
     * @param threadGroupName - the name of the thread group to find
     * @return A single ThreadGroup object
     */
    public static ThreadGroup getThreadGroup(String threadGroupName) {
        HashMap<String, ThreadGroup> threadGroups = getThreadGroups();
        if(threadGroups.containsKey(threadGroupName)) {
            return threadGroups.get(threadGroupName);
        }
        return null;
    }

    /**
     * Method that retrieves a thread group and transforms it to a JSONObject
     * @param threadGroupName - the name of the thread group to find
     * @return A single JSONObject containing the Thread Group info
     */
    public static JSONObject getThreadGroupAsJSON(String threadGroupName) {
        ThreadGroup threadGroup = getThreadGroup(threadGroupName);
        return createThreadGroupObject(threadGroup);
    }

    /**
     * Method that retrieves all thread groups and transforms them to a JSONArray
     * @return A JSONArray containing all Thread Groups info
     */
    public static JSONArray getAllThreadGroupsAsJSON() {
        JSONArray jsonArray = new JSONArray();
        for(Map.Entry entry : getThreadGroups().entrySet()) {
            jsonArray.put(createThreadGroupObject((ThreadGroup) entry.getValue()));
        }

        return jsonArray;
    }

    /**
     * Utility method that creates a JSONObject from a ThreadGroup
     * @param threadGroup - ThreadGroup object
     * @return A single JSONObject containing the useful info for a ThreadGroup
     */
    public static JSONObject createThreadGroupObject(ThreadGroup threadGroup) {
        if(threadGroup != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", threadGroup.getName());
            jsonObject.put("rampUp", threadGroup.getRampUp());
            jsonObject.put("isEnabled", threadGroup.isEnabled());
            jsonObject.put("active", threadGroup.numberOfActiveThreads());
            jsonObject.put("comment", threadGroup.getComment());
            jsonObject.put("getOnErrorStartNextLoop",  threadGroup.getOnErrorStartNextLoop());
            jsonObject.put("getOnErrorStopTest",  threadGroup.getOnErrorStopTest());
            jsonObject.put("getOnErrorStopTestNow",  threadGroup.getOnErrorStopTestNow());
            jsonObject.put("getOnErrorStopThread",  threadGroup.getOnErrorStopThread());

            if(threadGroup.getScheduler()) {
                JSONObject schedulerObject = new JSONObject();
                schedulerObject.put("delay", threadGroup.getDelay());
                schedulerObject.put("duration", threadGroup.getDuration());
                jsonObject.put("scheduler", schedulerObject);
            }
            return jsonObject;
        }
        return new JSONObject();
    }
}
