package com.digi.foolsjourney.event;

import com.digi.foolsjourney.registry.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CauldronBrewing {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            if (world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.WATER_CAULDRON) {

                ItemStack heldItem = player.getStackInHand(hand);

                if (heldItem.getItem() == Items.GLASS_BOTTLE) {

                    BlockPos pos = hitResult.getBlockPos();
                    Box cauldronBox = new Box(pos);
                    List<ItemEntity> itemsInside = world.getEntitiesByClass(ItemEntity.class, cauldronBox, entity -> true);

                    if (hasItems(itemsInside, Items.GLOW_INK_SAC, Items.AMETHYST_SHARD)) {
                        craftPotion(world, pos, player, heldItem, ModItems.SEER_POTION,
                                findItem(itemsInside, Items.GLOW_INK_SAC),
                                findItem(itemsInside, Items.AMETHYST_SHARD));
                        return ActionResult.SUCCESS;
                    }

                    else if (hasItems(itemsInside, Items.ROSE_BUSH, Items.SUGAR)) {
                        craftPotion(world, pos, player, heldItem, ModItems.CLOWN_POTION,
                                findItem(itemsInside, Items.ROSE_BUSH),
                                findItem(itemsInside, Items.SUGAR));
                        return ActionResult.SUCCESS;
                    }

                    else if (hasItems(itemsInside, Items.ENDER_PEARL, Items.GUNPOWDER)) {
                        craftPotion(world, pos, player, heldItem, ModItems.MAGICIAN_POTION,
                                findItem(itemsInside, Items.ENDER_PEARL),
                                findItem(itemsInside, Items.GUNPOWDER));
                        return ActionResult.SUCCESS;
                    }
                }
            }

            return ActionResult.PASS;
        });
    }

    private static boolean hasItems(List<ItemEntity> list, Item item1, Item item2) {
        return findItem(list, item1) != null && findItem(list, item2) != null;
    }
    private static ItemEntity findItem(List<ItemEntity> list, Item targetItem) {
        for (ItemEntity entity : list) {
            if (entity.getStack().isOf(targetItem)) return entity;
        }
        return null;
    }

    private static void craftPotion(net.minecraft.world.World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, ItemStack heldBottle, Item resultPotion, ItemEntity ingredient1, ItemEntity ingredient2) {
        decrementItemEntity(ingredient1);
        decrementItemEntity(ingredient2);

        heldBottle.decrement(1);

        ItemStack potionStack = new ItemStack(resultPotion);
        if (!player.getInventory().insertStack(potionStack)) {
            player.dropItem(potionStack, false);
        }

        LeveledCauldronBlock.decrementFluidLevel(world.getBlockState(pos), world, pos);

        world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    private static void decrementItemEntity(ItemEntity entity) {
        ItemStack stack = entity.getStack();
        stack.decrement(1);
        if (stack.isEmpty()) {
            entity.discard();
        }
    }
}