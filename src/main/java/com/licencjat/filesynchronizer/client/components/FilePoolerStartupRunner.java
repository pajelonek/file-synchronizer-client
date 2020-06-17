package com.licencjat.filesynchronizer.client.components;


import com.licencjat.filesynchronizer.client.config.HttpClientConfig;
import com.licencjat.filesynchronizer.client.config.RestTemplateConfig;
import com.licencjat.filesynchronizer.client.model.UpdateFile;
import com.licencjat.filesynchronizer.client.model.UpdateFilesRQ;
import com.licencjat.filesynchronizer.client.rsync.RSyncFileUpdaterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@ContextConfiguration(classes = {RestTemplateConfig.class, HttpClientConfig.class})
public class FilePoolerStartupRunner implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    FileUpdaterRequestSender fileUpdaterRequestSender;

    @Autowired
    FileSystemWatcher fileSystemWatcher;

    @Autowired
    RSyncFileUpdaterProvider rSyncFileUpdaterProvider;

    @Autowired
    FilePoolerServerListener filePoolerServerListener;

    @Value("${user.local.directory}")
    private String mainFolder;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    Logger logger = LoggerFactory.getLogger(FilePoolerStartupRunner.class);

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        logger.info("Application event start up");

        List<UpdateFile> clientFileList = getClientFileList();
        ResponseEntity<UpdateFilesRQ> updateFilesResponseEntity = fileUpdaterRequestSender.getServerFileList();

        rSyncFileUpdaterProvider
                .setRemoteMainFolder(Objects.requireNonNull(updateFilesResponseEntity.getBody()).getMainFolder())
                .processComparing(updateFilesResponseEntity.getBody().getUpdateFile(), clientFileList);

        fileSystemWatcher.start();
        logger.info("Started fileSystemWatcher service");
        filePoolerServerListener.initiateSynchronizeTime();
        filePoolerServerListener.triggerPoolerService();
        logger.info("Triggered filePoolerServiceListener service");
    }


    private List<UpdateFile> getClientFileList() {
        List<UpdateFile> clientFileList = new ArrayList<>();
        listFilesFromDirectory(Paths.get(userLocalDirectory), clientFileList);
        return clientFileList;
    }

    private String cutPrefixFromFilePath(String path) {
        return path.replace(mainFolder, "");
    }

    public void listFilesFromDirectory(Path path, List<UpdateFile> updateFile) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path pathInSubfolder : stream)
                if (!Files.isDirectory(pathInSubfolder)) {
                    UpdateFile fileRQ = new UpdateFile();
                    File file = pathInSubfolder.toFile();
                    fileRQ.setFilePath(cutPrefixFromFilePath(file.getPath()));
                    fileRQ.setLastModified(String.valueOf(file.lastModified()));
                    updateFile.add(fileRQ);
                } else listFilesFromDirectory(pathInSubfolder, updateFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}