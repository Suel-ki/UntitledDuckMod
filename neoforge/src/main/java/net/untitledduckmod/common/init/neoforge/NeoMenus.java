package net.untitledduckmod.common.init.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.init.ModMenus;
import net.untitledduckmod.common.screen.ExtendedFactory;

import java.util.function.Supplier;

public class NeoMenus extends ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, DuckMod.MOD_ID);

    public NeoMenus(IEventBus eventBus) {
        super();
        MENUS.register(eventBus);
    }

    @Override
    public <T extends AbstractContainerMenu, D> MenuType<T> createExtendedMenuType(
            ExtendedFactory<T, D> factory,
            StreamCodec<? super RegistryFriendlyByteBuf, D> codec
    ) {
        return IMenuTypeExtension.create((syncId, inventory, buf) -> {
            D data = codec.decode(buf);
            return factory.create(syncId, inventory, data);
        });
    }

    @Override
    public  <T extends MenuType<?>> Supplier<T> registerMenu(String name, Supplier<T> screenHandler) {
        return MENUS.register(name, screenHandler);
    }
}
