package pl.jelonek.filesynchronizer.client.model;

import com.fasterxml.jackson.annotation.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "currentTime",
        "logFileList",
        "lastSynchronizedTime"
})
public class FileLogger {

    @JsonProperty("currentTime")
    private String currentTime;

    @JsonProperty("logFileList")
    private List<LogFile> logFileList;

    @JsonProperty("lastSynchronizedTime")
    private String lastSynchronizedTime;

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

    public String getLastSynchronizedTime() {
        return lastSynchronizedTime;
    }

    public void setLastSynchronizedTime(String lastSynchronizedTime) {
        this.lastSynchronizedTime = lastSynchronizedTime;
    }
}