package net.untitledduckmod.common.init.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.init.ModSoundEvents;

import java.util.function.Supplier;

public class NeoSoundEvents extends ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, DuckMod.MOD_ID);

    public NeoSoundEvents(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }

    public Supplier<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(DuckMod.id(name)));
    }
}
