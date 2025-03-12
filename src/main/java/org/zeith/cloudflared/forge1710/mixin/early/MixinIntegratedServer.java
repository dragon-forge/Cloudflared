package org.zeith.cloudflared.forge1710.mixin.early;

import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.cloudflared.forge1710.proxy.ClientProxy;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(IntegratedServer.class)
public class MixinIntegratedServer {

    @Inject(
        method = "shareToLAN",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/integrated/IntegratedServer;func_147137_ag()Lnet/minecraft/network/NetworkSystem;",
            shift = At.Shift.BEFORE))
    private void patchShareToLAN(WorldSettings.GameType type, boolean allowCheats, CallbackInfoReturnable<String> cir,
        @Local(ordinal = 0) int i) {
        ClientProxy.onSharedToLan((IntegratedServer) (Object) this, i);

    }
}
