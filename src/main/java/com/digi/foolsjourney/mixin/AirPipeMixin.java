package com.digi.foolsjourney.mixin;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class AirPipeMixin extends LivingEntity {

    protected AirPipeMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void airPipeTick(CallbackInfo ci) {
        if (this.getWorld().isClient) return;

        if (this.isSubmergedInWater() && this.getAir() <= 0) {

            if (this instanceof IBeyonder beyonder) {

                if (beyonder.getSequence() != -1 && beyonder.getSequence() <= 7) {
                    double manaCostPerTick = 0.75;

                    if (beyonder.getSpirituality() >= manaCostPerTick) {
                        this.setAir(0);

                        PlayerEntity player = (PlayerEntity) (Object) this;
                        if (!player.isCreative()) {
                            beyonder.setSpirituality(beyonder.getSpirituality() - manaCostPerTick);
                            if (this.age % 5 == 0) {
                                beyonder.syncBeyonderData();
                            }
                        }
                    }
                }
            }
        }
    }
}