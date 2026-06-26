package net.untitledduckmod.fabric;

import net.fabricmc.api.ModInitializer;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.helper.MouthGuiHelper;
import net.untitledduckmod.common.helper.fabric.FabricMouthGuiHelper;
import net.untitledduckmod.common.init.*;
import net.untitledduckmod.common.init.fabric.FabricMenus;
import net.untitledduckmod.common.init.fabric.FabricPotions;
import net.untitledduckmod.common.platform.Services;

public class DuckModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DuckMod.preInit();
        new ModEntityTypes();
        new ModItems();
        new ModSoundEvents();
        new ModStatusEffects();
        new FabricPotions();
        new FabricMenus();
        Services.PLATFORM.setupSpawning(null);
        Services.PLATFORM.registerAttributes(null);
        Services.PLATFORM.setupItemGroups(null);
        MouthGuiHelper.INSTANCE = new FabricMouthGuiHelper();
        DuckMod.postInit();
        DuckMod.postEntityInit();
    }
}