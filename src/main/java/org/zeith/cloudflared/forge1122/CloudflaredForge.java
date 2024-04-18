package org.zeith.cloudflared.forge1122;

import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.*;
import org.zeith.cloudflared.forge1122.proxy.CommonProxy1122;

@Mod(
		modid = "cloudflared",
		version = "@VERSION@",
		name = "Cloudflared",
		guiFactory = "org.zeith.cloudflared.forge1122.ConfigFactory",
		certificateFingerprint = "9f5e2a811a8332a842b34f6967b7db0ac4f24856",
		updateJSON = "https://api.modrinth.com/updates/PlkSuVtM/forge_updates.json",
		acceptedMinecraftVersions = "[1.12.2]",
		dependencies = "required-after:hammercore"
)
public class CloudflaredForge
{
	public static final Logger LOG = LogManager.getLogger("CloudflaredAPI/Mod");
	
	@SidedProxy(clientSide = "org.zeith.cloudflared.forge1122.proxy.ClientProxy1122", serverSide = "org.zeith.cloudflared.forge1122.proxy.ServerProxy1122")
	public static CommonProxy1122 PROXY;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		PROXY.preInit(e);
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent e)
	{
		PROXY.serverStarting(e);
	}
	
	@Mod.EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent e)
	{
		PROXY.serverStarted(e);
	}
	
	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent e)
	{
		PROXY.serverStop(e);
	}
}