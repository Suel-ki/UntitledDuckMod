package net.untitledduckmod.common.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.untitledduckmod.common.init.ModEntityTypes;
import net.untitledduckmod.common.init.ModItems;

public class WaterfowlEggEntity extends ThrowableItemProjectile {
    private final EntityType<? extends WaterfowlEntity> mobEntityType;
    private static final EntityDimensions EMPTY_DIMENSIONS = EntityDimensions.fixed(0.0F, 0.0F);

    public WaterfowlEggEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
        this.mobEntityType = ModEntityTypes.getDuck();
    }

    public WaterfowlEggEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world, ItemStack stack, double x, double y, double z) {
        // Used for client side rendering, so mobEntityType doesn't matter
        super(entityType, x, y, z, world, stack);
        this.mobEntityType = ModEntityTypes.getDuck();
    }

    public WaterfowlEggEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world, LivingEntity owner, ItemStack stack, EntityType<? extends WaterfowlEntity> mobEntityType) {
        // This is the only constructor used on server side that matters
        super(entityType, owner, world, stack);
        this.mobEntityType = mobEntityType;
    }

    public WaterfowlEggEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world, ItemStack stack, double x, double y, double z, EntityType<? extends WaterfowlEntity> mobEntityType) {
        // Used for dispensing the item
        super(entityType, x, y, z, world, stack);
        this.mobEntityType = mobEntityType;
    }

    public void handleEntityEvent(byte status) {
        if (status == EntityEvent.DEATH) {
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level() instanceof ServerLevel serverWorld) {
            entityHitResult.getEntity().hurtServer(serverWorld, this.damageSources().thrown(this, this.getOwner()), 0.0F);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        Level world = this.level();
        if (!world.isClientSide()) {
            if (this.random.nextInt(8) == 0) {
                int i = 1;
                if (this.random.nextInt(32) == 0) {
                    i = 4;
                }

                for (int j = 0; j < i; ++j) {
                    WaterfowlEntity waterfowl = mobEntityType.create(this.level(), EntitySpawnReason.TRIGGERED);
                    if (waterfowl != null) {
                        waterfowl.setAge(-24000);
                        waterfowl.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                        waterfowl.setVariant((byte) this.level().getRandom().nextInt(2)); // Randomly choose between the two variants
                        if (!waterfowl.fudgePositionAfterSizeChange(EMPTY_DIMENSIONS)) {
                            break;
                        }
                        world.addFreshEntity(waterfowl);
                    }
                }
            }

            world.broadcastEntityEvent(this, EntityEvent.DEATH);
            this.discard();
        }
    }

    protected Item getDefaultItem() {
        if (mobEntityType == ModEntityTypes.getDuck()) {
            return ModItems.DUCK_EGG.get();
        } else {
            return ModItems.GOOSE_EGG.get();
        }
    }
}
