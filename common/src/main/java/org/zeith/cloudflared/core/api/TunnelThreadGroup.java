package org.zeith.cloudflared.core.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TunnelThreadGroup
        extends ThreadGroup {
    public static final TunnelThreadGroup GROUP = new TunnelThreadGroup("CloudflaredTunnels");
    private static final Logger LOG = LogManager.getLogger("CloudflaredTunnel");

    public TunnelThreadGroup(String name) {
        super(name);
        setMaxPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.error("Uncaught tunnel exception on tunnel {}:", t.getName(), e);
    }
}