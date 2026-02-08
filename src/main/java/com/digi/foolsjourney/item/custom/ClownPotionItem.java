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

public class ClownPotionItem extends Item {

    public ClownPotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
        boolean isCreative = playerEntity != null && playerEntity.getAbilities().creativeMode;

        if (!world.isClient) {
            if (user instanceof IBeyonder beyonder) {
                int currentSeq = beyonder.getSequence();
                double currentDigestion = beyonder.getDigestion();

                if (currentSeq == 9) {

                    if (currentDigestion >= 100.0 || isCreative) {
                        beyonder.setSequence(8);
                        beyonder.setSpirituality(200.0);
                        beyonder.setDigestion(0.0);

                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));

                        if (playerEntity != null) {
                            playerEntity.sendMessage(Text.translatable("message.foolsjourney.advance_clown").formatted(Formatting.DARK_RED, Formatting.BOLD), false);

                            world.playSound(null, playerEntity.getBlockPos(), SoundEvents.ENTITY_WITCH_CELEBRATE, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                        }
                    }
                    else if (currentDigestion >= 50.0) {
                        if (playerEntity != null) {
                            playerEntity.sendMessage(Text.translatable("message.foolsjourney.loss_of_control_risk").formatted(Formatting.RED), true);
                        }
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 600, 2));
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 600, 4));
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 1200, 2));
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));

                        float currentHealth = user.getHealth();
                        user.damage(user.getDamageSources().magic(), currentHealth * 0.8f);

                        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                    else {
                        if (playerEntity != null) {
                            playerEntity.sendMessage(Text.translatable("message.foolsjourney.loss_of_control_death").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), true);
                        }

                        user.damage(user.getDamageSources().outOfWorld(), Float.MAX_VALUE);
                    }
                }

                else if (currentSeq == 8) {
                    if (playerEntity != null) playerEntity.sendMessage(Text.translatable("message.foolsjourney.already_sequence").formatted(Formatting.RED), true);
                }

                else {
                    if (playerEntity != null) {
                        playerEntity.sendMessage(Text.translatable("message.foolsjourney.potion_failure").formatted(Formatting.RED, Formatting.OBFUSCATED), true);
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 2));
                        user.damage(user.getDamageSources().magic(), 10.0f);
                    }
                }
            }
        }

        if (playerEntity instanceof ServerPlayerEntity serverPlayer) {
            Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        if (isCreative) return stack;

        stack.decrement(1);
        if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);

        if (playerEntity != null) {
            ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!playerEntity.getInventory().insertStack(glassBottle)) {
                playerEntity.dropItem(glassBottle, false);
            }
        }
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 32; }
    @Override
    public UseAction getUseAction(ItemStack stack) { return UseAction.DRINK; }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) { return ItemUsage.consumeHeldItem(world, user, hand); }
    @Override
    public SoundEvent getDrinkSound() { return SoundEvents.ITEM_HONEY_BOTTLE_DRINK; }
    @Override
    public SoundEvent getEatSound() { return SoundEvents.ITEM_HONEY_BOTTLE_DRINK; }
}