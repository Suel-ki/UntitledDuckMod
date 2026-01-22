package net.untitledduckmod.fabric;

import net.fabricmc.api.ModInitializer;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.init.*;
import net.untitledduckmod.common.init.fabric.FabricModPotions;
import net.untitledduckmod.common.platform.Services;

public class DuckModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DuckMod.preInit();
        new ModEntityTypes();
        new ModItems();
        new ModSoundEvents();
        new ModStatusEffects();
        new FabricModPotions();
        Services.PLATFORM.setupSpawning(null);
        Services.PLATFORM.registerAttributes(null);
        Services.PLATFORM.setupItemGroups(null);
        DuckMod.postInit();
        DuckMod.postEntityInit();
    }
}