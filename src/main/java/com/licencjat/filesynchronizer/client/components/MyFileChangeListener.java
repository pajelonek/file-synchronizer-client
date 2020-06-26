package com.licencjat.filesynchronizer.client.components;

import com.licencjat.filesynchronizer.client.model.UpdateFile;
import com.licencjat.filesynchronizer.client.rsync.RSyncFileUpdaterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    final RSyncFileUpdaterProvider rSyncFileUpdaterProvider;

    final FileUpdaterRequestSender fileUpdaterRequestSender;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    Logger logger = LoggerFactory.getLogger(MyFileChangeListener.class);

    List<UpdateFile> filesFromServer = new ArrayList<>();

    public MyFileChangeListener(RSyncFileUpdaterProvider rSyncFileUpdaterProvider, FileUpdaterRequestSender fileUpdaterRequestSender) {
        this.rSyncFileUpdaterProvider = rSyncFileUpdaterProvider;
        this.fileUpdaterRequestSender = fileUpdaterRequestSender;
    }

    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        List<ChangedFile> filteredFiles = clearChangeSetFromFilesFromServer(changeSet);
        Map<String, ChangedFile.Type> updatedFilesMap = new HashMap<>();
        for (ChangedFile changedFile : filteredFiles) {
            if ((changedFile.getType().equals(ChangedFile.Type.MODIFY) || changedFile.getType().equals(ChangedFile.Type.ADD) && !isLocked(changedFile.getFile().toPath())) || changedFile.getType().equals(ChangedFile.Type.DELETE)) {
                logger.info("Changed file: {}", changedFile.getFile().getName());
                updatedFilesMap.put(changedFile.getFile().getPath(), changedFile.getType());
            }
        }

        List<UpdateFile> clientFileList = rSyncFileUpdaterProvider.mapToUpdateFileList(updatedFilesMap);
        rSyncFileUpdaterProvider.processForServer(clientFileList);
    }
    //todo if filesfromserver == 0 -> ignore
    //todo make better
    public List<ChangedFile> clearChangeSetFromFilesFromServer(Set<ChangedFiles> changeSet) {
        List<String> filesToDeleteFromBuffer = new ArrayList<>();

        if(!filesFromServer.isEmpty()) {

            List<String> changedFilesWithoutPrefixesList = changeSet.stream()
                    .map(ChangedFiles::getFiles)
                    .flatMap(Collection::stream)
                    .map(file -> file.getFile().getPath().replace(userLocalDirectory, ""))
                    .collect(Collectors.toList());

            for (UpdateFile file : filesFromServer) {
                if (changedFilesWithoutPrefixesList.contains(file.getFilePath())) {
                    filesToDeleteFromBuffer.add(userLocalDirectory + file.getFilePath());
                }
            }

             filesFromServer = filesFromServer.stream()
                    .filter(fileFromServer -> !filesToDeleteFromBuffer.contains(userLocalDirectory + fileFromServer.getFilePath()))
                    .collect(Collectors.toList());
        }

        return changeSet.stream()
                .map(ChangedFiles::getFiles)
                .flatMap(Collection::stream)
                .filter(file -> !filesToDeleteFromBuffer.contains(file.getFile().getPath()))
                .collect(Collectors.toList());
    }


    public boolean isLocked(Path path) {
        try (FileChannel ch = FileChannel.open(path, StandardOpenOption.WRITE); FileLock lock = ch.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            return true;
        }
    }

    public void addFilesFromServerToBuffer(List<UpdateFile> updateFileList) {
        this.filesFromServer.addAll(updateFileList);
    }

    public void setFilesFromServer(List<UpdateFile> filesFromServer) {
        this.filesFromServer = filesFromServer;
    }

    public List<UpdateFile> getFilesFromServer(){
        return this.filesFromServer;
    }

}
