package org.zeith.cloudflared.forge1122.proxy;

import com.zeitheron.hammercore.client.adapter.ChatMessageAdapter;
import com.zeitheron.hammercore.client.utils.gl.shading.VariableShaderProgram;
import com.zeitheron.hammercore.utils.FileSizeMetric;
import com.zeitheron.hammercore.utils.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.*;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.CloudflaredAPIFactory;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;
import org.zeith.cloudflared.core.process.CFDAccess;
import org.zeith.cloudflared.forge1122.*;
import org.zeith.cloudflared.forge1122.command.CommandCloudflared;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class ClientProxy1122
		implements CommonProxy1122
{
	private CloudflaredAPI api;
	protected final List<IGameListener> listeners = new ArrayList<>();
	
	public IGameSession startedSession;
	
	protected Thread gameThread;
	
	public static void onSharedToLan(IntegratedServer server, int port)
	{
		if(!CloudflaredForge.PROXY.getApi().isPresent()) return;
		
		server.setAllowPvp(Configs1122.enablePvP);
		server.setOnlineMode(Configs1122.onlineMode);
		server.setCanSpawnAnimals(Configs1122.canSpawnAnimals);
		server.setCanSpawnNPCs(Configs1122.canSpawnNPCs);
		
		if(Configs1122.startTunnel)
		{
			Minecraft mc = Minecraft.getMinecraft();
			mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("chat.cloudflared:starting_tunnel"));
			CloudflaredForge.PROXY.startSession(new MCGameSession1122(port, mc.player.getGameProfile().getId(), mc.player));
		}
	}
	
	@Override
	public void tryCreateApi()
	{
		try
		{
			api = CloudflaredAPIFactory.builder()
					.gameProxy(this)
					.hostname(() -> Configs1122.hostname)
					.build()
					.createApi();
		} catch(CloudflaredNotFoundException ex)
		{
			api = null;
			CloudflaredForge.LOG.fatal("Unable to communicate with cloudflared. Are you sure you have cloudflared installed?", ex);
			createToast(InfoLevel.CRITICAL, "Error", "Unable to access Cloudflared.");
		}
	}
	
	@Override
	public IFileDownload pushFileDownload()
	{
		if(gameThread != Thread.currentThread())
			return IFileDownload.DUMMY;
		return new IFileDownload()
		{
			ProgressManager.ProgressBar bar;
			final int totalSteps = 1000;
			int stepsLeft = totalSteps;
			
			long lastUpdateLen;
			long lastUpdateMs;
			
			@Override
			public void onStart()
			{
				onEnd();
				bar = ProgressManager.push("Downloading Cloudflared", totalSteps);
			}
			
			@Override
			public void onUpload(long uploaded, long total)
			{
				long bpp = total / totalSteps;
				if(uploaded - lastUpdateLen > bpp)
				{
					if(stepsLeft > 0)
					{
						bar.step(FileSizeMetric.toMaxSize(uploaded) + " / " + FileSizeMetric.toMaxSize(total));
						--stepsLeft;
					}
					lastUpdateLen = uploaded;
				} else if(System.currentTimeMillis() - lastUpdateMs > 50L)
				{
					setMessage(bar, FileSizeMetric.toMaxSize(uploaded) + " / " + FileSizeMetric.toMaxSize(total));
					lastUpdateMs = System.currentTimeMillis();
				}
			}
			
			@Override
			public void onEnd()
			{
				if(bar == null) return;
				while(bar.getStep() < bar.getSteps())
					bar.step("");
				ProgressManager.pop(bar);
				bar = null;
			}
		};
	}
	
	@Override
	public void preInit(FMLPreInitializationEvent e)
	{
		gameThread = Thread.currentThread();
		CommonProxy1122.super.preInit(e);
		if(api == null)
		{
			ChatMessageAdapter.sendOnFirstWorldLoad(new TextComponentTranslation("chat.cloudflared:not_installed")
					.appendText(" ")
					.appendSibling(new TextComponentTranslation("chat.cloudflared:not_installed.click")
							.setStyle(new Style()
									.setColor(TextFormatting.BLUE)
									.setUnderlined(true)
									.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("/cloudflared install")))
									.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cloudflared install"))
							)
					));
		}
		ClientCommandHandler.instance.registerCommand(new CommandCloudflared());
	}
	
	@Override
	public void serverStarted(FMLServerAboutToStartEvent e)
	{
		if(e.getServer() instanceof IntegratedServer && api != null)
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
	
	@Override
	public Optional<CloudflaredAPI> getApi()
	{
		return Optional.ofNullable(api);
	}
	
	@Override
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
		if(Minecraft.getMinecraft().player != null)
			Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.CHAT, new TextComponentTranslation(message));
		else
			ChatMessageAdapter.sendOnFirstWorldLoad(new TextComponentTranslation(message));
	}
	
	@Override
	public File getExtraDataDir()
	{
		File f = new File(Minecraft.getMinecraft().gameDir, "asm" + File.separator + "Cloudflared");
		if(f.isFile()) f.delete();
		if(!f.isDirectory()) f.mkdirs();
		return f;
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
		
		CFDAccess tunnel = CloudflaredForge.PROXY
				.getApi()
				.map(a -> a.getOrOpenAccess(hostname))
				.orElse(null);
		
		if(tunnel == null) return null;
		
		int openPort = tunnel.getOpenFuture().join();
		
		return ServerAddress.fromString("127.0.0.1:" + openPort);
	}
	
	public static Field pbMsg, pbLastTime;
	
	public static void setMessage(ProgressManager.ProgressBar bar, String msg)
	{
		if(pbMsg == null)
			pbMsg = ReflectionUtil.getField(ProgressManager.ProgressBar.class, "message");
		if(pbLastTime == null)
			pbLastTime = ReflectionUtil.getField(ProgressManager.ProgressBar.class, "lastTime");
		try
		{
			msg = FMLCommonHandler.instance().stripSpecialChars(msg);
			pbMsg.set(bar, msg);
			pbLastTime.setLong(bar, System.nanoTime());
			FMLCommonHandler.instance().processWindowMessages();
		} catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
}