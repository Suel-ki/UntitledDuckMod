package net.untitledduckmod.common.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.untitledduckmod.DuckMod;

import java.util.function.Supplier;

public class ModPotions {

    public static Holder<Potion> INTIMIDATION;

    public static Holder<Potion> LONG_INTIMIDATION;

    public ModPotions() {
        INTIMIDATION = registerPotion("intimidation", () -> new Potion("intimidation", new MobEffectInstance(ModStatusEffects.intimidation, 3600)));
        LONG_INTIMIDATION = registerPotion("long_intimidation", () -> new Potion("intimidation", new MobEffectInstance(ModStatusEffects.intimidation, 9600)));
    }

    public Holder<Potion> registerPotion(String name, Supplier<Potion> potion) {
        return Registry.registerForHolder(BuiltInRegistries.POTION, DuckMod.id(name), potion.get());
    }

}
