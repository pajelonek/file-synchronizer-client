package com.licencjat.filesynchronizer.client.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "status",
        "fileRQList"
})
public class UpdateFilesRS {

    @JsonProperty("status")
    private String status;

    @JsonProperty("fileRQList")
    private List<UpdateFileStatus> updateFile = null;

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("fileRQList")
    public List<UpdateFileStatus> getUpdateFile() {
        return updateFile;
    }

    @JsonProperty("fileRQList")
    public void setUpdateFile(List<UpdateFileStatus> updateFile) {
        this.updateFile = updateFile;

    }

}