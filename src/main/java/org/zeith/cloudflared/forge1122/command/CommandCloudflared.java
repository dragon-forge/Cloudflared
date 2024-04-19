package org.zeith.cloudflared.forge1122.command;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.*;
import net.minecraftforge.server.command.CommandTreeBase;
import org.zeith.cloudflared.core.CloudflaredAPI;
import org.zeith.cloudflared.core.util.*;
import org.zeith.cloudflared.forge1122.CloudflaredForge;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CommandCloudflared
		extends CommandTreeBase
{
	public CommandCloudflared()
	{
		addSubcommand(new Install());
	}
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		if(server.isDedicatedServer()) return sender instanceof MinecraftServer;
		return Objects.equals(server.getServerOwner(),
				sender instanceof EntityPlayer ? ((EntityPlayer) sender).getGameProfile().getName() : "-"
		);
	}
	
	@Override
	public String getName()
	{
		return "cloudflared";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "Access of cloudflared commands";
	}
	
	class Install
			extends CommandBase
	{
		@Override
		public boolean checkPermission(MinecraftServer server, ICommandSender sender)
		{
			return CommandCloudflared.this.checkPermission(server, sender);
		}
		
		@Override
		public String getName()
		{
			return "install";
		}
		
		@Override
		public String getUsage(ICommandSender sender)
		{
			return "Installs cloudflared if it isn't installed.";
		}
		
		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args)
				throws CommandException
		{
			if(CloudflaredForge.PROXY.getApi().filter(a -> a.getExecutableFilePath().isFile()).isPresent())
				throw new CommandException("command.cloudflared:install.installed");
			
			CompletableFuture<Integer> wg = CloudflaredUtils.download(CloudflaredForge.PROXY);
			
			if(wg.isDone() && wg.join() == null)
				throw new CommandException("command.cloudflared:install.unsupported_os",
						new TextComponentString(OSArch.getArchitecture().getType() + " (" + OSArch.getInstructions() + ")"),
						new TextComponentTranslation("chat.cloudflared:here")
								.setStyle(new Style()
										.setColor(TextFormatting.BLUE)
										.setUnderlined(true)
										.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("chat.cloudflared:open_url")))
										.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mcdoc.zeith.org/docs/cloudflared/download"))
								)
				);
			
			sender.sendMessage(new TextComponentTranslation("command.cloudflared:install.started"));
			
			wg.thenAccept(i ->
			{
				sender.sendMessage(new TextComponentTranslation("command.cloudflared:install.install_done", i));
				
				new Thread(() ->
				{
					CloudflaredForge.PROXY.tryCreateApi();
					
					Optional<CloudflaredAPI> api = CloudflaredForge.PROXY.getApi();
					if(api.isPresent())
					{
						sender.sendMessage(new TextComponentTranslation("command.cloudflared:install.ok",
								new TextComponentString(api.map(CloudflaredAPI::getVersion).map(CloudflaredVersion::toString).orElse("NOT FOUND"))
						));
						return;
					}
					
					if(i == 0)
					{
						sender.sendMessage(new TextComponentTranslation("command.cloudflared:install.restart"));
						return;
					}
					
					sender.sendMessage(new TextComponentTranslation("command.cloudflared:install.nope",
							new TextComponentTranslation("chat.cloudflared:here")
									.setStyle(new Style()
											.setColor(TextFormatting.BLUE)
											.setUnderlined(true)
											.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("chat.cloudflared:open_url")))
											.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mcdoc.zeith.org/docs/cloudflared/download"))
									)
					));
				}).start();
			});
		}
	}
}