package org.zeith.cloudflared.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.zeith.cloudflared.core.api.IGameListener;
import org.zeith.cloudflared.core.api.IGameSession;
import org.zeith.cloudflared.core.process.CFDTunnel;

class CloudflaredAPIListeners implements IGameListener {

    protected final CloudflaredAPI api;
    protected final Map<IGameSession, CFDTunnel> tunnels = new ConcurrentHashMap<>();

    public CloudflaredAPIListeners(CloudflaredAPI api) {
        this.api = api;
    }

    public void onHostingStart(IGameSession session) {
        CFDTunnel tunnel = this.api.createTunnel(
            session,
            session.getPort(),
            this.api.getConfigs()
                .getHostname()
                .get());
        tunnel.start();
        this.tunnels.put(session, tunnel);
    }

    public void onHostingEnd(IGameSession session) {
        CFDTunnel tunnel = this.tunnels.remove(session);
        if (tunnel == null) return;
        tunnel.interrupt();
    }
}
