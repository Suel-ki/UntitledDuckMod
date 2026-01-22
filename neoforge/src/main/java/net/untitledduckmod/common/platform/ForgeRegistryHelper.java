package net.untitledduckmod.common.platform;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import net.untitledduckmod.common.entity.CustomSpawnGroup;
import net.untitledduckmod.common.entity.DuckEntity;
import net.untitledduckmod.common.entity.GooseEntity;
import net.untitledduckmod.common.init.ModEntityTypes;
import net.untitledduckmod.common.init.ModItems;
import net.untitledduckmod.common.init.ModTags;
import net.untitledduckmod.common.platform.service.IRegistryHelper;

public class ForgeRegistryHelper implements IRegistryHelper {

    public void registerAttributes(Object optionalEvent) {
        if (optionalEvent instanceof EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.getDuck(), DuckEntity.getDefaultAttributes().add(NeoForgeMod.SWIM_SPEED, DuckEntity.SWIM_SPEED_MULTIPLIER).build());
            event.put(ModEntityTypes.getGoose(), GooseEntity.getDefaultAttributes().add(NeoForgeMod.SWIM_SPEED, GooseEntity.SWIM_SPEED_MULTIPLIER).build());
        }
    }

    public void setupSpawning(Object optionalEvent) {
        if (optionalEvent instanceof RegisterSpawnPlacementsEvent event) {
            event.register(ModEntityTypes.getDuck(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DuckEntity::checkDuckSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
            event.register(ModEntityTypes.getGoose(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, GooseEntity::checkGooseSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
        }
    }

    public static void addBiomeSpawns(Holder<Biome> biome, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (biome.is(ModTags.BiomeTags.DUCK_BIOMES)) {
            builder.getMobSpawnSettings().getSpawner(CustomSpawnGroup.WATERFOWL.spawnGroup).add(new MobSpawnSettings.SpawnerData(ModEntityTypes.getDuck(), Services.CONFIG.duckMinGroupSize(), Services.CONFIG.duckMaxGroupSize()), Services.CONFIG.duckWeight());
        }
        if (biome.is(ModTags.BiomeTags.GOOSE_BIOMES)) {
            builder.getMobSpawnSettings().getSpawner(CustomSpawnGroup.WATERFOWL.spawnGroup).add(new MobSpawnSettings.SpawnerData(ModEntityTypes.getGoose(), Services.CONFIG.gooseMinGroupSize(), Services.CONFIG.gooseMaxGroupSize()), Services.CONFIG.gooseWeight());
        }
    }

    public void setupItemGroups(Object optionalEvent) {
        if (optionalEvent instanceof BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
                event.accept(ModItems.DUCK_SPAWN_EGG.get());
                event.accept(ModItems.GOOSE_SPAWN_EGG.get());
            } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                event.insertAfter(Items.BLUE_EGG.getDefaultInstance(), ModItems.DUCK_EGG.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                event.insertAfter(Items.BLUE_EGG.getDefaultInstance(), ModItems.GOOSE_EGG.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                event.accept(ModItems.DUCK_FEATHER.get());
                event.accept(ModItems.GOOSE_FOOT.get());
            } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                event.accept(ModItems.DUCK_EGG.get());
                event.accept(ModItems.GOOSE_EGG.get());
            } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
                event.accept(ModItems.RAW_DUCK.get());
                event.accept(ModItems.COOKED_DUCK.get());
                event.accept(ModItems.RAW_GOOSE.get());
                event.accept(ModItems.COOKED_GOOSE.get());
            } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(ModItems.DUCK_SACK.get());
                event.accept(ModItems.EMPTY_DUCK_SACK.get());
            }
        }
    }
}
