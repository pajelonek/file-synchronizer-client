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
     * <p>
     * After successful start of the application we collect list of all the files from
     * our directory with the ones on our server-side application and then we pass it to the rSyncFileUpdateProvider
     * for further actions.
     * <p>
     * After comparing files, method starts FileSystemWatcher component and FilePoolerServerListener service.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (environment.equalsIgnoreCase("PROD")) {
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

    /**
     * This method simply creates a list of UpdateFile objects, then runs recursive method which list
     * all of the files in client directory which we returns.
     *
     * @return the list of UpdateFile objects which represents files in the client directory
     */
    public List<UpdateFile> getClientFileList() {
        List<UpdateFile> clientFileList = new ArrayList<>();
        listFilesFromDirectory(Paths.get(userLocalDirectory), clientFileList);
        return clientFileList;
    }

    /**
     * This method cuts prefix of file paths because prefix is not guaranteed to be equal on server and client
     *
     * @param path to the file
     * @return path without its prefix
     */
    private String cutPrefixFromFilePath(String path) {
        return path.replace(mainFolder, "");
    }

    /**
     * Recursive method which recursively list all the files in the directory and its subdirectories.
     * During going over files it creates an UpdateFile object from the file and add this to the list of UpdateFile
     * objects provided as parameters.
     * If it encounters directory, it recursively goes to encountered directory.
     *
     * @param path           to the current directory which is being listed of all files
     * @param updateFileList which contains information of all encountered files
     */
    public void listFilesFromDirectory(Path path, List<UpdateFile> updateFileList) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path currentPath : stream)
                if (!Files.isDirectory(currentPath)) {
                    UpdateFile updateFile = new UpdateFile();
                    File file = currentPath.toFile();
                    updateFile.setFilePath(cutPrefixFromFilePath(file.getPath()));
                    updateFile.setLastModified(String.valueOf(file.lastModified()));
                    updateFileList.add(updateFile);
                } else listFilesFromDirectory(currentPath, updateFileList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getEnvironment() {
        return environment;
    }
}