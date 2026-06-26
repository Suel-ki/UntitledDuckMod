package net.untitledduckmod.common.helper;

import net.minecraft.world.entity.player.Player;
import net.untitledduckmod.common.entity.WaterfowlEntity;

public abstract class MouthGuiHelper {

    public static MouthGuiHelper INSTANCE;

    public static void openMouthMenu(Player player, WaterfowlEntity entity) {
        if (INSTANCE != null) {
            INSTANCE.openMouthMenuInternal(player, entity);
        }
    }

    protected abstract void openMouthMenuInternal(Player player, WaterfowlEntity entity);
}
