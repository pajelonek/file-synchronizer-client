package pl.jelonek.filesynchronizer.client;

import static org.assertj.core.api.Assertions.assertThat;

import pl.jelonek.filesynchronizer.client.components.FilePoolerStartupRunner;
import pl.jelonek.filesynchronizer.client.model.UpdateFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(properties = {"environment=TEST"})
class FilePoolerStartupRunnerTest {

    @Autowired
    FilePoolerStartupRunner filePoolerStartupRunner;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    Logger logger = LoggerFactory.getLogger(FilePoolerStartupRunner.class);

    private List<String> setOne;
    private List<String> setOneDirectoryList;
    private List<String> setTwoDirectoryList;
    private List<String> setTwo;
    private String mainTestDirectory = "/testDirectory";


    @Test
    @Order(1)
    void contextLoads() {
        assertThat(filePoolerStartupRunner).isNotNull();
        assertThat(filePoolerStartupRunner.getEnvironment()).isEqualTo("TEST");
    }

//    @Test
//    void test() {
//        createResourcesFilesSetOne();
//    }
//
//    @Test
//    void getClientFilesFromSetOneTest() {
//        //when
//        createResourcesFilesSetOne();
//
//        //given
//        List<UpdateFile> clientFileList = new ArrayList<>();
//        filePoolerStartupRunner.listFilesFromDirectory(Paths.get(userLocalDirectory + mainTestDirectory), clientFileList);
//
//        //then
//        List<String> filePathsFromSetOne = clientFileList.stream()
//                .map(UpdateFile::getFilePath)
//                .map(filePath -> filePath.replace("\\", "/"))
//                .collect(Collectors.toList());
//        List<String> lastModifiedList = clientFileList.stream()
//                .map(UpdateFile::getLastModified)
//                .collect(Collectors.toList());
//
//        assertThat(filePathsFromSetOne).isEqualTo(setOne);
//        assertThat(clientFileList.size()).isEqualTo(setOne.size());
//
//        for (String lastModified : lastModifiedList) {
//            assertThat(Long.parseLong(lastModified)).isPositive();
//        }
//    }
//
//    @Test
//    void getClientFilesFromSetTwoTest() {
//        //when
//        createResourcesFilesSetTwo();
//
//        //given
//        List<UpdateFile> clientFileList = new ArrayList<>();
//        filePoolerStartupRunner.listFilesFromDirectory(Paths.get(userLocalDirectory + mainTestDirectory), clientFileList);
//
//        //then
//        List<String> filePathsFromSetTwo = clientFileList.stream()
//                .map(UpdateFile::getFilePath)
//                .collect(Collectors.toList());
//        List<String> lastModifiedList = clientFileList.stream()
//                .map(UpdateFile::getLastModified)
//                .collect(Collectors.toList());
//
//        assertThat(filePathsFromSetTwo).isEqualTo(setTwo);
//        assertThat(clientFileList.size()).isEqualTo(setTwo.size());
//
//        for (String lastModified : lastModifiedList) {
//            assertThat(Long.parseLong(lastModified)).isPositive();
//        }
//    }

    private void createResourcesFilesSetOne() {
        createSetOne();
        createSetOneDirectoryList();
        try {
            createDirectory(userLocalDirectory + mainTestDirectory);
            for (String directory : setOneDirectoryList) {
                createDirectory(userLocalDirectory + mainTestDirectory + directory);
            }

            for (String filePath : setOne) {
                File file = new File(userLocalDirectory + filePath);
                FileUtils.touch(file);
                file.deleteOnExit();
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
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

    private void createSetOneDirectoryList() {
        setOneDirectoryList = new ArrayList<>();
        setOneDirectoryList.add("/SubDirectory");
    }

    private void createSetOne() {
        setOne = new ArrayList<>();
        setOne.add("/testDirectory/fileOne.txt");
        setOne.add("/testDirectory/fileTwo.txt");
        setOne.add("/testDirectory/SubDirectory/FileOne.txt");
    }

    private void createResourcesFilesSetTwo() {
        createSetTwo();
        createSetTwoDirectoryList();
        try {
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
}