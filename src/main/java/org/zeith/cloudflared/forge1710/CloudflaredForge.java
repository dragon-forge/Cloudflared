package org.zeith.cloudflared.forge1710;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.*;
import org.apache.logging.log4j.*;
import org.zeith.cloudflared.forge1710.proxy.CommonProxy;

@Mod(modid = "cloudflared", version = "1.0.0", name = "CloudflaredForge", acceptedMinecraftVersions = "[1.7.10]")
public class CloudflaredForge
{
	public static final Logger LOG = LogManager.getLogger("CloudflaredAPI/Mod");
	
	@SidedProxy(
			clientSide = "org.zeith.cloudflared.forge1710.proxy.ClientProxy",
			serverSide = "org.zeith.cloudflared.forge1710.proxy.ServerProxy")
	public static CommonProxy PROXY;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		PROXY.preInit(event);
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		PROXY.serverStarting(event);
	}
	
	@Mod.EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent event)
	{
		PROXY.serverStarted(event);
	}
	
	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent event)
	{
		PROXY.serverStop(event);
	}
}
