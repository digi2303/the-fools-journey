package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
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

                        if (beyonder.getCooldown() > 0) {
                            player.sendMessage(Text.translatable("message.foolsjourney.ability_cooldown").formatted(Formatting.GRAY), true);
                            return;
                        }

                        double manaCost = 15.0;
                        if (beyonder.getSpirituality() >= manaCost) {

                            double maxDistance = 20.0;
                            Vec3d startPos = player.getEyePos();
                            Vec3d lookVec = player.getRotationVec(1.0F);
                            Vec3d endPos = startPos.add(lookVec.multiply(maxDistance));

                            BlockHitResult rayTraceResult = player.getWorld().raycast(new RaycastContext(
                                    startPos,
                                    endPos,
                                    RaycastContext.ShapeType.OUTLINE,
                                    RaycastContext.FluidHandling.NONE,
                                    player
                            ));

                            boolean blockHit = false;

                            if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
                                BlockPos hitPos = rayTraceResult.getBlockPos();
                                BlockPos firePos = hitPos.up();

                                if (player.getWorld().getBlockState(firePos).isReplaceable()) {
                                    player.getWorld().setBlockState(firePos, Blocks.FIRE.getDefaultState());
                                    blockHit = true;
                                }
                            }

                            if (!blockHit) {
                                SmallFireballEntity fireball = new SmallFireballEntity(player.getWorld(), player, lookVec);

                                fireball.setPosition(player.getX() + lookVec.x * 0.5, player.getEyeY(), player.getZ() + lookVec.z * 0.5);
                                player.getWorld().spawnEntity(fireball);
                            }

                            beyonder.setSpirituality(beyonder.getSpirituality() - manaCost);
                            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                            beyonder.setCooldown(20);
                            beyonder.syncBeyonderData();

                        } else {
                            player.sendMessage(Text.translatable("message.foolsjourney.not_enough_spirituality").formatted(Formatting.RED), true);
                        }
                    }
                }
            });
        });
    }
}