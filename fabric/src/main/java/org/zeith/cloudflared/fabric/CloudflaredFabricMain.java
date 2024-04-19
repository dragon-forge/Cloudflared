package org.zeith.cloudflared.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.zeith.cloudflared.CloudflaredMod;

public class CloudflaredFabricMain
		implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		ServerLifecycleEvents.SERVER_STARTING.register(server -> CloudflaredMod.PROXY.serverStarting(server));
		ServerLifecycleEvents.SERVER_STARTED.register(server -> CloudflaredMod.PROXY.serverStarted(server));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> CloudflaredMod.PROXY.serverStop());
	}
}