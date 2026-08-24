package org.zeith.cloudflared.proxy;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.*;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.zeith.cloudflared.*;
import org.zeith.cloudflared.architectury.*;
import org.zeith.cloudflared.core.*;
import org.zeith.cloudflared.core.api.*;
import org.zeith.cloudflared.core.exceptions.CloudflaredNotFoundException;

import java.io.File;
import java.util.*;

public class ClientProxy
		implements CommonProxy
{
	private CloudflaredAPI api;
	protected final List<IGameListener> listeners = new ArrayList<>();
	
	public IGameSession startedSession;
	protected final IMessageConsumer messages;
	
	public ClientProxy(IMessageConsumer messages)
	{
		this.messages = messages;
	}
	
	@Override
	public void tryCreateApi()
	{
		try
		{
			api = CloudflaredAPIFactory.builder()
			                           .gameProxy(this)
			                           .hostname(() -> CloudflaredConfig.getInstance().advancedNetwork.hostname)
			                           .build()
			                           .createApi();
		} catch(CloudflaredNotFoundException ex)
		{
			api = null;
			CloudflaredMod.LOG.fatal("Unable to communicate with cloudflared. Are you sure you have cloudflared installed?", ex);
			createToast(InfoLevel.CRITICAL, "Error", "Unable to access Cloudflared.");
		}
	}
	
	@Override
	public void setup()
	{
		CommonProxy.super.setup();
		if(api == null)
		{
			messages.chat(
					Component.translatable("chat.cloudflared:not_installed")
					         .append(" ")
					         .append(Component.translatable("chat.cloudflared:not_installed.click")
					                          .withStyle(Style.EMPTY
													  .withColor(ChatFormatting.BLUE)
							                          .withUnderlined(true)
							                          .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("/cloudflared install")))
							                          .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cloudflared install"))
											  )
							 )
			);
		}
	}
	
	@Override
	public void serverStarted(MinecraftServer server)
	{
		if(server instanceof IntegratedServer && api != null)
			api.closeAllAccesses();
	}
	
	@Override
	public void serverStop()
	{
		if(startedSession != null)
		{
			for(IGameListener listener : listeners)
				listener.onHostingEnd(startedSession);
			startedSession = null;
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
	
	public List<IGameListener> getListeners()
	{
		return Collections.unmodifiableList(listeners);
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
		messages.chat(Component.translatable(message));
	}
	
	@Override
	public File getExtraDataDir()
	{
		File f = new File(Minecraft.getInstance().gameDirectory, "asm" + File.separator + "Cloudflared");
		if(f.isFile()) f.delete();
		if(!f.isDirectory()) f.mkdirs();
		return f;
	}
	
	@Override
	public void createToast(InfoLevel level, String title, String subtitle)
	{
		var tc = Component.translatable(title);
		var sub = subtitle != null ? Component.translatable(subtitle) : null;
		
		var gui = Minecraft.getInstance().getToasts();
		gui.addToast(new ErrorToast(tc, sub));
	}
	
	public static class ErrorToast
			implements Toast
	{
		private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/system");
		
		private final Component title;
		private final List<FormattedCharSequence> messageLines;
		private long lastChanged;
		private boolean changed;
		private final int width;
		
		public ErrorToast(Component component, @Nullable Component component2)
		{
			this(component, nullToEmpty(component2), Math.max(160,
							30 + Math.max(Minecraft.getInstance().font.width(component), component2 == null ? 0 : Minecraft.getInstance().font.width(component2))
					)
			);
		}
		
		private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component component)
		{
			return component == null ? ImmutableList.of() : ImmutableList.of(component.getVisualOrderText());
		}
		
		private ErrorToast(Component component, List<FormattedCharSequence> list, int i)
		{
			this.title = component;
			this.messageLines = list;
			this.width = i;
		}
		
		@Override
		public int width()
		{
			return this.width;
		}
		
		@Override
		public int height()
		{
			return 20 + this.messageLines.size() * 12;
		}
		
		@Override
		public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l)
		{
			if(this.changed)
			{
				this.lastChanged = l;
				this.changed = false;
			}
			
			int i = this.width();
			if(i == 160 && this.messageLines.size() <= 1)
			{
				guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, i, this.height());
			} else
			{
				int j = this.height();
				int m = Math.min(4, j - 28);
				this.renderBackgroundRow(guiGraphics, i, 0, 0, 28);
				
				for(int n = 28; n < j - m; n += 10)
				{
					this.renderBackgroundRow(guiGraphics, i, 16, n, Math.min(16, j - n - m));
				}
				
				this.renderBackgroundRow(guiGraphics, i, 32 - m, j - m, m);
			}
			
			if(this.messageLines.isEmpty())
			{
				guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 18, 12, -256, false);
			} else
			{
				guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 18, 7, -256, false);
				
				for(int j = 0; j < this.messageLines.size(); ++j)
				{
					guiGraphics.drawString(toastComponent.getMinecraft().font, (FormattedCharSequence) this.messageLines.get(j), 18, 18 + j * 12, -1, false);
				}
			}
			
			return l - this.lastChanged < 5000L ? Visibility.SHOW : Visibility.HIDE;
		}
		
		private void renderBackgroundRow(GuiGraphics guiGraphics, int i, int j, int k, int l)
		{
			int m = j == 0 ? 20 : 5;
			int n = Math.min(60, i - m);
			ResourceLocation resourceLocation = BACKGROUND_SPRITE;
			guiGraphics.blitSprite(resourceLocation, 160, 32, 0, j, 0, k, m, l);
			
			for(int o = m; o < i - n; o += 64) {
				guiGraphics.blitSprite(resourceLocation, 160, 32, 32, j, o, k, Math.min(64, i - o - n), l);
			}
			
			guiGraphics.blitSprite(resourceLocation, 160, 32, 160 - n, j, i - n, k, n, l);
		}
	}
}
