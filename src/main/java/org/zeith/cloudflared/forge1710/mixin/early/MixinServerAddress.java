package org.zeith.cloudflared.forge1710.mixin.early;

import net.minecraft.client.multiplayer.ServerAddress;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.cloudflared.forge1710.proxy.ClientProxy;

@Mixin(ServerAddress.class)
public class MixinServerAddress {

    @Inject(method = "func_78860_a", at = @At("HEAD"), cancellable = true)
    private static void patchFromString(String addrString, CallbackInfoReturnable<ServerAddress> cir) {
        ServerAddress address = ClientProxy.decodeAddress(addrString);
        if (address != null) {
            cir.setReturnValue(address);
            cir.cancel();
        }
    }
}
