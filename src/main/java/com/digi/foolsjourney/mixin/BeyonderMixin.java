package com.digi.foolsjourney.mixin;

import com.digi.foolsjourney.networking.BeyonderSyncPayload;
import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class BeyonderMixin extends LivingEntity implements IBeyonder {

    protected BeyonderMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique private int sequence = -1;
    @Unique private double spirituality = 0;
    @Unique private boolean spiritVisionActive = false;
    @Unique private int abilityCooldown = 0;

    @SuppressWarnings("AddedMixinMembers")
    @Override
    public void syncBeyonderData() {
        if (this.getWorld().isClient) return;

        if ((Object)this instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new BeyonderSyncPayload(
                    this.sequence,
                    this.spirituality,
                    this.spiritVisionActive,
                    this.abilityCooldown
            ));
        }
    }

    @Override public int getSequence() { return this.sequence; }

    @Override
    public void setSequence(int sequence) {
        this.sequence = sequence;
        syncBeyonderData();
    }

    @Override public double getSpirituality() { return this.spirituality; }

    @Override
    public void setSpirituality(double spirituality) {
        this.spirituality = spirituality;
        syncBeyonderData();
    }

    @Override public boolean isSpiritVisionActive() { return this.spiritVisionActive; }

    @Override
    public void setSpiritVision(boolean active) {
        this.spiritVisionActive = active;
        if (!active) {
            this.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        syncBeyonderData();
    }

    @Override public int getCooldown() { return this.abilityCooldown; }

    @Override
    public void setCooldown(int ticks) {
        this.abilityCooldown = ticks;
        syncBeyonderData();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.getWorld().isClient) return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        boolean isCreative = player.getAbilities().creativeMode;
        boolean dataChanged = false;

        if (this.abilityCooldown > 0) {
            this.abilityCooldown--;
        }

        if (this.spiritVisionActive) {
            if (!this.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0, false, false, false));
            }

            if (!isCreative) {
                if (this.spirituality > 0) {
                    this.spirituality -= 1.0;
                    dataChanged = true;
                } else {
                    this.spirituality = 0;
                    this.setSpiritVision(false);
                    this.abilityCooldown = 100;
                    player.sendMessage(Text.of("§c[LotM] Mana drained!"), true);
                    dataChanged = true;
                }
            }
        }
        else {
            if (this.spirituality < 100.0) {
                this.spirituality += 0.5;
                if (this.spirituality > 100.0) this.spirituality = 100.0;
                dataChanged = true;
            }
        }

        if (dataChanged) {
            syncBeyonderData();
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("lotm_sequence", this.sequence);
        nbt.putDouble("lotm_spirituality", this.spirituality);
        nbt.putBoolean("lotm_spirit_vision", this.spiritVisionActive);
        nbt.putInt("lotm_cooldown", this.abilityCooldown);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if(nbt.contains("lotm_sequence")) this.sequence = nbt.getInt("lotm_sequence");
        if(nbt.contains("lotm_spirituality")) this.spirituality = nbt.getDouble("lotm_spirituality");
        if(nbt.contains("lotm_spirit_vision")) this.spiritVisionActive = nbt.getBoolean("lotm_spirit_vision");
        if(nbt.contains("lotm_cooldown")) this.abilityCooldown = nbt.getInt("lotm_cooldown");
    }
}