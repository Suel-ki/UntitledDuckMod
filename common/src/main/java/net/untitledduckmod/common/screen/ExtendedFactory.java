package net.untitledduckmod.common.screen;

import net.minecraft.entity.player.PlayerInventory;

public interface ExtendedFactory<T, D> {
    T create(int syncId, PlayerInventory inventory, D data);
}
