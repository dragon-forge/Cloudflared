package org.zeith.cloudflared.core.api;

import org.zeith.cloudflared.core.process.ITunnel;

public interface IGameListener
{
	void onHostingStart(IGameSession session);
	
	void onHostingEnd(IGameSession session);
	
	default void onTunnelOpened(ITunnel tunnel) {}
	
	default void onTunnelClosed(ITunnel tunnel) {}
}