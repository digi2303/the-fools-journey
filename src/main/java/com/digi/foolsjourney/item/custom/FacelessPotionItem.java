package com.digi.foolsjourney.item.custom;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class FacelessPotionItem extends Item {
    public FacelessPotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            if (player instanceof IBeyonder beyonder) {

                if (beyonder.getSequence() == 7) {
                    beyonder.setSequence(6);
                    beyonder.setSpirituality(beyonder.getMaxSpirituality());

                    player.sendMessage(Text.translatable("message.foolsjourney.advance_faceless").formatted(Formatting.DARK_GREEN), true);
                    player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }
                else if (beyonder.getSequence() < 7 && beyonder.getSequence() != -1) {
                    player.sendMessage(Text.translatable("message.foolsjourney.already_higher").formatted(Formatting.YELLOW), true);
                }
                else {
                    player.sendMessage(Text.translatable("message.foolsjourney.consumption_fail").formatted(Formatting.RED), true);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 600, 2));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 600, 2));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 1));
                }

                beyonder.syncBeyonderData();
            }
        }

        if (user instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
            stack.decrement(1);
        }
        return stack;
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
    public SoundEvent getDrinkSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }
}