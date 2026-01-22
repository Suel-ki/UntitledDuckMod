package net.untitledduckmod.common.init.fabric;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.untitledduckmod.common.init.ModPotions;

public class FabricModPotions extends ModPotions {

    public FabricModPotions() {
        super();
        FabricBrewingRecipeRegistryBuilder.BUILD.register(ModPotions::registerRecipes);
    }
}
