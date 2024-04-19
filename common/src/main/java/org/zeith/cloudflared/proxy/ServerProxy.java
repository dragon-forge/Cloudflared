package org.zeith.cloudflared.proxy;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.zeith.cloudflared.CloudflaredConfig;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.architectury.MCArchGameSession;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.CloudflaredAPIFactory;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;

import java.util.*;

public class ServerProxy
		implements CommonProxy
{
	private CloudflaredAPI api;
	protected final List<IGameListener> listeners = new ArrayList<>();
	
	public IGameSession startedSession;
	protected MinecraftServer server;
	
	@Override
	public void tryCreateApi()
	{
		try
		{
			api = CloudflaredAPIFactory.builder()
					.gameProxy(this)
					.hostname(() -> CloudflaredConfig.hostname)
					.build()
					.createApi();
		} catch(CloudflaredNotFoundException ex)
		{
			CloudflaredMod.LOG.fatal("Unable to create communicate with cloudflared. Are you sure you have cloudflared installed?", ex);
		}
	}
	
	@Override
	public void startSession(MCArchGameSession session)
	{
		startedSession = session;
		for(IGameListener listener : listeners)
			listener.onHostingStart(session);
	}
	
	@Override
	public Optional<CloudflaredAPI> getApi()
	{
		return Optional.ofNullable(api);
	}
	
	@Override
	public void serverStarted(MinecraftServer server)
	{
		if(api != null) api.closeAllAccesses();
		if(CloudflaredConfig.startTunnel)
		{
			server.sendSystemMessage(Component.translatable("chat.cloudflared:starting_tunnel"));
			startSession(new MCArchGameSession(server.getPort(), UUID.randomUUID(), server));
		}
	}
	
	@Override
	public void serverStop()
	{
		server = null;
		if(startedSession != null)
		{
			for(IGameListener listener : listeners)
				listener.onHostingEnd(startedSession);
			startedSession = null;
		}
	}
	
	@Override
	public void addListener(IGameListener listener)
	{
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(IGameListener listener)
	{
		listeners.remove(listener);
	}
	
	@Override
	public void sendChatMessage(String message)
	{
		server.sendSystemMessage(Component.translatable(message));
	}
	
	@Override
	public void createToast(InfoLevel level, String title, String subtitle)
	{
		server.sendSystemMessage(Component.translatable(title));
	}
}