package com.licencjat.filesynchronizer.client.components;

import com.licencjat.filesynchronizer.client.model.UpdateFile;
import com.licencjat.filesynchronizer.client.rsync.RSyncFileUpdaterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileChangeListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MyFileChangeListener implements FileChangeListener {

    @Autowired
    RSyncFileUpdaterProvider rSyncFileUpdaterProvider;

    @Autowired
    FileUpdaterRequestSender fileUpdaterRequestSender;

    Logger logger = LoggerFactory.getLogger(MyFileChangeListener.class);


    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        Map<String,ChangedFile.Type> updatedFiles = new HashMap<>();
        for(ChangedFiles changedfiles : changeSet) {
            for(ChangedFile changedFile: changedfiles.getFiles()) {
                if((changedFile.getType().equals(ChangedFile.Type.MODIFY) || changedFile.getType().equals(ChangedFile.Type.ADD) && !isLocked(changedFile.getFile().toPath())) || changedFile.getType().equals(ChangedFile.Type.DELETE)) {
                    logger.info("Changed file: {}",changedFile.getFile().getName());
                    updatedFiles.put(changedFile.getFile().getPath(),changedFile.getType());
                }
            }
        }
        List<UpdateFile> fileToUpdate = rSyncFileUpdaterProvider.mapToFileRQList(updatedFiles);
        rSyncFileUpdaterProvider.processForServer(fileToUpdate);
    }


    private boolean isLocked(Path path) {
        try (FileChannel ch = FileChannel.open(path, StandardOpenOption.WRITE); FileLock lock = ch.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            return true;
        }
    }

}
