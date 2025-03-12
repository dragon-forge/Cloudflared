package org.zeith.cloudflared.core.api;

import org.zeith.cloudflared.core.process.ITunnel;

public interface IGameListener {

    void onHostingStart(IGameSession session);

    void onHostingEnd(IGameSession session);

    @SuppressWarnings("unused")
    default void onTunnelOpened(ITunnel tunnel) {}

    @SuppressWarnings("unused")
    default void onTunnelClosed(ITunnel tunnel) {}
}
