package org.zeith.cloudflared.forge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.architectury.IMessageConsumer;
import org.zeith.cloudflared.proxy.ClientProxy;

public class CloudflaredForgeClient
{
	private final IMessageConsumer.ForClient messages = new IMessageConsumer.ForClient();
	
	static void setup(IEventBus bus)
	{
		CloudflaredForgeClient client = new CloudflaredForgeClient();
		CloudflaredMod.PROXY = new ClientProxy(client.messages);
		NeoForge.EVENT_BUS.addListener(client::tickEvent);
	}
	
	private void tickEvent(ClientTickEvent.Pre e)
	{
		messages.clientTick();
	}
}