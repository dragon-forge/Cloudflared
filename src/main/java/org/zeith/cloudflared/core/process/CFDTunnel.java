package org.zeith.cloudflared.core.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.IGameSession;

public class CFDTunnel extends BaseTunnel {

    public static final Pattern URL_REGEX = Pattern
        .compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");
    protected final IGameSession session;
    private static final Logger LOG = LogManager.getLogger("CloudflaredTunnel");
    protected final String hostname;

    protected final int port;
    private String generatedHostname;

    public String getGeneratedHostname() {
        return this.generatedHostname;
    }

    private boolean waitingForHostname = false;
    private boolean registered = false;

    public CFDTunnel(IGameSession session, CloudflaredAPI api, int port, String hostname) {
        super(api, "CFDTunnelThread[Ingress=" + port + "->Egress=" + hostname + "]");
        this.hostname = hostname;
        this.port = port;
        this.session = session;
    }

    protected Process createProcess() {
        String localAddr = "tcp://127.0.0.1:" + this.port;

        LOG.info("Starting tunnel pointing to {}...", localAddr);

        try {
            List<String> args = new ArrayList<>();

            args.add(
                this.api.getExecutable()
                    .get());
            args.add("tunnel");
            if (this.hostname != null && !this.hostname.isEmpty()) {

                args.add("--hostname");
                args.add(this.hostname);
            }
            args.add("--url");
            args.add(localAddr);

            Process p = (new ProcessBuilder(args.toArray(new String[0]))).redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start();

            this.startedProcess = p;

            LOG.info("Tunnel to {} started.", localAddr);

            return p;
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    protected void markOpen() {
        this.session.onTunnelOpen(this);
        super.markOpen();
    }

    protected void processLine(String line) {
        if (!this.registered) {

            if (line.contains("Registered")) {

                markOpen();
                this.registered = true;
            }

            if (line.contains("Failed")) {
                this.api.getGame()
                    .sendChatMessage(line.replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+", "*.*.*.*"));
            }

            if (line.contains("Retrying")) {
                this.api.getGame()
                    .sendChatMessage(line.replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+", "*.*.*.*"));
            }
        }

        if (this.generatedHostname == null) {

            if (line.contains("Visit it at")) {

                Matcher m = URL_REGEX.matcher(line);
                if (m.find()) {

                    this.generatedHostname = m.group();
                    this.waitingForHostname = false;
                    return;
                }
                this.waitingForHostname = true;
            }

            if (this.waitingForHostname) {

                Matcher m = URL_REGEX.matcher(line);
                if (m.find()) {

                    this.generatedHostname = m.group();
                    this.waitingForHostname = false;

                    return;
                }
            }
        }
        System.out.println(line);
    }
}
