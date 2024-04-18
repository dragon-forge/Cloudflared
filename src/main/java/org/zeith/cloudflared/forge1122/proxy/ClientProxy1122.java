package org.zeith.cloudflared.forge1122.proxy;

import com.zeitheron.hammercore.client.adapter.ChatMessageAdapter;
import com.zeitheron.hammercore.client.utils.gl.shading.VariableShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.toasts.*;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.*;
import org.zeith.cloudflared.core.*;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;
import org.zeith.cloudflared.core.process.CFDAccess;
import org.zeith.cloudflared.forge1122.*;
import org.zeith.cloudflared.forge1122.client.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class ClientProxy1122
		implements CommonProxy1122
{
	protected CloudflaredAPI api;
	protected final List<IGameListener> listeners = new ArrayList<>();
	
	public IGameSession startedSession;
	
	@Override
	public void preInit(FMLPreInitializationEvent e)
	{
		try
		{
			api = CloudflaredAPIFactory.builder()
					.gameProxy(this)
					.autoDownload(Configs1122.autodownload)
					.hostname(() -> Configs1122.hostname)
					.executable(() -> Configs1122.executable)
					.build()
					.createApi();
		} catch(CloudflaredNotFoundException ex)
		{
			createToast(InfoLevel.CRITICAL, "", "");
		}
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void overrideShareToLanGUI(GuiOpenEvent e)
	{
		if(e.getGui() instanceof GuiShareToLan)
		{
			try
			{
				Field f = GuiShareToLan.class.getDeclaredFields()[0];
				f.setAccessible(true);
				GuiScreen gs = (GuiScreen) f.get(e.getGui());
				e.setGui(new GuiShareToLanCloudflared(gs));
			} catch(ReflectiveOperationException err)
			{
				CloudflaredForge.LOG.error("Failed to open GuiShareToLanCloudflared.", err);
			}
		}
	}
	
	@Override
	public void serverStarted(FMLServerAboutToStartEvent e)
	{
		if(e.getServer() instanceof IntegratedServer)
			api.closeAllAccesses();
	}
	
	@Override
	public void serverStop(FMLServerStoppingEvent e)
	{
		if(startedSession != null)
		{
			for(IGameListener listener : listeners)
				listener.onHostingEnd(startedSession);
			startedSession = null;
		}
	}
	
	@Override
	public void startSession(MCGameSession1122 session)
	{
		startedSession = session;
		for(IGameListener listener : listeners)
			listener.onHostingStart(session);
	}
	
	public List<IGameListener> getListeners()
	{
		return Collections.unmodifiableList(listeners);
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
		ChatMessageAdapter.sendOnFirstWorldLoad(new TextComponentTranslation(message));
	}
	
	@Override
	public void createToast(InfoLevel level, String title, String subtitle)
	{
		ITextComponent tc = new TextComponentTranslation(title);
		ITextComponent sub = subtitle != null ? new TextComponentTranslation(subtitle) : null;
		
		GuiToast gui = Minecraft.getMinecraft().getToastGui();
		if(level == InfoLevel.CRITICAL)
		{
			gui.add(new VariableShaderProgram.ShaderErrorToast(tc, sub));
			return;
		}
		
		gui.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, tc, sub));
	}
	
	@Nullable
	public static Integer pickPort()
	{
		if(Configs1122.customPortOverride > 0 && Configs1122.customPortOverride < 65535)
			return Configs1122.customPortOverride;
		return null;
	}
	
	public static ServerAddress decodeAddress(String input)
	{
		if(!input.startsWith("cloudflared://")) return null;
		
		String hostname = input.substring(14);
		
		CFDAccess tunnel = ((ClientProxy1122) CloudflaredForge.PROXY).api.getOrOpenAccess(hostname);
		
		int openPort = tunnel.getOpenFuture().join();
		
		return ServerAddress.fromString("127.0.0.1:" + openPort);
	}
}