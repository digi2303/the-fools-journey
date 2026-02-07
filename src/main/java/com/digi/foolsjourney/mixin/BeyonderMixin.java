package com.digi.foolsjourney.mixin;

import com.digi.foolsjourney.networking.BeyonderSyncPayload;
import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class BeyonderMixin extends LivingEntity implements IBeyonder {

    protected BeyonderMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique private int foolsjourney$sequence = -1;
    @Unique private double foolsjourney$spirituality = 0;
    @Unique private boolean foolsjourney$spiritVisionActive = false;
    @Unique private int foolsjourney$abilityCooldown = 0;
    @Unique private long foolsjourney$lastDangerWarningTime = 0;

    @Unique private final Set<UUID> foolsjourney$knownThreats = new HashSet<>();

    @SuppressWarnings("AddedMixinMembers")
    @Override
    public void syncBeyonderData() {
        if (this.getWorld().isClient) return;

        if ((Object)this instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new BeyonderSyncPayload(
                    this.foolsjourney$sequence,
                    this.foolsjourney$spirituality,
                    this.foolsjourney$spiritVisionActive,
                    this.foolsjourney$abilityCooldown
            ));
        }
    }

    @Override public int getSequence() { return this.foolsjourney$sequence; }

    @Override
    public void setSequence(int sequence) {
        this.foolsjourney$sequence = sequence;
        syncBeyonderData();
    }

    @Override public double getSpirituality() { return this.foolsjourney$spirituality; }

    @Override
    public void setSpirituality(double spirituality) {
        this.foolsjourney$spirituality = spirituality;
        syncBeyonderData();
    }

    @Override public boolean isSpiritVisionActive() { return this.foolsjourney$spiritVisionActive; }

    @Override
    public void setSpiritVision(boolean active) {
        this.foolsjourney$spiritVisionActive = active;
        syncBeyonderData();
    }

    @Override public int getCooldown() { return this.foolsjourney$abilityCooldown; }

    @Override
    public void setCooldown(int ticks) {
        this.foolsjourney$abilityCooldown = ticks;
        syncBeyonderData();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.getWorld().isClient) return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        boolean isCreative = player.getAbilities().creativeMode;
        boolean dataChanged = false;

        if (this.foolsjourney$abilityCooldown > 0) {
            this.foolsjourney$abilityCooldown--;
        }

        long currentTime = System.currentTimeMillis();

        if (this.foolsjourney$sequence != -1 && (currentTime - this.foolsjourney$lastDangerWarningTime > 5000) && this.age % 10 == 0) {

            Box dangerZone = this.getBoundingBox().expand(10.0);

            List<HostileEntity> threats = this.getWorld().getEntitiesByClass(HostileEntity.class, dangerZone, entity -> entity.getTarget() == player);

            boolean dangerDetected = false;

            for (HostileEntity threat : threats) {
                Vec3d lookVec = player.getRotationVec(1.0F).normalize();
                Vec3d toEntityVec = threat.getPos().subtract(player.getPos()).normalize();
                double dot = lookVec.dotProduct(toEntityVec);

                if (dot > 0.5) {
                    this.foolsjourney$knownThreats.add(threat.getUuid());
                    continue;
                }

                if (this.foolsjourney$knownThreats.contains(threat.getUuid())) {
                    continue;
                }

                dangerDetected = true;
                break;
            }

            if (dangerDetected) {
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.WITCH,
                            player.getX(), player.getY() + 1.5, player.getZ(),
                            10,
                            0.5, 0.5, 0.5,
                            0.1
                    );
                }

                this.foolsjourney$lastDangerWarningTime = currentTime;
            }
        }

        if (this.age % 200 == 0) {
            this.foolsjourney$knownThreats.clear();
        }

        if (this.foolsjourney$spiritVisionActive) {
            StatusEffectInstance currentNv = this.getStatusEffect(StatusEffects.NIGHT_VISION);

            if (currentNv == null || (currentNv.getDuration() != -1 && currentNv.getDuration() < 300)) {
                this.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, -1, 0, false, false, false
                ));
            }

            if (!isCreative) {
                if (this.foolsjourney$spirituality > 0) {
                    this.foolsjourney$spirituality -= 1.0;
                    dataChanged = true;
                } else {
                    this.foolsjourney$spirituality = 0;
                    this.setSpiritVision(false);
                    this.foolsjourney$abilityCooldown = 100;
                    player.sendMessage(Text.of("§c[LotM] Mana drained!"), true);
                    dataChanged = true;
                }
            }
        }
        else {
            StatusEffectInstance currentNv = this.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (currentNv != null && currentNv.getDuration() == -1) {
                this.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }

            if (this.foolsjourney$spirituality < 100.0) {
                this.foolsjourney$spirituality += 0.5;
                if (this.foolsjourney$spirituality > 100.0) this.foolsjourney$spirituality = 100.0;
                dataChanged = true;
            }
        }

        if (dataChanged) {
            syncBeyonderData();
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("lotm_sequence", this.foolsjourney$sequence);
        nbt.putDouble("lotm_spirituality", this.foolsjourney$spirituality);
        nbt.putBoolean("lotm_spirit_vision", this.foolsjourney$spiritVisionActive);
        nbt.putInt("lotm_cooldown", this.foolsjourney$abilityCooldown);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if(nbt.contains("lotm_sequence")) this.foolsjourney$sequence = nbt.getInt("lotm_sequence");
        if(nbt.contains("lotm_spirituality")) this.foolsjourney$spirituality = nbt.getDouble("lotm_spirituality");
        if(nbt.contains("lotm_spirit_vision")) this.foolsjourney$spiritVisionActive = nbt.getBoolean("lotm_spirit_vision");
        if(nbt.contains("lotm_cooldown")) this.foolsjourney$abilityCooldown = nbt.getInt("lotm_cooldown");
    }
}