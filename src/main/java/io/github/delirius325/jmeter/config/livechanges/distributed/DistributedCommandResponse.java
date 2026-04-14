package io.github.delirius325.jmeter.config.livechanges.distributed;

import org.json.JSONObject;

/**
 * Represents one worker's response to a distributed command.
 */
public class DistributedCommandResponse {
    private final int statusCode;
    private final JSONObject body;
    private final String failureReason;

    private DistributedCommandResponse(int statusCode, JSONObject body, String failureReason) {
        this.statusCode = statusCode;
        this.body = body;
        this.failureReason = failureReason;
    }

    public static DistributedCommandResponse success(int statusCode, JSONObject body) {
        return new DistributedCommandResponse(statusCode, body, null);
    }

    public static DistributedCommandResponse failure(int statusCode, String failureReason) {
        return new DistributedCommandResponse(statusCode, new JSONObject(), failureReason);
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public JSONObject getBody() {
        return this.body;
    }

    public String getFailureReason() {
        return this.failureReason;
    }

    public boolean isSuccess() {
        return this.failureReason == null && this.statusCode >= 200 && this.statusCode < 300
                && !"error".equalsIgnoreCase(this.body.optString("info"));
    }

    public String getInfo() {
        if (this.body.has("info")) {
            return this.body.optString("info");
        }
        return this.isSuccess() ? "success" : "error";
    }

    public String getDescription() {
        if (this.failureReason != null && !this.failureReason.isEmpty()) {
            return this.failureReason;
        }
        return this.body.optString("description");
    }
}
