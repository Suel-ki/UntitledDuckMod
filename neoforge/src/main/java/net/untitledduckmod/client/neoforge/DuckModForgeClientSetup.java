package net.untitledduckmod.client.neoforge;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.untitledduckmod.client.renderer.entity.DuckRenderer;
import net.untitledduckmod.client.renderer.entity.GooseRenderer;
import net.untitledduckmod.client.screen.MouthScreen;
import net.untitledduckmod.common.init.ModEntityTypes;
import net.untitledduckmod.common.init.ModScreenHandlers;

@EventBusSubscriber
public class DuckModForgeClientSetup {

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.getDuck(), DuckRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.getDuckEgg(), FlyingItemEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.getGoose(), GooseRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.getGooseEgg(), FlyingItemEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModScreenHandlers.MOUTH.get(), MouthScreen::new);
    }
}