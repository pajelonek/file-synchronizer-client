package com.licencjat.filesynchronizer.client.components;


import com.licencjat.filesynchronizer.client.model.FileRQList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FilePoolerStartupRunner implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    @Autowired
    FileUpdaterRequestSender fileUpdaterRequestSender;

    @Autowired
    FileSystemWatcher fileSystemWatcher;

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

//        System.out.println("Application event start up");

//        fileSystemWatcher.start();
//        System.out.println("started fileSystemWatcher");

        List<FileRQList> fileRQList = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(userLocalDirectory))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    FileRQList fileRQ = new FileRQList();
                    File file = path.toFile();
                    fileRQ.setFilePath(file.getPath());
                    fileRQ.setLastModified(String.valueOf(file.lastModified()));
                    fileRQList.add(fileRQ);
                }
                else listFilesFromDirectory(path, fileRQList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileUpdaterRequestSender.process(fileRQList);
    }

    public void listFilesFromDirectory(Path path,  List<FileRQList> fileRQList) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path pathInSubfolder : stream)
                if (!Files.isDirectory(pathInSubfolder)) {
                    FileRQList fileRQ = new FileRQList();
                    File file = pathInSubfolder.toFile();
                    fileRQ.setFilePath(file.getPath());
                    fileRQ.setLastModified(String.valueOf(file.lastModified()));
                    fileRQList.add(fileRQ);
                }
                else listFilesFromDirectory(pathInSubfolder, fileRQList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}