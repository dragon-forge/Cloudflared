package org.zeith.cloudflared.forge1710.mixin.early;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.cloudflared.forge1710.proxy.ClientProxy;

@Mixin(IntegratedServer.class)
public class MixinIntegratedServer
{
	@Inject(
			method = "shareToLAN",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/integrated/IntegratedServer;func_147137_ag()Lnet/minecraft/network/NetworkSystem;"
			)
	)
	private void patchShareToLAN(WorldSettings.GameType type, boolean allowCheats, CallbackInfoReturnable<String> cir, @Local(ordinal = 0) int i)
	{
		ClientProxy.onSharedToLan((IntegratedServer) (Object) this, i);
	}
}