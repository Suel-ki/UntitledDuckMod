package net.untitledduckmod.common.screen;

import net.minecraft.world.entity.player.Inventory;

public interface ExtendedFactory<T, D> {
    T create(int syncId, Inventory inventory, D data);
}