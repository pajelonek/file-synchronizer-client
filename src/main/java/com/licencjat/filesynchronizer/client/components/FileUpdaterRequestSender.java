package com.licencjat.filesynchronizer.client.components;

import com.licencjat.filesynchronizer.client.config.HttpClientConfig;
import com.licencjat.filesynchronizer.client.config.RestTemplateConfig;
import com.licencjat.filesynchronizer.client.model.FileLogger;
import com.licencjat.filesynchronizer.client.model.UpdateFile;
import com.licencjat.filesynchronizer.client.model.UpdateFilesRQ;
import com.licencjat.filesynchronizer.client.model.UpdateFilesRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private String serverAddress;

    @Value("${user.local.directory}")
    private String mainFolder;

    @Value("${client.name}")
    private String host;

    @Value("${file.synchronizer.removeFiles.endpoint}")
    private String removeFilesEndpoint;

    @Value("${file.synchronizer.fileList.endpoint}")
    private String fileListEndpoint;

    @Value("${file.synchronizer.setModificationDate.endpoint}")
    private String setModificationDateEndpoint;

    @Value("${file.synchronizer.logfile.endpoint}")
    private String logFileEndpoint;

    RestTemplate restTemplate = new RestTemplate();

    Logger logger = LoggerFactory.getLogger(FileUpdaterRequestSender.class);

    public void removeFilesOnServer(List<UpdateFile> updateFile) {
        if(updateFile.size() > 0) {
            HttpEntity<UpdateFilesRQ> updateFilesRQHttpEntity = createUpdateFilesRQEntity(updateFile);
            ResponseEntity<UpdateFilesRS> updateFilesResponseEntity = restTemplate.postForEntity(serverAddress + removeFilesEndpoint, updateFilesRQHttpEntity, UpdateFilesRS.class);
            if(updateFilesResponseEntity.getStatusCode().value() == 200){
                logger.info("Successfully removed files on server");

            } else throw new Error("Could not remove files on server");
        }
    }

    public ResponseEntity<UpdateFilesRQ> getServerFileList() {
        logger.info("Getting fileList from server");
        ResponseEntity<UpdateFilesRQ> getFileListRSResponseEntity = restTemplate.getForEntity(serverAddress + fileListEndpoint, UpdateFilesRQ.class);
        if (getFileListRSResponseEntity.getStatusCodeValue() == 200) {
            logger.info("Successfully received fileList from server");
        } else throw new Error("Could not obtain file list from server, check your connectivity to the server");

        return getFileListRSResponseEntity;
    }
    //todo add host to work
    public void updateDateModification(List<UpdateFile> updateFile) {
        logger.info("Updating modification date on server for {}", updateFile.toString());
        HttpEntity<UpdateFilesRQ> updateFilesRQHttpEntity = createUpdateFilesRQEntity(updateFile);
        ResponseEntity<UpdateFilesRS> updateFilesResponseEntity = restTemplate.postForEntity(serverAddress + setModificationDateEndpoint, updateFilesRQHttpEntity, UpdateFilesRS.class);
        if(updateFilesResponseEntity.getStatusCode().value() == 200){
            logger.info("Successfully updated modification date on server");
        }else throw new Error("Could not update modification date on server");
    }

    private HttpEntity<UpdateFilesRQ> createUpdateFilesRQEntity(List<UpdateFile> updateFile) {
        UpdateFilesRQ updateFilesRQ = new UpdateFilesRQ();
        updateFilesRQ.setName("UpdateFilesRQ");
        updateFilesRQ.setHost(host);
        updateFilesRQ.setMainFolder(mainFolder);
        updateFilesRQ.setUpdateFile(updateFile);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return new HttpEntity<>(updateFilesRQ, httpHeaders);
    }

    public ResponseEntity<FileLogger> getServerLogFile() {
        logger.info("Getting LogFile from server");
        ResponseEntity<FileLogger> getLogFileResponseEntity = restTemplate.getForEntity(serverAddress + logFileEndpoint, FileLogger.class);
        if (getLogFileResponseEntity.getStatusCodeValue() == 200) {
            logger.info("Successfully received logFile from server");
        } else throw new Error("Could not obtain logFile from server, check your connectivity to the server");

        return getLogFileResponseEntity;
    }
}
