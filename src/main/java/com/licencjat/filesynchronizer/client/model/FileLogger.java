package com.licencjat.filesynchronizer.client.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "currentTime",
        "logFileList"
})
public class FileLogger {

    @JsonProperty("currentTime")
    private String currentTime;
    @JsonProperty("logFileList")
    private List<LogFile> logFileList;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("currentTime")
    public String getCurrentTime() {
        return currentTime;
    }

    @JsonProperty("currentTime")
    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    @JsonProperty("logFileList")
    public List<LogFile> getLogFileList() {
        return logFileList;
    }

    @JsonProperty("logFileList")
    public void setLogFileList(List<LogFile> logFileList) {
        this.logFileList = logFileList;
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