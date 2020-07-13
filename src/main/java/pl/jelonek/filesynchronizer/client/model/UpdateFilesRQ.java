package pl.jelonek.filesynchronizer.client.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "host",
        "mainFolder",
        "fileRQList",
})
public class UpdateFilesRQ {

    @JsonProperty("name")
    private String name;

    @JsonProperty("host")
    private String host;

    @JsonProperty("mainFolder")
    private String mainFolder;

    @JsonProperty("fileRQList")
    private List<UpdateFile> updateFile = null;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("mainFolder")
    public String getMainFolder() {
        return mainFolder;
    }

    @JsonProperty("mainFolder")
    public void setMainFolder(String mainFolder) {
        this.mainFolder = mainFolder;
    }

    @JsonProperty("fileRQList")
    public List<UpdateFile> getUpdateFile() {
        return updateFile;
    }

    @JsonProperty("fileRQList")
    public void setUpdateFile(List<UpdateFile> updateFile) {
        this.updateFile = updateFile;
    }

}