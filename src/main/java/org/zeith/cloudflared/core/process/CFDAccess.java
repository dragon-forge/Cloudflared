package org.zeith.cloudflared.core.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.cloudflared.core.CloudflaredAPI;

public class CFDAccess extends BaseTunnel {

    private static final Logger LOG = LogManager.getLogger("CloudflaredAccess");
    protected final int localPort;

    protected CompletableFuture<Integer> openFuture = new CompletableFuture<>();

    public CompletableFuture<Integer> getOpenFuture() {
        return this.openFuture;
    }

    protected final String hostname;
    protected boolean hasBeenOpened;

    public CFDAccess(CloudflaredAPI api, String hostname, int localPort) {
        super(api, "CFDAccessThread[Ingress=" + hostname + "->OnPort=" + localPort + "]");

        this.hasBeenOpened = false;
        this.hostname = hostname;
        this.localPort = localPort;
    }

    protected void processLine(String line) {
        if (!this.hasBeenOpened) {

            this.hasBeenOpened = true;
            this.openFuture.complete(this.localPort);
            markOpen();
        }

        System.out.println(line);
    }

    protected Process createProcess() {
        String localAddr = "127.0.0.1:" + this.localPort;
        LOG.info("Starting access point from {} to {}...", this.hostname, localAddr);
        try {
            List<String> args = new ArrayList<>();
            args.add(
                this.api.getExecutable()
                    .get());
            args.add("access");
            args.add("tcp");
            args.add("--hostname");
            args.add(this.hostname);
            args.add("--url");
            args.add(localAddr);
            Process p = (new ProcessBuilder(args.toArray(new String[0]))).redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start();
            this.startedProcess = p;
            LOG.info("Access to {} on port {} started.", this.hostname, localAddr);
            return p;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
