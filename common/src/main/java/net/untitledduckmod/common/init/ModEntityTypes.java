package net.untitledduckmod.common.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.entity.CustomSpawnGroup;
import net.untitledduckmod.common.entity.DuckEntity;
import net.untitledduckmod.common.entity.GooseEntity;
import net.untitledduckmod.common.entity.WaterfowlEggEntity;

import java.util.function.Supplier;

public class ModEntityTypes {

    public static Supplier<EntityType<DuckEntity>> DUCK;
    public static Supplier<EntityType<WaterfowlEggEntity>> DUCK_EGG;
    public static Supplier<EntityType<GooseEntity>> GOOSE;
    public static Supplier<EntityType<WaterfowlEggEntity>> GOOSE_EGG;

    public ModEntityTypes() {
        DUCK = registerEntity("duck", () -> EntityType.Builder.of(DuckEntity::new, CustomSpawnGroup.WATERFOWL.spawnGroup).sized(0.6f, 0.6f).clientTrackingRange(10));
        DUCK_EGG = registerEntity("duck_egg", () -> EntityType.Builder.<WaterfowlEggEntity>of(WaterfowlEggEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
        GOOSE = registerEntity("goose", () -> EntityType.Builder.of(GooseEntity::new, CustomSpawnGroup.WATERFOWL.spawnGroup).sized(0.7f, 1.2f).clientTrackingRange(10));
        GOOSE_EGG = registerEntity("goose_egg", () -> EntityType.Builder.<WaterfowlEggEntity>of(WaterfowlEggEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    }

    public  <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> builder) {
        var register = Registry.register(
                BuiltInRegistries.ENTITY_TYPE, DuckMod.id(name),
                builder.get().build(ResourceKey.create(Registries.ENTITY_TYPE, DuckMod.id(name)))
        );
        return () -> register;
    }

    public static EntityType<DuckEntity> getDuck() {
        return DUCK.get();
    }

    public static EntityType<WaterfowlEggEntity> getDuckEgg() {
        return DUCK_EGG.get();
    }

    public static EntityType<GooseEntity> getGoose() {
        return GOOSE.get();
    }

    public static EntityType<WaterfowlEggEntity> getGooseEgg() {
        return GOOSE_EGG.get();
    }
}
