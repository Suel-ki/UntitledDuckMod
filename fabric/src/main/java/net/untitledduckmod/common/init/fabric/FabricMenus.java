package net.untitledduckmod.common.init.fabric;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.untitledduckmod.common.init.ModMenus;
import net.untitledduckmod.common.screen.ExtendedFactory;

public class FabricMenus extends ModMenus {
    public FabricMenus() {
        super();
    }

    public <T extends AbstractContainerMenu, D> MenuType<T> createExtendedMenuType(ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        return new ExtendedMenuType<>(factory::create, codec);
    }
}
