package com.digi.foolsjourney.item.custom;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
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
        return 32;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity player = user instanceof PlayerEntity ? (PlayerEntity) user : null;

        if (player instanceof ServerPlayerEntity serverPlayer) {
            Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        if (!world.isClient && player instanceof IBeyonder beyonder) {
            if (beyonder.getSequence() > -1) {
                player.sendMessage(Text.of("§cAlready a Beyonder! Potion wasted..."), true);
            } else {
                beyonder.setSequence(9);
                beyonder.setSpirituality(100.0);

                player.sendMessage(Text.of("§5[LotM] You consumed the potion. The fog clears..."), false);
                player.sendMessage(Text.of("§dYou are now Sequence 9: Seer."), false);
            }
        }

        if (player != null) {
            if (player.getAbilities().creativeMode) {
                return stack;
            }

            stack.decrement(1);
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }

        if (player != null) {
            player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
        }

        return stack;
    }
}