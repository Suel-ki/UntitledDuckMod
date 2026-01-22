package net.untitledduckmod.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.untitledduckmod.common.entity.GooseEntity;
import net.untitledduckmod.common.init.ModStatusEffects;
import net.untitledduckmod.common.platform.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractIllager.class)
public abstract class IllagerIntimidationMixin extends Raider {
    protected IllagerIntimidationMixin(EntityType<? extends Raider> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "registerGoals")
    private void addGoals(CallbackInfo info) {
        String entityID = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).toString();
        if (!Services.CONFIG.intimidationBlacklist().contains(entityID)) {
            this.goalSelector.addGoal(0, new AvoidEntityGoal<>(this, LivingEntity.class, entity -> entity.hasEffect(ModStatusEffects.intimidation), 12.0F, 1.0D, 1.1D, EntitySelector.NO_SPECTATORS));
        }
        this.goalSelector.addGoal(0, new AvoidEntityGoal<>(this, GooseEntity.class, 10.0F, 1.0D, 1.1D));
    }
}
