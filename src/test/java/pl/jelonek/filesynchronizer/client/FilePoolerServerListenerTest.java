package pl.jelonek.filesynchronizer.client;

import pl.jelonek.filesynchronizer.client.components.FilePoolerServerListener;
import pl.jelonek.filesynchronizer.client.model.LogFile;
import pl.jelonek.filesynchronizer.client.model.UpdateFile;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "environment=TEST")
public class FilePoolerServerListenerTest {

    @Autowired
    FilePoolerServerListener filePoolerServerListener;

    private List<UpdateFile> sampleUpdateFileList;
    private List<LogFile> sampleLogFileList;
    private Map<String, String> sampleLogFileAnswerMap;

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(filePoolerServerListener).isNotNull();
    }

    @Test
    void filesToDeleteOnClientTest() {
        //when
        createSetOne();

        //given
        List<UpdateFile> filesToDeleteOnClient = filePoolerServerListener.getFilesToDeleteOnClient(sampleUpdateFileList);
        long deletedFilesSize = sampleUpdateFileList.stream()
                .filter(file -> file.getAction().equals("DELETE"))
                .count();
        //then
        assertThat(filesToDeleteOnClient.size()).isEqualTo(deletedFilesSize);

        boolean isActionCorrect = filesToDeleteOnClient.stream()
                .map(UpdateFile::getAction)
                .allMatch(action -> action.equals("DELETE"));
        assertThat(isActionCorrect).isEqualTo(true);
    }

    @Test
    void getFilesToUpdateOnClient() {
        //when
        createSetOne();

        //given
        List<UpdateFile> filesToUpdateOnClientList = filePoolerServerListener.getFilesToUpdateOnClient(sampleUpdateFileList);
        long updatedFilesSize = sampleUpdateFileList.stream()
                .filter(file -> file.getAction().equals("ADD") || file.getAction().equals("MODIFY"))
                .count();
        //then
        assertThat(filesToUpdateOnClientList.size()).isEqualTo(updatedFilesSize);

        boolean isActionCorrect = filesToUpdateOnClientList.stream()
                .map(UpdateFile::getAction)
                .allMatch(action -> action.equals("ADD") || action.equals("MODIFY"));
        assertThat(isActionCorrect).isEqualTo(true);
    }

    @Test
    void processForNewFilesTest() {
        //when
        createSetTwo();
        filePoolerServerListener.setLastSynchronizedTime("10");

        //given
        List<UpdateFile> processedNewFiles = filePoolerServerListener.processForNewFiles(sampleLogFileList);

        //then
        assertThat(processedNewFiles.size()).isEqualTo(sampleLogFileAnswerMap.size());
        for (UpdateFile updateFile : processedNewFiles) {
            assertThat(sampleLogFileAnswerMap.get(updateFile.getFilePath())).isEqualTo(updateFile.getLastModified());
        }
    }

    private void createSetOne() {
        sampleUpdateFileList = new ArrayList<>();
        sampleUpdateFileList.add(createUpdateFile("/testDirectory/fileOne.txt", "30", "DELETE"));
        sampleUpdateFileList.add(createUpdateFile("/testDirectory/fileOne.txt", "50", "ADD"));
        sampleUpdateFileList.add(createUpdateFile("/testDirectory/subDirectory/fileOne.txt", "30", "MODIFY"));
        sampleUpdateFileList.add(createUpdateFile("/testDirectory/SubDirectory/FileTwo.txt", "40", "DELETE"));
        sampleUpdateFileList.add(createUpdateFile("/testDirectory/subDirectory/subSubDirectory/fileOne.txt", "30", "ADD"));
        sampleUpdateFileList.add(createUpdateFile("/testDirectory/SubDirectory/subSubDirectory/FileTwo.txt", "40", "MODIFY"));
    }

    private void createSetTwo() {
        sampleLogFileList = new ArrayList<>();
        sampleLogFileList.add(createLogFile("/testDirectory/fileOne.txt", "30", "30", "DELETE"));
        sampleLogFileList.add(createLogFile("/testDirectory/fileTwo.txt", "50", "50", "ADD"));
        sampleLogFileList.add(createLogFile("/testDirectory/fileOne.txt", "35", "35", "MODIFY"));
        sampleLogFileList.add(createLogFile("/testDirectory/SubDirectory/FileTwo.txt", "40", "40", "DELETE"));
        sampleLogFileList.add(createLogFile("/testDirectory/SubDirectory/subSubDirectory/FileOne.txt", "30", "30", "ADD"));
        sampleLogFileList.add(createLogFile("/testDirectory/fileOne.txt", "40", "40", "DELETE"));
        sampleLogFileList.add(createLogFile("/testDirectory/SubDirectory/subSubDirectory/FileOne.txt", "40", "40", "MODIFY"));

        sampleLogFileAnswerMap = new HashMap<>();
        sampleLogFileAnswerMap.put("/testDirectory/SubDirectory/subSubDirectory/FileOne.txt", "40");
        sampleLogFileAnswerMap.put("/testDirectory/fileOne.txt", "40");
        sampleLogFileAnswerMap.put("/testDirectory/SubDirectory/FileTwo.txt", "40");
        sampleLogFileAnswerMap.put("/testDirectory/fileTwo.txt", "50");
    }

    private LogFile createLogFile(String filePath, String timeOfChange, String lastModified, String action) {
        LogFile logFile = new LogFile();
        logFile.setFilePath(filePath);
        logFile.setTimeOfChange(timeOfChange);
        logFile.setLastModified(lastModified);
        logFile.setAction(action);
        String hostName = "TEST";
        logFile.setHost(hostName);
        return logFile;
    }

    private UpdateFile createUpdateFile(String filePath, String lastModified, String action) {
        UpdateFile updateFile = new UpdateFile();
        updateFile.setFilePath(filePath);
        updateFile.setAction(action);
        updateFile.setLastModified(lastModified);
        return updateFile;
    }

}
