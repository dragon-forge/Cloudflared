package org.zeith.cloudflared.mixin.client;

import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.cloudflared.CloudflaredMod;
import org.zeith.cloudflared.core.process.CFDAccess;

@Mixin(ServerAddress.class)
public class ServerAddressMixin
{
	@Inject(
			method = "parseString",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void Cloudflared_parseString(String string, CallbackInfoReturnable<ServerAddress> cir)
	{
		var addr = decodeAddress(string);
		if(addr != null) cir.setReturnValue(addr);
	}
	
	@Inject(
			method = "isValidAddress",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void Cloudflared_isValidAddress(String string, CallbackInfoReturnable<Boolean> cir)
	{
		var addr = decodeAddress(string);
		if(addr != null) cir.setReturnValue(true);
	}
	
	private static ServerAddress decodeAddress(String input)
	{
		if(!input.startsWith("cloudflared://")) return null;
		
		String hostname = input.substring(14);
		
		CFDAccess tunnel = CloudflaredMod.PROXY
				.getApi()
				.map(a -> a.getOrOpenAccess(hostname))
				.orElse(null);
		
		if(tunnel == null) return null;
		
		int openPort = tunnel.getOpenFuture().join();
		
		return ServerAddress.parseString("127.0.0.1:" + openPort);
	}
}