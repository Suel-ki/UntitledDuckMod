package net.untitledduckmod.common.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.effect.IntimidationStatusEffect;

import java.util.function.Supplier;

public class ModStatusEffects {
    public static Holder<MobEffect> intimidation;

    public ModStatusEffects() {
        intimidation = registerStatusEffect("intimidation", IntimidationStatusEffect::new);
    }

    public Holder<MobEffect> registerStatusEffect(String name, Supplier<MobEffect> statusEffect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, DuckMod.id(name), statusEffect.get());
    }
}

