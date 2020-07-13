package pl.jelonek.filesynchronizer.client.rsync;


import pl.jelonek.filesynchronizer.client.components.FileUpdaterRequestSender;
import pl.jelonek.filesynchronizer.client.config.HttpClientConfig;
import pl.jelonek.filesynchronizer.client.config.RestTemplateConfig;
import pl.jelonek.filesynchronizer.client.model.UpdateFile;
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
    private String sshServerHostName;

    private String remoteMainFolder;

    Logger logger = LoggerFactory.getLogger(RSyncFileUpdaterExecutor.class);

    public RSyncFileUpdaterProvider(RSyncFileUpdaterExecutor rSyncFileUpdaterExecutor, FileUpdaterRequestSender fileUpdaterRequestSender) {
        this.rSyncFileUpdaterExecutor = rSyncFileUpdaterExecutor;
        this.fileUpdaterRequestSender = fileUpdaterRequestSender;
    }

    /**
     * This method its the logic which leads the process of compering files.
     * It sorts files to the ones to be downloaded from server, deleted on client and ones which
     * not required any action.
     * At the start of the application we !DO NOT SEND ANY CHANGES TO SERVER!, we only update our directory
     * to match the one on our server.
     *
     * @param serverFileList is the list of all the files from server
     * @param clientFileList is the list of all the files from client
     */
    public void processComparing(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        logger.info("Starting comparing files with server");

        List<UpdateFile> filesToUpdateOnClientList = getExistingFilesToUpdate(serverFileList, clientFileList);
        logger.info("Found {} files to update on client", filesToUpdateOnClientList.size());

        List<UpdateFile> filesNotFoundOnClient = getNewFilesToUploadOnClient(serverFileList, clientFileList);
        logger.info("Found {} new files to download on client", filesNotFoundOnClient.size());

        List<UpdateFile> combinedListOfFilesToDownload = Stream.of(filesToUpdateOnClientList, filesNotFoundOnClient)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        processOnClient(combinedListOfFilesToDownload);

        List<UpdateFile> filesToDeleteOnClientList = getFilesToDeleteOnClient(serverFileList, clientFileList);
        logger.info("Found {} new files to delete on client", filesToDeleteOnClientList.size());

        deleteOnClient(filesToDeleteOnClientList);
    }

    /**
     * This method its the logic which leads on making changes on the server.
     *
     * @param clientFileList is the list of changed files from client directory
     */
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

    /**
     * This method deletes files on client directory based on param.
     * It checks is file is present, this is basic validation for this case, if is we deletes it with
     * method from File class.
     *
     * @param filesToDeleteOnClientList is list of all files to delete on client.
     */
    public void deleteOnClient(List<UpdateFile> filesToDeleteOnClientList) {
        for (UpdateFile updateFile : filesToDeleteOnClientList) {
            logger.info("Removing file: " + updateFile.getFilePath());
            File file = new File(userLocalDirectory + updateFile.getFilePath());
            if (file.exists() && file.delete()) {
                logger.info("Successfully deleted file {} on client: " + updateFile.getFilePath());
            } else logger.warn("Could not find or delete file on client: " + updateFile.getFilePath());
        }
    }

    /**
     * This method filters list of the files from server and based on the validation method return files
     * which should be deleted on the client directory.
     *
     * @param serverFileList is the list of all files from server directory
     * @param clientFileList is the list of all files from client directory
     * @return list of files to delete on client directory
     */
    public List<UpdateFile> getFilesToDeleteOnClient(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        List<String> serverFileNames = serverFileList.stream()
                .map(UpdateFile::getFilePath)
                .collect(Collectors.toList());

        return clientFileList.stream()
                .filter(clientFile -> !serverFileNames.contains(clientFile.getFilePath()))
                .collect(Collectors.toList());
    }

    /**
     * This method list files from server which weren't found in the client directory.
     *
     * @param serverFileList is the list of all the files from server
     * @param clientFileList is the list of all the files from client
     * @return list of the files not found in the client directory.
     */
    public List<UpdateFile> getNewFilesToUploadOnClient(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        List<String> filePathsFromClientList = clientFileList.stream()
                .map(UpdateFile::getFilePath)
                .collect(Collectors.toList());

        return serverFileList.stream()
                .filter(serverFile -> isFileOnClient(serverFile, filePathsFromClientList))
                .collect(Collectors.toList());
    }

    /**
     * This method filters list of the files from server and based on the validation method return files
     * which should be updated on the client directory.
     *
     * @param serverFileList is the list of all the files from server
     * @param clientFileList is the list of all the files from client
     * @return list of files to be updated on client
     */
    public List<UpdateFile> getExistingFilesToUpdate(List<UpdateFile> serverFileList, List<UpdateFile> clientFileList) {
        return serverFileList.stream()
                .filter(serverFile -> checkIfUpdateIsNeeded(serverFile, clientFileList))
                .collect(Collectors.toList());
    }

    /**
     * This method checks if file from server is existing in the list of files from client.
     *
     * @param serverFile     is the file from server
     * @param clientFileList is the list of all files from client
     * @return result of checking if serverFile is present in the clientFileList
     */
    private boolean isFileOnClient(UpdateFile serverFile, List<String> clientFileList) {
        return !clientFileList.contains(serverFile.getFilePath());
    }

    /**
     * This method splits files to be updated on the client in the way that theirs prefixes matches.
     * <p>
     * Example: /directory1/fileOne.txt, /directory2/fileOne.txt, /directory1/fileTwo.txt
     * Method will send files from directory1 in the first loop of foreach because their prefixes matches and it
     * will save time to download them from server in the same time. File from directory2 will be downloaded in the
     * second loop of the foreach.
     *
     * @param filesToUpdateOnClientList is list of the files to be updated in client directory
     */
    public void processOnClient(List<UpdateFile> filesToUpdateOnClientList) {
        filesToUpdateOnClientList.stream()
                .collect(Collectors.groupingBy(fileToUpdate -> fileToUpdate.getFilePath().substring(0, fileToUpdate.getFilePath().lastIndexOf('\\'))))
                .forEach((prefixOfPath, updateFileList) -> modifyFilesOnClient(updateFileList, prefixOfPath));
    }

    /**
     * This method prepare provided files to be send to rSyncFileUpdateExecutor.
     * It maps their file paths so that ssh will find them on the server and then
     * update their modification dates.
     *
     * @param clientFileList is the list of all files to modify on client directory
     * @param prefixOfPath   is the unique prefix of path that mathes all clientFileList elements
     */
    private void modifyFilesOnClient(List<UpdateFile> clientFileList, String prefixOfPath) {
        List<String> sourcesList = clientFileList.stream()
                .map(UpdateFile::getFilePath)
                .map(filePath -> sshServerHostName + ":" + remoteMainFolder + filePath)
                .collect(Collectors.toList());

        logger.info("Downloading {} file/files from client", sourcesList.toString());
        rSyncFileUpdaterExecutor
                .setSources(sourcesList)
                .setDestination(userLocalDirectory + prefixOfPath + "\\")
                .execute();

    }

    /**
     * This method filters and splits files from @param in the way that in each loop serverFileList elements have
     * the same destination folder and will be updated on server at the same time.
     *
     * @param filesToSendToServerList is the files from client to send to server
     */
    public void processOnServer(List<UpdateFile> filesToSendToServerList) {
        filesToSendToServerList.stream()
                .collect(Collectors.groupingBy(fileToUpdate -> fileToUpdate.getFilePath().substring(0, fileToUpdate.getFilePath().lastIndexOf('\\'))))
                .forEach((prefixOfPath, updateFileList) -> modifyFileOnServer(updateFileList, prefixOfPath));
    }

    /**
     * This method maps @params as sources and destination to send files from updateFileList to the server directory.
     *
     * @param updateFileList is the list of sources from same destination directory to send to server
     * @param prefixOfPath   is the prefix of path to the folder that matchees all updateFileList element
     */
    private void modifyFileOnServer(List<UpdateFile> updateFileList, String prefixOfPath) {
        List<String> sources = updateFileList.stream()
                .map(UpdateFile::getFilePath)
                .map(FilePath -> userLocalDirectory + FilePath)
                .collect(Collectors.toList());

        logger.info("Modifying {} file/files on server", sources.toString());
        rSyncFileUpdaterExecutor
                .setSources(sources)
                .setDestination(sshServerHostName + ":" + remoteMainFolder + prefixOfPath + "\\")
                .execute();

        registerFilesOnServer(updateFileList);
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

    private void registerFilesOnServer(List<UpdateFile> updateFile) {
        fileUpdaterRequestSender.registerFile(updateFile);
    }

    /**
     * This method validates if file from the server is present in the client directory, if it finds same file
     * it compare their modification dates and returns true if file from server is newer
     *
     * @param serverFile     is the file from server
     * @param clientFileList is the list of all the files from client
     * @return result of validation if file from server should be updated on client
     */
    private boolean checkIfUpdateIsNeeded(UpdateFile serverFile, List<UpdateFile> clientFileList) {
        Optional<UpdateFile> isFileOnClient = clientFileList.stream()
                .filter(clientFile -> clientFile.getFilePath().equals(serverFile.getFilePath()))
                .findAny();
        return isFileOnClient.filter(fileRQList -> Long.parseLong(fileRQList.getLastModified()) < Long.parseLong(serverFile.getLastModified())).isPresent();
    }

    public RSyncFileUpdaterProvider setRemoteMainFolder(String remoteMainFolder) {
        this.remoteMainFolder = remoteMainFolder;
        return this;
    }

}