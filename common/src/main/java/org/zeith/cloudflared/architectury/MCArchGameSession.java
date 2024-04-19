package org.zeith.cloudflared.architectury;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.*;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.core.api.MCGameSession;
import org.zeith.cloudflared.core.process.CFDTunnel;

import java.util.UUID;

public class MCArchGameSession
		extends MCGameSession
{
	protected final CommandSource owner;
	
	public MCArchGameSession(int serverPort, UUID host, CommandSource owner)
	{
		super(serverPort, host);
		this.owner = owner;
	}
	
	@Override
	public void onTunnelOpen(CFDTunnel tunnel)
	{
		String hostnameSTR = tunnel.getGeneratedHostname();
		if(hostnameSTR == null) hostnameSTR = tunnel.getApi().getConfigs().getHostname().get();
		if(hostnameSTR != null && hostnameSTR.isEmpty()) hostnameSTR = null;
		
		if(hostnameSTR == null)
		{
			var txt = Component.translatable("chat.cloudflared:game_logs")
					.setStyle(Style.EMPTY
							.withColor(ChatFormatting.BLUE)
							.withUnderlined(true)
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.cloudflared:click_to_open")))
							.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, CloudflaredMod.PROXY.getLatestLogFile().getAbsolutePath()))
					);
			owner.sendSystemMessage(Component.translatable("chat.cloudflared:tunnel_open_unknown", txt));
			return;
		}
		
		if(hostnameSTR.contains("://"))
		{
			hostnameSTR = "cloudflared://" + hostnameSTR.substring(hostnameSTR.indexOf("://") + 3);
		}
		
		var hostname = Component.literal(hostnameSTR)
				.setStyle(Style.EMPTY
						.withColor(ChatFormatting.BLUE)
						.withUnderlined(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, hostnameSTR))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.cloudflared:click_to_copy")))
				);
		
		owner.sendSystemMessage(Component.translatable("chat.cloudflared:tunnel_open", hostname));
		CloudflaredMod.LOG.warn("Game tunnel open: {}", hostnameSTR);
	}
	
	@Override
	public void onTunnelClosed(CFDTunnel tunnel)
	{
		String hostnameSTR = tunnel.getGeneratedHostname();
		if(hostnameSTR == null) hostnameSTR = tunnel.getApi().getConfigs().getHostname().get();
		if(hostnameSTR != null && hostnameSTR.isEmpty()) hostnameSTR = null;
		
		if(hostnameSTR != null && hostnameSTR.contains("://"))
			hostnameSTR = "cloudflared://" + hostnameSTR.substring(hostnameSTR.indexOf("://") + 3);
		
		CloudflaredMod.LOG.warn("Game tunnel closed: {}", hostnameSTR);
	}
}