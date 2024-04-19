package org.zeith.cloudflared.proxy;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.zeith.cloudflared.CloudflaredConfig;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.architectury.IMessageConsumer;
import org.zeith.cloudflared.architectury.MCArchGameSession;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.CloudflaredAPIFactory;
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
		private final Component title;
		private final List<FormattedCharSequence> messageLines;
		private long lastChanged;
		private boolean changed;
		private final int width;
		
		public ErrorToast(Component component, @Nullable Component component2)
		{
			this(component, nullToEmpty(component2), Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(component), component2 == null ? 0 : Minecraft.getInstance().font.width(component2))));
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
		public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l)
		{
			if(this.changed)
			{
				this.lastChanged = l;
				this.changed = false;
			}
			
			RenderSystem.setShaderTexture(0, TEXTURE);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			int i = this.width();
			int j;
			if(i == 160 && this.messageLines.size() <= 1)
			{
				toastComponent.blit(poseStack, 0, 0, 0, 64, i, this.height());
			} else
			{
				j = this.height();
				int m = Math.min(4, j - 28);
				this.renderBackgroundRow(poseStack, toastComponent, i, 0, 0, 28);
				
				for(int n = 28; n < j - m; n += 10)
				{
					this.renderBackgroundRow(poseStack, toastComponent, i, 16, n, Math.min(16, j - n - m));
				}
				
				this.renderBackgroundRow(poseStack, toastComponent, i, 32 - m, j - m, m);
			}
			
			if(this.messageLines == null)
			{
				toastComponent.getMinecraft().font.draw(poseStack, this.title, 18.0F, 12.0F, -256);
			} else
			{
				toastComponent.getMinecraft().font.draw(poseStack, this.title, 18.0F, 7.0F, -256);
				
				for(j = 0; j < this.messageLines.size(); ++j)
				{
					toastComponent.getMinecraft().font.draw(poseStack, this.messageLines.get(j), 18.0F, (float) (18 + j * 12), -1);
				}
			}
			
			return l - this.lastChanged < 5000L ? Visibility.SHOW : Visibility.HIDE;
		}
		
		private void renderBackgroundRow(PoseStack poseStack, ToastComponent toastComponent, int i, int j, int k, int l)
		{
			int m = j == 0 ? 20 : 5;
			int n = Math.min(60, i - m);
			toastComponent.blit(poseStack, 0, k, 0, 64 + j, m, l);
			
			for(int o = m; o < i - n; o += 64)
			{
				toastComponent.blit(poseStack, o, k, 32, 64 + j, Math.min(64, i - o - n), l);
			}
			
			toastComponent.blit(poseStack, i - n, k, 160 - n, 64 + j, n, l);
		}
	}
}
