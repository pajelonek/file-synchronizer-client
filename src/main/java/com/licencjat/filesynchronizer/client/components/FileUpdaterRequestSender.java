package com.licencjat.filesynchronizer.client.components;

import com.licencjat.filesynchronizer.client.config.HttpClientConfig;
import com.licencjat.filesynchronizer.client.config.RestTemplateConfig;
import com.licencjat.filesynchronizer.client.model.FileRQList;
import com.licencjat.filesynchronizer.client.model.UpdateFilesRQ;
import com.licencjat.filesynchronizer.client.model.UpdateFilesRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
@ContextConfiguration(classes = {RestTemplateConfig.class, HttpClientConfig.class})
public class FileUpdaterRequestSender {

    @Value("${file.synchronizer.address}")
    private String serverAddres;


    RestTemplate rest = new RestTemplate();


    public void process(List<FileRQList> fileRQList) {

        UpdateFilesRQ updateFilesRQ = new UpdateFilesRQ();
        updateFilesRQ.setName("UpdateFilesRQ");
        updateFilesRQ.setFileRQList(fileRQList);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<UpdateFilesRQ> updateFilesRQHttpEntity = new HttpEntity<>(updateFilesRQ, httpHeaders);

        ResponseEntity<UpdateFilesRS> updateFilesResponseEntity = rest.postForEntity(getServerUri(serverAddres), updateFilesRQHttpEntity, UpdateFilesRS.class);
    }

    private String getServerUri(String serverAddres) {
        return serverAddres + "compareFiles";
    }
}
