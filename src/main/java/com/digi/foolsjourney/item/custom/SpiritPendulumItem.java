package com.digi.foolsjourney.item.custom;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public class SpiritPendulumItem extends Item {

    private static final Set<Block> VALUABLE_BLOCKS = Set.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.ANCIENT_DEBRIS,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE
    );

    public SpiritPendulumItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient) {
            if (user instanceof IBeyonder beyonder) {
                if (beyonder.getSequence() == -1) {
                    user.sendMessage(Text.translatable("item.foolsjourney.spirit_pendulum.fail_sequence").formatted(Formatting.RED), true);
                    return TypedActionResult.fail(itemStack);
                }

                boolean isCreative = user.getAbilities().creativeMode;

                if (!isCreative && beyonder.getSpirituality() < 10) {
                    user.sendMessage(Text.translatable("item.foolsjourney.spirit_pendulum.fail_mana").formatted(Formatting.RED), true);
                    return TypedActionResult.fail(itemStack);
                }

                if (!isCreative) {
                    beyonder.setSpirituality(beyonder.getSpirituality() - 10);
                }

                ItemStack offHandStack = user.getOffHandStack();
                String targetName = getTargetNameFromItem(offHandStack);

                if (targetName != null) {
                    ServerPlayerEntity targetPlayer = findPlayerByName((ServerWorld) world, targetName);

                    if (targetPlayer != null) {
                        if (targetPlayer == user) {
                            user.sendMessage(Text.translatable("item.foolsjourney.spirit_pendulum.track_self").formatted(Formatting.YELLOW), true);
                            return TypedActionResult.fail(itemStack);
                        }

                        user.sendMessage(Text.translatable("item.foolsjourney.spirit_pendulum.track_success", targetName).formatted(Formatting.GOLD), true);
                        world.playSound(null, user.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.5f, 0.5f);

                        spawnParticleTrail(user.getEyePos(), targetPlayer.getPos().add(0, 1, 0), user, ParticleTypes.WAX_ON);

                        increaseDigestion(user, beyonder, 2.0);

                        user.getItemCooldownManager().set(this, 100);
                    } else {
                        user.sendMessage(Text.translatable("item.foolsjourney.spirit_pendulum.track_fail", targetName).formatted(Formatting.RED), true);
                        user.getItemCooldownManager().set(this, 40);
                    }
                    return TypedActionResult.success(itemStack);
                }

                BlockPos foundOre = findNearestValuableOre(user, world);

                if (foundOre != null) {
                    user.sendMessage(Text.translatable("item.foolsjourney.spirit_pendulum.found").formatted(Formatting.LIGHT_PURPLE), true);

                    world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    spawnParticleTrail(user.getEyePos(), new Vec3d(foundOre.getX() + 0.5, foundOre.getY() + 0.5, foundOre.getZ() + 0.5), user, ParticleTypes.DRAGON_BREATH);

                    increaseDigestion(user, beyonder, 0.5);

                    user.getItemCooldownManager().set(this, 40);
                } else {
                    user.sendMessage(Text.translatable("item.foolsjourney.spirit_pendulum.not_found").formatted(Formatting.GRAY), true);
                    world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1.0f);
                    user.getItemCooldownManager().set(this, 20);
                }
            }
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.foolsjourney.spirit_pendulum.ore_mode").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.translatable("tooltip.foolsjourney.spirit_pendulum.player_mode").formatted(Formatting.GOLD));
        tooltip.add(Text.translatable("tooltip.foolsjourney.spirit_pendulum.cost").formatted(Formatting.BLUE));

        super.appendTooltip(stack, context, tooltip, type);
    }

    private void increaseDigestion(PlayerEntity player, IBeyonder beyonder, double amount) {
        if (beyonder.getDigestion() < 100.0) {
            beyonder.addDigestion(amount);

            if (beyonder.getDigestion() >= 100.0) {
                player.sendMessage(Text.translatable("message.foolsjourney.digestion_complete").formatted(Formatting.GOLD, Formatting.BOLD), false);
                player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }

    private String getTargetNameFromItem(ItemStack stack) {
        if (stack.isEmpty()) return null;

        if (stack.isOf(Items.PLAYER_HEAD)) {
            ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
            if (profile != null && profile.name().isPresent()) {
                return profile.name().get();
            }
        }

        if (stack.isOf(Items.NAME_TAG) && stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            return stack.getName().getString();
        }

        return null;
    }

    private ServerPlayerEntity findPlayerByName(ServerWorld world, String name) {
        return world.getServer().getPlayerManager().getPlayer(name);
    }

    private BlockPos findNearestValuableOre(PlayerEntity user, World world) {
        BlockPos playerPos = user.getBlockPos();
        BlockPos nearestOre = null;
        double minDistanceSq = Double.MAX_VALUE;
        int radius = 16;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos currentPos = playerPos.add(x, y, z);
                    Block block = world.getBlockState(currentPos).getBlock();
                    if (VALUABLE_BLOCKS.contains(block)) {
                        double distanceSq = playerPos.getSquaredDistance(currentPos);
                        if (distanceSq < minDistanceSq) {
                            minDistanceSq = distanceSq;
                            nearestOre = currentPos;
                        }
                    }
                }
            }
        }
        return nearestOre;
    }

    private void spawnParticleTrail(Vec3d start, Vec3d end, PlayerEntity user, ParticleEffect particleType) {
        Vec3d direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        if (user instanceof ServerPlayerEntity serverPlayer) {
            for (double i = 0; i < distance; i += 0.5) {
                Vec3d pos = start.add(direction.multiply(i));
                serverPlayer.networkHandler.sendPacket(new ParticleS2CPacket(
                        particleType,
                        false, pos.x, pos.y, pos.z, 0, 0, 0, 0, 1));
            }
            serverPlayer.networkHandler.sendPacket(new ParticleS2CPacket(
                    ParticleTypes.END_ROD, false, end.x, end.y, end.z, 0.2f, 0.2f, 0.2f, 0.05f, 5));
        }
    }
}