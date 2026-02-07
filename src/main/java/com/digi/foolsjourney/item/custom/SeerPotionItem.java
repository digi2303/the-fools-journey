package com.digi.foolsjourney.item.custom;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class SeerPotionItem extends Item {

    public SeerPotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity)user : null;

        boolean isCreative = playerEntity != null && playerEntity.getAbilities().creativeMode;

        if (!world.isClient) {
            if (user instanceof IBeyonder beyonder) {
                if (beyonder.getSequence() != -1) {
                    if (playerEntity != null) {
                        playerEntity.sendMessage(Text.translatable("message.foolsjourney.already_beyonder").formatted(Formatting.RED), true);
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 1));
                    }
                } else {
                    beyonder.setSequence(9);
                    beyonder.setSpirituality(100.0);

                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));

                    if (playerEntity != null) {
                        playerEntity.sendMessage(Text.translatable("message.foolsjourney.seer_potion_consumed").formatted(Formatting.DARK_PURPLE), false);

                        world.playSound(null, playerEntity.getBlockPos(), SoundEvents.BLOCK_END_PORTAL_SPAWN, net.minecraft.sound.SoundCategory.PLAYERS, 0.5f, 1.0f);
                    }
                }
            }
        }

        if (playerEntity instanceof ServerPlayerEntity serverPlayer) {
            Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        if (isCreative) {
            return stack;
        }

        stack.decrement(1);

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (playerEntity != null) {
                ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!playerEntity.getInventory().insertStack(glassBottle)) {
                    playerEntity.dropItem(glassBottle, false);
                }
            }
            return stack;
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public SoundEvent getDrinkSound() {
        return SoundEvents.ITEM_HONEY_BOTTLE_DRINK;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.ITEM_HONEY_BOTTLE_DRINK;
    }
}