package com.licencjat.filesynchronizer.client.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "host",
        "filepath",
        "timeOfChange",
        "lastModified",
        "action"
})
public class LogFile {
    @JsonProperty("host")
    private String host;
    @JsonProperty("filePath")
    private String filePath;
    @JsonProperty("timeOfChange")
    private String timeOfChange;
    @JsonProperty("lastModified")
    private String lastModified;
    @JsonProperty("action")
    private String action;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("filePath")
    public String getFilePath() {
        return filePath;
    }

    @JsonProperty("filePath")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @JsonProperty("timeOfChange")
    public String getTimeOfChange() {
        return timeOfChange;
    }

    @JsonProperty("timeOfChange")
    public void setTimeOfChange(String timeOfChange) {
        this.timeOfChange = timeOfChange;
    }

    @JsonProperty("lastModified")
    public String getLastModified() {
        return lastModified;
    }

    @JsonProperty("lastModified")
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
