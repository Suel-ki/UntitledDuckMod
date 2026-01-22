package net.untitledduckmod.common.init.neoforge;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.init.ModStatusEffects;

import java.util.function.Supplier;

public class NeoStatusEffects extends ModStatusEffects {
    public static final DeferredRegister<MobEffect> STATUS_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, DuckMod.MOD_ID);

    public NeoStatusEffects(IEventBus bus) {
        STATUS_EFFECTS.register(bus);
    }

    public Holder<MobEffect> registerStatusEffect(String name, Supplier<MobEffect> statusEffect) {
        return STATUS_EFFECTS.register(name, statusEffect);
    }
}
