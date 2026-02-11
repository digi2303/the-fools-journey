package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    private static final double MANA_COST_FLAME_SNAP = 15.0;
    private static final double MANA_COST_FLAMING_JUMP = 100.0;

    private static final int COOLDOWN_FLAME_SNAP = 20;
    private static final int COOLDOWN_FLAMING_JUMP = 40;

    private static final double RANGE_FLAME_SNAP = 20.0;
    private static final double RANGE_FLAMING_JUMP = 15.0;

    public record FlameSnapPayload() implements CustomPayload {
        public static final CustomPayload.Id<FlameSnapPayload> ID = new CustomPayload.Id<>(FLAME_SNAP_ID);
        public static final PacketCodec<RegistryByteBuf, FlameSnapPayload> CODEC = PacketCodec.unit(new FlameSnapPayload());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
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

                        if (beyonder.getCooldown() > 0) return;

                        if (player.isSubmergedInWater()) {
                            playExtinguishEffect(player, player.getEyePos().add(player.getRotationVec(1.0F).multiply(0.5)));
                            return;
                        }

                        if (player.isSneaking() && beyonder.getSequence() <= 7) {

                            if (player.isCreative() || beyonder.getSpirituality() >= MANA_COST_FLAMING_JUMP) {
                                Vec3d start = player.getEyePos();
                                Vec3d look = player.getRotationVec(1.0F);
                                Vec3d end = start.add(look.multiply(RANGE_FLAMING_JUMP));

                                BlockHitResult hitResult = player.getWorld().raycast(new RaycastContext(
                                        start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player
                                ));

                                Vec3d targetPos = end;
                                if (hitResult.getType() == HitResult.Type.BLOCK) {
                                    targetPos = hitResult.getPos().subtract(look.multiply(0.5));
                                }

                                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                                if (player.getWorld() instanceof ServerWorld serverWorld) {
                                    serverWorld.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 1, 0.5, 0.1);
                                    serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1, player.getZ(), 10, 0.5, 1, 0.5, 0.1);
                                }

                                player.requestTeleport(targetPos.x, targetPos.y, targetPos.z);

                                player.getWorld().playSound(null, targetPos.x, targetPos.y, targetPos.z,
                                        SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                                if (player.getWorld() instanceof ServerWorld serverWorld) {
                                    serverWorld.spawnParticles(ParticleTypes.FLAME, targetPos.x, targetPos.y + 1, targetPos.z, 20, 0.5, 1, 0.5, 0.1);
                                }

                                if (!player.isCreative()) {
                                    beyonder.setSpirituality(beyonder.getSpirituality() - MANA_COST_FLAMING_JUMP);
                                    beyonder.setCooldown(COOLDOWN_FLAMING_JUMP);
                                    beyonder.syncBeyonderData();
                                }

                            } else {
                                player.sendMessage(Text.translatable("message.foolsjourney.mana_drained").formatted(Formatting.RED), true);
                            }
                            return;
                        }

                        if (player.isCreative() || beyonder.getSpirituality() >= MANA_COST_FLAME_SNAP) {

                            Vec3d startPos = player.getEyePos();
                            Vec3d lookVec = player.getRotationVec(1.0F);
                            Vec3d endPos = startPos.add(lookVec.multiply(RANGE_FLAME_SNAP));

                            BlockHitResult blockHitResult = player.getWorld().raycast(new RaycastContext(
                                    startPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.SOURCE_ONLY, player));

                            double effectiveDistance = RANGE_FLAME_SNAP;
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
                            boolean actionDone = false;

                            if (player.getWorld() instanceof ServerWorld serverWorld) {
                                Vec3d handPos = player.getEyePos().add(lookVec.multiply(0.5));
                                serverWorld.spawnParticles(ParticleTypes.SMALL_FLAME, handPos.x, handPos.y, handPos.z, 5, 0.1, 0.1, 0.1, 0.05);
                                serverWorld.spawnParticles(ParticleTypes.SMOKE, handPos.x, handPos.y, handPos.z, 2, 0.1, 0.1, 0.1, 0.02);
                            }

                            if (targetIsEntity) {
                                SmallFireballEntity fireball = new SmallFireballEntity(player.getWorld(), player, lookVec);
                                fireball.setPosition(player.getX() + lookVec.x * 0.5, player.getEyeY(), player.getZ() + lookVec.z * 0.5);
                                player.getWorld().spawnEntity(fireball);
                                if (beyonder.getDigestion() < 100.0) beyonder.addDigestion(0.5);
                                actionDone = true;
                            }
                            else if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                                BlockPos hitPos = blockHitResult.getBlockPos();
                                BlockPos firePos = hitPos;

                                if (!player.getWorld().getBlockState(hitPos).isReplaceable()) {
                                    firePos = hitPos.up();
                                }

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
                            else {
                                SmallFireballEntity fireball = new SmallFireballEntity(player.getWorld(), player, lookVec);
                                fireball.setPosition(player.getX() + lookVec.x * 0.5, player.getEyeY(), player.getZ() + lookVec.z * 0.5);
                                player.getWorld().spawnEntity(fireball);
                                actionDone = true;
                            }

                            if (actionDone) {
                                if (!player.isCreative()) {
                                    beyonder.setSpirituality(beyonder.getSpirituality() - MANA_COST_FLAME_SNAP);
                                }
                                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);
                                beyonder.setCooldown(COOLDOWN_FLAME_SNAP);
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