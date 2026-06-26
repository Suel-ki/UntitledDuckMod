package net.untitledduckmod.common.entity;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.untitledduckmod.common.entity.inv.ListenableSimpleContainer;
import net.untitledduckmod.common.helper.MouthGuiHelper;
import net.untitledduckmod.common.platform.Services;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;

public abstract class WaterfowlEntity extends TamableAnimal implements GeoAnimatable, MouthHoldable {
    protected ProblemReporter errorReporter;
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final float BABY_MIN_SCALE = 0.25f;
    public static final float BABY_MAX_SCALE = 0.7f;
    public static final String EGG_LAY_TIME_TAG = "EggLayTime";
    public static final String HELD_FOOD_TICK_TAG = "HeldFoodTick";
    public static final String RANDOM_FORCE_EAT_TICK_TAG = "RandomForceEatTick";
    public static final String MOUTH_ITEM_TAG = "MouthItem";
    public static final String VARIANT_TAG = "Variant";
    public static final String BABY_SCALE_TAG = "BabyScale";
    public static final float SWIM_SPEED_MULTIPLIER = 3.0f;
    public static final DataTicket<Boolean> LOOKING_AROUND_TICKET = DataTicket.create("look_around", Boolean.class);
    public static final DataTicket<Byte> VARIANT_TICKET = DataTicket.create("waterfowl_variant", Byte.class);
    public static final DataTicket<Float> BABY_SCALE_TICKET = DataTicket.create("waterfowl_baby_scale", Float.class);
    protected static final EntityDataAccessor<Byte> VARIANT = SynchedEntityData.defineId(WaterfowlEntity.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Float> BABY_SCALE = SynchedEntityData.defineId(WaterfowlEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Byte> ANIMATION = SynchedEntityData.defineId(WaterfowlEntity.class, EntityDataSerializers.BYTE);
    public static final byte ANIMATION_IDLE = 0;
    public static final byte ANIMATION_CLEAN = 1;
    public static final byte ANIMATION_DANCE = 3;
    public static final byte ANIMATION_PANIC = 4;
    public static final byte ANIMATION_EAT = 5;

    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenPlay("walk");
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");
    protected static final RawAnimation SWIM_ANIM = RawAnimation.begin().thenPlay("swim");
    protected static final RawAnimation SWIM_IDLE_ANIM = RawAnimation.begin().thenPlay("idle_swim");
    protected static final RawAnimation PANIC_ANIM = RawAnimation.begin().thenPlay("panic");
    protected static final RawAnimation FLY_ANIM = RawAnimation.begin().thenPlay("fly");
    protected static final RawAnimation CLEAN_ANIM = RawAnimation.begin().thenPlay("clean").thenPlay("idle");
    protected static final RawAnimation EAT_ANIM = RawAnimation.begin().thenPlay("eat");
    protected static final RawAnimation SIT_ANIM = RawAnimation.begin().thenPlay("sit");

    public final ListenableSimpleContainer mouthInventory = new ListenableSimpleContainer(1);

    protected int maxVariant = 3;
    protected int eggLayTime;
    private int heldFoodTick = 0;
    private int randomForceEatTick = 0;
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    public float flapping = 1.0F;
    private float nextFlap = 1.0F;
    protected boolean panicked = false;
    protected WaterfowlEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
        eggLayTime = getRandomLayTime();
        this.setPathfindingMalus(PathType.WATER, 0.0f);

        errorReporter = new ProblemReporter.ScopedCollector(() -> entityType.getDescription().toString(), LOGGER);

        this.mouthInventory.addListener(inventory -> {
            ItemStack stackInGui = inventory.getItem(0);
            this.setItemSlot(EquipmentSlot.MAINHAND, stackInGui);
        });
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData) {
        var babyScale = getRandomBabyScale();
        var variant = getRandomVariant();

        this.setVariant(variant); // Randomly choose between the two variants
        this.setBabyScale(babyScale);
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, (byte) 0);
        builder.define(ANIMATION, ANIMATION_IDLE);
        builder.define(BABY_SCALE, getRandomBabyScale());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte(VARIANT_TAG, getVariant());
        output.putInt(EGG_LAY_TIME_TAG, eggLayTime);
        output.putInt(HELD_FOOD_TICK_TAG, heldFoodTick);
        output.putInt(RANDOM_FORCE_EAT_TICK_TAG, randomForceEatTick);
        output.putFloat(BABY_SCALE_TAG, getBabyScale());
        output.storeNullable(MOUTH_ITEM_TAG, ItemStack.OPTIONAL_CODEC, this.mouthInventory.getItem(0));
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        setVariant(view.getByteOr(VARIANT_TAG, getRandomVariant()));
        setBabyScale(view.getFloatOr(BABY_SCALE_TAG, getRandomBabyScale()));
        this.eggLayTime = view.getIntOr(EGG_LAY_TIME_TAG, getRandomLayTime());
        this.heldFoodTick = view.getIntOr(HELD_FOOD_TICK_TAG, 0);
        this.randomForceEatTick = view.getIntOr(RANDOM_FORCE_EAT_TICK_TAG, generateRandomForceEatTick());
        this.mouthInventory.setItem(0, view.read(MOUTH_ITEM_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }

    @Override
    public ListenableSimpleContainer getMouthInventory() {
        return this.mouthInventory;
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (slot == EquipmentSlot.MAINHAND && !ItemStack.matches(this.mouthInventory.getItem(0), stack)) {
            this.mouthInventory.setItem(0, stack);
        }
    }

    @Override
    public void setTame(boolean tamed, boolean updateAttributes) {
        super.setTame(tamed, updateAttributes);
        if (tamed) {
            Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20.0);
            setHealth(20.0F);
        } else {
            Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(7.0);
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        ItemStack mainHand = getMainHandItem();
        if (isEdibleFood(mainHand)) {
            heldFoodTick++;
            if (randomForceEatTick == 0) {
                randomForceEatTick = generateRandomForceEatTick();
            }
        } else {
            heldFoodTick = 0;
            randomForceEatTick = 0;
        }
    }

    private int generateRandomForceEatTick() {
        int min = Services.CONFIG.forceEatRandomMinTick();
        int max = Services.CONFIG.forceEatRandomMaxTick();
        int actualMin = Math.min(min, max);
        int actualMax = Math.max(min, max);
        return random.nextIntBetweenInclusive(actualMin, actualMax);
    }

    public abstract boolean isEdibleFood(ItemStack stack);

    public byte getVariant() {
        return entityData.get(VARIANT);
    }

    public void setVariant(byte variant) {
        entityData.set(VARIANT, variant);
    }

    public float getRandomBabyScale() {
        return random.nextFloat() * (BABY_MAX_SCALE - BABY_MIN_SCALE) + BABY_MIN_SCALE;
    }

    public float getBabyScale() {
        return entityData.get(BABY_SCALE);
    }

    public void setBabyScale(float scale) {
        entityData.set(BABY_SCALE, scale);
    }

    public byte getRandomVariant() {
        return (byte) random.nextInt(maxVariant);
    }

    public int getRandomLayTime() {
        int min = Services.CONFIG.minEggLayTime();
        int max = Services.CONFIG.maxEggLayTime();
        return random.nextInt(min) + (max - min);
    }

    public byte getAnimation() {
        return entityData.get(ANIMATION);
    }

    public void setAnimation(byte animation) {
        entityData.set(ANIMATION, animation);
    }

    public boolean isHungry() {
        return getHealth() <= getMaxHealth() - 0.5f;
    }

    public void tryEating() {
        assert !this.level().isClientSide();

        ItemStack stack = getMainHandItem();
        stack.shrink(1);
        playSound(SoundEvents.GENERIC_EAT.value(), 0.5F + 0.5F * (float) this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        if (stack.isEmpty()) {
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        if (isHungry()) {
            FoodProperties food = stack.get(DataComponents.FOOD);
            heal(food != null ? food.nutrition() : Services.CONFIG.foodHealingValue());
        }
    }

    public boolean lookingAround() {
        return getAnimation() != ANIMATION_CLEAN || getAnimation() != ANIMATION_EAT;
    }

    protected abstract SoundEvent getLayEggSound();

    public abstract Item getEggItem();

    public abstract boolean dropEggItem(ServerLevel world);

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level() instanceof ServerLevel world) {
            // Lay egg
            if (isAlive() && !isBaby() && --eggLayTime <= 0) {
                if (this.dropEggItem(world)) {
                    this.playSound(this.getLayEggSound(), 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                    this.gameEvent(GameEvent.ENTITY_PLACE);
                }
                this.eggLayTime = getRandomLayTime();
            }

            this.oFlap = this.flap;
            this.oFlapSpeed = this.flapSpeed;
            this.flapSpeed += (this.onGround() ? -1.0F : 4.0F) * 0.3F;
            this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
            if (!this.onGround() && this.flapping < 1.0F) {
                this.flapping = 1.0F;
            }

            this.flapping *= 0.9F;
            // Slow fall speed when flapping
            Vec3 vec3 = this.getDeltaMovement();
            if (!this.onGround() && vec3.y < (double)0.0F) {
                this.setDeltaMovement(vec3.multiply(1.0F, 0.6, 1.0F));
            }

            this.flap += this.flapping * 2.0F;

            // Trigger panic animation when being attacked or being on fire
            this.handlePanicAnimation();
        }

    }

    // Play flapping/fly animation when falling
    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    protected void onFlap() {
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    protected abstract void handlePanicAnimation();

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // TODO: Cleanup
        if (player.isShiftKeyDown() && this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide()) {
                MouthGuiHelper.openMouthMenu(player, this);
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (this.level().isClientSide() && (!this.isBaby() || !this.isFood(stack))) {
            if (this.isTame() && this.isOwnedBy(player)) {
                return InteractionResult.SUCCESS;
            } else {
                return !isTamableItem(stack) || !(this.getHealth() < this.getMaxHealth()) && this.isTame() ? InteractionResult.PASS : InteractionResult.SUCCESS;
            }
        } else {
            if (isTame() && this.isOwnedBy(player)) {
                if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
                    FoodProperties food = stack.get(DataComponents.FOOD);
                    this.usePlayerItem(player, hand, stack);
                    heal(food != null ? food.nutrition() : Services.CONFIG.foodHealingValue());
                    return InteractionResult.CONSUME;
                }
                InteractionResult actionResult = super.mobInteract(player, hand);
                if ((!actionResult.consumesAction() || this.isBaby())) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                    return InteractionResult.SUCCESS.withoutItem();
                }
                return actionResult;
            } else if (tryTaming(player, stack)) {
                stack.consume(1, player);
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, EntityEvent.TAMING_SUCCEEDED);
                } else {
                    this.level().broadcastEntityEvent(this, EntityEvent.TAMING_FAILED);
                }
                return InteractionResult.SUCCESS_SERVER;
            } else {
                return super.mobInteract(player, hand);
            }
        }
    }

    protected boolean tryTaming(Player player, ItemStack stack) {
        return this.isTamable(player, stack);
    }

    protected abstract boolean isTamableItem(ItemStack stack);

    protected boolean isTamable(Player player, ItemStack stack) {
        return this.isTamableItem(stack) && !this.isTame();
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> fluid) {
        // This bypasses forge modifying jump depending on swim speed
        if (this.getNavigation().canFloat()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.03999999910593033D, 0.0D));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.3D, 0.0D));
        }
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader world) {
        return world.isUnobstructed(this);
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        } else {
            this.setOrderedToSit(false);
            return super.hurtServer(world, source, amount);
        }
    }

    @Override
    public double getFluidJumpThreshold() {
        return this.isBaby() ? 0.1D : 0.2D;
    }

    public int getEggLayTime() {
        return this.eggLayTime;
    }

    public int getHeldFoodTick() {
        return this.heldFoodTick;
    }

    public int getRandomForceEatTick() {
        return randomForceEatTick;
    }

    public boolean tamedFollowOwner() {
        return true;
    }

}
