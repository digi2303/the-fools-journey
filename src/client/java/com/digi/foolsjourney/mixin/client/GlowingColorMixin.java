package com.digi.foolsjourney.mixin.client;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class GlowingColorMixin {

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    public void getTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) return;

        Entity entity = (Entity) (Object) this;

        if (entity == client.player) return;

        if (!(entity instanceof LivingEntity)) return;

        if (client.player instanceof IBeyonder beyonder && beyonder.isSpiritVisionActive()) {

            if (client.player.distanceTo(entity) < 30.0) {

                if (entity instanceof HostileEntity) {
                    cir.setReturnValue(0xFF0000);
                    return;
                }

                if (entity instanceof AnimalEntity ||
                        entity instanceof VillagerEntity ||
                        entity instanceof WaterCreatureEntity ||
                        entity instanceof GolemEntity ||
                        entity instanceof AmbientEntity) {

                    cir.setReturnValue(0x55FF55);
                    return;
                }

                if (entity instanceof PlayerEntity) {
                    cir.setReturnValue(0xA020F0);
                    return;
                }

                cir.setReturnValue(0xFFFFFF);
            }
        }
    }
}