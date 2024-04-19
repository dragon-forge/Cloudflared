package org.zeith.cloudflared.architectury;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface IMessageConsumer
{
	void chat(Component message);
	
	class ForClient
			implements IMessageConsumer
	{
		private final List<Component> messages = new ArrayList<>();
		
		public void clientTick()
		{
			var mc = Minecraft.getInstance();
			if(mc.level != null)
			{
				while(!messages.isEmpty())
				{
					mc.getChatListener().handleSystemMessage(messages.remove(0), false);
				}
			}
		}
		
		@Override
		public void chat(Component message)
		{
		
		}
	}
}