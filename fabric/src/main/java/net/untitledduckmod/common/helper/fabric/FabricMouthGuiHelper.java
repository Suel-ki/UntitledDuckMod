package net.untitledduckmod.common.helper.fabric;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import net.untitledduckmod.common.helper.MouthGuiHelper;
import net.untitledduckmod.common.packet.MouthData;
import net.untitledduckmod.common.screen.MouthMenu;

public class FabricMouthGuiHelper extends MouthGuiHelper {

    @Override
    protected void openMouthMenuInternal(Player player, WaterfowlEntity entity) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedMenuProvider<MouthData>() {
                @Override
                public MouthData getScreenOpeningData(ServerPlayer player) {
                    return new MouthData(entity.getMouthHolderType(), entity.getId());
                }

                @Override
                public Component getDisplayName() {
                    return entity.getDisplayName();
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                    return new MouthMenu(syncId, inv, entity.getMouthInventory(), entity.getMouthHolderType(), entity.getId());
                }
            });
        }
    }
}
