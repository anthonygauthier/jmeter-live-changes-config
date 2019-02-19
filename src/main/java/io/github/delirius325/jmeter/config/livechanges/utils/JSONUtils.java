package io.github.delirius325.jmeter.config.livechanges.utils;

import org.json.JSONObject;

public class JSONUtils {
    public static void jsonSetInfo(JSONObject obj, String info, String description) {
        obj.put("info", info);
        obj.put("description", description);
    }
}
