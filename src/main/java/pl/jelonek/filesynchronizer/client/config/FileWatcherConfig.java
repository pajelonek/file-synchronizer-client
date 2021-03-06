package pl.jelonek.filesynchronizer.client.config;

import pl.jelonek.filesynchronizer.client.components.MyFileChangeListener;
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

    final MyFileChangeListener myFileChangeListener;

    public FileWatcherConfig(MyFileChangeListener myFileChangeListener) {
        this.myFileChangeListener = myFileChangeListener;
    }

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
