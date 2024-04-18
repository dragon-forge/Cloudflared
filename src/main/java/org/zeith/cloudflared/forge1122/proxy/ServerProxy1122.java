package org.zeith.cloudflared.forge1122.proxy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.event.*;
import org.zeith.cloudflared.core.*;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.forge1122.Configs1122;
import org.zeith.cloudflared.forge1122.MCGameSession1122;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class ServerProxy1122
		implements CommonProxy1122
{
	protected CloudflaredAPI api;
	protected final List<IGameListener> listeners = new ArrayList<>();
	
	public IGameSession startedSession;
	protected MinecraftServer server;
	
	@Override
	public void preInit(FMLPreInitializationEvent e)
	{
		api = CloudflaredAPIFactory.builder()
				.gameProxy(this)
				.autoDownload(Configs1122.autodownload)
				.hostname(() -> Configs1122.hostname)
				.executable(() -> Configs1122.executable)
				.build()
				.createApi();
	}
	
	@Override
	public void startSession(MCGameSession1122 session)
	{
		startedSession = session;
		for(IGameListener listener : listeners)
			listener.onHostingStart(session);
	}
	
	@Override
	public void serverStarted(FMLServerAboutToStartEvent e)
	{
		server = e.getServer();
		api.closeAllAccesses();
		if(Configs1122.startTunnel)
		{
			server.sendMessage(new TextComponentTranslation("chat.cloudflared:starting_tunnel"));
			startSession(new MCGameSession1122(server.getServerPort(), UUID.randomUUID(), server));
		}
	}
	
	@Override
	public void serverStop(FMLServerStoppingEvent e)
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
	public ExecutorService getBackgroundExecutor()
	{
		return HttpUtil.DOWNLOADER_EXECUTOR;
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
		server.sendMessage(new TextComponentTranslation(message));
	}
	
	@Override
	public void createToast(InfoLevel level, String title, String subtitle)
	{
		server.sendMessage(new TextComponentTranslation(title));
	}
}