package com.licencjat.filesynchronizer.client.model;


public class FileRQ {
    private String filePath;
    private String lastModified;

    public FileRQ() {
    }

    public FileRQ(String filePath, String lastModified) {
        this.filePath = filePath;
        this.lastModified = lastModified;
    }

    public String getFilePath() {
        return filePath;
    }


    public String getLastModified() {
        return lastModified;
    }
}
