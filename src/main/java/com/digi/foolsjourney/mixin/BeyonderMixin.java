package com.digi.foolsjourney.mixin;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
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

    @Override
    public int getSequence() { return this.sequence; }

    @Override
    public void setSequence(int sequence) { this.sequence = sequence; }

    @Override
    public double getSpirituality() { return this.spirituality; }

    @Override
    public void setSpirituality(double spirituality) { this.spirituality = spirituality; }

    @Override
    public boolean isSpiritVisionActive() { return this.spiritVisionActive; }

    @Override
    public void setSpiritVision(boolean active) {
        this.spiritVisionActive = active;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("lotm_sequence", this.sequence);
        nbt.putDouble("lotm_spirituality", this.spirituality);
        nbt.putBoolean("lotm_spirit_vision", this.spiritVisionActive);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if(nbt.contains("lotm_sequence")) this.sequence = nbt.getInt("lotm_sequence");
        if(nbt.contains("lotm_spirituality")) this.spirituality = nbt.getDouble("lotm_spirituality");
        if(nbt.contains("lotm_spirit_vision")) this.spiritVisionActive = nbt.getBoolean("lotm_spirit_vision");
    }
}