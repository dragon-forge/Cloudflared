package org.zeith.cloudflared.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.zeith.cloudflared.CloudflaredMod;

@Mod(CloudflaredMod.MOD_ID)
public class CloudflaredForge
{
	public CloudflaredForge()
	{
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
		
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> CloudflaredForgeClient::setup);
		DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> CloudflaredForgeServer::setup);
		
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