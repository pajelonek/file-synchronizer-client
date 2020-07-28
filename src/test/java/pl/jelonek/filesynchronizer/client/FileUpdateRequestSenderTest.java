package pl.jelonek.filesynchronizer.client;

import pl.jelonek.filesynchronizer.client.components.FileUpdaterRequestSender;
import pl.jelonek.filesynchronizer.client.model.UpdateFile;
import pl.jelonek.filesynchronizer.client.model.UpdateFilesRQ;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "environment=TEST")
public class FileUpdateRequestSenderTest {

    @Autowired
    FileUpdaterRequestSender fileUpdateRequestSender;

    private List<String> setOne;

    @Test
    @Order(1)
    void contextLoads(){
        assertThat(fileUpdateRequestSender).isNotNull();
    }

    @Test
    void createUpdateFilesRequestTest(){
        //when
        List<UpdateFile> updateFileList = createUpdateFileList();

        //given
        HttpEntity<UpdateFilesRQ> updateFileRQEntity = fileUpdateRequestSender.createUpdateFilesRQ(updateFileList);

        //then
        assertThat(Objects.requireNonNull(updateFileRQEntity.getBody()).getMainFolder()).isNotNull();
        assertThat(updateFileRQEntity.getBody().getHost()).isNotNull();
        assertThat(updateFileRQEntity.getBody().getName()).isNotNull();
        assertThat(updateFileRQEntity.getBody().getUpdateFile().size()).isEqualTo(setOne.size());
    }


    private List<UpdateFile> createUpdateFileList() {
        createSetOne();
        List<UpdateFile> updateFileList = new ArrayList<>();

        for(String testFile : setOne){
            UpdateFile updateFile = new UpdateFile();
            updateFile.setFilePath(testFile);
            updateFile.setLastModified("1");
            updateFile.setAction("TEST");
            updateFileList.add(updateFile);
        }
        return updateFileList;
    }

    private void createSetOne() {
        setOne = new ArrayList<>();
        setOne.add("/testDirectory/fileOne.txt");
        setOne.add("/testDirectory/fileTwo.txt");
        setOne.add("/testDirectory/SubDirectory/FileOne.txt");
    }

}

