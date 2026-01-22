package net.untitledduckmod.common.init.neoforge;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.init.ModPotions;

import java.util.function.Supplier;

@EventBusSubscriber
public class NeoPotions extends ModPotions{

    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, DuckMod.MOD_ID);

    public NeoPotions(IEventBus bus) {
        POTIONS.register(bus);
    }

    public Holder<Potion> registerPotion(String name, Supplier<Potion> potion) {
        return POTIONS.register(name, potion);
    }

    @SubscribeEvent
    public static void registerRecipes(RegisterBrewingRecipesEvent event) {
        ModPotions.registerRecipes(event.getBuilder());
    }
}
