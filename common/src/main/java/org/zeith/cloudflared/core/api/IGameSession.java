package org.zeith.cloudflared.core.api;

import org.zeith.cloudflared.core.process.CFDTunnel;

import java.util.UUID;

public interface IGameSession
{
	UUID getSessionID();
	
	int getPort();
	
	void onTunnelOpen(CFDTunnel tunnel);
	
	void onTunnelClosed(CFDTunnel tunnel);
}