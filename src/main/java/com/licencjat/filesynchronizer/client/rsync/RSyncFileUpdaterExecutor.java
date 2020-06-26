package com.licencjat.filesynchronizer.client.rsync;

import com.github.fracpete.processoutput4j.output.StreamingProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RSyncFileUpdaterExecutor {

    @Value("${rsync.remote.shell}")
    private String rsh;

    Logger logger = LoggerFactory.getLogger(RSyncFileUpdaterExecutor.class);

    private RSync rSync = new RSync();
    private List<String> sources;
    private String destination;

    //todo check if rsh should be /*"C:\\rsync4j\\bin\\ssh.exe"Binaries.sshBinary()+ " -i " + Binaries.convertPath(sshPrivateKeyPath)*/
    public void execute() {
        try {
            rSync
                    .sources(sources)
                    .destination(destination)
                    .outputCommandline(true)
                    .verbose(true)
                    .protectArgs(true)
                    .compress(true)
                    .archive(true)
                    .rsh(rsh);

            StreamingProcessOutput output = new StreamingProcessOutput(new RsyncOutput());
            output.monitor(rSync.builder());

            if (output.getExitCode() > 0) {
                throw new Error("Error while executing rsync");
            } else logger.info("Successfully modified {}", sources.toString());

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
