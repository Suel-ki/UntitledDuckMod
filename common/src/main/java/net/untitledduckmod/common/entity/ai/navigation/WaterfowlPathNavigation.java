package net.untitledduckmod.common.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

/**
 * Ground navigation that also accepts a spot on open water as a destination.
 * <p>
 * The inherited check wants a solid block directly below the target, which nothing on a water
 * surface has, so goals that pick their destination through {@code DefaultRandomPos} - the random
 * stroll, above all - find nothing at all for a bird floating on a pond and it ends up sitting
 * still. Goals that navigate straight at something, like the tempt goal, never went through that
 * check and kept working, which is why only seeds used to get ducks moving.
 * <p>
 * This stays a {@link GroundPathNavigation} on purpose: land behaviour is unchanged, and
 * {@code FollowOwnerGoal} rejects any navigation that is neither ground nor flying.
 */
public class WaterfowlPathNavigation extends GroundPathNavigation {

    public WaterfowlPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        return super.isStableDestination(pos) || this.level.getBlockState(pos).getFluidState().is(FluidTags.ENTITY_FLOATABLE);
    }
}
