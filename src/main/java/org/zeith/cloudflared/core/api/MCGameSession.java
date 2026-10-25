package org.zeith.cloudflared.core.api;

import lombok.Data;

import java.util.UUID;

@Data
public abstract class MCGameSession
		implements IGameSession
{
	public final int serverPort;
	public final UUID host;
	
	@Override
	public UUID getSessionID()
	{
		return host;
	}
	
	@Override
	public int getPort()
	{
		return serverPort;
	}
}