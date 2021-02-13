package net.untitledduckmod;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class DuckEntity extends AnimalEntity implements IAnimatable {
    public static final String EGG_LAY_TIME_TAG = "duckEggLayTime";
    public static final String VARIANT_TAG = "duckVariant";
    protected static final TrackedData<Byte> VARIANT = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.BYTE);

    protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final byte ANIMATION_IDLE = 0;
    protected static final byte ANIMATION_CLEAN = 1;
    protected static final byte ANIMATION_SIT = 2;
    private final AnimationFactory factory = new AnimationFactory(this);
    private static final AnimationBuilder WALK_ANIM = new AnimationBuilder().addAnimation("walk", true);
    private static final AnimationBuilder IDLE_ANIM = new AnimationBuilder().addAnimation("idle", true);
    private static final AnimationBuilder SWIM_ANIM = new AnimationBuilder().addAnimation("swim", true);
    private static final AnimationBuilder SWIM_IDLE_ANIM = new AnimationBuilder().addAnimation("idle_swim", true);
    private static final AnimationBuilder CLEAN_ANIM = new AnimationBuilder().addAnimation("clean");
    private static final AnimationBuilder SWIM_CLEAN_ANIM = new AnimationBuilder().addAnimation("clean_swim");
    private static final AnimationBuilder SIT_ANIM = new AnimationBuilder().addAnimation("sit", true);
    private static final AnimationBuilder FLY_ANIM = new AnimationBuilder().addAnimation("fly", true);

    private static final Ingredient BREEDING_INGREDIENT = Ingredient.ofItems(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
    private static final int MIN_EGG_LAY_TIME = 6000;
    private static final int MAX_EGG_LAY_TIME = 12000;
    private int eggLayTime;
    private boolean isFlapping;

    public DuckEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        eggLayTime = random.nextInt(MIN_EGG_LAY_TIME) + (MAX_EGG_LAY_TIME - MIN_EGG_LAY_TIME);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
    }

    public static DefaultAttributeContainer.Builder getDefaultAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 7.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2D);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
        return false;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        byte variant = (byte) random.nextInt(2); // Chooses between 0 and 1 randomly
        this.dataTracker.startTracking(VARIANT, variant);
        this.dataTracker.startTracking(ANIMATION, ANIMATION_IDLE);
    }

    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);
        tag.putByte(VARIANT_TAG, getVariant());
        tag.putInt(EGG_LAY_TIME_TAG, eggLayTime);
    }

    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        setVariant(tag.getByte(VARIANT_TAG));
        if (tag.contains(EGG_LAY_TIME_TAG)) {
            this.eggLayTime = tag.getInt(EGG_LAY_TIME_TAG);
        }
    }

    public byte getVariant() {
        return dataTracker.get(VARIANT);
    }

    public void setVariant(byte variant) {
        dataTracker.set(VARIANT, variant);
    }

    public void setAnimation(byte animation) {
        dataTracker.set(ANIMATION, animation);
    }

    public byte getAnimation() {
        return dataTracker.get(ANIMATION);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new DuckSwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.4D));
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0D));
        this.goalSelector.add(3, new TemptGoal(this, 1.0D, false, BREEDING_INGREDIENT));
        this.goalSelector.add(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.add(5, new WanderAroundGoal(this, 1.0D));
        this.goalSelector.add(5, new DuckCleaningGoal(this));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    public boolean isBreedingItem(ItemStack stack) {
        return BREEDING_INGREDIENT.test(stack);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!world.isClient && isAlive() && !isBaby() && --eggLayTime <= 0) {
            this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            this.dropItem(ModItems.getDuckEgg());
            this.eggLayTime = random.nextInt(MIN_EGG_LAY_TIME) + (MAX_EGG_LAY_TIME - MIN_EGG_LAY_TIME);
        }

        // Play flapping/fly animation when falling
        //System.out.printf("client: %s water: %s, onGround: %s, velocity: %s\n", world.isClient, isTouchingWater(), onGround, velocity);
        if(world.isClient && !isTouchingWater() && !this.onGround) {
            isFlapping = true;
        } else {
            isFlapping = false;
        }

        // Slow fall speed
        Vec3d velocity = this.getVelocity();
        if (!world.isClient && !this.onGround && velocity.y < 0.0D) {
            this.setVelocity(velocity.multiply(1.0D, 0.6D, 1.0D));
        }
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityTypes.getDuck().create(world);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 2, this::predicate));
    }

    public boolean lookingAround() {
        return getAnimation() != ANIMATION_CLEAN;
    }

    @SuppressWarnings("rawtypes")
    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        float limbSwingAmount = event.getLimbSwingAmount();
        boolean isMoving = !(limbSwingAmount > -0.05F && limbSwingAmount < 0.05F);
        boolean inWater = isTouchingWater();
        AnimationController controller = event.getController();
        if (isFlapping) {
            controller.setAnimation(FLY_ANIM);
            return PlayState.CONTINUE;
        }
        if (getAnimation() == ANIMATION_CLEAN) {
            controller.setAnimation(inWater ? SWIM_CLEAN_ANIM : CLEAN_ANIM);
            return PlayState.CONTINUE;
        }
        if (inWater) {
            controller.setAnimation(isMoving ? SWIM_ANIM : SWIM_IDLE_ANIM);
        } else {
            controller.setAnimation(isMoving ? WALK_ANIM : IDLE_ANIM);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
