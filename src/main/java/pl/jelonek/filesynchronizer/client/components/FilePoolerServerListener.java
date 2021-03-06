package pl.jelonek.filesynchronizer.client.components;

import pl.jelonek.filesynchronizer.client.model.FileLogger;
import pl.jelonek.filesynchronizer.client.model.LogFile;
import pl.jelonek.filesynchronizer.client.model.UpdateFile;
import pl.jelonek.filesynchronizer.client.rsync.RSyncFileUpdaterProvider;
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
public class FilePoolerServerListener implements Runnable {

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

    /**
     * This method is being run every 3 seconds by default. If enabled(if enabled set to true by FilePoolerStartupRunner)
     * this method gets log list list from server and based on that pull new changes from server directory, then set
     * new lastSynchronized time.
     */
    @Override
    @Scheduled(fixedDelay = 5000)
    public void run() {
        if (enabled.get()) {
            ResponseEntity<FileLogger> fileLoggerResponseEntity = fileUpdaterRequestSender.getServerLogFile();
            List<UpdateFile> filesToProcessOnClient = processForNewFiles(Objects.requireNonNull(fileLoggerResponseEntity.getBody()).getLogFileList());

            if (!filesToProcessOnClient.isEmpty()) {
                myFileChangeListener.addFilesFromServerToBuffer(addAsLogFileList(filesToProcessOnClient));
                List<UpdateFile> filesToUpdateOnClient = getFilesToUpdateOnClient(filesToProcessOnClient);
                logger.info("Found {} added/modified files to update on client", filesToProcessOnClient.size());
                rSyncFileUpdaterProvider.processOnClient(filesToUpdateOnClient);

                List<UpdateFile> filesToDeleteOnClient = getFilesToDeleteOnClient(filesToProcessOnClient);
                logger.info("Found {} files to delete on client", filesToDeleteOnClient.size());
                rSyncFileUpdaterProvider.deleteOnClient(filesToDeleteOnClient);
            }
            myFileChangeListener.cleanUpFilesFromServer(Long.parseLong(fileLoggerResponseEntity.getBody().getCurrentTime()));
            setNewLastSynchronizedTime(fileLoggerResponseEntity);
        }
    }

    private List<LogFile> addAsLogFileList(List<UpdateFile> filesToProcessOnClient) {
        List<LogFile> logFileList = new ArrayList<>();
        for(UpdateFile updateFile : filesToProcessOnClient){
            LogFile logFile = new LogFile();
            logFile.setLastModified(updateFile.getLastModified());
            logFile.setFilePath(updateFile.getFilePath());
            logFile.setAction(updateFile.getAction());
            logFile.setTimeOfChange(String.valueOf(Instant.now().getEpochSecond()));
            logFileList.add(logFile);
        }
        return logFileList;
    }

    private void setNewLastSynchronizedTime(ResponseEntity<FileLogger> fileLoggerResponseEntity) {
        String lastSynchronizedTimeFromLogs = Objects.requireNonNull(fileLoggerResponseEntity.getBody()).getLastSynchronizedTime();
        if (Long.parseLong(this.lastSynchronizedTime) < Long.parseLong(lastSynchronizedTimeFromLogs)){
            setLastSynchronizedTime(lastSynchronizedTimeFromLogs);
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

    /**
     * This method takes logFile list and based on that filters so that we at the end we have
     * newest change for each file modified after last synchronization of this component.
     *
     * @param logFileList is the list of all logs from server
     * @return updateFileList of files to update on client directory
     */
    public List<UpdateFile> processForNewFiles(List<LogFile> logFileList) {
        Set<String> set = new TreeSet<>();
        List<UpdateFile> logFileListToParse = new ArrayList<>();
        Collections.reverse(logFileList);
        logFileList.stream()
                .filter(fileLog -> !fileLog.getHost().equals(hostName))
                .filter(fileLog -> Long.parseLong(fileLog.getTimeOfChange()) > Long.parseLong(lastSynchronizedTime))
                .forEach(logFile -> {
                    if (!set.contains(logFile.getFilePath())) {
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

    public void initiateSynchronizeTime() {
        this.lastSynchronizedTime = String.valueOf(Instant.now().getEpochSecond());
    }

    public void setLastSynchronizedTime(String lastSynchronizedTime) {
        this.lastSynchronizedTime = lastSynchronizedTime;
    }

    public String getLastSynchronizedTime() {
        return lastSynchronizedTime;
    }

    public void triggerPoolerService() {
        enabled.set(true);
    }

    public void stopPoolerService() {
        enabled.set(false);
    }
}
