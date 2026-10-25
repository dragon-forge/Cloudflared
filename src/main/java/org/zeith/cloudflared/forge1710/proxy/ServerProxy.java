package org.zeith.cloudflared.forge1710.proxy;

import cpw.mods.fml.common.event.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import org.zeith.cloudflared.core.*;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;
import org.zeith.cloudflared.forge1710.*;
import org.zeith.cloudflared.forge1710.command.CommandCloudflared;

import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("unused")
public class ServerProxy
		implements CommonProxy
{
	static private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	protected final List<IGameListener> listeners = new ArrayList<>();
	private CloudflaredAPI api;
	public IGameSession startedSession;
	protected MinecraftServer server;
	
	@Override
	public void tryCreateApi()
	{
		try
		{
			this.api = CloudflaredAPIFactory
					.builder()
					.gameProxy(this)
					.hostname(() -> Configs1710.hostname)
					.build()
					.createApi();
		} catch(CloudflaredNotFoundException e)
		{
			CloudflaredForge.LOG.error("CloudflaredAPI not found!", e);
		}
	}
	
	@Override
	public void startSession(MCGameSession1710 session)
	{
		this.startedSession = session;
		for(IGameListener listener : this.listeners)
		{
			listener.onHostingStart(session);
		}
	}
	
	@Override
	public Optional<CloudflaredAPI> getApi()
	{
		return Optional.ofNullable(this.api);
	}
	
	@Override
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandCloudflared());
	}
	
	@Override
	public void serverStarted(FMLServerAboutToStartEvent event)
	{
		this.server = event.getServer();
		if(this.api != null) this.api.closeAllAccesses();
		if(Configs1710.startTunnel)
		{
			this.server.addChatMessage(new ChatComponentTranslation("chat.cloudflared:starting_tunnel"));
			startSession(new MCGameSession1710(this.server.getServerPort(), UUID.randomUUID(), this.server));
		}
	}
	
	@Override
	public void serverStop(FMLServerStoppingEvent event)
	{
		this.server = null;
		if(this.startedSession != null)
		{
			for(IGameListener listener : this.listeners) listener.onHostingEnd(this.startedSession);
			this.startedSession = null;
		}
	}
	
	@Override
	public ExecutorService getBackgroundExecutor()
	{
		return ServerProxy.executor;
	}
	
	@Override
	public void addListener(IGameListener listener)
	{
		this.listeners.add(listener);
	}
	
	@Override
	public void removeListener(IGameListener listener)
	{
		this.listeners.remove(listener);
	}
	
	@Override
	public void sendChatMessage(String string)
	{
		this.server.addChatMessage(new ChatComponentTranslation(string));
	}
	
	@Override
	public void createToast(InfoLevel level, String title, String subtitle)
	{
		switch(level)
		{
			case INFO:
				CloudflaredForge.LOG.info(title + ": " + subtitle);
				break;
			case WARNING:
				CloudflaredForge.LOG.warn(title + ": " + subtitle);
				break;
			case CRITICAL:
				CloudflaredForge.LOG.error(title + ": " + subtitle);
				break;
			default:
				return;
		}
	}
}