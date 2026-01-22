package net.untitledduckmod.common.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.untitledduckmod.DuckMod;

import java.util.function.Supplier;

public class ModSoundEvents {
    public static Supplier<SoundEvent> DUCK_AMBIENT;
    public static Supplier<SoundEvent> DUCKLING_AMBIENT;
    public static Supplier<SoundEvent> DUCK_LAY_EGG;
    public static Supplier<SoundEvent> DUCK_STEP;
    public static Supplier<SoundEvent> DUCK_HURT;
    public static Supplier<SoundEvent> DUCKLING_HURT;
    public static Supplier<SoundEvent> DUCK_DEATH;
    public static Supplier<SoundEvent> DUCKLING_DEATH;
    public static Supplier<SoundEvent> DUCK_SACK_USE;

    public static Supplier<SoundEvent> GOOSE_HONK;
    public static Supplier<SoundEvent> GOOSE_LAY_EGG;
    public static Supplier<SoundEvent> GOOSE_DEATH;
    public static Supplier<SoundEvent> GOSLING_DEATH;
    public static Supplier<SoundEvent> GOSLING_AMBIENT;
    public static Supplier<SoundEvent> GOSLING_HURT;
    
    public ModSoundEvents() {
        DUCK_AMBIENT = registerSoundEvent("duck_ambient");
        DUCKLING_AMBIENT = registerSoundEvent("duckling_ambient");
        DUCK_LAY_EGG = registerSoundEvent("duck_lay_egg");
        DUCK_STEP = registerSoundEvent("duck_step");
        DUCK_HURT = registerSoundEvent("duck_hurt");
        DUCKLING_HURT = registerSoundEvent("duckling_hurt");
        DUCK_DEATH = registerSoundEvent("duck_death");
        DUCKLING_DEATH = registerSoundEvent("duckling_death");
        DUCK_SACK_USE = registerSoundEvent("duck_sack_use");

        GOOSE_HONK = registerSoundEvent("goose_honk");
        GOOSE_LAY_EGG = registerSoundEvent("goose_lay_egg");
        GOOSE_DEATH = registerSoundEvent("goose_death");
        GOSLING_DEATH = registerSoundEvent("gosling_death");
        GOSLING_AMBIENT = registerSoundEvent("gosling_ambient");
        GOSLING_HURT = registerSoundEvent("gosling_hurt");
    }

    public Supplier<SoundEvent> registerSoundEvent(String name) {
        var registry = Registry.register(BuiltInRegistries.SOUND_EVENT, DuckMod.id(name), SoundEvent.createVariableRangeEvent(DuckMod.id(name)));
        return () -> registry;
    }


}