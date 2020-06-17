package com.licencjat.filesynchronizer.client.config;

import com.licencjat.filesynchronizer.client.components.MyFileChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.File;
import java.time.Duration;

@Configuration
public class FileWatcherConfig {

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    @Autowired
    MyFileChangeListener myFileChangeListener;

    @Bean
    public FileSystemWatcher fileSystemWatcher() {
        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher(true, Duration.ofMillis(500L), Duration.ofMillis(300L));
        fileSystemWatcher.addSourceFolder(new File(userLocalDirectory));
        fileSystemWatcher.addListener(myFileChangeListener);
        return fileSystemWatcher;
    }

    @PreDestroy
    public void onDestroy(){
        fileSystemWatcher().stop();
    }
}
