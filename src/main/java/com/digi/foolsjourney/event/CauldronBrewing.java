package com.digi.foolsjourney.event;

import com.digi.foolsjourney.registry.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.ItemEntity;
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

                    boolean hasInkSac = false;
                    boolean hasAmethyst = false;

                    ItemEntity inkEntity = null;
                    ItemEntity amethystEntity = null;

                    for (ItemEntity itemEntity : itemsInside) {
                        ItemStack stack = itemEntity.getStack();
                        if (stack.getItem() == Items.GLOW_INK_SAC) {
                            hasInkSac = true;
                            inkEntity = itemEntity;
                        } else if (stack.getItem() == Items.AMETHYST_SHARD) {
                            hasAmethyst = true;
                            amethystEntity = itemEntity;
                        }
                    }

                    if (hasInkSac && hasAmethyst) {

                        ItemStack inkStack = inkEntity.getStack();
                        inkStack.decrement(1);
                        if (inkStack.isEmpty()) inkEntity.discard();

                        ItemStack amethystStack = amethystEntity.getStack();
                        amethystStack.decrement(1);
                        if (amethystStack.isEmpty()) amethystEntity.discard();

                        heldItem.decrement(1);

                        ItemStack potionStack = new ItemStack(ModItems.SEER_POTION);
                        if (!player.getInventory().insertStack(potionStack)) {
                            player.dropItem(potionStack, false);
                        }

                        LeveledCauldronBlock.decrementFluidLevel(world.getBlockState(pos), world, pos);

                        world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);

                        return ActionResult.SUCCESS;
                    }
                }
            }

            return ActionResult.PASS;
        });
    }
}