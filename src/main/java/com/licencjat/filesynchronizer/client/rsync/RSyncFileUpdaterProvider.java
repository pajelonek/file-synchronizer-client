package com.licencjat.filesynchronizer.client.rsync;


import com.licencjat.filesynchronizer.client.components.FileUpdaterRequestSender;
import com.licencjat.filesynchronizer.client.config.HttpClientConfig;
import com.licencjat.filesynchronizer.client.config.RestTemplateConfig;
import com.licencjat.filesynchronizer.client.model.UpdateFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    final RSyncFileUpdaterExecutor rSyncFileUpdaterExecutor;

    final FileUpdaterRequestSender fileUpdaterRequestSender;

    @Value("${user.local.directory}")
    private String userLocalDirectory;

    @Value("${ssh.hostname}")
    private String hostName;

    private String remoteMainFolder;

    Logger logger = LoggerFactory.getLogger(RSyncFileUpdaterExecutor.class);

    public RSyncFileUpdaterProvider(RSyncFileUpdaterExecutor rSyncFileUpdaterExecutor, FileUpdaterRequestSender fileUpdaterRequestSender) {
        this.rSyncFileUpdaterExecutor = rSyncFileUpdaterExecutor;
        this.fileUpdaterRequestSender = fileUpdaterRequestSender;
    }

    public void processComparing(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        logger.info("Starting comparing files with server");

        List<UpdateFile> filesToUpdateOnClientList = getExistingFilesToUpdate(serverFileList, clientFileList);
        logger.info("Found {} files to update on client", filesToUpdateOnClientList.size());

        List<UpdateFile> filesToUploadOnClientList = getNewFilesToUploadOnClient(serverFileList, clientFileList);
        logger.info("Found {} new files to upload on client", filesToUploadOnClientList.size());

        List<UpdateFile> filesToUploadFromServerList = Stream.of(filesToUpdateOnClientList, filesToUploadOnClientList).flatMap(List::stream).collect(Collectors.toList());

        processOnClient(filesToUploadFromServerList);

        List<UpdateFile> filesToDeleteOnClientList = getFilesToDeleteOnClient(serverFileList, clientFileList);
        logger.info("Found {} new files to delete on client", filesToDeleteOnClientList.size());

        deleteOnClient(filesToDeleteOnClientList);

        updateModificationDateOnDirectories(userLocalDirectory);
    }

    public void processForServer(List<UpdateFile> clientFileList) {
        List<UpdateFile> filesToSendToServerList = clientFileList.stream()
                .filter(file -> file.getAction().equals("MODIFY") || file.getAction().equals("ADD"))
                .collect(Collectors.toList());

        logger.info("Found {} files to update on server: {}", filesToSendToServerList.size(), filesToSendToServerList.toString());
        processOnServer(filesToSendToServerList);

        List<UpdateFile> filesToRemoveOnServerList = clientFileList.stream()
                .filter(file -> file.getAction().equals("DELETE"))
                .collect(Collectors.toList());
        logger.info("Found {} files to remove on server: {}", filesToRemoveOnServerList.size(), filesToRemoveOnServerList.toString());
        fileUpdaterRequestSender.removeFilesOnServer(filesToRemoveOnServerList);
    }

    //TODO put into diff branch after everything
    private void updateModificationDateOnDirectories(String userLocalDirectory) {
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

    public void deleteOnClient(List<UpdateFile> filesToDeleteOnClientList) {
        for (UpdateFile updateFile : filesToDeleteOnClientList) {
            logger.info("Removing file: " + updateFile.getFilePath());
            File file = new File(userLocalDirectory + updateFile.getFilePath());
            if (file.exists() && file.delete()) {
                logger.info("Successfully deleted file {} on client: " + updateFile.getFilePath());
            } else throw new Error("Could not find or delete file on client");
        }
    }

    public List<UpdateFile> getFilesToDeleteOnClient(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        List<String> serverFileNames = serverFileList.stream()
                .map(UpdateFile::getFilePath)
                .collect(Collectors.toList());

        return clientFileList.stream()
                .filter(clientFile -> !serverFileNames.contains(clientFile.getFilePath()))
                .collect(Collectors.toList());
    }

    public List<UpdateFile> getNewFilesToUploadOnClient(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        List<String> filesFromClientList = clientFileList.stream()
                .map(UpdateFile::getFilePath)
                .collect(Collectors.toList());

        return serverFileList.stream()
                .filter(serverFile -> isFileOnClient(serverFile, filesFromClientList))
                .collect(Collectors.toList());
    }

    public List<UpdateFile> getExistingFilesToUpdate(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        return serverFileList.stream()
                .filter(file -> validateIfUpdateFile(file, clientFileList))
                .collect(Collectors.toList());
    }

    private boolean isFileOnClient(UpdateFile file, List<String> clientFileList) {
        return !clientFileList.contains(file.getFilePath());
    }

    //todo opisz
    public void processOnClient(List<UpdateFile> fileToUpdateOnClient) {
        fileToUpdateOnClient.stream()
                .collect(Collectors.groupingBy(fileToUpdate -> fileToUpdate.getFilePath().substring(0, fileToUpdate.getFilePath().lastIndexOf('\\'))))
                .forEach((prefixOfPath, fileRQ) -> modifyFileOnClient(fileRQ, prefixOfPath));
    }

    //todo change hostName to sshHostName/sshServerName
    private void modifyFileOnClient(List<UpdateFile> updateFileList, String prefixOfPath) {
        List<String> sourcesList = updateFileList.stream()
                .map(UpdateFile::getFilePath)
                .map(filePath -> hostName + ":" + remoteMainFolder + filePath)
                .collect(Collectors.toList());

        logger.info("Modifying {} file/files on client", sourcesList.toString());
        rSyncFileUpdaterExecutor
                .setSources(sourcesList)
                .setDestination(userLocalDirectory + prefixOfPath + "\\")
                .execute();

        updateFileModificationDateOnClient(updateFileList);
    }

    public void updateFileModificationDateOnClient(List<UpdateFile> updateFileList) {
        for (UpdateFile updateFile : updateFileList) {
            logger.info("Changing modification date for file: " + updateFile.getFilePath());
            File file = new File(userLocalDirectory + updateFile.getFilePath());
            if (file.exists() && file.setLastModified(Long.parseLong(updateFile.getLastModified()))) {
                logger.info("Successfully modified date for file: " + updateFile.getFilePath());
            } else throw new Error("Could not find file on client");
        }
    }

    public void processOnServer(List<UpdateFile> filesToSendToServerList) {
        filesToSendToServerList.stream()
                .collect(Collectors.groupingBy(fileToUpdate -> fileToUpdate.getFilePath().substring(0, fileToUpdate.getFilePath().lastIndexOf('\\'))))
                .forEach((prefixOfPath, file) -> modifyFileOnServer(file, prefixOfPath));
    }

    private void modifyFileOnServer(List<UpdateFile> updateFileList, String prefixOfPath) {
        List<String> sources = updateFileList.stream()
                .map(UpdateFile::getFilePath)
                .map(FilePath -> userLocalDirectory + FilePath)
                .collect(Collectors.toList());

        logger.info("Modifying {} file/files on server", sources.toString());
        rSyncFileUpdaterExecutor
                .setSources(sources)
                .setDestination(hostName + ":" + remoteMainFolder + prefixOfPath + "\\")
                .execute();

        updateFileModificationDateOnServer(updateFileList);
    }

    public List<UpdateFile> mapToUpdateFileList(Map<String, ChangedFile.Type> updatedFiles) {
        List<UpdateFile> listToUpdate = new ArrayList<>();
        for (Map.Entry<String, ChangedFile.Type> entry : updatedFiles.entrySet()) {
            UpdateFile updateFile = new UpdateFile();
            updateFile.setFilePath(entry.getKey().replace(userLocalDirectory, ""));
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

    //todo think about != instead of <
    private boolean validateIfUpdateFile(UpdateFile serverFile, List<UpdateFile> clientFileList) {
        Optional<UpdateFile> fileOnServer = clientFileList.stream()
                .filter(clientFile -> clientFile.getFilePath().equals(serverFile.getFilePath()))
                .findAny();
        return fileOnServer.filter(fileRQList -> Long.parseLong(fileRQList.getLastModified()) < Long.parseLong(serverFile.getLastModified())).isPresent();
    }

    public RSyncFileUpdaterProvider setRemoteMainFolder(String remoteMainFolder) {
        this.remoteMainFolder = remoteMainFolder;
        return this;
    }

}