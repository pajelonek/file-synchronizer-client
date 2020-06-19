package com.licencjat.filesynchronizer.client.rsync;

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput;
import com.github.fracpete.processoutput4j.output.StreamingProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import com.github.fracpete.rsync4j.core.Binaries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RSyncFileUpdaterExecutor {

    Logger logger = LoggerFactory.getLogger(RSyncFileUpdaterExecutor.class);

    List<String> sources;
    String destination;
    RSync rSync = new RSync();

    public void execute() {
        try {
            rSync
                    .sources(sources)
                    .outputCommandline(true)
                    .destination(destination)
                    .verbose(true)
                    .protectArgs(true)
                    .compress(true)
//                    .debug("ALL")
                    .archive(true)
                    .rsh("ssh"/*"C:\\rsync4j\\bin\\ssh.exe"Binaries.sshBinary()+ " -i " + Binaries.convertPath(sshPrivateKeyPath)*/);

            StreamingProcessOutput output = new StreamingProcessOutput(new RsyncOutput());
            output.monitor(rSync.builder());
            if (output.getExitCode() > 0) {
                throw new Error("Error while executing rsync");
            } else logger.info("Successfully modified {} on server", sources.toString());

        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw new Error("Caught exception while executing rsync");
        }
    }


    public RSyncFileUpdaterExecutor setSources(List<String> sources) {
        this.sources = sources;
        return this;
    }


    public RSyncFileUpdaterExecutor setDestination(String destination) {
        this.destination = destination;
        return this;
    }
}
