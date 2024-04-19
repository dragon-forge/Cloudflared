package org.zeith.cloudflared.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.cloudflared.CloudflaredConfig;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.architectury.MCArchGameSession;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin
{
	@Shadow
	@Final
	private Minecraft minecraft;
	
	@Inject(
			method = "publishServer",
			at = @At("HEAD")
	)
	private void Cloudflared_publishServer(GameType gameType, boolean cheats, int port, CallbackInfoReturnable<Boolean> cir)
	{
		if(CloudflaredConfig.startTunnel)
		{
			minecraft.gui.getChat().addMessage(Component.translatable("chat.cloudflared:starting_tunnel"));
			CloudflaredMod.PROXY.startSession(new MCArchGameSession(port, minecraft.player.getUUID(), minecraft.player));
		}
	}
}