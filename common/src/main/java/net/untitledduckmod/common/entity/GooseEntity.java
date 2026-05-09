package net.untitledduckmod.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.untitledduckmod.common.entity.ai.goal.common.EatGoal;
import net.untitledduckmod.common.entity.ai.goal.common.WFollowOwnerGoal;
import net.untitledduckmod.common.entity.ai.goal.common.SwimGoal;
import net.untitledduckmod.common.init.*;
import net.untitledduckmod.common.platform.Services;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.animation.state.AnimationTest;
import software.bernie.geckolib.animation.state.KeyFrameEvent;
import software.bernie.geckolib.cache.animation.keyframeevent.ParticleKeyframeData;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.function.Predicate;

public class GooseEntity extends WaterfowlEntity implements NeutralMob, AnimationController.KeyframeEventHandler<GooseEntity, ParticleKeyframeData> {
    private static final EntityDataAccessor<Long> ANGER_TIME = SynchedEntityData.defineId(GooseEntity.class, EntityDataSerializers.LONG);
    private static final UniformInt ANGER_TIME_RANGE = UniformInt.of(20, 39);
    private @Nullable EntityReference<LivingEntity> persistentAngerTarget;
    public static final byte ANIMATION_BITE = 2;
    public static final int ANIMATION_BITE_LEN = 22;
    public static final byte ANIMATION_INTIMIDATE = 6;

    private static final RawAnimation INTIMIDATE_ANIM = RawAnimation.begin().thenPlay("intimidate");
    private static final RawAnimation HONK_ANIM = RawAnimation.begin().thenPlay("honk");
    private static final RawAnimation BITE_ANIM = RawAnimation.begin().thenPlay("bite");
    private static final RawAnimation CHARGE_ANIM = RawAnimation.begin().thenPlay("charge");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int animationTimer = 0;

    public GooseEntity(EntityType<? extends WaterfowlEntity> entityType, Level world) {
        super(entityType, world);
        this.setCanPickUpLoot(true);
    }

    public static boolean checkGooseSpawnRules(EntityType<GooseEntity> goose, ServerLevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return world.getBlockState(pos.below()).is(ModTags.BlockTags.GEESE_SPAWNABLE_ON) || world.getBlockState(pos.below()).getFluidState().is(FluidTags.WATER);
    }

    public static AttributeSupplier.Builder getDefaultAttributes() {
        return WaterfowlEntity.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 7.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    public void setTame(boolean tamed, boolean updateAttributes) {
        super.setTame(tamed, updateAttributes);
        Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(2.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER_TIME, 0L);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        this.addPersistentAngerSaveData(view);
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.readPersistentAngerSaveData(this.level(), view);
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status == 100) {
            for (int i = 0; i < 7; ++i) {
                double d = this.random.nextGaussian() * 0.02D;
                double e = this.random.nextGaussian() * 0.02D;
                double f = this.random.nextGaussian() * 0.02D;
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d, e, f);
            }
        }
        super.handleEntityEvent(status);
    }

    public boolean isEdibleFood(ItemStack stack) {
        return !stack.isEmpty() && getFoodIngredient().test(stack);
    }

    public static Ingredient getFoodIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.ItemTags.GOOSE_FOOD));
    }

    public static Ingredient getBreedingIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.ItemTags.GOOSE_BREEDING_FOOD));
    }

    public static Ingredient getTamingIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.ItemTags.GOOSE_TAMING_FOOD));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new GooseEscapeDangerGoal(this, 2D));

        this.goalSelector.addGoal(2, new IntimidateMobsGoal(this));

        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new SitWhenOrderedToGoal(this));

        this.goalSelector.addGoal(4, new EatGoal(this));

        this.goalSelector.addGoal(4, new StealItemGoal(this));

        this.goalSelector.addGoal(5, new PickupFoodGoal(this));
        this.goalSelector.addGoal(5, new SpearUseGoal(this, 2.0D,2.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(6, new GooseMeleeAttackGoal(this, 1.5D, true));

        this.goalSelector.addGoal(7, new TemptGoal(this, 1.0D, getBreedingIngredient().or(getFoodIngredient()).or(getTamingIngredient()), false));
        this.goalSelector.addGoal(8, new FollowParentGoal(this, 1.1D));

        this.goalSelector.addGoal(9, new WFollowOwnerGoal(this, 1.6D, 10.0F, 2.0F));

        // Idle behaviour when there is nothing too urgent
        this.goalSelector.addGoal(9, new CleanGoal(this));

        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new GooseRevengeGoal(this).setAlertOthers());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        if (isAngry()) {
            return false;
        }
        if (this.isTame()) {
            return getTamingIngredient().test(stack) || getBreedingIngredient().test(stack);
        }
        return getBreedingIngredient().test(stack);
    }

    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        if (this.level().isClientSide()) {
            return;
        }
        Entity holder = newStack.getEntityRepresentation();
        if (holder != null && isAngry() && holder instanceof ItemEntity ie) {
            if (ie.getOwner() != null) {
                stopBeingAngry();
            }
        }
        super.onEquipItem(slot, oldStack, newStack);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            // Tick animation timer
            if (animationTimer > 0) {
                animationTimer--;
                if (animationTimer == 0) {
                    setAnimation(ANIMATION_IDLE);
                }
            }
        }
    }

    @Override
    protected void handlePanicAnimation() {
        if (!panicked && (((getHealth() < getMaxHealth() / 2) || isBaby()) && (getLastHurtByMob() != null || isOnFire()))) {
            setAnimation(ANIMATION_PANIC);
            panicked = true;
        } else if (panicked && getLastHurtByMob() == null && !isOnFire()) {
            setAnimation(ANIMATION_IDLE);
            panicked = false;
        }
    }

    protected boolean tryTaming(Player player, ItemStack stack) {
        if (isAngry() && this.level() instanceof ServerLevel world) {
            // Peace goose when angry with food
            if (getFoodIngredient().test(stack)) {
                ItemStack newStack = stack.copy();
                newStack.setCount(1);
                stack.consume(1, player);
                if (!equipItemIfPossible(world, newStack).isEmpty()) {
                    stopBeingAngry();
                }
            }
            return false;
        }
        return super.tryTaming(player, stack);
    }

    protected boolean isTamableItem(ItemStack stack) {
        return getTamingIngredient().test(stack);
    }

    protected boolean isTamable(Player player, ItemStack stack) {
        return super.isTamable(player, stack) && !this.isAngry();
    }

    @Override
    public void stopBeingAngry() {
        this.setLastHurtByMob(null);
        this.setPersistentAngerTarget(null);
        this.setTarget(null);
        this.setPersistentAngerEndTime(-1L);
        this.level().broadcastEntityEvent(this, (byte) 100);
    }

    @Override
    public ItemStack equipItemIfPossible(ServerLevel world, ItemStack equipment) {
        EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
        ItemStack itemStack = getMainHandItem();
        if (getFoodIngredient().test(equipment) || this.canHoldItem(equipment)) {
            if (!itemStack.isEmpty()) {
                ItemEntity itemEntity = this.spawnAtLocation(world, itemStack);
                if (itemEntity != null) {
                    itemEntity.setPickUpDelay(40);
                }
            }

            this.setItemSlotAndDropWhenKilled(equipmentSlot, equipment);
            return equipment;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected void pickUpItem(ServerLevel world, ItemEntity item) {
        // Don't pick up threw/spat items
        if (item.getOwner() == this) {
            return;
        }
        if (this.isTame()) {
            if (!Objects.equals(getOwner(), item.getOwner())) {
                return;
            }
        }
        super.pickUpItem(world, item);
    }

    private boolean isWeapon(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES);
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        ItemStack mainHandStack = getMainHandItem();

        if (isWeapon(stack) && isTame()) {
            return getOwner() != null && this.getHealth() == this.getMaxHealth();
        }
        if ((!getFoodIngredient().test(mainHandStack) && getFoodIngredient().test(stack))) {
            return true;
        }
        if (mainHandStack.isEmpty()) {
            return !stack.is(ModItems.GOOSE_EGG.get());
        }
        return false;
    }

    @Override
    public boolean canPickUpLoot() {
        // Gosling shouldn't pick up items
        if (isBaby()) {
            return false;
        }
        return true;
       // return super.canPickUpLoot();
    }

    @Nullable
    @Override
    public GooseEntity getBreedOffspring(ServerLevel world, AgeableMob entity) {
        GooseEntity gooseEntity = ModEntityTypes.getGoose().create(world, EntitySpawnReason.BREEDING);
        if (gooseEntity != null && entity instanceof GooseEntity goose) {
            if (this.random.nextBoolean()) {
                gooseEntity.setVariant(this.getVariant());
            } else {
                gooseEntity.setVariant(goose.getVariant());
            }
            gooseEntity.setBabyScale(getRandomBabyScale());
            if (this.isTame()) {
                goose.setOwnerReference(this.getOwnerReference());
                goose.setTame(true, true);
            }
        }
        return gooseEntity;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        AnimationController<GooseEntity> controller = new AnimationController<>("controller", 2, this::predicate);
        controller.setParticleKeyframeHandler(this);
        controllerRegistrar.add(controller);
    }

    @Override
    protected SoundEvent getLayEggSound() {
        return ModSoundEvents.GOOSE_LAY_EGG.get();
    }

    @Override
    public Item getEggItem() {
        return ModItems.GOOSE_EGG.get();
    }

    @Override
    public boolean dropEggItem(ServerLevel world) {
        return this.dropFromGiftLootTable(world, ModLootTables.GOOSE_LAY, this::spawnAtLocation);
    }

    @SuppressWarnings("SameReturnValue")
    private <P extends GeoAnimatable> PlayState predicate(AnimationTest<P> event) {
        boolean isMoving = event.isMoving();
        boolean inWater = isInWater();
        AnimationController<P> controller = event.controller();
        if (isFlapping()) {
            controller.setAnimation(FLY_ANIM);
            return PlayState.CONTINUE;
        }
        if (isInSittingPose()) {
            controller.setAnimation(SIT_ANIM);
            return PlayState.CONTINUE;
        }

        byte currentAnimation = getAnimation();
        switch (currentAnimation) {
            case ANIMATION_BITE -> {
                controller.setAnimation(BITE_ANIM);
                animationTimer = ANIMATION_BITE_LEN;
            }
            case ANIMATION_INTIMIDATE -> controller.setAnimation(INTIMIDATE_ANIM);
            case ANIMATION_EAT -> controller.setAnimation(EAT_ANIM);
            case ANIMATION_CLEAN -> controller.setAnimation(inWater ? SWIM_ANIM : CLEAN_ANIM);
            case ANIMATION_DANCE -> controller.setAnimation(HONK_ANIM);
            case ANIMATION_PANIC -> controller.setAnimation(PANIC_ANIM);
            default -> {
                if (inWater) {
                    controller.setAnimation(isMoving ? SWIM_ANIM : SWIM_IDLE_ANIM);
                } else {
                    if (isAggressive()) {
                        controller.setAnimation(CHARGE_ANIM);
                        return PlayState.CONTINUE;
                    }
                    controller.setAnimation(isMoving ? WALK_ANIM : IDLE_ANIM);
                }
            }
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void playAmbientSound() {
        if (isBaby()) {
            this.playSound(ModSoundEvents.GOSLING_AMBIENT.get(), 0.3f, getVoicePitch());
            return;
        }
        if (isAngry()) {
            this.playSound(ModSoundEvents.GOOSE_HONK.get(), 0.6f, getVoicePitch());
        }
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ServerLevel world, ItemStack stack, float yOffset) {
        ItemEntity droppedStack = super.spawnAtLocation(world, stack, yOffset);
        if (droppedStack == null) {
            return null;
        }
        droppedStack.setThrower(this);
        return droppedStack;
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        if (isBaby()) {
            this.playSound(ModSoundEvents.GOSLING_HURT.get(), 0.3f, getVoicePitch() + 0.25F);
            return;
        }
        this.playSound(ModSoundEvents.GOOSE_HONK.get(), 0.7f, getVoicePitch());
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        if (isBaby()) {
            return ModSoundEvents.GOSLING_DEATH.get();
        }
        return ModSoundEvents.GOOSE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSoundEvents.DUCK_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    public long getPersistentAngerEndTime() {
        return entityData.get(ANGER_TIME);
    }

    @Override
    public void setPersistentAngerEndTime(long ticks) {
       entityData.set(ANGER_TIME, ticks);
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getPersistentAngerTarget() {
        return persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setPersistentAngerEndTime(ANGER_TIME_RANGE.sample(this.random));
    }

    @Override
    public void handle(KeyFrameEvent<GooseEntity, ParticleKeyframeData> event) {
        ItemStack stack = getMainHandItem();
        if (stack == ItemStack.EMPTY) {
            return;
        }
        for (int i = 0; i < 8; ++i) {
            Vec3 vel = new Vec3(((double) this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            vel = vel.xRot(-this.getXRot() * 0.017453292F);
            vel = vel.yRot(-this.getYRot() * 0.017453292F);

            Vec3 rotationVec = Vec3.directionFromRotation(0, yBodyRot);
            Vec3 pos = new Vec3(this.getX() + rotationVec.x / 2.0D, getEyeY() - 0.2D, this.getZ() + rotationVec.z / 2.0D);
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), pos.x, pos.y, pos.z,
                    vel.x, vel.y + 0.05D, vel.z);
        }
    }

    public boolean wantsToPickupItem() {
        return !isOrderedToSit() && getLastHurtByMob() == null && getTarget() == null;
    }

    public boolean isHungry() {
        return isAngry() || super.isHungry();
    }

    @Override
    public boolean tamedFollowOwner() {
        return !Services.CONFIG.gooseTamedNotFollow();
    }

    @Override
    public float sanitizeScale(float scale) {
        if (Services.CONFIG.gooseBabyRandomSize()) {
            float babyScale = getBabyScale();
            float modelScale;
            if (isBaby()) {
                modelScale = babyScale;
            } else {
                modelScale = 0.8f + babyScale * 0.5f;
            }
            return modelScale;
        }
        return super.sanitizeScale(scale);
    }

    static class CleanGoal extends Goal {
        private static final int ANIMATION_LENGTH = 32;
        private final GooseEntity goose;
        private int cleanTime;
        private int nextCleanTime;

        public CleanGoal(GooseEntity goose) {
            this.goose = goose;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
            nextCleanTime = goose.tickCount + (10 * 20 + goose.getRandom().nextInt(10) * 20);
        }

        @Override
        public boolean canUse() {
            // Don't clean if not near player
            if (nextCleanTime > goose.tickCount || goose.getNoActionTime() >= 100 || goose.getAnimation() != GooseEntity.ANIMATION_IDLE) {
                return false;
            }
            return goose.getRandom().nextInt(40) == 0;
        }

        @Override
        public void start() {
            cleanTime = ANIMATION_LENGTH;
            goose.setAnimation(GooseEntity.ANIMATION_CLEAN);
            nextCleanTime = goose.tickCount + (10 * 20 + goose.getRandom().nextInt(10) * 20);
        }

        @Override
        public void stop() {
            goose.setAnimation(GooseEntity.ANIMATION_IDLE);
        }

        @Override
        public boolean canContinueToUse() {
            return cleanTime >= 0;
        }

        @Override
        public void tick() {
            cleanTime--;
        }
    }

    static class GooseEscapeDangerGoal extends PanicGoal {
        private final GooseEntity goose;

        public GooseEscapeDangerGoal(GooseEntity goose, double speed) {
            super(goose, speed);
            this.goose = goose;
        }

        @Override
        public boolean canUse() {
            return ((goose.getHealth() < goose.getMaxHealth() / 2) || goose.isBaby()) && super.canUse();
        }

        @Override
        protected boolean findRandomPosition() {
            if (this.goose.getLastDamageSource() != null) {
                Entity attacker = this.goose.getLastDamageSource().getEntity();
                if (attacker != null) {
                    Vec3 awayPos = LandRandomPos.getPosAway(this.goose, 16, 7, attacker.position());
                    if (awayPos != null) {
                        this.posX = awayPos.x;
                        this.posY = awayPos.y;
                        this.posZ = awayPos.z;
                        return true;
                    }
                }
            }
            return super.findRandomPosition();
        }

        @Override
        public void start() {
            super.start();

            if (!this.goose.getMainHandItem().isEmpty() && this.goose.getMainHandItem().has(DataComponents.KINETIC_WEAPON)) {
                this.goose.startUsingItem(InteractionHand.MAIN_HAND);
            }
        }

        @Override
        public void stop() {
            super.stop();

            if (!this.goose.getMainHandItem().isEmpty()) {
                this.goose.stopUsingItem();
            }
        }
    }

    static class IntimidateMobsGoal extends Goal {
        private static final int STARTING_DELAY = 10;
        private static final int ANIMATION_LENGTH = 25;
        private static final double INTIMIDATE_DISTANCE = 12;
        private final GooseEntity goose;
        private int animationTime;
        private int delayTime;
        private int cooldown;
        protected Entity targetEntity;
        private Vec3 originalLocation;

        public IntimidateMobsGoal(GooseEntity goose) {
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
            this.goose = goose;
        }

        @Override
        public boolean canUse() {
            // Check if creeper nearby
            if (cooldown-- > 0) {
                return false;
            }
            if (goose.tickCount % 5 == 0) {
                targetEntity = getServerLevel(this.goose).getNearestEntity(AbstractIllager.class, TargetingConditions.forCombat(), goose, goose.getX(), goose.getY(), goose.getZ(), goose.getBoundingBox().inflate(INTIMIDATE_DISTANCE, 3, INTIMIDATE_DISTANCE));
                return targetEntity != null;
            }
            return false;
        }

        @Override
        public void start() {
            goose.getNavigation().stop();
            animationTime = ANIMATION_LENGTH;
            delayTime = STARTING_DELAY;

            originalLocation = goose.position();
            goose.getNavigation().moveTo(targetEntity, 1.2);
        }

        @Override
        public void stop() {
            goose.setAnimation(GooseEntity.ANIMATION_IDLE);
            goose.getNavigation().moveTo(originalLocation.x, originalLocation.y, originalLocation.z, 1.2);
            cooldown = 20;
            targetEntity = null;
        }

        @Override
        public boolean canContinueToUse() {
            return targetEntity.isAlive() && goose.distanceToSqr(originalLocation) <= INTIMIDATE_DISTANCE * INTIMIDATE_DISTANCE && animationTime >= 0;
        }

        @Override
        public void tick() {
            goose.getLookControl().setLookAt(targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ());
            if (delayTime > 0) {
                delayTime--;
                if (delayTime == 0) {
                    goose.getNavigation().stop();
                    goose.setAnimation(GooseEntity.ANIMATION_INTIMIDATE);
                    goose.playSound(ModSoundEvents.GOOSE_HONK.get(), 1.0f, 1.0f);
                }
                return;
            }
            animationTime--;
        }
    }

    static class GooseMeleeAttackGoal extends MeleeAttackGoal {
        private final GooseEntity goose;

        public GooseMeleeAttackGoal(GooseEntity gooseEntity, double speed, boolean pauseWhenIdle) {
            super(gooseEntity, speed, pauseWhenIdle);
            this.goose = gooseEntity;
        }

        private static final int ANIMATION_LEN = GooseEntity.ANIMATION_BITE_LEN;
        private static final int ANIMATION_ATTACK = 5; // Point in animation where to attack, counted from back
        private int animationTimer = 0;

        @Override
        public boolean canUse() {
            return !goose.isBaby() && !goose.getMainHandItem().has(DataComponents.KINETIC_WEAPON) && super.canUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (canPerformAttack(target) && animationTimer <= 0) {
                goose.setAnimation(GooseEntity.ANIMATION_BITE);
                animationTimer = ANIMATION_LEN;
                goose.playSound(ModSoundEvents.GOOSE_HONK.get(), 0.8f, 1.2f);
            }
            if (animationTimer > 0) {
                animationTimer--;
                if (animationTimer == ANIMATION_ATTACK) {
                    this.mob.doHurtTarget(getServerLevel(this.goose), target);
                }
                if (animationTimer == 0) {
                    goose.setAnimation(GooseEntity.ANIMATION_IDLE);
                }
            }
        }

        @Override
        public void stop() {
            super.stop();
            goose.setAnimation(GooseEntity.ANIMATION_IDLE);
            animationTimer = 0;
        }
    }

    static class PickupFoodGoal extends Goal {
        public static final double SPEED = 1.3D;
        private final GooseEntity goose;

        public PickupFoodGoal(GooseEntity goose) {
            this.goose = goose;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        private static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = (itemEntity) -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive() && GooseEntity.getFoodIngredient().test(itemEntity.getItem());

        public boolean canUse() {
            if (goose.isBaby() || !goose.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            } else {
                if (!goose.wantsToPickupItem()) {
                    return false;
                } else if (goose.getRandom().nextInt(10) != 0) {
                    return false;
                } else {
                    List<ItemEntity> list = goose.level().getEntitiesOfClass(ItemEntity.class, goose.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), PICKABLE_DROP_FILTER);
                    return !list.isEmpty() && goose.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
                }
            }
        }

        public void tick() {
            ItemStack itemStack = goose.getItemBySlot(EquipmentSlot.MAINHAND);
            if (itemStack.isEmpty()) {
                start();
            }
        }

        public void start() {
            List<ItemEntity> list = goose.level().getEntitiesOfClass(ItemEntity.class, goose.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), PICKABLE_DROP_FILTER);
            if (!list.isEmpty()) {
                goose.getNavigation().moveTo(list.getFirst(), SPEED);
            }
        }
    }

    static class GooseRevengeGoal extends HurtByTargetGoal {
        private final GooseEntity goose;

        public GooseRevengeGoal(GooseEntity goose) {
            super(goose);
            this.goose = goose;
        }

        @Override
        public boolean canContinueToUse() {
            return goose.getTarget() != null && super.canContinueToUse();
        }
    }

    static class StealItemGoal extends Goal {
        private static final double SPEED = 1.3D;
        private final GooseEntity goose;

        Player targetPlayer;
        int nextStealTime;
        private ItemStack playerHandStack;

        public StealItemGoal(GooseEntity goose) {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.goose = goose;
            nextStealTime = goose.tickCount + goose.getRandom().nextInt(20 * 60) + 20 * 60;
        }

        @Override
        public boolean canUse() {
            // Only wild geese with nothing in their beak(main hand) will attempt to steal items from you
            if (goose.isBaby() || goose.isTame() || !goose.getMainHandItem().isEmpty()) {
                return false;
            }
            if (goose.tickCount <= nextStealTime) {
                return false;
            }
            // Throttle starts
            if (goose.getRandom().nextInt(10) != 0) {
                return false;
            }
            targetPlayer = goose.level().getNearestPlayer(goose.getX(), goose.getY(), goose.getZ(), 10.0D, true);
            if (targetPlayer == null) {
                nextStealTime = goose.tickCount + goose.getRandom().nextInt(10 * 20) + 10 * 20;
                return false;
            }

            playerHandStack = targetPlayer.getMainHandItem();
            return GooseEntity.getFoodIngredient().test(playerHandStack);
        }

        @Override
        public boolean canContinueToUse() {
            if (targetPlayer == null) {
                return false;
            }

            playerHandStack = targetPlayer.getMainHandItem();
            return GooseEntity.getFoodIngredient().test(playerHandStack);
        }

        @Override
        public void start() {
            goose.getNavigation().moveTo(targetPlayer, SPEED);
        }

        @Override
        public void stop() {
            nextStealTime = goose.tickCount + goose.getRandom().nextInt(20 * 60) + 20 * 60;
            targetPlayer = null;
        }

        @Override
        public void tick() {
            if (goose.distanceTo(targetPlayer) <= 2.0f) {
                ItemStack stolenItemStack = playerHandStack.copy();
                stolenItemStack.setCount(1);
                if (!goose.equipItemIfPossible(getServerLevel(goose), stolenItemStack).isEmpty()) {
                    playerHandStack.shrink(1);
                }

                goose.setItemInHand(InteractionHand.MAIN_HAND, stolenItemStack);

                stop();
            } else {
                // Continue going to the player
                goose.getNavigation().moveTo(targetPlayer, SPEED);
            }
        }
    }

    static class SpearUseGoal extends Goal {
        static final int MIN_REPOSITION_DISTANCE = 6;
        static final int MAX_REPOSITION_DISTANCE = 7;
        static final int MIN_COOLDOWN_DISTANCE = 9;
        static final int MAX_COOLDOWN_DISTANCE = 11;
        private static final double MAX_FLEEING_TIME = reducedTickDelay(100);
        private final GooseEntity mob;
        private @Nullable SpearUseState state;
        private final double speedModifierWhenCharging;
        private final double speedModifierWhenRepositioning;
        private final float approachDistanceSq;
        private final float targetInRangeRadiusSq;

        public SpearUseGoal(GooseEntity mob, double speedModifierWhenCharging, double speedModifierWhenRepositioning, float approachDistance, float targetInRangeRadius) {
            this.mob = mob;
            this.speedModifierWhenCharging = speedModifierWhenCharging;
            this.speedModifierWhenRepositioning = speedModifierWhenRepositioning;
            this.approachDistanceSq = approachDistance * approachDistance;
            this.targetInRangeRadiusSq = targetInRangeRadius * targetInRangeRadius;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            return this.ableToAttack() && !this.mob.isUsingItem();
        }

        private boolean ableToAttack() {
            return this.mob.getTarget() != null && this.mob.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
        }

        private int getKineticWeaponUseDuration() {
            int durationTicks = Optional.ofNullable(this.mob.getMainHandItem().get(DataComponents.KINETIC_WEAPON)).map(KineticWeapon::computeDamageUseDuration).orElse(0);
            return reducedTickDelay(durationTicks);
        }

        public boolean canContinueToUse() {
            return this.state != null && !this.state.done && this.ableToAttack();
        }

        public void start() {
            super.start();
            this.mob.setAggressive(true);
            this.state = new SpearUseState();
        }

        public void stop() {
            super.stop();
            this.mob.getNavigation().stop();
            this.mob.setAggressive(false);
            this.state = null;
            this.mob.stopUsingItem();
        }

        public void tick() {
            if (this.state != null) {
                LivingEntity target = this.mob.getTarget();
                double targetDistSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
                Entity mount = this.mob.getRootVehicle();
                float speedModifier = 1.0F;
                if (mount instanceof Mob) {
                    Mob vehicleMob = (Mob) mount;
                    speedModifier = vehicleMob.chargeSpeedModifier();
                }

                int mountDistance = this.mob.isPassenger() ? 2 : 0;
                this.mob.lookAt(target, 30.0F, 30.0F);
                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
                if (this.state.notEngagedYet()) {
                    if (targetDistSqr > (double) this.approachDistanceSq) {
                        this.mob.getNavigation().moveTo(target, (double) speedModifier * this.speedModifierWhenRepositioning);
                        return;
                    }

                    this.state.startEngagement(this.getKineticWeaponUseDuration());
                    this.mob.startUsingItem(InteractionHand.MAIN_HAND);
                }

                if (this.state.tickAndCheckEngagement()) {
                    this.mob.stopUsingItem();
                    double distance = Math.sqrt(targetDistSqr);
                    this.state.awayPos = LandRandomPos.getPosAway(this.mob, Math.max(0.0F, (double) (9 + mountDistance) - distance), Math.max(1.0F, (double) (11 + mountDistance) - distance), 7, target.position());
                    this.state.fleeingTime = 1;
                }

                if (!this.state.tickAndCheckFleeing()) {
                    if (this.state.awayPos != null) {
                        this.mob.getNavigation().moveTo(this.state.awayPos.x, this.state.awayPos.y, this.state.awayPos.z, (double) speedModifier * this.speedModifierWhenRepositioning);
                        if (this.mob.getNavigation().isDone()) {
                            if (this.state.fleeingTime > 0) {
                                this.state.done = true;
                                return;
                            }

                            this.state.awayPos = null;
                        }
                    } else {
                        this.mob.getNavigation().moveTo(target, (double) speedModifier * this.speedModifierWhenCharging);
                        if (targetDistSqr < (double) this.targetInRangeRadiusSq || this.mob.getNavigation().isDone()) {
                            double distance = Math.sqrt(targetDistSqr);
                            this.state.awayPos = LandRandomPos.getPosAway(this.mob, (double) (6 + mountDistance) - distance, (double) (7 + mountDistance) - distance, 7, target.position());
                        }
                    }
                }
            }
        }

        public static class SpearUseState {
            private int engageTime = -1;
            private int fleeingTime = -1;
            private @org.jspecify.annotations.Nullable Vec3 awayPos;
            private boolean done = false;

            public boolean notEngagedYet() {
                return this.engageTime < 0;
            }

            public void startEngagement(int spearDownTime) {
                this.engageTime = spearDownTime;
            }

            public boolean tickAndCheckEngagement() {
                if (this.engageTime > 0) {
                    --this.engageTime;
                    if (this.engageTime == 0) {
                        return true;
                    }
                }

                return false;
            }

            public boolean tickAndCheckFleeing() {
                if (this.fleeingTime > 0) {
                    ++this.fleeingTime;
                    if ((double) this.fleeingTime > MAX_FLEEING_TIME) {
                        this.done = true;
                        return true;
                    }
                }

                return false;
            }
        }
    }

}