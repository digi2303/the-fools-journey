package com.digi.foolsjourney.mixin;

import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PaperFigurineMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (player.getWorld().isClient || player.isDead() || player.isSpectator()) return;
        if (source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        if (player instanceof IBeyonder beyonder) {
            if (beyonder.getSequence() != -1 && beyonder.getSequence() <= 8) {

                if (beyonder.getCooldown() > 0) return;

                boolean isLethal = amount >= player.getHealth();
                boolean isCritical = player.getHealth() <= (player.getMaxHealth() * 0.25f);
                if (!isLethal && !isCritical) return;

                PlayerInventory inventory = player.getInventory();
                int paperSlot = findPaper(inventory);
                if (paperSlot == -1) return;

                double cost = 25.0;
                if (beyonder.getSpirituality() < cost) {
                    player.sendMessage(Text.translatable("message.foolsjourney.mana_drained").formatted(Formatting.RED), true);
                    return;
                }

                Vec3d targetPos = findTacticalPosition(player, source.getAttacker());
                if (targetPos == null) return;

                cir.setReturnValue(false);

                beyonder.setSpirituality(beyonder.getSpirituality() - cost);
                inventory.getStack(paperSlot).decrement(1);

                beyonder.setCooldown(200);

                if (beyonder.getDigestion() < 100.0) {
                    beyonder.addDigestion(1.5);
                    player.sendMessage(Text.translatable("message.foolsjourney.acting_clown_kill").formatted(Formatting.RED), true);
                }

                beyonder.syncBeyonderData();

                executeEffects(player, source, targetPos);
            }
        }
    }

    @Unique
    private int findPaper(PlayerInventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isOf(Items.PAPER)) return i;
        }
        return -1;
    }

    @Unique
    private void executeEffects(PlayerEntity player, DamageSource source, Vec3d targetPos) {
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d oldPos = player.getPos();

        world.spawnParticles(ParticleTypes.POOF, oldPos.x, oldPos.y + 1, oldPos.z, 20, 0.2, 0.5, 0.2, 0.05);
        world.playSound(null, oldPos.x, oldPos.y, oldPos.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0f, 1.0f);

        ItemEntity paperEntity = new ItemEntity(world, oldPos.x, oldPos.y + 0.5, oldPos.z, new ItemStack(Items.PAPER));

        NbtCompound nbt = new NbtCompound();
        paperEntity.writeCustomDataToNbt(nbt);
        nbt.putShort("Age", (short) 5800);
        nbt.putShort("PickupDelay", (short) 32767);
        paperEntity.readCustomDataFromNbt(nbt);

        if (source.getAttacker() != null) {
            Vec3d motion = oldPos.subtract(source.getAttacker().getPos()).normalize().multiply(0.15);

            paperEntity.setVelocity(motion.x, 0.1, motion.z);
        } else {
            paperEntity.setVelocity(0, 0, 0);
        }

        world.spawnEntity(paperEntity);

        player.requestTeleport(targetPos.x, targetPos.y, targetPos.z);
        world.spawnParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1.5, player.getZ(), 10, 0.5, 0.5, 0.5, 0.1);

        player.sendMessage(Text.translatable("message.foolsjourney.paper_figurine_used").formatted(Formatting.GOLD, Formatting.BOLD), true);
    }

    @Unique
    private Vec3d findTacticalPosition(PlayerEntity player, net.minecraft.entity.Entity attacker) {
        ServerWorld world = (ServerWorld) player.getWorld();
        if (attacker instanceof LivingEntity) {
            Vec3d dir = attacker.getRotationVec(1.0f).multiply(-2.5);
            BlockPos target = BlockPos.ofFloored(attacker.getPos().add(dir));
            if (isSafe(world, target)) return Vec3d.ofBottomCenter(target);
        }

        BlockPos pPos = player.getBlockPos();
        for (int i = 0; i < 8; i++) {
            BlockPos check = pPos.add(world.random.nextInt(9) - 4, world.random.nextInt(3) - 1, world.random.nextInt(9) - 4);
            if (isSafe(world, check)) return Vec3d.ofBottomCenter(check);
        }
        return null;
    }

    @Unique
    private boolean isSafe(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir() && !world.getBlockState(pos.down()).isAir();
    }
}