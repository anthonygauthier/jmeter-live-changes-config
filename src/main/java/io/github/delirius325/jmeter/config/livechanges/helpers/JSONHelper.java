package io.github.delirius325.jmeter.config.livechanges.helpers;

import org.json.JSONObject;

/**
 * Helper class that contains static methods to manipulate JSON
 */
public class JSONHelper {
    /**
     * Static method that adds informational properties to a JSON object
     * @param obj JSONObject
     * @param info String
     * @param description String
     */
    public static void jsonSetInfo(JSONObject obj, String info, String description) {
        obj.put("info", info);
        obj.put("description", description);
    }
}
