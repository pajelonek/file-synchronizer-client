package pl.jelonek.filesynchronizer.client;

import pl.jelonek.filesynchronizer.client.rsync.RSyncFileUpdaterExecutor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"environment=TEST", "rsync.remote.shell="})
public class RsyncFileUpdaterExecutorTest {

    @Autowired
    RSyncFileUpdaterExecutor rSyncFileUpdaterExecutor;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    Logger logger = LoggerFactory.getLogger(RsyncFileUpdaterExecutorTest.class);

    private List<String> setOne;
    private List<String> setOneDirectoryList;
    private String testDirectory = "/testDirectory";
    private String rsyncDirectory = testDirectory + "/rsyncDirectory";

    @Test
    @Order(1)
    void contextLoads(){
        assertThat(rSyncFileUpdaterExecutor).isNotNull();
    }

    @Test
    void testExecution(){
        //when
        createResourcesFilesSetOne();
        List<String> sourcesList = createSourcesList(setOne);
        String destinationDirectory = userLocalDirectory.replace("\\", "/") + rsyncDirectory + "/";

        //given
        rSyncFileUpdaterExecutor
                .setSources(sourcesList)
                .setDestination(destinationDirectory)
                .execute();

        //then
        validateAreFilesCopied(sourcesList);
        deleteTestFiles(setOne);
    }

    private void deleteTestFiles(List<String> setOne) {
        for(String path : setOne){
            String fixedPosix = path.replace(testDirectory ,rsyncDirectory);
            String correctNewPath = userLocalDirectory + fixedPosix;

            File file = new File(correctNewPath);
            file.deleteOnExit();
        }
    }

    private void validateAreFilesCopied(List<String> sourcesList) {
        for(String source : sourcesList){
            File file = new File(source);
            assertThat(file.exists());
            file.deleteOnExit();
        }

    }

    private List<String> createSourcesList(List<String> setOne) {
        return setOne.stream()
                .map(file -> userLocalDirectory + file)
                .collect(Collectors.toList());
    }


    private void createResourcesFilesSetOne() {
        createSetOne();
        createSetOneDirectoryList();
        createDirectory(userLocalDirectory + testDirectory);
        for (String directory : setOneDirectoryList) {
            createDirectory(userLocalDirectory + directory);
        }
        try {
            for (String filePath : setOne) {
                File file = new File(userLocalDirectory + filePath);
                if(!file.exists()){
                    if(file.createNewFile()){
                        logger.info("Test file created");
                    } else throw new Error("Could not create test file, check permissions");
                }
                file.deleteOnExit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createSetOneDirectoryList() {
        setOneDirectoryList = new ArrayList<>();
        setOneDirectoryList.add(rsyncDirectory);
    }

    private void createSetOne() {
        setOne = new ArrayList<>();
        setOne.add("/testDirectory/fileOne.txt");
        setOne.add("/testDirectory/fileTwo.txt");
        setOne.add("/testDirectory/fileThree.txt");
        setOne.add("/testDirectory/fileFour.txt");
        setOne.add("/testDirectory/fileFive.txt");
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
