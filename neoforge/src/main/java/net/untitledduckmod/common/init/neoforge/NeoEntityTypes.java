package net.untitledduckmod.common.init.neoforge;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.entity.CustomSpawnGroup;
import net.untitledduckmod.common.entity.neoforge.DuckEntityForge;
import net.untitledduckmod.common.entity.neoforge.GooseEntityForge;
import net.untitledduckmod.common.init.ModEntityTypes;

import java.util.function.Supplier;

public class NeoEntityTypes extends ModEntityTypes {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, DuckMod.MOD_ID);

    public NeoEntityTypes(IEventBus eventBus)
    {
        ENTITY_TYPES.register(eventBus);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> builder)
    {
        if (name.equals("duck")) {
            builder = () -> (EntityType.Builder<T>) EntityType.Builder.of(DuckEntityForge::new, CustomSpawnGroup.WATERFOWL.spawnGroup).sized(0.6f, 0.6f).clientTrackingRange(10);
        } else if (name.equals("goose")) {
            builder = () -> (EntityType.Builder<T>) EntityType.Builder.of(GooseEntityForge::new, CustomSpawnGroup.WATERFOWL.spawnGroup).sized(0.7f, 1.2f).clientTrackingRange(10);
        }
        Supplier<EntityType.Builder<T>> finalBuilder = builder;
        return ENTITY_TYPES.register(name, () -> finalBuilder.get().build(ResourceKey.create(Registries.ENTITY_TYPE, DuckMod.id(name))));
    }
}
