package io.github.delirius325.jmeter.config.livechanges.utils;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that contains static methods to manipulate Thread Groups and retrieve information
 */
public class ThreadGroupHelper {
    /**
     * Static method that retrieves Thread Groups from the test tree
     * @return Set of ThreadGroup objects
     */
    private static Set<ThreadGroup> getThreadGroups() {
        HashSet<ThreadGroup> threadGroups = new HashSet<>();
        HashTree testPlanTree = LiveChanges.getTestPlanTree();
        for (Map.Entry entry : testPlanTree.entrySet()) {
            if (entry.getKey() instanceof TestPlan) {
                HashTree testPlan = (HashTree) entry.getValue();

                for (Map.Entry element : testPlan.entrySet()) {
                    if(element.getKey() instanceof ThreadGroup) {
                        threadGroups.add((ThreadGroup) element.getKey());
                    }
                }
            }
        }
        return threadGroups;
    }

    /**
     * Static method that retrieves a specific Thread Group from the test tree
     * @param threadGroupName - the name of the thread group to find
     * @return A single ThreadGroup object
     */
    public static ThreadGroup getThreadGroup(String threadGroupName) {
        for(ThreadGroup threadGroup : getThreadGroups()) {
            if(threadGroup.getName().equalsIgnoreCase(threadGroupName)) {
                return threadGroup;
            }
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
        for(ThreadGroup threadGroup : getThreadGroups()) {
            jsonArray.put(createThreadGroupObject(threadGroup));
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
            jsonObject.put("delay", threadGroup.getDelay());
            jsonObject.put("duration", threadGroup.getDuration());
            jsonObject.put("rampUp", threadGroup.getRampUp());
            jsonObject.put("scheduler", threadGroup.getScheduler());
            jsonObject.put("isEnabled", threadGroup.isEnabled());
            jsonObject.put("active", threadGroup.numberOfActiveThreads());
            return jsonObject;
        }
        return new JSONObject();
    }
}
