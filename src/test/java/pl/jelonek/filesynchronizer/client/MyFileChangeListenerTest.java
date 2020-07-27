package pl.jelonek.filesynchronizer.client;

import pl.jelonek.filesynchronizer.client.components.MyFileChangeListener;
import pl.jelonek.filesynchronizer.client.model.LogFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "environment=TEST")
public class MyFileChangeListenerTest {

    @Autowired
    MyFileChangeListener myFileChangeListener;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    Logger logger = LoggerFactory.getLogger(MyFileChangeListenerTest.class);

    private Set<ChangedFiles> changedFilesTest;
    private Map<String, ChangedFile.Type> setOne;
    int numberOfChangedFiles = 2;
    private List<String> setTwo;
    private List<String> setTwoDirectoryList;

    @Test
    void contextLoads() {
        assertThat(myFileChangeListener).isNotNull();
    }

//    @Test
//    void clearChangeSetFromFilesFromServerTest() {
//        //when
//        mockChangeSetOne();
//
//
//        //given
//        Set<ChangedFile> changedFileList = myFileChangeListener.clearChangeSetFromFilesFromServer(changedFilesTest);
//
//        //then
//        assertThat(myFileChangeListener.getFilesFromServer().isEmpty()).isTrue();
//        assertThat(changedFileList.size()).isEqualTo(numberOfChangedFiles);
//
//        for (ChangedFile changedFile : changedFileList) {
//            assertThat(changedFile.getType()).isNotEqualTo(ChangedFile.Type.MODIFY);
//        }
//
//
//    }

    private void mockChangeSetOne() {
        createSetOne();
        mockFilesFromServer();
        changedFilesTest = new HashSet<>();
        Set<ChangedFile> changedFilesSet = new HashSet<>();

        for (Map.Entry<String, ChangedFile.Type> entry : setOne.entrySet()) {
            changedFilesSet.add(new ChangedFile(new File(userLocalDirectory), new File(userLocalDirectory + entry.getKey()), entry.getValue()));
        }
        changedFilesTest.add(new ChangedFiles(new File(userLocalDirectory), changedFilesSet));
    }

    private void mockFilesFromServer() {
        List<LogFile> listFromSever = new ArrayList<>();
        for (Map.Entry<String, ChangedFile.Type> entry : setOne.entrySet()) {
            if (entry.getValue().equals(ChangedFile.Type.MODIFY)) {
                LogFile logFile = new LogFile();
                logFile.setFilePath(entry.getKey());
                logFile.setLastModified("123123");
                logFile.setAction(entry.getValue().toString());
                listFromSever.add(logFile);
            }
        }
        myFileChangeListener.addFilesFromServerToBuffer(listFromSever);
    }

    /**
     * For testing purposes we assume that files marked as MODIFY are from server
     */
    private void createSetOne() {
        setOne = new HashMap<>();
        setOne.put("\\testDirectory\\fileOne.txt", ChangedFile.Type.ADD);
        setOne.put("\\testDirectory\\SubDirectory\\subSubDirectory\\FileOne.txt", ChangedFile.Type.MODIFY);
        setOne.put("\\testDirectory\\SubDirectory\\FileTwo.txt", ChangedFile.Type.DELETE);
        setOne.put("\\testDirectory\\fileTwo.txt", ChangedFile.Type.MODIFY);
    }

//    @Test
//    void isLockedOpenFilesTest() {
//        //when
//        createResourcesFilesSetTwo();
//
//
//        //given
//        List<Boolean> areFilesLocked = new ArrayList<>();
//        for (String filePath : setTwo) {
//            File file = new File(userLocalDirectory + filePath);
//            areFilesLocked.add(file.exists() && !myFileChangeListener.isLocked(file.toPath()));
//        }
//
//        //then
//        boolean areFilesNotLocked = areFilesLocked.stream()
//                .allMatch(result -> result.equals(true));
//        assertThat(areFilesNotLocked).isTrue();
//
//    }

    private void createResourcesFilesSetTwo() {
        createSetTwo();
        createSetTwoDirectoryList();
        try {
            String mainTestDirectory = "/testDirectory";
            createDirectory(userLocalDirectory + mainTestDirectory);
            for (String directory : setTwoDirectoryList) {
                createDirectory(userLocalDirectory + mainTestDirectory + directory);
            }

            for (String filePath : setTwo) {
                File file = new File(userLocalDirectory + filePath);
                FileUtils.touch(file);
                file.deleteOnExit();
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void createSetTwoDirectoryList() {
        setTwoDirectoryList = new ArrayList<>();
        setTwoDirectoryList.add("/SubDirectory");
        setTwoDirectoryList.add("/SubDirectory/SubSubDirectory");
    }

    private void createSetTwo() {
        setTwo = new ArrayList<>();
        setTwo.add("\\testDirectory\\fileOne.txt");
        setTwo.add("\\testDirectory\\fileTwo.txt");
        setTwo.add("\\testDirectory\\SubDirectory\\FileOne.txt");
        setTwo.add("\\testDirectory\\SubDirectory\\FileTwo.txt");
        setTwo.add("\\testDirectory\\SubDirectory\\SubSubDirectory\\FileOne.txt");
    }

    void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (directory.mkdir())
                logger.info("Successfully created test directory '{}'", path);
            else throw new Error("Could not create directory");
            directory.deleteOnExit();
        } else logger.info("Test directory '{}' already exists", path);

    }
}
