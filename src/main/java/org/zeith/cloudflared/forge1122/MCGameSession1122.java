package org.zeith.cloudflared.forge1122;

import com.zeitheron.hammercore.utils.base.Cast;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.*;
import org.zeith.cloudflared.core.api.MCGameSession;
import org.zeith.cloudflared.core.process.CFDTunnel;

import java.util.UUID;

public class MCGameSession1122
		extends MCGameSession
{
	protected final ICommandSender owner;
	
	public MCGameSession1122(int serverPort, UUID host, ICommandSender owner)
	{
		super(serverPort, host);
		this.owner = owner;
	}
	
	@Override
	public void onTunnelOpen(CFDTunnel tunnel)
	{
		String hostnameSTR = Cast.or(tunnel.getGeneratedHostname(), tunnel.getApi().getConfigs().getHostname().get());
		if(hostnameSTR != null && hostnameSTR.isEmpty()) hostnameSTR = null;
		
		if(hostnameSTR == null)
		{
			owner.sendMessage(new TextComponentTranslation("chat.cloudflared:tunnel_open_unknown"));
			return;
		}
		
		if(hostnameSTR.contains("://"))
		{
			hostnameSTR = "cloudflared://" + hostnameSTR.substring(hostnameSTR.indexOf("://") + 3);
		}
		
		ITextComponent hostname = new TextComponentString(hostnameSTR)
				.setStyle(new Style()
						.setColor(TextFormatting.BLUE)
						.setUnderlined(true)
						.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, hostnameSTR))
						.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("chat.cloudflared:click_to_suggest")))
				);
		
		owner.sendMessage(new TextComponentTranslation("chat.cloudflared:tunnel_open", hostname));
		CloudflaredForge.LOG.warn("Game tunnel open: {}", hostnameSTR);
	}
	
	@Override
	public void onTunnelClosed(CFDTunnel tunnel)
	{
		String hostnameSTR = Cast.or(tunnel.getGeneratedHostname(), tunnel.getApi().getConfigs().getHostname().get());
		if(hostnameSTR != null && hostnameSTR.isEmpty()) hostnameSTR = null;
		
		if(hostnameSTR != null && hostnameSTR.contains("://"))
			hostnameSTR = "cloudflared://" + hostnameSTR.substring(hostnameSTR.indexOf("://") + 3);
		
		CloudflaredForge.LOG.warn("Game tunnel closed: {}", hostnameSTR);
	}
}