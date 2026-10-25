package org.zeith.cloudflared.core.api;

public interface IGameListener
{
	void onHostingStart(IGameSession session);
	
	void onHostingEnd(IGameSession session);
}