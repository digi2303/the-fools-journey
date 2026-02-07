package com.digi.foolsjourney.item.custom;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class SeerPotionItem extends Item {

    public SeerPotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32; // 1.6 saniye
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            if (user instanceof PlayerEntity player && user instanceof IBeyonder beyonder) {

                if (beyonder.getSequence() > -1) {
                    player.sendMessage(Text.of("§c[Fail] You are already a Beyonder! Consuming this would be madness..."), true);
                    return stack;
                }

                beyonder.setSequence(9);
                beyonder.setSpirituality(100.0);

                player.sendMessage(Text.of("§5[LotM] You consumed the potion. The fog clears..."), false);
                player.sendMessage(Text.of("§dYou are now Sequence 9: Seer."), false);

                System.out.println("Player " + player.getName().getString() + " became Sequence 9.");
            }
        }

        return super.finishUsing(stack, world, user);
    }
}