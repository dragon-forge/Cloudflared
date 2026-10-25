package org.zeith.cloudflared.forge1710.command;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import org.zeith.cloudflared.core.util.*;
import org.zeith.cloudflared.forge1710.CloudflaredForge;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CommandCloudflared
		extends CommandBase
{
	public int getRequiredPermissionLevel()
	{
		return 0;
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		MinecraftServer server = MinecraftServer.getServer();
		if(server == null)
		{
			return false;
		}
		if(server.isDedicatedServer()) return sender instanceof MinecraftServer;
		return Objects.equals(
				server.getServerOwner(),
				(sender instanceof EntityPlayer ep) ? ep.getGameProfile().getName() : "-"
		);
	}
	
	@Override
	public String getCommandName()
	{
		return "cloudflared";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "Access of cloudflared commands";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if(args.length == 0 || !args[0].equalsIgnoreCase("install"))
		{
			sender.addChatMessage(new ChatComponentText("Usage: /cloudflared install"));
			return;
		}
		if(CloudflaredForge.PROXY.getApi().filter(a -> a.getExecutableFilePath().isFile()).isPresent())
		{
			throw new CommandException("command.cloudflared:install.installed");
		}
		CompletableFuture<Integer> wg = CloudflaredUtils.download(CloudflaredForge.PROXY);
		if(wg.isDone() && wg.join() == null)
		{
			throw new CommandException(
					"command.cloudflared:install.unsupported_os",
					new ChatComponentText(
							OSArch.getArchitecture()
							      .getType() + " ("
							+ OSArch.getInstructions()
							+ ")"),
					(new ChatComponentTranslation("chat.cloudflared:here")).setChatStyle(
							(new ChatStyle()).setColor(EnumChatFormatting.BLUE)
							                 .setUnderlined(true)
							                 .setChatHoverEvent(
													 new HoverEvent(
															 HoverEvent.Action.SHOW_TEXT,
															 new ChatComponentTranslation("chat.cloudflared:open_url")
													 ))
							                 .setChatClickEvent(
													 new ClickEvent(
															 ClickEvent.Action.OPEN_URL,
															 "https://mcdoc.zeith.org/docs/cloudflared/download"
													 )))
			);
		}
		sender.addChatMessage(new ChatComponentTranslation("command.cloudflared:install.started"));
		wg.thenAccept(i ->
				sender.addChatMessage(new ChatComponentTranslation("command.cloudflared:install.install_done", i))
		);
	}
}