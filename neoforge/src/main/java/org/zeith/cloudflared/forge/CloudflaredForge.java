package org.zeith.cloudflared.forge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforgespi.Environment;
import org.zeith.cloudflared.CloudflaredMod;

@Mod(CloudflaredMod.MOD_ID)
public class CloudflaredForge
{
	public CloudflaredForge(IEventBus bus)
	{
		NeoForge.EVENT_BUS.addListener(this::serverStarting);
		NeoForge.EVENT_BUS.addListener(this::serverStarted);
		NeoForge.EVENT_BUS.addListener(this::serverStopped);
		
		if(Environment.get().getDist() == Dist.CLIENT)
		{
			CloudflaredForgeClient.setup(bus);
		} else
		{
			CloudflaredForgeServer.setup();
		}
		
		CloudflaredMod.init(FMLPaths.CONFIGDIR.get().resolve("cloudflared.cfg"));
	}
	
	private void serverStarting(ServerStartingEvent e)
	{
		CloudflaredMod.PROXY.serverStarting(e.getServer());
	}
	
	private void serverStarted(ServerStartedEvent e)
	{
		CloudflaredMod.PROXY.serverStarted(e.getServer());
	}
	
	private void serverStopped(ServerStoppingEvent e)
	{
		CloudflaredMod.PROXY.serverStop();
	}
}