package net.untitledduckmod.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import net.untitledduckmod.common.screen.MouthMenu;

public class MouthScreen extends AbstractContainerScreen<MouthMenu> {
    private static final Identifier CHEST_SLOTS_TEXTURE = Identifier.withDefaultNamespace("container/horse/chest_slots");
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/horse.png");
    private WaterfowlEntity renderEntity;
    private float mouseX;
    private float mouseY;

    public MouthScreen(MouthMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        Entity entity = inventory.player.level().getEntity(menu.getEntityId());
        if (entity instanceof WaterfowlEntity waterfowl) {
            renderEntity = waterfowl;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHEST_SLOTS_TEXTURE, 90, 54, 0, 0, x + 7, y + 17, 18, 18);

        if (this.renderEntity != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x + 26, y + 18, x + 78, y + 70, 30, 0.0625F, this.mouseX, this.mouseY, this.renderEntity);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
