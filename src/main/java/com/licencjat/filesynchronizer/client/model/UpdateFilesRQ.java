package com.licencjat.filesynchronizer.client.model;


import java.util.ArrayList;

public class UpdateFilesRQ {

    private String name;
    private ArrayList<FileRQ> fileRQList;

    public UpdateFilesRQ() {

    }

    public UpdateFilesRQ(String name, ArrayList<FileRQ> fileRQList) {
        this.name = name;
        this.fileRQList = fileRQList;
    }

    public String getName() {
        return name;
    }


    public ArrayList<FileRQ> getFileRQList() {
        return fileRQList;
    }
}