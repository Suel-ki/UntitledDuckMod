package net.untitledduckmod.common.platform.fabric;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.screen.ExtendedFactory;

import java.util.function.Supplier;

public class RegistryHelperImpl {
    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        T registry = Registry.register(Registries.ITEM, DuckMod.id(name), item.get());
        return () -> registry;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Item> Supplier<T> registerSpawnEggItem
            (
            String name, Supplier<? extends EntityType<? extends MobEntity>> type,
            int primaryColor, int secondaryColor, Item.Settings settings
            ) {
        return (Supplier<T>) registerItem(name, () -> new SpawnEggItem(type.get(), primaryColor, secondaryColor, settings));
    }

    public static <T extends EntityType<?>> Supplier<T> registerEntity(String name, Supplier<T> entityType) {
        T registry = Registry.register(Registries.ENTITY_TYPE, DuckMod.id(name), entityType.get());
        return () -> registry;
    }

    public static Supplier<SoundEvent> registerSoundEvent(String name) {
        var registry = Registry.register(Registries.SOUND_EVENT, DuckMod.id(name), SoundEvent.of(DuckMod.id(name)));
        return () -> registry;
    }

    public static RegistryEntry<Potion> registerPotion(String name, Supplier<Potion> potion) {
        return Registry.registerReference(Registries.POTION, DuckMod.id(name), potion.get());
    }

    public static RegistryEntry<StatusEffect> registerStatusEffect(String name, Supplier<StatusEffect> statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, DuckMod.id(name), statusEffect.get());
    }

    public static <T extends ScreenHandler, D> ScreenHandlerType<T> createExtendedScreenHandler(ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> codec) {
        return new ExtendedScreenHandlerType<>(factory::create, codec);
    }

    public static <T extends ScreenHandlerType<?>> Supplier<T> registerScreenHandler(String name, Supplier<T> screenHandler) {
        var registry = Registry.register(Registries.SCREEN_HANDLER, DuckMod.id(name), screenHandler.get());
        return () -> registry;
    }
}
