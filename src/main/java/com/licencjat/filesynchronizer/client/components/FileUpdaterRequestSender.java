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

    private RestTemplate restTemplate = new RestTemplate();

    private Logger logger = LoggerFactory.getLogger(FileUpdaterRequestSender.class);

    /**
     * This method sends request to the server on the /removeFiles endpoint.
     * If we receive 200 from server we assume that files were removed successfully.
     *
     * @param updateFileList is the list of files to delete on the server directory
     */
    //todo add validation for RS
    public void removeFilesOnServer(List<UpdateFile> updateFileList) {
        if (updateFileList.size() > 0) {
            HttpEntity<UpdateFilesRQ> updateFilesRQHttpEntity = createUpdateFilesRQ(updateFileList);
            ResponseEntity<UpdateFilesRS> updateFilesResponseEntity = restTemplate.postForEntity(serverAddress + removeFilesEndpoint, updateFilesRQHttpEntity, UpdateFilesRS.class);
            if (updateFilesResponseEntity.getStatusCode().value() == 200) {
                logger.info("Successfully removed files on server");
            } else throw new Error("Could not remove files on server");
        }
    }

    /**
     * This method send GET request to the server to get server's file list.
     * If we receive 200 from server we assume that response is successful.
     *
     * @return list of all files to as Response Entity
     */
    //todo check if UPDATEFILESRS ma byc
    public ResponseEntity<UpdateFilesRQ> getServerFileList() {
        ResponseEntity<UpdateFilesRQ> getFileListRSResponseEntity = restTemplate.getForEntity(serverAddress + fileListEndpoint, UpdateFilesRQ.class);
        if (getFileListRSResponseEntity.getStatusCodeValue() == 200) {
            logger.info("Successfully received fileList from server");
        } else throw new Error("Could not obtain file list from server, check your connectivity to the server");

        return getFileListRSResponseEntity;
    }

    /**
     * This method sends request to the server to update modification dates on server.
     * If we receive 200 from server we assume that response is successful.
     *
     * @param updateFileList is the list of all files to update on server.
     */
    public void updateDateModification(List<UpdateFile> updateFileList) {
        logger.info("Updating modification date on server for {}", updateFileList.toString());
        HttpEntity<UpdateFilesRQ> updateFilesRQHttpEntity = createUpdateFilesRQ(updateFileList);
        ResponseEntity<UpdateFilesRS> updateFilesResponseEntity = restTemplate.postForEntity(serverAddress + setModificationDateEndpoint, updateFilesRQHttpEntity, UpdateFilesRS.class);
        if (updateFilesResponseEntity.getStatusCode().value() == 200) {
            logger.info("Successfully updated modification date on server");
        } else throw new Error("Could not update modification date on server");
    }

    /**
     * This method returns server's logfile list.
     * If servers returns 200 we assume response is successful.
     *
     * @return FileLogger that contains all changes that happened on the server for set interval
     */
    public ResponseEntity<FileLogger> getServerLogFile() {
        ResponseEntity<FileLogger> getLogFileResponseEntity = restTemplate.getForEntity(serverAddress + logFileEndpoint, FileLogger.class);
        if (getLogFileResponseEntity.getStatusCodeValue() != 200)
            throw new Error("Could not obtain logFile from server, check your connectivity to the server");
        return getLogFileResponseEntity;
    }

    /**
     * This method creates UpdateFileRQ as HttpEntity based on provided @param and
     * application-properties values
     *
     * @param updateFileList is the list of all files to set as body in RQ
     * @return HttpEntity to send to server endpoints
     */
    public HttpEntity<UpdateFilesRQ> createUpdateFilesRQ(List<UpdateFile> updateFileList) {
        UpdateFilesRQ updateFilesRQ = new UpdateFilesRQ();
        updateFilesRQ.setName("UpdateFilesRQ");
        updateFilesRQ.setHost(host);
        updateFilesRQ.setMainFolder(mainFolder);
        updateFilesRQ.setUpdateFile(updateFileList);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return new HttpEntity<>(updateFilesRQ, httpHeaders);
    }

}
