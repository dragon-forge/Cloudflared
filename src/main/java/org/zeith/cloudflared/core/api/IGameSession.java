package org.zeith.cloudflared.core.api;

import org.zeith.cloudflared.core.process.CFDTunnel;

public interface IGameSession {

    int getPort();

    void onTunnelOpen(CFDTunnel tunnel);
}
