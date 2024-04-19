package org.zeith.cloudflared.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.architectury.IMessageConsumer;
import org.zeith.cloudflared.proxy.ClientProxy;

public class CloudflaredFabricClient
		implements ClientModInitializer
{
	public final IMessageConsumer.ForClient messages = new IMessageConsumer.ForClient();
	
	@Override
	public void onInitializeClient()
	{
		CloudflaredMod.PROXY = new ClientProxy(messages);
		ClientTickEvents.START_CLIENT_TICK.register(client -> messages.clientTick());
		
		CloudflaredMod.init();
	}
}