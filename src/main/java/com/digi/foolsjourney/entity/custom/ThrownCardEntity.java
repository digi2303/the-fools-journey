package com.digi.foolsjourney.entity.custom;

import com.digi.foolsjourney.registry.ModEntities;
import com.digi.foolsjourney.registry.ModItems;
import com.digi.foolsjourney.util.IBeyonder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThrownCardEntity extends PersistentProjectileEntity implements FlyingItemEntity {

    private static final float BASE_DAMAGE = 8.0F;
    private static final float FIRE_DAMAGE_BONUS = 4.0F;

    private int customPierceLevel = 0;
    private int piercedEntities = 0;

    public ThrownCardEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public ThrownCardEntity(World world, LivingEntity owner) {
        super(ModEntities.THROWN_CARD, owner, world, new ItemStack(ModItems.THROWING_CARD), null);
        configureStatsBasedOnSequence(owner);
    }

    private void configureStatsBasedOnSequence(LivingEntity owner) {
        if (owner instanceof PlayerEntity player && player instanceof IBeyonder beyonder) {
            int seq = beyonder.getSequence();
            if (seq != -1 && seq <= 8) {
                if (seq <= 7) this.customPierceLevel = 1;
                if (seq <= 6) this.customPierceLevel = 3;
            }
        }
    }

    @Override
    protected double getGravity() {
        return isClownOrStronger() ? 0.01 : 0.05;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient && !this.inGround && isClownOrStronger() && this.age > 2) {
            Vec3d velocity = this.getVelocity();
            double d = this.getX() - velocity.x * 0.5;
            double e = this.getY() - velocity.y * 0.5;
            double f = this.getZ() - velocity.z * 0.5;
            if (this.random.nextFloat() < 0.3f) {
                this.getWorld().addParticle(ParticleTypes.CRIT, d, e, f, 0, 0, 0);
            }
        }

        if (!this.inGround && this.age % 5 == 0 && this.getVelocity().lengthSquared() > 0.5) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 0.5F, 1.5F);
        }

        if (this.isOnFire() && this.getWorld().isClient) {
            this.getWorld().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }

        if (this.age > 200) this.discard();
    }

    private int getSequence() {
        Entity owner = this.getOwner();
        if (owner instanceof PlayerEntity player && player instanceof IBeyonder beyonder) {
            return beyonder.getSequence();
        }
        return -1;
    }

    private boolean isClownOrStronger() {
        int seq = getSequence();
        return seq != -1 && seq <= 8;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity hitEntity = entityHitResult.getEntity();
        Entity owner = this.getOwner();

        if (owner != null && hitEntity.equals(owner)) {
            return;
        }

        if (this.getWorld().isClient) return;

        if (isClownOrStronger()) {
            float totalDamage = BASE_DAMAGE;

            if (this.isOnFire()) {
                hitEntity.setOnFireFor(5);
                totalDamage += FIRE_DAMAGE_BONUS;
            }

            boolean damaged;
            if (owner instanceof LivingEntity) {
                damaged = hitEntity.damage(this.getDamageSources().thrown(this, owner), totalDamage);
            } else {
                damaged = hitEntity.damage(this.getDamageSources().magic(), totalDamage);
            }

            if (damaged) {
                if (hitEntity instanceof LivingEntity livingHit) {
                    livingHit.takeKnockback(0.3, this.getX() - hitEntity.getX(), this.getZ() - hitEntity.getZ());
                }

                if (this.piercedEntities < this.customPierceLevel) {
                    this.piercedEntities++;

                    this.setVelocity(this.getVelocity().multiply(0.85));
                } else {
                    this.discard();
                }
            } else {
                this.setVelocity(this.getVelocity().multiply(-0.1));
                this.setYaw(this.getYaw() + 180.0F);
                this.prevYaw += 180.0F;
            }
        } else {
            spawnItemAt(this.getPos());
            this.discard();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        Entity owner = this.getOwner();

        if (owner instanceof PlayerEntity player && player.isSneaking()) {
            int seq = getSequence();
            if (seq != -1 && seq <= 7) {
                if (!this.getWorld().isClient) {
                    this.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, player.getSoundCategory(), 1.0F, 1.0F);
                    player.requestTeleport(this.getX(), this.getY(), this.getZ());
                    this.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, player.getSoundCategory(), 0.5F, 2.0F);
                }
                this.discard();
                return;
            }
        }

        if (isClownOrStronger()) {
            super.onBlockHit(blockHitResult);
        } else {
            if (!this.getWorld().isClient) {
                Vec3d hitPos = blockHitResult.getPos();
                Vec3d offset = Vec3d.of(blockHitResult.getSide().getVector()).multiply(0.2);
                spawnItemAt(hitPos.add(offset));
            }
            this.discard();
        }
    }

    private void spawnItemAt(Vec3d pos) {
        ItemEntity item = new ItemEntity(this.getWorld(), pos.x, pos.y, pos.z, this.asItemStack());
        item.setVelocity(0, 0.1, 0);
        item.setPickupDelay(10);
        this.getWorld().spawnEntity(item);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(ModItems.THROWING_CARD);
    }

    @Override
    public ItemStack getStack() {
        return this.getItemStack();
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.INTENTIONALLY_EMPTY;
    }

    public boolean isStuckInGround() {
        return this.inGround;
    }
}