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
        final String uri = "http://localhost:8888/updateFiles";

        File file = new File("C:/Users/SG0306258/OneDrive - Sabre/Desktop/testDirectory/test.txt");
        File file2 = new File("C:/Users/SG0306258/OneDrive - Sabre/Desktop/testDirectory/test2.txt");

        List<FileRQList> listOfFileRQLists = new ArrayList<>();
        FileRQList fileRQList = new FileRQList();
        fileRQList.setFilePath(file.getPath());
        fileRQList.setLastModified(String.valueOf(file.lastModified()));

        FileRQList fileRQList2 = new FileRQList();
        fileRQList2.setFilePath(file2.getPath());
        fileRQList2.setLastModified(String.valueOf(file2.lastModified()));

        listOfFileRQLists.add(fileRQList);
        listOfFileRQLists.add(fileRQList2);

        UpdateFilesRQ updateFilesRQ = new UpdateFilesRQ();
        updateFilesRQ.setName("UpdateFilesRQ");
        updateFilesRQ.setFileRQList(listOfFileRQLists);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<UpdateFilesRQ> updateFilesRQHttpEntity = new HttpEntity<>(updateFilesRQ, httpHeaders);

        ResponseEntity<UpdateFilesRS> updateFilesResponseEntity = restTemplate.postForEntity(uri, updateFilesRQHttpEntity, UpdateFilesRS.class);

        Assert.assertEquals(Objects.requireNonNull(updateFilesResponseEntity.getBody()).getStatus(), "success");
    }
}