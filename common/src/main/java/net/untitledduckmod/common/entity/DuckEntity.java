package net.untitledduckmod.common.entity;


import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.cache.animation.keyframeevent.ParticleKeyframeData;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.*;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.untitledduckmod.common.entity.ai.goal.common.EatGoal;
import net.untitledduckmod.common.entity.ai.goal.common.WFollowOwnerGoal;
import net.untitledduckmod.common.entity.ai.goal.common.SwimGoal;
import net.untitledduckmod.common.init.*;
import net.untitledduckmod.common.platform.Services;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class DuckEntity extends WaterfowlEntity implements VibrationSystem, AnimationController.KeyframeEventHandler<DuckEntity, ParticleKeyframeData> {
    public static final String IS_FROM_SACK_TAG = "isFromSack";

    private static final EntityDataAccessor<Boolean> DANCING = SynchedEntityData.defineId(DuckEntity.class, EntityDataSerializers.BOOLEAN);

    public static final byte ANIMATION_DIVE = 2;
    private static final RawAnimation SWIM_CLEAN_ANIM = RawAnimation.begin().thenPlay("clean_swim").thenPlay("idle_swim");
    private static final RawAnimation DIVE_ANIM = RawAnimation.begin().thenPlay("dive").thenPlay("idle_swim");
    private static final RawAnimation DANCE_ANIM = RawAnimation.begin().thenPlay("dance");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean isFromSack = false;
    private @Nullable BlockPos jukeboxPos;
    private VibrationSystem.Data vibrationListenerData;
    private final VibrationSystem.User vibrationUser;
    private final DynamicGameEventListener<JukeboxEventListener> jukeboxEventHandler;

    public DuckEntity(EntityType<? extends WaterfowlEntity> entityType, Level world) {
        super(entityType, world);

        this.maxVariant = 4;
        this.vibrationUser = new VibrationUser();
        this.vibrationListenerData = new VibrationSystem.Data();
        this.jukeboxEventHandler = new DynamicGameEventListener<>(new JukeboxEventListener(this.vibrationUser.getPositionSource(), GameEvent.JUKEBOX_PLAY.value().notificationRadius()));
    }

    public static AttributeSupplier.Builder getDefaultAttributes() {
        return WaterfowlEntity.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 7.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.LUCK, 2.0D);
    }

    @Override
    public void setTame(boolean tamed, boolean updateAttributes) {
        super.setTame(tamed, updateAttributes);
        Objects.requireNonNull(getAttribute(Attributes.LUCK)).setBaseValue(2.0);
    }

    public static boolean checkDuckSpawnRules(EntityType<DuckEntity> duck, ServerLevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return world.getBlockState(pos.below()).is(ModTags.BlockTags.DUCKS_SPAWNABLE_ON) || world.getBlockState(pos.below()).getFluidState().is(FluidTags.WATER);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DANCING, false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putBoolean(IS_FROM_SACK_TAG, isFromSack);
        view.store("listener", Data.CODEC, this.vibrationListenerData);
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        setFromSack(view.getBooleanOr(IS_FROM_SACK_TAG, false));
        this.vibrationListenerData = view.read("listener", Data.CODEC).orElseGet(VibrationSystem.Data::new);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            Ticker.tick(this.level(), this.vibrationListenerData, this.vibrationUser);
        }
    }

    public void setJukeboxPlaying(BlockPos jukeboxPos, boolean playing) {
        if (playing) {
            if (!this.isDancing()) {
                this.jukeboxPos = jukeboxPos;
                this.setDancing(true);
            }
        } else if (jukeboxPos.equals(this.jukeboxPos) || this.jukeboxPos == null) {
            this.jukeboxPos = null;
            this.setDancing(false);
        }
    }

    public boolean isDancing() {
        return this.entityData.get(DANCING);
    }

    public void setDancing(boolean dancing) {
        if (!this.level().isClientSide() && this.isEffectiveAi() && (!dancing || !this.panicked)) {
            if (dancing) {
                setAnimation(ANIMATION_DANCE);
            } else {
                setAnimation(ANIMATION_IDLE);
            }
            this.entityData.set(DANCING, dancing);
        }
    }

    private boolean shouldStopDancing() {
        return this.jukeboxPos == null
                || !this.jukeboxPos.closerToCenterThan(this.position(), GameEvent.JUKEBOX_PLAY.value().notificationRadius())
                || !this.level().getBlockState(this.jukeboxPos).is(Blocks.JUKEBOX)
                || this.panicked;
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationListenerData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> user) {
        Level world = this.level();
        if (world instanceof ServerLevel serverWorld) {
            user.accept(this.jukeboxEventHandler, serverWorld);
        }
    }

    public boolean isEdibleFood(ItemStack stack) {
        return !stack.isEmpty() && getFoodIngredient().test(stack);
    }

    public static Ingredient getFoodIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.ItemTags.DUCK_FOOD));
    }

    public static Ingredient getBreedingIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.ItemTags.DUCK_BREEDING_FOOD));
    }

    public static Ingredient getTamingIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.ItemTags.DUCK_TAMING_FOOD));
    }

    @Override
    public int getMouthHolderType() {
        return 2;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimalPanicGoal(1.6D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new EatGoal(this));
        this.goalSelector.addGoal(3, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, getBreedingIngredient().or(getFoodIngredient()).or(getTamingIngredient()), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new WFollowOwnerGoal(this, 1.6D, 10.0F, 2.0F));
        this.goalSelector.addGoal(6, new CleanGoal(this));
        this.goalSelector.addGoal(6, new DiveGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.isTame()) {
            return getTamingIngredient().test(stack) || getBreedingIngredient().test(stack);
        }
        return getBreedingIngredient().test(stack);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            // Stop dancing under certain conditions
            if (this.isDancing() && this.shouldStopDancing() && this.tickCount % 20 == 0) {
                this.setDancing(false);
                this.jukeboxPos = null;
            }
        }
    }

    @Override
    protected void handlePanicAnimation() {
        // Trigger panic animation when being attacked or being on fire
        if (!panicked && getLastHurtByMob() != null || isOnFire()) {
            setAnimation(ANIMATION_PANIC);
            panicked = true;
        } else if (panicked && getLastHurtByMob() == null && !isOnFire()) {
            setAnimation(ANIMATION_IDLE);
            panicked = false;
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.getItem() == ModItems.EMPTY_DUCK_SACK.get()) {
            TagValueOutput duckData = TagValueOutput.createWithoutContext(errorReporter);

            if (saveAsPassenger(duckData)) {
                stackInHand.consume(1, player);

                ItemStack duckSack = new ItemStack(ModItems.DUCK_SACK.get());
                duckSack.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(duckData.buildResult()));

                if (stackInHand.isEmpty()) {
                    player.setItemInHand(hand, duckSack);
                } else if (!player.addItem(duckSack)) {
                    player.drop(duckSack, false);
                }
                this.level().playSound(null, blockPosition(), ModSoundEvents.DUCK_SACK_USE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            } else {
                LOGGER.error("Could not save duck data to duck sack!");
            }

            discard();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    protected boolean isTamableItem(ItemStack stack) {
        return getTamingIngredient().test(stack);
    }

    @Nullable
    @Override
    public DuckEntity getBreedOffspring(ServerLevel world, AgeableMob entity) {
        DuckEntity duckEntity = ModEntityTypes.getDuck().create(world, EntitySpawnReason.BREEDING);
        if (duckEntity != null && entity instanceof DuckEntity duck) {
            if (this.random.nextBoolean()) {
                duckEntity.setVariant(this.getVariant());
            } else {
                duckEntity.setVariant(duck.getVariant());
            }
            duckEntity.setBabyScale(getRandomBabyScale());
            if (this.isTame()) {
                duckEntity.setOwnerReference(this.getOwnerReference());
                duckEntity.setTame(true, true);
            }
        }
        return duckEntity;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        AnimationController<DuckEntity> controller = new AnimationController<>("controller", 2, this::predicate);
        controller.setParticleKeyframeHandler(this);
        controllerRegistrar.add(controller);
    }

    @Override
    protected SoundEvent getLayEggSound() {
        return ModSoundEvents.DUCK_LAY_EGG.get();
    }

    @Override
    public Item getEggItem() {
        return ModItems.DUCK_EGG.get();
    }

    @Override
    public boolean dropEggItem(ServerLevel world) {
        return this.dropFromGiftLootTable(world, ModLootTables.DUCK_LAY, this::spawnAtLocation);
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
            case ANIMATION_CLEAN -> controller.setAnimation(inWater ? SWIM_CLEAN_ANIM : CLEAN_ANIM);
            case ANIMATION_DIVE -> controller.setAnimation(DIVE_ANIM);
            case ANIMATION_DANCE -> controller.setAnimation(DANCE_ANIM);
            case ANIMATION_PANIC -> controller.setAnimation(PANIC_ANIM);
            case ANIMATION_EAT -> controller.setAnimation(EAT_ANIM);
            default -> {
                if (inWater) {
                    controller.setAnimation(isMoving ? SWIM_ANIM : SWIM_IDLE_ANIM);
                } else {
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
            this.playSound(ModSoundEvents.DUCKLING_AMBIENT.get(), 0.3f, getVoicePitch());
            return;
        }
        this.playSound(ModSoundEvents.DUCK_AMBIENT.get(), 0.10f, getVoicePitch());
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        if (isBaby()) {
            this.playSound(ModSoundEvents.DUCKLING_HURT.get(), 0.3f, getVoicePitch() + 0.25F);
            return;
        }
        this.playSound(ModSoundEvents.DUCK_HURT.get(), 0.10f, getVoicePitch() + 0.5F);
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        if (isBaby()) {
            return ModSoundEvents.DUCKLING_DEATH.get();
        }
        return ModSoundEvents.DUCK_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSoundEvents.DUCK_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    public void handle(KeyFrameEvent<DuckEntity, ParticleKeyframeData> event) {
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
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack.getItem()), pos.x, pos.y, pos.z,
                    vel.x, vel.y + 0.05D, vel.z);
        }
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || isFromSack;
    }

    public boolean isFromSack() {
        return isFromSack;
    }

    public void setFromSack(boolean fromSack) {
        isFromSack = fromSack;
    }

    @Override
    public ItemStack equipItemIfPossible(ServerLevel world, ItemStack equipment) {
        EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
        ItemStack itemStack = getMainHandItem();

        if (canHoldItem(equipment)) {
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
        // Tamed duck should not be traded with non-owners
        if (this.isTame() && !this.getMainHandItem().isEmpty() && item.getOwner() != null && !item.getOwner().equals(this.getOwner()))
            return;
        if (item.getOwner() == this) {
            return;
        }
        super.pickUpItem(world, item);
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        ItemStack mainHandStack = getMainHandItem();
        // If the main hand is empty, allow pickup if it's a breeding or fish item
        if (mainHandStack.isEmpty()) {
            return isFood(stack) || isTamableItem(stack);
        }

        // If the entity is tamed and the main hand is not empty, allow pickup if it's a breeding item
        if (this.isTame() && !mainHandStack.isEmpty()) {
            return isFood(stack);
        }

        // If the main hand has a fish item, allow pickup of a breeding item
        return isTamableItem(mainHandStack) && isFood(stack);
    }

    @Override
    public boolean canPickUpLoot() {
        // Duckling shouldn't pick up items
        if (isBaby()) {
            return false;
        }
        return !getMainHandItem().isEmpty() || isHungry();
    }

    public void fishing() {
        MinecraftServer server = this.level().getServer();
        if (!this.level().isClientSide() && server != null) {
            ServerLevel world = (ServerLevel) this.level();
            LootParams lootWorldContext = new LootParams.Builder(world)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withLuck((float) this.getAttributeValue(Attributes.LUCK))
                    .create(LootContextParamSets.FISHING);
            LootTable lootTable = server.reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
            List<ItemStack> list = lootTable.getRandomItems(lootWorldContext);
            for (ItemStack stack : list) {
                if (this.isTame() || isTamableItem(stack)) {
                    this.setItemInHand(InteractionHand.MAIN_HAND, stack);
                    this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, stack);
                    break;
                }
            }
        }
    }

    @Override
    public boolean tamedFollowOwner() {
        return !Services.CONFIG.duckTamedNotFollow();
    }

    @Override
    public float sanitizeScale(float scale) {
        if (Services.CONFIG.duckBabyRandomSize()) {
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

    private class VibrationUser implements VibrationSystem.User {

        private final PositionSource positionSource = new EntityPositionSource(DuckEntity.this, DuckEntity.this.getEyeHeight());

        @Override
        public int getListenerRadius() {
            return 10;
        }

        @Override
        public PositionSource getPositionSource() {
            return positionSource;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> gameEvent, GameEvent.Context context) {
            return !DuckEntity.this.isNoAi();
        }

        @Override
        public void onReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> gameEvent, @org.jspecify.annotations.Nullable Entity entity, @org.jspecify.annotations.Nullable Entity playerEntity, float distance) {
        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return ModGameEventTags.DUCK_CAN_LISTEN;
        }
    }

    private class JukeboxEventListener implements GameEventListener {
        private final PositionSource positionSource;
        private final int range;

        public JukeboxEventListener(PositionSource positionSource, int range) {
            this.positionSource = positionSource;
            this.range = range;
        }

        public PositionSource getListenerSource() {
            return this.positionSource;
        }

        public int getListenerRadius() {
            return this.range;
        }

        public boolean handleGameEvent(ServerLevel level, Holder<GameEvent> event, GameEvent.Context context, Vec3 emitterPos) {
            if (event == GameEvent.JUKEBOX_PLAY) {
                DuckEntity.this.setJukeboxPlaying(BlockPos.containing(emitterPos), true);
                return true;
            } else if (event == GameEvent.JUKEBOX_STOP_PLAY) {
                DuckEntity.this.setJukeboxPlaying(BlockPos.containing(emitterPos), false);
                return true;
            } else {
                return false;
            }
        }
    }

    static class CleanGoal extends Goal {
        private static final int ANIMATION_LENGTH = 32;
        private final DuckEntity duck;
        private int cleanTime;
        private int nextCleanTime;

        public CleanGoal(DuckEntity duck) {
            this.duck = duck;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
            nextCleanTime = duck.tickCount + (10 * 20 + duck.getRandom().nextInt(10) * 20);
        }

        @Override
        public boolean canUse() {
            // Don't clean if not near player
            if (nextCleanTime > duck.tickCount || duck.getNoActionTime() >= 100 || duck.getAnimation() != DuckEntity.ANIMATION_IDLE) {
                return false;
            }
            return duck.getRandom().nextInt(40) == 0;
        }

        @Override
        public void start() {
            cleanTime = ANIMATION_LENGTH;
            duck.setAnimation(DuckEntity.ANIMATION_CLEAN);
            nextCleanTime = duck.tickCount + (10 * 20 + duck.getRandom().nextInt(10) * 20);
        }

        @Override
        public void stop() {
            duck.setAnimation(DuckEntity.ANIMATION_IDLE);
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

    static class DiveGoal extends Goal {
        private static final int ANIMATION_LENGTH = 32;
        private final DuckEntity duck;
        private int diveTime;
        private int nextDiveTime;

        public DiveGoal(DuckEntity duck) {
            this.duck = duck;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
            nextDiveTime = duck.tickCount + (8 * 20 + duck.getRandom().nextInt(10) * 20);
        }

        @Override
        public boolean canUse() {
            // Don't dive if not in water
            if (nextDiveTime > duck.tickCount || duck.getNoActionTime() >= 100 || !duck.isInWater() || duck.getAnimation() != DuckEntity.ANIMATION_IDLE) {
                return false;
            }
            return duck.getRandom().nextInt(40) == 0 && duck.getMainHandItem().isEmpty();
        }

        @Override
        public void start() {
            //System.out.printf("[%d:%d] Start diving\n", nextDiveTime, duck.age);
            diveTime = ANIMATION_LENGTH;
            duck.setAnimation(DuckEntity.ANIMATION_DIVE);
            nextDiveTime = duck.tickCount + (8 * 20 + duck.getRandom().nextInt(10) * 20);
        }

        @Override
        public void stop() {
            duck.setAnimation(DuckEntity.ANIMATION_IDLE);
        }

        @Override
        public boolean canContinueToUse() {
            return diveTime >= 0;
        }

        @Override
        public void tick() {
            diveTime--;
            // Play splash sound 10 ticks = 0.5 seconds into the animation
            if (diveTime == 32 - 10) {
                if (duck.getRandom().nextDouble() < Services.CONFIG.duckFishingChange()) {
                    duck.fishing();
                }
                duck.playSound(SoundEvents.GENERIC_SPLASH, 1.0f, 1.0f);
            }
        }
    }
}