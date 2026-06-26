package net.untitledduckmod.common.neoforge;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.helper.MouthGuiHelper;
import net.untitledduckmod.common.helper.neoforge.NeoMouthGuiHelper;
import net.untitledduckmod.common.init.neoforge.*;
import net.untitledduckmod.common.platform.Services;

@Mod(DuckMod.MOD_ID)
public class DuckModForge {

    public DuckModForge(IEventBus bus) {
        DuckMod.preInit();
        new NeoEntityTypes(bus);
        new NeoItemsImpl(bus);
        new NeoSoundEvents(bus);
        new NeoStatusEffects(bus);
        new NeoPotions(bus);
        new NeoBiomeModifier(bus);
        new NeoMenus(bus);
        MouthGuiHelper.INSTANCE = new NeoMouthGuiHelper();
    }

    @EventBusSubscriber
    public static class ModSetup {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            DuckMod.postInit();
            DuckMod.postEntityInit();
        }

        @SubscribeEvent
        public static void spawnSetting(RegisterSpawnPlacementsEvent event) {
            Services.PLATFORM.setupSpawning(event);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
            Services.PLATFORM.registerAttributes(event);
        }

        @SubscribeEvent
        public static void buildContentsOfCreativeModeTab(BuildCreativeModeTabContentsEvent event) {
            Services.PLATFORM.setupItemGroups(event);
        }
    }
}
