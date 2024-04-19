package org.zeith.cloudflared.mixin.client;

import net.minecraft.util.HttpUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.cloudflared.CloudflaredConfig;

@Mixin(HttpUtil.class)
public class HttpUtilMixin
{
	@Inject(
			method = "getAvailablePort",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void Cloudflared_getAvailablePort(CallbackInfoReturnable<Integer> cir)
	{
		int customPortOverride = CloudflaredConfig.customPortOverride;
		if(customPortOverride > 0 && customPortOverride < 65535)
			cir.setReturnValue(customPortOverride);
	}
}