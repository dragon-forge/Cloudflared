package org.zeith.cloudflared.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.proxy.ServerProxy;

public class CloudflaredFabricServer
		implements DedicatedServerModInitializer
{
	@Override
	public void onInitializeServer()
	{
		CloudflaredMod.PROXY = new ServerProxy();
		CloudflaredMod.init(FabricLoader.getInstance().getConfigDir().resolve("cloudflared.cfg"));
	}
}