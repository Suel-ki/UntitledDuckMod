package net.untitledduckmod.common.init.fabric;

import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.untitledduckmod.common.init.ModPotions;

public class FabricModPotions extends ModPotions {

    public FabricModPotions() {
        super();
        FabricPotionBrewingBuilder.BUILD.register(ModPotions::registerRecipes);
    }
}
