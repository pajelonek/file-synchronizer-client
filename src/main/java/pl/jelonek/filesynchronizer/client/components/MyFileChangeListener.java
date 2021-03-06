package pl.jelonek.filesynchronizer.client.components;

import pl.jelonek.filesynchronizer.client.model.LogFile;
import pl.jelonek.filesynchronizer.client.model.UpdateFile;
import pl.jelonek.filesynchronizer.client.rsync.RSyncFileUpdaterProvider;
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

    List<LogFile> filesFromServer = new ArrayList<>();

    private long filesFromServerCleanUpInterval = 8000;

    public MyFileChangeListener(RSyncFileUpdaterProvider rSyncFileUpdaterProvider, FileUpdaterRequestSender fileUpdaterRequestSender) {
        this.rSyncFileUpdaterProvider = rSyncFileUpdaterProvider;
        this.fileUpdaterRequestSender = fileUpdaterRequestSender;
    }

    /**
     * This method is being triggered when an file is being modified on client directory.
     * We receive set of changed files as @param and based on that we send new changes to server.
     *
     * @param changeSet is the set of change files over watched client directory
     */
    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        Set<ChangedFile> filteredFiles = clearChangeSetFromFilesFromServer(changeSet);
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

    /**
     * This method filters changeFiles set with the buffer of changes from server. If we find files from fileFromServer
     * buffer in changedFiles set we ignore those changes anc clear them from mentioned buffer.
     *
     * @param changeSet is the set of changed files in watched client directory
     * @return filtered @param as ChangedFile list
     */
    public Set<ChangedFile> clearChangeSetFromFilesFromServer(Set<ChangedFiles> changeSet) {
        List<String> filesToDeleteFromBuffer = new ArrayList<>();

        if (!filesFromServer.isEmpty()) {
            Set<String> changedFilesWithoutPrefixesList = mapToStringListWithoutPrefixes(changeSet);

            addChangesFromServerToLocalBuffer(filesToDeleteFromBuffer, changedFilesWithoutPrefixesList);

            filesFromServer = cleanUpFilesFromServerList(filesFromServer, filesToDeleteFromBuffer);
        }

        return changeSet.stream()
                .map(ChangedFiles::getFiles)
                .flatMap(Collection::stream)
                .filter(file -> !filesToDeleteFromBuffer.contains(file.getFile().getPath()))
                .collect(Collectors.toSet());
    }

    /**
     * This method add changes caught by FileSystemWatcher to buffer because those changes were
     * pulled from server and should be ignored locally.
     *
     * @param filesToDeleteFromBuffer is the list of files to delete from buffer
     * @param changedFilesWithoutPrefixesList is the set of changed files without paths prefixes
     */
    private void addChangesFromServerToLocalBuffer(List<String> filesToDeleteFromBuffer, Set<String> changedFilesWithoutPrefixesList) {
        filesFromServer.stream()
                .filter(file -> changedFilesWithoutPrefixesList.contains(file.getFilePath()))
                .forEach(file -> {
                    filesToDeleteFromBuffer.add(userLocalDirectory + file.getFilePath());
                });
    }

    private List<LogFile> cleanUpFilesFromServerList(List<LogFile> filesFromServer, List<String> filesToDeleteFromBuffer) {
        return filesFromServer.stream()
                .filter(fileFromServer -> !filesToDeleteFromBuffer.contains(userLocalDirectory + fileFromServer.getFilePath()))
                .collect(Collectors.toList());
    }

    private Set<String> mapToStringListWithoutPrefixes(Set<ChangedFiles> changeSet) {
        return changeSet.stream()
                .map(ChangedFiles::getFiles)
                .flatMap(Collection::stream)
                .map(file -> file.getFile().getPath().replace(userLocalDirectory, ""))
                .collect(Collectors.toSet());
    }


    public boolean isLocked(Path path) {
        try (FileChannel ch = FileChannel.open(path, StandardOpenOption.WRITE); FileLock lock = ch.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            return true;
        }
    }

    public void addFilesFromServerToBuffer(List<LogFile> updateFileList) {
        this.filesFromServer.addAll(updateFileList);
    }

    public void setFilesFromServer(List<LogFile> filesFromServer) {
        this.filesFromServer = filesFromServer;
    }

    public List<LogFile> getFilesFromServer() {
        return this.filesFromServer;
    }

    public void cleanUpFilesFromServer(Long currentTime) {
        filesFromServer = filesFromServer.stream()
                .filter(file -> Long.parseLong(file.getTimeOfChange()) > currentTime - filesFromServerCleanUpInterval)
                .collect(Collectors.toList());
    }
}
