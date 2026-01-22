package net.untitledduckmod.common.entity.ai.goal.common;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.untitledduckmod.common.entity.WaterfowlEntity;

import java.util.EnumSet;

public class SwimGoal extends Goal {
    private final WaterfowlEntity entity;

    public SwimGoal(WaterfowlEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP));
        entity.getNavigation().setCanFloat(true);
    }

    @Override
    public boolean canUse() {
        return entity.isInWater() && entity.getFluidHeight(FluidTags.WATER) > entity.getFluidJumpThreshold() || entity.isInLava();
    }

    @Override
    public void tick() {
        if (entity.getRandom().nextFloat() < 0.8F) {
            entity.getJumpControl().jump();
        }
    }
}
