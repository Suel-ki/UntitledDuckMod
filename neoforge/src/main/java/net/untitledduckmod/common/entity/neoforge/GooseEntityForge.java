package net.untitledduckmod.common.entity.neoforge;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.neoforged.neoforge.fluids.FluidType;
import net.untitledduckmod.common.entity.GooseEntity;
import net.untitledduckmod.common.entity.WaterfowlEntity;

public class GooseEntityForge extends GooseEntity {

    public GooseEntityForge(EntityType<? extends WaterfowlEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void jumpInFluid(FluidType type) {
        this.setVelocity(this.getVelocity().add(0.0D, 0.04D, 0.0D));
    }
}