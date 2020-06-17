package com.licencjat.filesynchronizer.client.rsync;


import com.licencjat.filesynchronizer.client.components.FileUpdaterRequestSender;
import com.licencjat.filesynchronizer.client.config.HttpClientConfig;
import com.licencjat.filesynchronizer.client.config.RestTemplateConfig;
import com.licencjat.filesynchronizer.client.model.UpdateFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ContextConfiguration(classes = {RestTemplateConfig.class, HttpClientConfig.class})
public class RSyncFileUpdaterProvider {

    @Autowired
    RSyncFileUpdaterExecutor rSyncFileUpdaterExecutor;

    @Autowired
    FileUpdaterRequestSender fileUpdaterRequestSender;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    @Value("${ssh.hostname}")
    private String hostName;

    private String remoteMainFolder;

    Logger logger = LoggerFactory.getLogger(RSyncFileUpdaterExecutor.class);

    public void processComparing(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        logger.info("Starting comparing files with server");
//        updateExistingFilesOnServer(serverFileList, clientFileList);
//        updateExistingFilesOnClient(serverFileList, clientFileList);

        updateDirectoryOnClient(serverFileList, clientFileList);
//        updateDirectoryOnServer(serverFileList, clientFileList);

//        addNewFilesFromClientToServer(serverFileList, clientFileList);
//        addNewFilesFromServerToClient(serverFileList, clientFileList);
    }
    public void processForServer(List<UpdateFile> clientFileList) {
        List<UpdateFile> filesToRsync = clientFileList.stream()
                .filter(file -> file.getAction().equals("MODIFY") || file.getAction().equals("ADD"))
                .collect(Collectors.toList());

        logger.info("Found {} files to update on server: {}", filesToRsync.size(), filesToRsync.toString());
        processOnServer(filesToRsync);

        List<UpdateFile> fileToRemoveOnServer = clientFileList.stream()
                .filter(file -> file.getAction().equals("DELETE"))
                .collect(Collectors.toList());
        logger.info("Found {} files to remove on server: {}", fileToRemoveOnServer.size(), fileToRemoveOnServer.toString());
        fileUpdaterRequestSender.removeFilesOnServer(fileToRemoveOnServer);
    }

 /*       private void updateDirectoryOnServer(List<FileRQList> serverFileList, List<FileRQList> clientFileList) {
        List<FileRQList> fileToUpdateOnServer = getExistingFilesToUpdate(clientFileList, serverFileList);

        logger.info("Found {} files to update on server: {}", fileToUpdateOnServer.size(), fileToUpdateOnServer.toString());

        List<FileRQList> filesNotFoundOnServer = getNewFilesToUploadOnServer(serverFileList, clientFileList);

        logger.info("Found {} new files to upload to server: {}", filesNotFoundOnServer.size(), filesNotFoundOnServer.toString());

        processOnServer(fileToUpdateOnServer);
    }*/

   /* private List<FileRQList> getNewFilesToUploadOnServer(List<FileRQList> serverFileList, List<FileRQList> clientFileList) {
        return clientFileList.stream()
                .filter(file -> !isFileOnServer(file, serverFileList))
                .collect(Collectors.toList());
    }*/

    private void updateDirectoryOnClient(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        List<UpdateFile> existingFilesToUpdateOnClient = getExistingFilesToUpdate(serverFileList, clientFileList);

        logger.info("Found {} files to update on client", existingFilesToUpdateOnClient.size());

        List<UpdateFile> newFilesToUploadOnClient = getNewFilesToUploadOnClient(serverFileList, clientFileList);

        logger.info("Found {} new files to upload on client", newFilesToUploadOnClient.size());

        List<UpdateFile> fileToUpdateOnClient = Stream.of(existingFilesToUpdateOnClient, newFilesToUploadOnClient).flatMap(List::stream).collect(Collectors.toList());

        processOnClient(fileToUpdateOnClient);

        List<UpdateFile> filesToDeleteOnClient = getFilesToDeleteOnClient(serverFileList, clientFileList);

        logger.info("Found {} new files to delete on client", filesToDeleteOnClient.size());
        deleteOnClient(filesToDeleteOnClient);

        updateModificationDateOnDirectories(userLocalDirectory);
    }
    //TODO Do I need this functionality?
    private void updateModificationDateOnDirectories(String userLocalDirectory){
//        try {
//            List<String> directoryPathsList = Files.walk(Paths.get(userLocalDirectory+"\\"))
//                    .filter(Files::isDirectory)
//                    .map(path ->  path.getParent().toString() + "\\" +path.getFileName().toString())
//                    .sorted().
//                    .collect(Collectors.toList());
//
//            for (String path : directoryPathsList){
//                File directory = new File(path);
//                String[] filePathList = directory.list();
//                System.out.println("kekw");
//            }
//            System.out.println("test");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void deleteOnClient(List<UpdateFile> filesToDeleteOnClient) {
        for (UpdateFile fileRQ : filesToDeleteOnClient) {
            logger.info("Removing file: " + fileRQ.getFilePath());
            File file = new File(userLocalDirectory + fileRQ.getFilePath());
            if (file.exists() && file.delete()) {
                logger.info("Successfully deleted file {} on server: " + fileRQ.getFilePath());
            } else throw new Error("Could not find file on server");
        }
    }

    private List<UpdateFile> getFilesToDeleteOnClient(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        List<String> serverFileNames = serverFileList.stream()
                .map(UpdateFile::getFilePath)
                .collect(Collectors.toList());

        return clientFileList.stream()
                .filter(file -> !serverFileNames.contains(file.getFilePath()))
                .collect(Collectors.toList());
    }

    private List<UpdateFile> getNewFilesToUploadOnClient(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        List<String> filesNameListFromClient = clientFileList.stream()
                .map(UpdateFile::getFilePath)
                .collect(Collectors.toList());

        return serverFileList.stream()
                .filter(file -> validateIfUploadFile(file, filesNameListFromClient))
                .collect(Collectors.toList());
    }

    private List<UpdateFile> getExistingFilesToUpdate(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        return serverFileList.stream()
                .filter(file -> validateIfUpdateFile(file, clientFileList))
                .collect(Collectors.toList());
    }

    private boolean validateIfUploadFile(UpdateFile file, List<String> clientFileList) {
        return !clientFileList.contains(file.getFilePath());
    }

//    public void updateExistingFilesOnClient(List<FileRQList> serverFileList, List<FileRQList> clientFileList) {
//        List<FileRQList> fileToUpdateOnClient = serverFileList.stream()
//                .filter(file -> validateIfUpdateFile(file, clientFileList))
//                .collect(Collectors.toList());
//
//        logger.info("Found {} files to update on client", fileToUpdateOnClient.size());
//        processOnClient(fileToUpdateOnClient);
//    }
//
//    public void addNewFilesFromServerToClient(List<FileRQList> serverFileList, List<FileRQList> clientFileList) {
//        List<String> fileNamesFromClient = clientFileList.stream()
//                .map(FileRQList::getFilePath)
//                .collect(Collectors.toList());
//
//        List<FileRQList> fileToUpdateOnClient = serverFileList.stream()
//                .filter(file -> !fileNamesFromClient.contains(file.getFilePath()))
//                .collect(Collectors.toList());
//        logger.info("Found {} files to upload on client", fileToUpdateOnClient.size());
//        processOnClient(fileToUpdateOnClient);
//    }

    public void processOnClient(List<UpdateFile> fileToUpdateOnClient) {
        fileToUpdateOnClient.stream()
                .collect(Collectors.groupingBy(fileToUpdate -> fileToUpdate.getFilePath().substring(0, fileToUpdate.getFilePath().lastIndexOf('\\'))))
                .forEach((prefixOfPath, fileRQ) -> modifyFileOnClient(fileRQ, prefixOfPath));
    }

    private void modifyFileOnClient(List<UpdateFile> fileRQ, String prefixOfPath) {
        List<String> sources = fileRQ.stream()
                .map(UpdateFile::getFilePath)
                .map(FilePath -> hostName+ ":"+ remoteMainFolder + FilePath)
                .collect(Collectors.toList());

        logger.info("Modifying {} file/files on client", sources.toString());
        rSyncFileUpdaterExecutor
                .setSources(sources)
                .setDestination(userLocalDirectory + prefixOfPath + "\\")
                .execute();

        updateFileModificationDateOnClient(fileRQ);
    }

    private void updateFileModificationDateOnClient(List<UpdateFile> updateFile) {
        for (UpdateFile fileRQ : updateFile) {
            logger.info("Changing modification date for file: " + fileRQ.getFilePath());
            File file = new File(userLocalDirectory + fileRQ.getFilePath());
            if (file.exists() && file.setLastModified(Long.parseLong(fileRQ.getLastModified()))) {
                logger.info("Successfully modified date for file: " + fileRQ.getFilePath());
//                validateDirectoryModificationDate(fileRQ);
            } else throw new Error("Could not find file on client");
        }
    }



//    private void updateExistingFilesOnServer(List<FileRQList> serverFileList, List<FileRQList> clientFileList) {
//        List<FileRQList> fileToUpdateOnServer = clientFileList.stream()
//                .filter(file -> validateIfUpdateFile(file, serverFileList))
//                .collect(Collectors.toList());
//
//        logger.info("Found {} files to update on server", fileToUpdateOnServer.size());
//        processOnServer(fileToUpdateOnServer);
//    }

//    private void addNewFilesFromClientToServer(List<FileRQList> serverFileList, List<FileRQList> clientFileList) {
//        List<FileRQList> filesNotFoundOnServer = clientFileList.stream()
//                .filter(file -> !isFileOnServer(file, serverFileList))
//                .collect(Collectors.toList());
//
//        logger.info("Found {} file/files to upload to server", filesNotFoundOnServer.size());
//        processOnServer(filesNotFoundOnServer);
//    }

    public void processOnServer(List<UpdateFile> fileToUpdateOnServer) {
        fileToUpdateOnServer.stream()
                .collect(Collectors.groupingBy(fileToUpdate -> fileToUpdate.getFilePath().substring(0, fileToUpdate.getFilePath().lastIndexOf('\\'))))
                .forEach((prefixOfPath, fileRQ) -> modifyFileOnServer(fileRQ, prefixOfPath));
    }


    private void modifyFileOnServer(List<UpdateFile> fileRQ, String prefixOfPath) {
        List<String> sources = fileRQ.stream()
                .map(UpdateFile::getFilePath)
                .map(FilePath -> userLocalDirectory + FilePath)
                .collect(Collectors.toList());

        logger.info("Modifying {} file/files on server", sources.toString());
        rSyncFileUpdaterExecutor
                .setSources(sources)
                .setDestination(hostName + ":" + remoteMainFolder + prefixOfPath + "\\")
                .execute();

        updateFileModificationDateOnServer(fileRQ);
    }

    public List<UpdateFile> mapToFileRQList(Map<String, ChangedFile.Type> updatedFiles) {
        List<UpdateFile> listToUpdate = new ArrayList<>();
        for (Map.Entry<String,ChangedFile.Type> entry : updatedFiles.entrySet()) {
            UpdateFile updateFile = new UpdateFile();
            updateFile.setFilePath(entry.getKey().replace(userLocalDirectory,""));
            File file = new File(entry.getKey());
            updateFile.setLastModified(String.valueOf(file.lastModified()));
            updateFile.setAction(entry.getValue().toString());
            listToUpdate.add(updateFile);
        }
        return listToUpdate;
    }


    private void updateFileModificationDateOnServer(List<UpdateFile> updateFile) {
        fileUpdaterRequestSender.updateDateModification(updateFile);
    }

    private boolean validateIfUpdateFile(UpdateFile clientFile, List<UpdateFile> serverFileList) {
        Optional<UpdateFile> fileOnServer = serverFileList.stream()
                .filter(serverFile -> serverFile.getFilePath().equals(clientFile.getFilePath()))
                .findAny();
        return fileOnServer.filter(fileRQList -> Long.parseLong(fileRQList.getLastModified()) < Long.parseLong(clientFile.getLastModified())).isPresent();
    }

//    private boolean isFileOnServer(FileRQList filePath, List<FileRQList> serverFileList) {
//        return serverFileList.stream()
//                .map(FileRQList::getFilePath)
//                .map(serverPath -> serverPath.replace(remoteMainFolder, ""))
//                .anyMatch(fileOnServer -> fileOnServer.equals(filePath.getFilePath()));
//    }

    public RSyncFileUpdaterProvider setRemoteMainFolder(String remoteMainFolder) {
        this.remoteMainFolder = remoteMainFolder;
        return this;
    }

}