package com.licencjat.filesynchronizer.client.components;

import com.licencjat.filesynchronizer.client.model.UpdateFile;
import com.licencjat.filesynchronizer.client.rsync.RSyncFileUpdaterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileChangeListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MyFileChangeListener implements FileChangeListener {

    @Autowired
    RSyncFileUpdaterProvider rSyncFileUpdaterProvider;

    @Autowired
    FileUpdaterRequestSender fileUpdaterRequestSender;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    Logger logger = LoggerFactory.getLogger(MyFileChangeListener.class);

    List<UpdateFile> filesFromServer = new ArrayList<>();

    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        List<ChangedFile> filterredFiles = clearChangeSetFromFilesFromServer(changeSet);
            Map<String, ChangedFile.Type> updatedFiles = new HashMap<>();
                for (ChangedFile changedFile : filterredFiles) {
                    if ((changedFile.getType().equals(ChangedFile.Type.MODIFY) || changedFile.getType().equals(ChangedFile.Type.ADD) && !isLocked(changedFile.getFile().toPath())) || changedFile.getType().equals(ChangedFile.Type.DELETE)) {
                        logger.info("Changed file: {}", changedFile.getFile().getName());
                        updatedFiles.put(changedFile.getFile().getPath(), changedFile.getType());
                    }
                }

            List<UpdateFile> fileToUpdate = rSyncFileUpdaterProvider.mapToFileRQList(updatedFiles);
            rSyncFileUpdaterProvider.processForServer(fileToUpdate);
    }

    private List<ChangedFile> clearChangeSetFromFilesFromServer(Set<ChangedFiles> changeSet) {
        List<String> changedFilesPaths = changeSet.stream()
                .map(ChangedFiles::getFiles)
                .flatMap(Collection::stream)
                .map(file -> file.getFile().getPath().replace(userLocalDirectory,""))
                .collect(Collectors.toList());

        List<String> filesToDeleteFromBuffer = new ArrayList<>();

        for(UpdateFile file : filesFromServer){
            if(changedFilesPaths.contains(file.getFilePath())){
             filesToDeleteFromBuffer.add(userLocalDirectory + file.getFilePath());
            }
        }

        filesFromServer = filesFromServer.stream()
                .filter(file1 -> !filesToDeleteFromBuffer.contains(userLocalDirectory + file1.getFilePath()))
                .collect(Collectors.toList());


        return changeSet.stream()
                .map(ChangedFiles::getFiles)
                .flatMap(Collection::stream)
                .filter(file -> !filesToDeleteFromBuffer.contains(file.getFile().getPath()))
                .collect(Collectors.toList());
    }


    private boolean isLocked(Path path) {
        try (FileChannel ch = FileChannel.open(path, StandardOpenOption.WRITE); FileLock lock = ch.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            return true;
        }
    }

    public void addFilesFromServer(List<UpdateFile> updateFileList){
        this.filesFromServer.addAll(updateFileList);
    }

}
