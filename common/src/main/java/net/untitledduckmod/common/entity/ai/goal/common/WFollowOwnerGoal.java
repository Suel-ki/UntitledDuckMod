package net.untitledduckmod.common.entity.ai.goal.common;

import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.untitledduckmod.common.entity.WaterfowlEntity;

public class WFollowOwnerGoal extends FollowOwnerGoal {
    private final WaterfowlEntity entity;
    public WFollowOwnerGoal(WaterfowlEntity tameable, double speed, float minDistance, float maxDistance) {
        super(tameable, speed, minDistance, maxDistance);
        this.entity = tameable;
    }

    @Override
    public boolean canUse() {
        return entity.tamedFollowOwner() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.entity.tamedFollowOwner() && super.canContinueToUse();
    }

}
