package org.zeith.cloudflared.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.architectury.IMessageConsumer;
import org.zeith.cloudflared.proxy.ClientProxy;

public class CloudflaredForgeClient
{
	private final IMessageConsumer.ForClient messages = new IMessageConsumer.ForClient();
	
	static void setup()
	{
		CloudflaredForgeClient client = new CloudflaredForgeClient();
		CloudflaredMod.PROXY = new ClientProxy(client.messages);
		MinecraftForge.EVENT_BUS.addListener(client::tickEvent);
	}
	
	private void tickEvent(TickEvent.ClientTickEvent e)
	{
		messages.clientTick();
	}
}