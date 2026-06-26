package net.untitledduckmod.client.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.untitledduckmod.client.renderer.entity.DuckRenderer;
import net.untitledduckmod.client.renderer.entity.GooseRenderer;
import net.untitledduckmod.client.screen.MouthScreen;
import net.untitledduckmod.common.init.ModEntityTypes;
import net.untitledduckmod.common.init.ModMenus;

public class DuckModFabricClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRenderers.register(ModEntityTypes.getDuck(), DuckRenderer::new);
        EntityRenderers.register(ModEntityTypes.getDuckEgg(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.getGoose(), GooseRenderer::new);
        EntityRenderers.register(ModEntityTypes.getGooseEgg(), ThrownItemRenderer::new);
        MenuScreens.register(ModMenus.MOUTH.get(), MouthScreen::new);
    }
}