package com.licencjat.filesynchronizer.client;


import com.licencjat.filesynchronizer.client.model.FileRQList;
import com.licencjat.filesynchronizer.client.model.UpdateFilesRQ;
import com.licencjat.filesynchronizer.client.model.UpdateFilesRS;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import com.licencjat.filesynchronizer.client.config.HttpClientConfig;
import com.licencjat.filesynchronizer.client.config.RestTemplateConfig;

import com.github.fracpete.rsync4j.SshKeyGen;
import com.github.fracpete.processoutput4j.output.ConsoleOutputProcessOutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RestTemplateConfig.class, HttpClientConfig.class})
public class TestApplication {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void updateFilesTestConnectivity() throws IOException {

        SshKeyGen keygen = new SshKeyGen()
                .outputCommandline(true)
//                .verbose(1)
                .keyType("dsa")
//                .newPassPhrase("")
//                .comment("test")
                .keyFile("~/testkey");
        ConsoleOutputProcessOutput output = new ConsoleOutputProcessOutput();
        try {
            output.monitor(keygen.builder());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println("kekw");
//        final String uri = "http://localhost:8888/updateFiles";
//
//        File file = new File("C:/Users/SG0306258/OneDrive - Sabre/Desktop/clientFiles/test1.txt");
//        File file2 = new File("C:/Users/SG0306258/OneDrive - Sabre/Desktop/clientFiles/test/test2.txt");
//        File file3 = new File("C:/Users/SG0306258/OneDrive - Sabre/Desktop/clientFiles/test3.txt");
//        File file4 = new File("C:/Users/SG0306258/OneDrive - Sabre/Desktop/clientFiles/test/0.jpg");
//
//        List<FileRQList> listOfFileRQLists = new ArrayList<>();
//
//        FileRQList fileRQList = new FileRQList();
//        fileRQList.setFilePath(file.getPath());
//        fileRQList.setLastModified(String.valueOf(file.lastModified()));
//        fileRQList.setAction("CREATED/MODIFIED");
//
//        FileRQList fileRQList2 = new FileRQList();
//        fileRQList2.setFilePath(file2.getPath());
//        fileRQList2.setLastModified(String.valueOf(file2.lastModified()));
//        fileRQList2.setAction("CREATED/MODIFIED");
//
//        FileRQList fileRQList3 = new FileRQList();
//        fileRQList3.setFilePath(file3.getPath());
//        fileRQList3.setLastModified(String.valueOf(file3.lastModified()));
//        fileRQList3.setAction("DELETED");
//
//        FileRQList fileRQList4 = new FileRQList();
//        fileRQList4.setFilePath(file4.getPath());
//        fileRQList4.setLastModified(String.valueOf(file4.lastModified()));
//        fileRQList4.setAction("CREATED/MODIFIED");
//
//        listOfFileRQLists.add(fileRQList);
//        listOfFileRQLists.add(fileRQList2);
//        listOfFileRQLists.add(fileRQList3);
//        listOfFileRQLists.add(fileRQList4);
//
//        UpdateFilesRQ updateFilesRQ = new UpdateFilesRQ();
//        updateFilesRQ.setName("UpdateFilesRQ");
//        updateFilesRQ.setFileRQList(listOfFileRQLists);
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//        HttpEntity<UpdateFilesRQ> updateFilesRQHttpEntity = new HttpEntity<>(updateFilesRQ, httpHeaders);
//
//        ResponseEntity<UpdateFilesRS> updateFilesResponseEntity = restTemplate.postForEntity(uri, updateFilesRQHttpEntity, UpdateFilesRS.class);
//
//        Assert.assertEquals(Objects.requireNonNull(updateFilesResponseEntity.getBody()).getStatus(), "success");
    }
}