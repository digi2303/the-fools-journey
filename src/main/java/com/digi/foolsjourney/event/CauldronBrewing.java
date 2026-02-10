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

                    ItemEntity glowInkEntity = null;
                    ItemEntity amethystEntity = null;

                    ItemEntity roseBushEntity = null;
                    ItemEntity sugarEntity = null;

                    ItemEntity enderPearlEntity = null;
                    ItemEntity gunpowderEntity = null;

                    for (ItemEntity itemEntity : itemsInside) {
                        ItemStack stack = itemEntity.getStack();

                        if (stack.isOf(Items.GLOW_INK_SAC)) glowInkEntity = itemEntity;
                        else if (stack.isOf(Items.AMETHYST_SHARD)) amethystEntity = itemEntity;
                        else if (stack.isOf(Items.ROSE_BUSH)) roseBushEntity = itemEntity;
                        else if (stack.isOf(Items.SUGAR)) sugarEntity = itemEntity;
                        else if (stack.isOf(Items.ENDER_PEARL)) enderPearlEntity = itemEntity;
                        else if (stack.isOf(Items.GUNPOWDER)) gunpowderEntity = itemEntity;
                    }
                    if (glowInkEntity != null && amethystEntity != null) {
                        craftPotion(world, pos, player, heldItem, ModItems.SEER_POTION, glowInkEntity, amethystEntity);
                        return ActionResult.SUCCESS;
                    }

                    else if (roseBushEntity != null && sugarEntity != null) {
                        craftPotion(world, pos, player, heldItem, ModItems.CLOWN_POTION, roseBushEntity, sugarEntity);
                        return ActionResult.SUCCESS;
                    }

                    else if (enderPearlEntity != null && gunpowderEntity != null) {
                        craftPotion(world, pos, player, heldItem, ModItems.MAGICIAN_POTION, enderPearlEntity, gunpowderEntity);
                        return ActionResult.SUCCESS;
                    }
                }
            }

            return ActionResult.PASS;
        });
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