package com.licencjat.filesynchronizer.client.components;

import com.licencjat.filesynchronizer.client.model.FileLogger;
import com.licencjat.filesynchronizer.client.model.LogFile;
import com.licencjat.filesynchronizer.client.model.UpdateFile;
import com.licencjat.filesynchronizer.client.rsync.RSyncFileUpdaterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class FilePoolerServerListener implements Runnable{

    final FileUpdaterRequestSender fileUpdaterRequestSender;

    final RSyncFileUpdaterProvider rSyncFileUpdaterProvider;

    final MyFileChangeListener myFileChangeListener;

    final FileSystemWatcher fileSystemWatcher;

    @Value("${client.name}")
    private String hostName;

    private AtomicBoolean enabled = new AtomicBoolean(false);

    private String lastSynchronizedTime;

    Logger logger = LoggerFactory.getLogger(FilePoolerServerListener.class);

    public FilePoolerServerListener(FileUpdaterRequestSender fileUpdaterRequestSender, RSyncFileUpdaterProvider rSyncFileUpdaterProvider, MyFileChangeListener myFileChangeListener, FileSystemWatcher fileSystemWatcher) {
        this.fileUpdaterRequestSender = fileUpdaterRequestSender;
        this.rSyncFileUpdaterProvider = rSyncFileUpdaterProvider;
        this.myFileChangeListener = myFileChangeListener;
        this.fileSystemWatcher = fileSystemWatcher;
    }

    @Override
    @Scheduled(fixedDelay = 3000)
    public void run() {
        if(enabled.get()) {
            ResponseEntity<FileLogger> fileLoggerResponseEntity = fileUpdaterRequestSender.getServerLogFile();
            List<UpdateFile> filesToProcessOnClient =  processForNewFiles(Objects.requireNonNull(fileLoggerResponseEntity.getBody()).getLogFileList());

            if(!filesToProcessOnClient.isEmpty()) {
                myFileChangeListener.addFilesFromServerToBuffer(filesToProcessOnClient);
                List<UpdateFile> filesToUpdateOnClient = getFilesToUpdateOnClient(filesToProcessOnClient);
                logger.info("Found {} added/modified files to update on client", filesToProcessOnClient.size());
                rSyncFileUpdaterProvider.processOnClient(filesToUpdateOnClient);

                List<UpdateFile> filesToDeleteOnClient = getFilesToDeleteOnClient(filesToProcessOnClient);
                logger.info("Found {} files to delete on client", filesToDeleteOnClient.size());
                rSyncFileUpdaterProvider.deleteOnClient(filesToDeleteOnClient);
            }

            setLastSynchronizedTime(String.valueOf(fileLoggerResponseEntity.getBody().getCurrentTime()));
        }
    }

    public List<UpdateFile> getFilesToDeleteOnClient(List<UpdateFile> filesToProcessOnClient) {
        return filesToProcessOnClient.stream()
                .filter(updateFile -> updateFile.getAction().equals("DELETE"))
                .collect(Collectors.toList());
    }

    public List<UpdateFile> getFilesToUpdateOnClient(List<UpdateFile> filesToProcessOnClient) {
        return filesToProcessOnClient.stream()
                .filter(updateFile -> updateFile.getAction().equals("ADD") || updateFile.getAction().equals("MODIFY"))
                .collect(Collectors.toList());
    }

    public List<UpdateFile> processForNewFiles(List<LogFile> logFileList) {
        Set<String> set = new TreeSet<>();
        List<UpdateFile> logFileListToParse = new ArrayList<>();
        Collections.reverse(logFileList);
        logFileList.stream()
                .filter(fileLog -> !fileLog.getHost().equals(hostName))
                .filter(fileLog -> Long.parseLong(fileLog.getTimeOfChange()) > Long.parseLong(lastSynchronizedTime))
                .forEach(logFile -> {
                    if(!set.contains(logFile.getFilePath())){
                        logFileListToParse.add(mapToUpdateFileObject(logFile));
                        set.add(logFile.getFilePath());
                    }
                });
        return logFileListToParse;
    }

    private UpdateFile mapToUpdateFileObject(LogFile logFile) {
        UpdateFile updateFile = new UpdateFile();
        updateFile.setFilePath(logFile.getFilePath());
        updateFile.setLastModified(logFile.getLastModified());
        updateFile.setAction(logFile.getAction());
        return updateFile;
    }

    public void initiateSynchronizeTime(){
        this.lastSynchronizedTime = String.valueOf(Instant.now().getEpochSecond());
    }

    public void setLastSynchronizedTime(String lastSynchronizedTime){
        this.lastSynchronizedTime = lastSynchronizedTime;
    }

    public String getLastSynchronizedTime(){
        return lastSynchronizedTime;
    }

    public void triggerPoolerService(){
        enabled.set(true);
    }

    public void stopPoolerService(){
        enabled.set(false);
    }
}
