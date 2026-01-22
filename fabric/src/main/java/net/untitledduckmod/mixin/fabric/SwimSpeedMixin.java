package net.untitledduckmod.mixin.fabric;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.untitledduckmod.common.entity.DuckEntity;
import net.untitledduckmod.common.entity.GooseEntity;
import net.untitledduckmod.common.init.ModEntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class SwimSpeedMixin extends Entity {
    public SwimSpeedMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyArg(
            method = "travelInWater",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 0),
            index = 0
    )
    public float mixin(float original) {
        if (getType() == ModEntityTypes.getDuck()) {
            return original * DuckEntity.SWIM_SPEED_MULTIPLIER;
        }
        if (getType() == ModEntityTypes.getGoose()) {
            return original * GooseEntity.SWIM_SPEED_MULTIPLIER;
        }
        return original;
    }
}
