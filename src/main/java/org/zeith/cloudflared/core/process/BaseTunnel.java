package org.zeith.cloudflared.core.process;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.api.IGameListener;
import org.zeith.cloudflared.core.api.TunnelThreadGroup;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public abstract class BaseTunnel extends Thread implements ITunnel {

    private static final Logger LOG = LogManager.getLogger("CloudflaredTunnels");
    protected final Supplier<Process> process = Suppliers.memoize(this::createProcess);
    protected Process startedProcess;

    public CloudflaredAPI getApi() {
        return this.api;
    }

    protected final CloudflaredAPI api;

    public BaseTunnel(CloudflaredAPI api, String name) {
        super(TunnelThreadGroup.GROUP, name);
        this.api = api;
    }

    protected void markClosed() {
        if (this.startedProcess == null) return;
        this.startedProcess = null;
        for (IGameListener listener : this.api.getGame()
            .getListeners()) {
            listener.onTunnelClosed(this);
        }
    }

    protected void markOpen() {
        for (IGameListener listener : this.api.getGame()
            .getListeners()) {
            listener.onTunnelOpened(this);
        }
    }

    protected String preprocessLine(String line) {
        return line.split("\\s", 3)[2];
    }

    public void run() {
        this.startedProcess = this.process.get();
        try (Scanner in = new Scanner(this.startedProcess.getErrorStream())) {

            while (in.hasNextLine()) {
                processLine(preprocessLine(in.nextLine()));
            }
            if (this.startedProcess != null) this.startedProcess.waitFor();
            markClosed();
        } catch (InterruptedException ignored) {

            LOG.error("Access forcefully interrupted.");
        } catch (Exception e) {

            LOG.error("Failed to launch tunnel:", e);
        }
    }

    public void interrupt() {
        if (this.startedProcess != null) {

            this.startedProcess.destroy();

            try {
                if (this.startedProcess.waitFor(10L, TimeUnit.SECONDS)) {

                    int code = this.startedProcess.exitValue();
                    LOG.info("Tunnel stopped with exit value {}.", code);
                    markClosed();
                }
            } catch (Exception e) {

                LOG.error("Failed to wait until tunnel shutdown. Sending force-destroy instruction.");
                try {
                    this.startedProcess.destroyForcibly();
                } catch (NullPointerException ignored) {}
                markClosed();
            }
        }

        super.interrupt();
    }

    public void closeTunnel() {
        interrupt();
    }

    protected abstract Process createProcess();

    protected abstract void processLine(String line);
}
