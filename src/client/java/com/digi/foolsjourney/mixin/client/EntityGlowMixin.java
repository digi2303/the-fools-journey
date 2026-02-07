package com.digi.foolsjourney.mixin.client;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityGlowMixin {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    public void isGlowing(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) return;

        Entity entity = (Entity) (Object) this;
        if (entity == client.player) return;

        if (!(entity instanceof LivingEntity)) return;

        if (client.player instanceof IBeyonder beyonder && beyonder.isSpiritVisionActive()) {
            if (client.player.distanceTo(entity) < 30.0) {
                cir.setReturnValue(true);
            }
        }
    }
}