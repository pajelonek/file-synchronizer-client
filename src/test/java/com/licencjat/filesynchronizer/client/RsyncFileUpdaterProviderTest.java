package com.licencjat.filesynchronizer.client;

import com.licencjat.filesynchronizer.client.model.UpdateFile;
import com.licencjat.filesynchronizer.client.rsync.RSyncFileUpdaterProvider;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "environment=TEST")
public class RsyncFileUpdaterProviderTest {

    @Autowired
    RSyncFileUpdaterProvider rsyncFileUpdaterProvider;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    Logger logger = LoggerFactory.getLogger(RsyncFileUpdaterProviderTest.class);

    private List<String> setOne;
    private List<String> setOneDirectoryList;
    private List<String> setTwo;
    private List<String> setTwoDirectoryList;
    private String mainTestDirectory = "/testDirectory";


    @Test
    @Order(1)
    void contextLoads() {
        assertThat(rsyncFileUpdaterProvider).isNotNull();
    }

    @Test
    void deleteOnClientTest() {
        //when
        createResourcesFilesSetOne();
        List<UpdateFile> updateFileList = createUpdateFileList(setOne);

        //given
        rsyncFileUpdaterProvider.deleteOnClient(updateFileList);


        //then
        validateAreFilesDeleted(setOne);
    }

    @Test
    void getFilesToDeleteOnClientTest() {
        //when
        createResourcesFilesSetOne();
        createResourcesFilesSetTwo();
        List<UpdateFile> serverFileList = createUpdateFileList(setOne);
        List<UpdateFile> clientFileList = createUpdateFileList(setTwo);
        List<String> serverFileNames = createFileNamesFromUpdateFiles(serverFileList);
        List<String> clientFileNames = createFileNamesFromUpdateFiles(clientFileList);


        //given
        List<UpdateFile> filesToDeleteOnClientList = rsyncFileUpdaterProvider.getFilesToDeleteOnClient(serverFileList, clientFileList);

        //then
        for (UpdateFile fileToDelete : filesToDeleteOnClientList) {
            assertThat(serverFileNames.contains(fileToDelete.getFilePath())).isFalse();
            assertThat(clientFileNames.contains(fileToDelete.getFilePath())).isTrue();
        }

    }

    @Test
    void getNewFilesToUploadOnClientTest() {
        //when
        createResourcesFilesSetOne();
        createResourcesFilesSetTwo();
        List<UpdateFile> serverFileList = createUpdateFileList(setOne);
        List<UpdateFile> clientFileList = createUpdateFileList(setTwo);
        List<String> serverFileNames = createFileNamesFromUpdateFiles(serverFileList);
        List<String> clientFileNames = createFileNamesFromUpdateFiles(clientFileList);

        //given
        List<UpdateFile> newFilesToUploadOnClient = rsyncFileUpdaterProvider.getNewFilesToUploadOnClient(serverFileList, clientFileList);

        //then
        for (UpdateFile newFileToUpload : newFilesToUploadOnClient) {
            assertThat(serverFileNames.contains(newFileToUpload.getFilePath())).isTrue();
            assertThat(clientFileNames.contains(newFileToUpload.getFilePath())).isFalse();
        }

    }

    @Test
    void getExistingFilesToUpdateTest() {
        //when
        createResourcesFilesSetOne();
        createResourcesFilesSetTwo();
        List<UpdateFile> serverFileList = createUpdateFileList(setOne);
        List<UpdateFile> clientFileList = createUpdateFileList(setTwo);

        //given
        List<UpdateFile> existingFilesToUpdate = rsyncFileUpdaterProvider.getExistingFilesToUpdate(serverFileList, clientFileList);

        //then
        validateIfNewerFilesAreUploaded(serverFileList, clientFileList, existingFilesToUpdate);
    }

    @Test
    void updateFileModificationDateOnClientTest() {
        //when
        createResourcesFilesSetOne();
        List<UpdateFile> updateFileList = createUpdateFileList(setOne);
        Map<String, String> newModificationDatesMap = createNewModificationDateMap(updateFileList);

        //given
        rsyncFileUpdaterProvider.updateFileModificationDateOnClient(updateFileList);

        //then
        validateCorrectNewModificationDates(updateFileList, newModificationDatesMap);

    }

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

    private void createSetOne() {
        setOne = new ArrayList<>();
        setOne.add("\\testDirectory\\fileOne.txt");
        setOne.add("\\testDirectory\\fileTwo.txt");
        setOne.add("\\testDirectory\\SubDirectory\\FileOne.txt");
        setOne.add("\\testDirectory\\SubDirectory\\FileTwo.txt");
        setOne.add("\\testDirectory\\SubDirectory\\SubSubDirectory\\FileOne.txt");
    }

    private void createSetOneDirectoryList() {
        setOneDirectoryList = new ArrayList<>();
        setOneDirectoryList.add("/SubDirectory");
        setOneDirectoryList.add("/SubDirectory/SubSubDirectory");
    }

    private void createSetTwo() {
        setTwo = new ArrayList<>();
        setTwo.add("\\testDirectory\\fileTwo.txt");
        setTwo.add("\\testDirectory\\SubDirectory\\FileOne.txt");
        setTwo.add("\\testDirectory\\SubDirectory\\FileTwo.txt");
        setTwo.add("\\testDirectory\\SubDirectory\\FileThree.txt");
        setTwo.add("\\testDirectory\\SubDirectory\\FileFour.txt");
        setTwo.add("\\testDirectory\\SubDirectory\\SubSubDirectory\\FileOne.txt");
        setTwo.add("\\testDirectory\\SubDirectory\\SubSubDirectory\\FileThree.txt");
    }

    private void createSetTwoDirectoryList() {
        setTwoDirectoryList = new ArrayList<>();
        setTwoDirectoryList.add("/SubDirectory");
        setTwoDirectoryList.add("/SubDirectory/SubSubDirectory");
    }

    private void validateAreFilesDeleted(List<String> setOne) {
        for (String filePath : setOne) {
            File file = new File(userLocalDirectory + filePath);
            assertThat(file.exists()).isFalse();
        }
    }

    private List<UpdateFile> createUpdateFileList(List<String> setOne) {
        List<UpdateFile> updateFileList = new ArrayList<>();
        String testAction = "TEST";
        File file;
        for (String filePath : setOne) {
            UpdateFile updateFile = new UpdateFile();
            file = new File(userLocalDirectory + updateFile.getFilePath());
            updateFile.setFilePath(filePath);
            updateFile.setAction(testAction);
            updateFile.setLastModified(String.valueOf(file.lastModified()));
            updateFileList.add(updateFile);
        }
        return updateFileList;
    }

    private List<String> createFileNamesFromUpdateFiles(List<UpdateFile> clientFileList) {
        return clientFileList.stream()
                .map(UpdateFile::getFilePath)
                .collect(Collectors.toList());
    }

    private void validateIfNewerFilesAreUploaded(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList, List<UpdateFile> existingFilesToUpdate) {
        for (UpdateFile newFileToUpload : existingFilesToUpdate) {
            Optional<String> modifiedDateFromServer = serverFileList.stream()
                    .filter(file -> newFileToUpload.getFilePath().equals(file.getFilePath()))
                    .map(UpdateFile::getLastModified)
                    .findFirst();
            assertThat(modifiedDateFromServer.isPresent()).isTrue();

            Optional<String> modifiedDateFromClient = clientFileList.stream()
                    .filter(file -> newFileToUpload.getFilePath().equals(file.getFilePath()))
                    .map(UpdateFile::getLastModified)
                    .findFirst();
            assertThat(modifiedDateFromClient.isPresent()).isTrue();
            assertThat(Long.parseLong(modifiedDateFromServer.get())).isGreaterThan(Long.parseLong(modifiedDateFromClient.get()));
        }
    }

    private void validateCorrectNewModificationDates(List<UpdateFile> updateFileList, Map<String, String> newModificationDatesMap) {
        for(UpdateFile updateFile : updateFileList){
            assertThat(newModificationDatesMap.get(updateFile.getFilePath())).isEqualTo(updateFile.getLastModified());
        }
    }

    private Map<String, String> createNewModificationDateMap(List<UpdateFile> updateFileList) {
        Map<String, String> newModificationDateMap = new HashMap<>();
        int randomPositiveNumber;
        int leftLimit = 1;
        int rightLimit = 10;
        for (UpdateFile updateFile : updateFileList) {
            randomPositiveNumber = new Random().nextInt(rightLimit - leftLimit + 1) + leftLimit;
            String newModificationDate = String.valueOf(Long.parseLong(updateFile.getLastModified()) + (long)randomPositiveNumber);
            updateFile.setLastModified(newModificationDate);
            newModificationDateMap.put(updateFile.getFilePath(), newModificationDate);
        }
        return newModificationDateMap;
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
