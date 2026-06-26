package net.untitledduckmod.common.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.packet.MouthData;
import net.untitledduckmod.common.screen.ExtendedFactory;
import net.untitledduckmod.common.screen.MouthMenu;

import java.util.function.Supplier;

public abstract class ModMenus {
    public static Supplier<MenuType<MouthMenu>> MOUTH;

    public ModMenus() {
        MOUTH = registerMenu("mouth", () ->
                createExtendedMenuType(MouthMenu::new, MouthData.CODEC)
        );
    }

    public abstract <T extends AbstractContainerMenu, D> MenuType<T> createExtendedMenuType(ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec);

    public <T extends MenuType<?>> Supplier<T> registerMenu(String name, Supplier<T> screenHandler) {
        var registry = Registry.register(BuiltInRegistries.MENU, DuckMod.id(name), screenHandler.get());
        return () -> registry;
    }
}
