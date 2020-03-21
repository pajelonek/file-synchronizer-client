package com.licencjat.filesynchronizer.client.model;


public class UpdateFilesRS {

    private String status;

    public UpdateFilesRS() {
    }

    public UpdateFilesRS(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
