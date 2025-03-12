package org.zeith.cloudflared.forge1710.mixin.early;

import net.minecraft.util.HttpUtil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HttpUtil.class)
public class MixinHttpUtil {

    @Inject(method = "func_76181_a", at = @At("HEAD"), cancellable = true)
    private static void patchGetSuitableLanPort(CallbackInfoReturnable<Integer> cir) {
        Integer port = org.zeith.cloudflared.forge1710.proxy.ClientProxy.pickPort();
        if (port != null) {
            cir.setReturnValue(port);
            cir.cancel();
        }
    }
}
