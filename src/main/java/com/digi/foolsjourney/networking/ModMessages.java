package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ModMessages {

    public static final Identifier FLAME_SNAP_ID = Identifier.of("foolsjourney", "flame_snap");

    public record FlameSnapPayload() implements CustomPayload {
        public static final CustomPayload.Id<FlameSnapPayload> ID = new CustomPayload.Id<>(FLAME_SNAP_ID);
        public static final PacketCodec<RegistryByteBuf, FlameSnapPayload> CODEC = PacketCodec.unit(new FlameSnapPayload());
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(SpiritVisionPayload.ID, SpiritVisionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FlameSnapPayload.ID, FlameSnapPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BeyonderSyncPayload.ID, BeyonderSyncPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SpiritVisionPayload.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                if (context.player() instanceof IBeyonder beyonder) {
                    if (beyonder.getSequence() == -1) return;
                    if (beyonder.getCooldown() > 0) return;
                    if (!beyonder.isSpiritVisionActive() && beyonder.getSpirituality() < 1.0) return;
                    boolean newState = !beyonder.isSpiritVisionActive();
                    beyonder.setSpiritVision(newState);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(FlameSnapPayload.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                var player = context.player();
                if (player instanceof IBeyonder beyonder) {

                    if (beyonder.getSequence() != -1 && beyonder.getSequence() <= 8) {

                        if (player.isSubmergedInWater()) {
                            playExtinguishEffect(player, player.getEyePos().add(player.getRotationVec(1.0F).multiply(0.5)));
                            return;
                        }

                        if (beyonder.getCooldown() > 0) return;

                        double manaCost = 15.0;
                        if (beyonder.getSpirituality() >= manaCost) {

                            double maxDistance = 20.0;
                            Vec3d startPos = player.getEyePos();
                            Vec3d lookVec = player.getRotationVec(1.0F);
                            Vec3d endPos = startPos.add(lookVec.multiply(maxDistance));

                            BlockHitResult blockHitResult = player.getWorld().raycast(new RaycastContext(
                                    startPos,
                                    endPos,
                                    RaycastContext.ShapeType.OUTLINE,
                                    RaycastContext.FluidHandling.SOURCE_ONLY,
                                    player));

                            double effectiveDistance = maxDistance;
                            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                                effectiveDistance = startPos.distanceTo(blockHitResult.getPos());
                            }

                            Vec3d entityCheckEnd = startPos.add(lookVec.multiply(effectiveDistance));
                            Box box = player.getBoundingBox().stretch(lookVec.multiply(effectiveDistance)).expand(1.0D, 1.0D, 1.0D);

                            EntityHitResult entityHitResult = ProjectileUtil.raycast(
                                    player, startPos, entityCheckEnd, box,
                                    (entity) -> !entity.isSpectator() && entity.canHit(),
                                    effectiveDistance * effectiveDistance
                            );

                            boolean targetIsEntity = (entityHitResult != null);
                            boolean targetIsBlock = (blockHitResult.getType() == HitResult.Type.BLOCK);
                            boolean actionDone = false;

                            if (player.getWorld() instanceof ServerWorld serverWorld) {
                                Vec3d handPos = player.getEyePos().add(lookVec.multiply(0.5));
                                serverWorld.spawnParticles(ParticleTypes.SMALL_FLAME, handPos.x, handPos.y, handPos.z, 5, 0.1, 0.1, 0.1, 0.05);
                                serverWorld.spawnParticles(ParticleTypes.SMOKE, handPos.x, handPos.y, handPos.z, 2, 0.1, 0.1, 0.1, 0.02);
                            }

                            if (targetIsEntity) {
                                if (entityHitResult.getEntity().isInsideWaterOrBubbleColumn()) {
                                    playExtinguishEffect(player, entityHitResult.getPos());
                                } else {
                                    SmallFireballEntity fireball = new SmallFireballEntity(player.getWorld(), player, lookVec);
                                    fireball.setPosition(player.getX() + lookVec.x * 0.5, player.getEyeY(), player.getZ() + lookVec.z * 0.5);
                                    player.getWorld().spawnEntity(fireball);
                                    if (beyonder.getDigestion() < 100.0) beyonder.addDigestion(0.5);
                                }
                                actionDone = true;
                            }
                            else if (targetIsBlock) {
                                BlockPos hitPos = blockHitResult.getBlockPos();

                                if (!player.getWorld().getFluidState(hitPos).isEmpty()) {
                                    playExtinguishEffect(player, new Vec3d(hitPos.getX() + 0.5, hitPos.getY() + 1, hitPos.getZ() + 0.5));
                                    actionDone = true;
                                }
                                else {
                                    BlockPos firePos = hitPos.up();
                                    if (player.getWorld().getBlockState(firePos).isReplaceable()) {
                                        player.getWorld().setBlockState(firePos, Blocks.FIRE.getDefaultState());
                                        if (player.getWorld() instanceof ServerWorld serverWorld) {
                                            serverWorld.spawnParticles(ParticleTypes.FLAME, firePos.getX() + 0.5, firePos.getY() + 0.5, firePos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.05);
                                        }
                                    } else {
                                        SmallFireballEntity fireball = new SmallFireballEntity(player.getWorld(), player, lookVec);
                                        fireball.setPosition(player.getX() + lookVec.x * 0.5, player.getEyeY(), player.getZ() + lookVec.z * 0.5);
                                        player.getWorld().spawnEntity(fireball);
                                    }
                                    actionDone = true;
                                }
                            }
                            else {
                                SmallFireballEntity fireball = new SmallFireballEntity(player.getWorld(), player, lookVec);
                                fireball.setPosition(player.getX() + lookVec.x * 0.5, player.getEyeY(), player.getZ() + lookVec.z * 0.5);
                                player.getWorld().spawnEntity(fireball);
                                actionDone = true;
                            }

                            if (actionDone) {
                                beyonder.setSpirituality(beyonder.getSpirituality() - manaCost);
                                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);
                                beyonder.setCooldown(20);
                                beyonder.syncBeyonderData();
                            }
                        }
                    }
                }
            });
        });
    }

    private static void playExtinguishEffect(net.minecraft.entity.player.PlayerEntity player, Vec3d pos) {
        player.getWorld().playSound(null, BlockPos.ofFloored(pos), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y, pos.z, 5, 0.1, 0.1, 0.1, 0.05);
        }
    }
}