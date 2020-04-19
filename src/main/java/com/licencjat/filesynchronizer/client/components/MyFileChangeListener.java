package com.licencjat.filesynchronizer.client.components;

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
import java.util.Map;
import java.util.Set;

@Component
public class MyFileChangeListener implements FileChangeListener {

    @Autowired
    FileUpdaterRequestSender fileUpdaterRequestSender;

    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        Map<String,ChangedFile.Type> updatedFiles = new HashMap<>();
        for(ChangedFiles cfiles : changeSet) {
            for(ChangedFile cfile: cfiles.getFiles()) {
                if((cfile.getType().equals(ChangedFile.Type.MODIFY) || cfile.getType().equals(ChangedFile.Type.ADD) || cfile.getType().equals(ChangedFile.Type.DELETE)) && !isLocked(cfile.getFile().toPath())) {
                    System.out.println("Done writing: "+cfile.getFile().getName());
                    updatedFiles.put(cfile.getFile().getPath(),cfile.getType());
                }
            }
        }
//        fileUpdaterRequestSender.process(updatedFiles);
    }

    private boolean isLocked(Path path) {
        try (FileChannel ch = FileChannel.open(path, StandardOpenOption.WRITE); FileLock lock = ch.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            return true;
        }
    }

}
