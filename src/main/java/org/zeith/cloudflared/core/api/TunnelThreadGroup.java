package org.zeith.cloudflared.core.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TunnelThreadGroup extends ThreadGroup {

    private static final Logger LOG = LogManager.getLogger("CloudflaredTunnel");
    public static final TunnelThreadGroup GROUP = new TunnelThreadGroup("CloudflaredTunnels");

    public TunnelThreadGroup(String name) {
        super(name);
        setMaxPriority(1);
    }

    public void uncaughtException(Thread thread, Throwable e) {
        LOG.error("Uncaught tunnel exception on tunnel {}:", thread.getName(), e);
    }
}
