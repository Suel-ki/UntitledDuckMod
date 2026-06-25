package net.untitledduckmod.common.helper.neoforge;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import net.untitledduckmod.common.packet.MouthData;
import net.untitledduckmod.common.screen.MouthScreenHandler;

public class MouthGuiHelperImpl {
    public static void openMouthGui(PlayerEntity player, WaterfowlEntity entity) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openMenu(new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return entity.getDisplayName();
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity p) {
                    return new MouthScreenHandler(syncId, inv, entity.getMouthInventory(), entity.getMouthHolderType(), entity.getId());
                }
            }, buf -> {
                MouthData data = new MouthData(entity.getMouthHolderType(), entity.getId());
                MouthData.CODEC.encode(buf, data);
            });
        }
    }
}
