package org.zeith.cloudflared.core;

import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.process.CFDTunnel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class CloudflaredAPIListeners
		implements IGameListener
{
	protected final CloudflaredAPI api;
	
	protected final Map<IGameSession, CFDTunnel> tunnels = new ConcurrentHashMap<>();
	
	public CloudflaredAPIListeners(CloudflaredAPI api)
	{
		this.api = api;
	}
	
	@Override
	public void onHostingStart(IGameSession session)
	{
		CFDTunnel tunnel = api.createTunnel(session, session.getPort(), api.getConfigs().getHostname().get());
		tunnel.start();
		tunnels.put(session, tunnel);
	}
	
	@Override
	public void onHostingEnd(IGameSession session)
	{
		CFDTunnel tunnel = tunnels.remove(session);
		if(tunnel == null) return;
		tunnel.interrupt();
	}
}