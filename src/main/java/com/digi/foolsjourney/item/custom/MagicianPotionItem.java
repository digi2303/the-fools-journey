package com.digi.foolsjourney.item.custom;

import com.digi.foolsjourney.registry.ModDamageTypes;
import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class MagicianPotionItem extends Item {

    public MagicianPotionItem(Settings settings) {
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

                if (currentSeq != -1 && currentSeq <= 7) {
                    if (playerEntity != null) playerEntity.sendMessage(Text.translatable("message.foolsjourney.already_sequence").formatted(Formatting.RED), true);
                }


                else if (isCreative || (currentSeq == 8 && currentDigestion >= 100.0)) {
                    beyonder.setSequence(7);
                    beyonder.setSpirituality(400.0);
                    beyonder.setDigestion(0.0);
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));

                    if (playerEntity != null) {
                        playerEntity.sendMessage(Text.translatable("message.foolsjourney.advance_magician").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), false);
                        world.playSound(null, playerEntity.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 0.8f);
                    }
                }

                else if (currentSeq == 8 && currentDigestion >= 50.0) {
                    if (playerEntity != null) {
                        playerEntity.sendMessage(Text.translatable("message.foolsjourney.loss_of_control_risk").formatted(Formatting.RED), true);
                    }
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 600, 2));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 600, 4));

                    float currentHealth = user.getHealth();
                    user.damage(user.getDamageSources().magic(), currentHealth * 0.9f);
                }

                else {
                    if (playerEntity != null) {
                        playerEntity.sendMessage(Text.translatable("message.foolsjourney.potion_failure").formatted(Formatting.DARK_RED, Formatting.OBFUSCATED), true);
                    }

                    if (!isCreative) {
                        if (world instanceof ServerWorld serverWorld) {
                            DamageSource source = serverWorld.getDamageSources().create(ModDamageTypes.LOSS_OF_CONTROL);
                            user.damage(source, Float.MAX_VALUE);
                        }
                        user.setHealth(0);
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
        return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
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