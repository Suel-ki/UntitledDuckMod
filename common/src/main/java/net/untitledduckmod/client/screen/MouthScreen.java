package net.untitledduckmod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import net.untitledduckmod.common.screen.MouthScreenHandler;

public class MouthScreen extends HandledScreen<MouthScreenHandler> {
    private static final Identifier CHEST_SLOTS_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/container/horse/chest_slots.png");
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/horse.png");
    private WaterfowlEntity renderEntity;
    private float mouseX;
    private float mouseY;

    public MouthScreen(MouthScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        Entity entity = inventory.player.getEntityWorld().getEntityById(handler.getEntityId());
        if (entity instanceof WaterfowlEntity waterfowl) {
            renderEntity = waterfowl;
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        context.drawTexture(CHEST_SLOTS_TEXTURE, x + 7, y + 17, 0f, 0f, 18, 18, 90, 54);

        if (this.renderEntity != null) {
            InventoryScreen.drawEntity(context, x + 26, y + 18, x + 78, y + 70, 30, 0.0625F, this.mouseX, this.mouseY, this.renderEntity);
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.mouseX = (float)mouseX;
        this.mouseY = (float)mouseY;
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
