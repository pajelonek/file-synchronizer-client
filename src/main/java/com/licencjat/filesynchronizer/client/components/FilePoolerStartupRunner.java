package com.licencjat.filesynchronizer.client.components;


import com.licencjat.filesynchronizer.client.config.HttpClientConfig;
import com.licencjat.filesynchronizer.client.config.RestTemplateConfig;
import com.licencjat.filesynchronizer.client.model.UpdateFile;
import com.licencjat.filesynchronizer.client.model.UpdateFilesRQ;
import com.licencjat.filesynchronizer.client.rsync.RSyncFileUpdaterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    final FileUpdaterRequestSender fileUpdaterRequestSender;

    final FileSystemWatcher fileSystemWatcher;

    final RSyncFileUpdaterProvider rSyncFileUpdaterProvider;

    final FilePoolerServerListener filePoolerServerListener;

    @Value("${user.local.directory}")
    private String mainFolder;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    @Value("${environment}")
    private String environment;


    Logger logger = LoggerFactory.getLogger(FilePoolerStartupRunner.class);

    public FilePoolerStartupRunner(FileUpdaterRequestSender fileUpdaterRequestSender, FileSystemWatcher fileSystemWatcher, RSyncFileUpdaterProvider rSyncFileUpdaterProvider, FilePoolerServerListener filePoolerServerListener) {
        this.fileUpdaterRequestSender = fileUpdaterRequestSender;
        this.fileSystemWatcher = fileSystemWatcher;
        this.rSyncFileUpdaterProvider = rSyncFileUpdaterProvider;
        this.filePoolerServerListener = filePoolerServerListener;
    }

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if(environment.equalsIgnoreCase("PROD")) {
            logger.info("Application event start up");

            List<UpdateFile> clientFileList = getClientFileList();
            ResponseEntity<UpdateFilesRQ> updateFileResponse = fileUpdaterRequestSender.getServerFileList();

            rSyncFileUpdaterProvider
                    .setRemoteMainFolder(Objects.requireNonNull(updateFileResponse.getBody()).getMainFolder())
                    .processComparing(updateFileResponse.getBody().getUpdateFile(), clientFileList);

            fileSystemWatcher.start();
            logger.info("Started fileSystemWatcher service");

            filePoolerServerListener.initiateSynchronizeTime();
            filePoolerServerListener.triggerPoolerService();
            logger.info("Triggered filePoolerServerListener service");
        }
    }


    public List<UpdateFile> getClientFileList() {
        List<UpdateFile> clientFileList = new ArrayList<>();
        listFilesFromDirectory(Paths.get(userLocalDirectory), clientFileList);
        return clientFileList;
    }

    private String cutPrefixFromFilePath(String path) {
        return path.replace(mainFolder, "");
    }

    public void listFilesFromDirectory(Path path, List<UpdateFile> updateFileList) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path pathInSubfolder : stream)
                if (!Files.isDirectory(pathInSubfolder)) {
                    UpdateFile updateFile = new UpdateFile();
                    File file = pathInSubfolder.toFile();
                    updateFile.setFilePath(cutPrefixFromFilePath(file.getPath()));
                    updateFile.setLastModified(String.valueOf(file.lastModified()));
                    updateFileList.add(updateFile);
                } else listFilesFromDirectory(pathInSubfolder, updateFileList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getEnvironment(){
        return environment;
    }
}