package net.lunix.pvptoggle.mixin;

import net.lunix.pvptoggle.PvpToggle;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class DedicatedServerMixin {

    @Inject(method = "isPvpAllowed", at = @At("HEAD"), cancellable = true)
    private void pvpToggle_isPvpAllowed(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(PvpToggle.pvpEnabled);
    }
}
