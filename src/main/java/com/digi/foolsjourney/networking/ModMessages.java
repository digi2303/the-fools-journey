package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
    public static final Identifier AIR_BULLET_ID = Identifier.of("foolsjourney", "air_bullet");

    public record FlameSnapPayload() implements CustomPayload {
        public static final CustomPayload.Id<FlameSnapPayload> ID = new CustomPayload.Id<>(FLAME_SNAP_ID);
        public static final PacketCodec<RegistryByteBuf, FlameSnapPayload> CODEC = PacketCodec.unit(new FlameSnapPayload());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record AirBulletPayload() implements CustomPayload {
        public static final CustomPayload.Id<AirBulletPayload> ID = new CustomPayload.Id<>(AIR_BULLET_ID);
        public static final PacketCodec<RegistryByteBuf, AirBulletPayload> CODEC = PacketCodec.unit(new AirBulletPayload());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(SpiritVisionPayload.ID, SpiritVisionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FlameSnapPayload.ID, FlameSnapPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AirBulletPayload.ID, AirBulletPayload.CODEC);
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

        ServerPlayNetworking.registerGlobalReceiver(AirBulletPayload.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                var player = context.player();
                if (player instanceof IBeyonder beyonder) {
                    if (beyonder.getSequence() != -1 && beyonder.getSequence() <= 7) {

                        if (beyonder.getCooldown() > 0) return;

                        double cost = 10.0;
                        if (!player.isCreative() && beyonder.getSpirituality() < cost) {
                            player.sendMessage(Text.translatable("message.foolsjourney.mana_drained").formatted(Formatting.RED), true);
                            return;
                        }

                        double maxRange = 50.0;
                        Vec3d start = player.getEyePos();
                        Vec3d look = player.getRotationVec(1.0F);
                        Vec3d end = start.add(look.multiply(maxRange));

                        BlockHitResult blockHit = player.getWorld().raycast(new RaycastContext(
                                start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player
                        ));

                        double effectiveDistance = maxRange;
                        if (blockHit.getType() == HitResult.Type.BLOCK) {
                            effectiveDistance = start.distanceTo(blockHit.getPos());
                            end = blockHit.getPos();
                        }

                        Box box = player.getBoundingBox().stretch(look.multiply(effectiveDistance)).expand(1.0D);
                        EntityHitResult entityHit = ProjectileUtil.raycast(
                                player, start, end, box,
                                (entity) -> !entity.isSpectator() && entity.canHit(),
                                effectiveDistance * effectiveDistance
                        );

                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENTITY_BREEZE_WIND_BURST, SoundCategory.PLAYERS, 1.0F, 1.5F);

                        if (player.getWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                                    player.getX() + look.x, player.getEyeY() + look.y, player.getZ() + look.z,
                                    1, 0.0, 0.0, 0.0, 0.0);
                        }

                        if (entityHit != null) {
                            Entity target = entityHit.getEntity();
                            float damage = 12.0f;
                            if (beyonder.getSequence() <= 6) damage += 4.0f;

                            target.damage(player.getDamageSources().magic(), damage);

                            if (target instanceof LivingEntity livingTarget) {
                                livingTarget.takeKnockback(0.8, player.getX() - target.getX(), player.getZ() - target.getZ());
                            }
                        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
                            player.getWorld().playSound(null, blockHit.getBlockPos(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.5F, 2.0F);
                        }

                        if (!player.isCreative()) {
                            beyonder.setSpirituality(beyonder.getSpirituality() - cost);
                            beyonder.setCooldown(15);
                            beyonder.syncBeyonderData();
                        }

                        if (beyonder.getSequence() == 7 && beyonder.getDigestion() < 100.0) {
                            if (player.getWorld().random.nextInt(10) == 0) beyonder.addDigestion(0.2);
                        }
                    }
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
                            playExtinguishEffect(player, player.getEyePos());
                            return;
                        }

                        if (player.isSneaking() && beyonder.getSequence() <= 7) {
                            double jumpCost = 100.0;
                            if (player.isCreative() || beyonder.getSpirituality() >= jumpCost) {
                                Vec3d start = player.getEyePos();
                                Vec3d look = player.getRotationVec(1.0F);
                                Vec3d end = start.add(look.multiply(15.0));

                                BlockHitResult hitResult = player.getWorld().raycast(new RaycastContext(
                                        start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

                                Vec3d targetPos = end;
                                if (hitResult.getType() == HitResult.Type.BLOCK) targetPos = hitResult.getPos().subtract(look.multiply(0.5));

                                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                                if (player.getWorld() instanceof ServerWorld sw) sw.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), 10, 0.5, 1, 0.5, 0.1);

                                player.requestTeleport(targetPos.x, targetPos.y, targetPos.z);

                                player.getWorld().playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                                if (player.getWorld() instanceof ServerWorld sw) sw.spawnParticles(ParticleTypes.FLAME, targetPos.x, targetPos.y, targetPos.z, 10, 0.5, 1, 0.5, 0.1);

                                if (!player.isCreative()) {
                                    beyonder.setSpirituality(beyonder.getSpirituality() - jumpCost);
                                    beyonder.setCooldown(40);
                                    beyonder.syncBeyonderData();
                                }
                            }
                            return;
                        }

                        double manaCost = 15.0;
                        if (player.isCreative() || beyonder.getSpirituality() >= manaCost) {
                            Vec3d startPos = player.getEyePos();
                            Vec3d lookVec = player.getRotationVec(1.0F);
                            Vec3d endPos = startPos.add(lookVec.multiply(20.0));

                            BlockHitResult blockHitResult = player.getWorld().raycast(new RaycastContext(
                                    startPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.SOURCE_ONLY, player));

                            double effectiveDistance = 20.0;
                            if (blockHitResult.getType() == HitResult.Type.BLOCK) effectiveDistance = startPos.distanceTo(blockHitResult.getPos());

                            Vec3d entityCheckEnd = startPos.add(lookVec.multiply(effectiveDistance));
                            Box box = player.getBoundingBox().stretch(lookVec.multiply(effectiveDistance)).expand(1.0D);
                            EntityHitResult entityHitResult = ProjectileUtil.raycast(
                                    player, startPos, entityCheckEnd, box,
                                    (entity) -> !entity.isSpectator() && entity.canHit(), effectiveDistance * effectiveDistance);

                            boolean targetIsEntity = (entityHitResult != null);

                            if (player.getWorld() instanceof ServerWorld sw) {
                                Vec3d handPos = player.getEyePos().add(lookVec.multiply(0.5));
                                sw.spawnParticles(ParticleTypes.SMALL_FLAME, handPos.x, handPos.y, handPos.z, 5, 0.1, 0.1, 0.1, 0.05);
                            }

                            if (targetIsEntity) {
                                SmallFireballEntity fireball = new SmallFireballEntity(player.getWorld(), player, lookVec);
                                fireball.setPosition(player.getX() + lookVec.x * 0.5, player.getEyeY(), player.getZ() + lookVec.z * 0.5);
                                player.getWorld().spawnEntity(fireball);
                                if (beyonder.getDigestion() < 100.0) beyonder.addDigestion(0.5);
                            } else if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                                BlockPos hitPos = blockHitResult.getBlockPos();
                                BlockPos firePos = hitPos;
                                if (!player.getWorld().getBlockState(hitPos).isReplaceable()) firePos = hitPos.up();
                                if (player.getWorld().getBlockState(firePos).isReplaceable()) {
                                    player.getWorld().setBlockState(firePos, Blocks.FIRE.getDefaultState());
                                } else {
                                    SmallFireballEntity fireball = new SmallFireballEntity(player.getWorld(), player, lookVec);
                                    fireball.setPosition(player.getX() + lookVec.x * 0.5, player.getEyeY(), player.getZ() + lookVec.z * 0.5);
                                    player.getWorld().spawnEntity(fireball);
                                }
                            } else {
                                SmallFireballEntity fireball = new SmallFireballEntity(player.getWorld(), player, lookVec);
                                fireball.setPosition(player.getX() + lookVec.x * 0.5, player.getEyeY(), player.getZ() + lookVec.z * 0.5);
                                player.getWorld().spawnEntity(fireball);
                            }

                            if (!player.isCreative()) {
                                beyonder.setSpirituality(beyonder.getSpirituality() - manaCost);
                            }
                            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);
                            beyonder.setCooldown(20);
                            beyonder.syncBeyonderData();
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