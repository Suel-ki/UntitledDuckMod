package net.untitledduckmod.common.entity.ai.goal.common;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import net.untitledduckmod.common.platform.Services;

import java.util.EnumSet;

public class EatGoal extends Goal {
    private static final int STARTING_DELAY = 10;
    private static final int ANIMATION_LENGTH = 22;
    private static final int ANIMATION_EAT_POINT = ANIMATION_LENGTH - 13;
    private final WaterfowlEntity entity;
    private int animationTime;
    private int delayTime;

    public EatGoal(WaterfowlEntity entity) {
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        // TODO: Should throttle this?
        ItemStack stack = entity.getMainHandItem();
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        boolean isEdible = entity.isEdibleFood(stack);
        if (!isEdible) {
            return false;
        }
        return entity.isHungry() || (Services.CONFIG.enableForceEat() && entity.getHeldFoodTick() >= entity.getRandomForceEatTick());
    }

    @Override
    public void start() {
        entity.getNavigation().stop();
        animationTime = ANIMATION_LENGTH;
        delayTime = STARTING_DELAY;
    }

    @Override
    public void stop() {
        entity.setAnimation(WaterfowlEntity.ANIMATION_IDLE);
    }

    @Override
    public boolean canContinueToUse() {
        return animationTime >= 0;
    }

    @Override
    public void tick() {
        if (delayTime > 0) {
            delayTime--;
            if (delayTime == 0) {
                entity.setAnimation(WaterfowlEntity.ANIMATION_EAT);
            }
            return;
        }
        animationTime--;
        if (animationTime == ANIMATION_EAT_POINT) {
            entity.tryEating();
        }
    }
}

