package com.licencjat.filesynchronizer.client.rsync;

import com.github.fracpete.processoutput4j.core.StreamingProcessOutputType;
import com.github.fracpete.processoutput4j.core.StreamingProcessOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RsyncOutput implements StreamingProcessOwner {

    Logger logger = LoggerFactory.getLogger(RsyncOutput.class);

    public StreamingProcessOutputType getOutputType() {
        return StreamingProcessOutputType.BOTH;
    }

    public void processOutput(String line, boolean stdout) {
        if (stdout) {
            logger.info("RSYNC: " + line);
        } else {
            logger.error("RSYNC: " + line);
        }
    }
}


