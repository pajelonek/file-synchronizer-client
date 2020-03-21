package com.licencjat.filesynchronizer.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.filesynchronizer.client.model.FileRQ;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RestTemplateConfig.class, HttpClientConfig.class})
public class TestApplication {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void updateFilesTestConnectivity() throws IOException {
        final String uri = "http://localhost:8888/updateFiles";

        ArrayList<FileRQ> listOfFileRQS = new ArrayList<>();
        listOfFileRQS.add(new FileRQ("src/a", "01.01.01"));
        listOfFileRQS.add(new FileRQ("src/b", "02.02.02"));
        UpdateFilesRQ updateFilesRQ = new UpdateFilesRQ("update", listOfFileRQS);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<UpdateFilesRQ> updateFilesRQHttpEntity = new HttpEntity<>(updateFilesRQ, httpHeaders);

        ResponseEntity<UpdateFilesRS> updateFilesRSResponseEntity = restTemplate.postForEntity(uri, updateFilesRQHttpEntity, UpdateFilesRS.class);


        Assert.assertEquals(Objects.requireNonNull(updateFilesRSResponseEntity.getBody()).getStatus(),"success");
    }
}