package net.untitledduckmod.common.entity;

import net.minecraft.network.chat.Component;
import net.untitledduckmod.common.entity.inv.ListenableSimpleContainer;

public interface MouthHoldable {
    ListenableSimpleContainer getMouthInventory();

    Component getDisplayName();

    int getMouthHolderType();
}