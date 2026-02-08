package com.digi.foolsjourney.item.custom;

import com.digi.foolsjourney.entity.custom.ThrownCardEntity;
import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ThrowingCardItem extends Item {
    public ThrowingCardItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 2.0F);

        if (!world.isClient) {
            ThrownCardEntity cardEntity = new ThrownCardEntity(world, user);

            float speed = 1.5f;
            float divergence = 1.0f;
            double damage = 2.0;

            if (user instanceof IBeyonder beyonder && beyonder.getSequence() != -1 && beyonder.getSequence() <= 8) {
                speed = 8.0f;

                divergence = 0.0f;

                damage = 8.0;
            }

            cardEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, speed, divergence);
            cardEntity.setDamage(damage);

            world.spawnEntity(cardEntity);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));

        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }
}