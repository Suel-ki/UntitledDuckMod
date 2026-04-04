package net.untitledduckmod.common.platform;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.untitledduckmod.common.entity.CustomSpawnGroup;
import net.untitledduckmod.common.entity.DuckEntity;
import net.untitledduckmod.common.entity.GooseEntity;
import net.untitledduckmod.common.init.ModEntityTypes;
import net.untitledduckmod.common.init.ModItems;
import net.untitledduckmod.common.init.ModTags;
import net.untitledduckmod.common.platform.service.IRegistryHelper;

public class FabricRegistryHelper implements IRegistryHelper {
    public  void registerAttributes(Object optionalEvent) {
        FabricDefaultAttributeRegistry.register(ModEntityTypes.getDuck(), DuckEntity.getDefaultAttributes());
        FabricDefaultAttributeRegistry.register(ModEntityTypes.getGoose(), GooseEntity.getDefaultAttributes());
    }

    public void setupSpawning(Object optionalEvent) {
        SpawnPlacements.register(ModEntityTypes.getDuck(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DuckEntity::checkDuckSpawnRules);
        SpawnPlacements.register(ModEntityTypes.getGoose(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, GooseEntity::checkGooseSpawnRules);
        // BiomeModifications is experimental but approved
        BiomeModifications.addSpawn(context -> context.hasTag(ModTags.BiomeTags.DUCK_BIOMES), CustomSpawnGroup.WATERFOWL.spawnGroup, ModEntityTypes.getDuck(),
                Services.CONFIG.duckWeight(),
                Services.CONFIG.duckMinGroupSize(),
                Services.CONFIG.duckMaxGroupSize());
        BiomeModifications.addSpawn(context -> context.hasTag(ModTags.BiomeTags.GOOSE_BIOMES), CustomSpawnGroup.WATERFOWL.spawnGroup, ModEntityTypes.getGoose(),
                Services.CONFIG.gooseWeight(),
                Services.CONFIG.gooseMinGroupSize(),
                Services.CONFIG.gooseMaxGroupSize());
    }

    public void setupItemGroups(Object optionalEvent) {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(content -> {
            content.accept(ModItems.DUCK_SPAWN_EGG.get());
            content.accept(ModItems.GOOSE_SPAWN_EGG.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(content -> {
            content.insertAfter(Items.BLUE_EGG, ModItems.DUCK_EGG.get());
            content.insertAfter(Items.BLUE_EGG, ModItems.GOOSE_EGG.get());
            content.accept(ModItems.DUCK_FEATHER.get());
            content.accept(ModItems.GOOSE_FOOT.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(content -> {
            content.accept(ModItems.RAW_DUCK.get());
            content.accept(ModItems.COOKED_DUCK.get());
            content.accept(ModItems.RAW_GOOSE.get());
            content.accept(ModItems.COOKED_GOOSE.get());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(ModItems.DUCK_SACK.get());
            content.accept(ModItems.EMPTY_DUCK_SACK.get());
        });
    }
}
