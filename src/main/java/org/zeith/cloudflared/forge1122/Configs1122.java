package org.zeith.cloudflared.forge1122;

import com.zeitheron.hammercore.cfg.*;
import com.zeitheron.hammercore.cfg.fields.*;
import net.minecraftforge.common.config.Configuration;

@HCModConfigurations(modid = "cloudflared")
public class Configs1122
		implements IConfigReloadListener
{
	public static Configs1122 configInstance;
	public static Configuration cfg;
	
	@ModConfigPropertyInt(
			name = "Custom Port Override",
			category = "Hosting",
			defaultValue = 0,
			min = 0,
			max = 65534,
			comment = "Which port should be forced when opening world to LAN? Keep at 0 to retain Vanilla behavior."
	)
	public static int customPortOverride;
	
	@ModConfigPropertyBool(
			name = "Enable PvP",
			category = "Hosting",
			defaultValue = true,
			comment = "Should PvP be enabled on the shared to LAN server?"
	)
	public static boolean enablePvP;
	
	@ModConfigPropertyBool(
			name = "Online Mode",
			category = "Hosting",
			defaultValue = true,
			comment = "Should online mode be active when hosting a LAN server?"
	)
	public static boolean onlineMode;
	
	@ModConfigPropertyBool(
			name = "Can Spawn Animals",
			category = "Hosting",
			defaultValue = true,
			comment = "Should animals be allowed to spawn on a hosted a LAN server?"
	)
	public static boolean canSpawnAnimals;
	
	@ModConfigPropertyBool(
			name = "Can Spawn NPCs",
			category = "Hosting",
			defaultValue = true,
			comment = "Should NPCs be allowed to spawn on a hosted a LAN server?"
	)
	public static boolean canSpawnNPCs;
	
	@ModConfigPropertyString(
			name = "Cloudflare Hostname",
			category = "Network: Advanced",
			defaultValue = "",
			allowedValues = { },
			comment = "Which host should the cloudflared tunnel be configured to?\nIf your cloudflared is not authorized, this won't work."
	)
	public static String hostname = "";
	
	@ModConfigPropertyString(
			name = "Cloudflared Executable",
			category = "Local Install",
			defaultValue = "cloudflared",
			allowedValues = { },
			comment = "Which file should be used for executing commands?"
	)
	public static String executable = "cloudflared";
	
	@ModConfigPropertyBool(
			name = "Auto-Download Cloudflared",
			category = "Local Install",
			defaultValue = false,
			comment = "Should the mod attempt auto-download Cloudflared?\nGenerally not recommended, but is available for Windows and MacOS."
	)
	public static boolean autodownload;
	
	@ModConfigPropertyBool(
			name = "Start Tunnel",
			category = "Hosting",
			defaultValue = true,
			comment = "Should Argo Tunnel be started whenever the hosting session starts?"
	)
	public static boolean startTunnel = true;
	
	public Configs1122()
	{
		configInstance = this;
	}
	
	public static Configs1122 get()
	{
		return configInstance;
	}
	
	private static long lastReload = System.currentTimeMillis();
	
	@Override
	public void reloadCustom(Configuration cfgs)
	{
		cfg = cfgs;
		if(System.currentTimeMillis() - lastReload < 1000L) return;
		CloudflaredForge.LOG.info("Reloaded configs.");
		if(CloudflaredForge.PROXY.getApi().isPresent()) return;
		CloudflaredForge.PROXY.tryCreateApi();
		lastReload = System.currentTimeMillis();
	}
}