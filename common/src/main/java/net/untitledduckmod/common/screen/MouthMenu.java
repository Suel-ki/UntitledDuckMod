package net.untitledduckmod.common.screen;

import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.untitledduckmod.common.entity.DuckEntity;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import net.untitledduckmod.common.entity.inv.ListenableSimpleContainer;
import net.untitledduckmod.common.init.ModItems;
import net.untitledduckmod.common.init.ModMenus;
import net.untitledduckmod.common.packet.MouthData;

public class MouthMenu extends AbstractContainerMenu implements ExtendedFactory<MouthMenu, MouthData> {
    private final Container entityInventory;
    private final int entityId;
    private WaterfowlEntity entity;

    public MouthMenu(int syncId, Inventory inventory, MouthData data) {
        this(syncId, inventory, new ListenableSimpleContainer(1), data.holderType(), data.entityId());
    }

    public MouthMenu(int syncId, Inventory playerInventory, Container entityInventory, int holderType, int entityId) {
        super(ModMenus.MOUTH.get(), syncId);
        checkContainerSize(entityInventory, 1);
        this.entityInventory = entityInventory;
        this.entityId = entityId;
        Entity e = playerInventory.player.level().getEntity(entityId);
        if (e instanceof WaterfowlEntity waterfowl) {
            entity = waterfowl;
        }
        entityInventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(entityInventory, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (holderType == 1 && stack.is(ModItems.GOOSE_EGG.get())) return false;
                if (holderType == 2 && (stack.is(ModItems.DUCK_EGG.get()) || !DuckEntity.getFoodIngredient().test(stack))) return false;
                return super.mayPlace(stack);
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public int getEntityId() {
        return this.entityId;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.entityInventory.stillValid(player) && this.entity.isAlive() && player.isWithinEntityInteractionRange(this.entity, 4.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();

            if (invSlot == 0) {
                if (!this.moveItemStackTo(itemStack2, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.getSlot(0).mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.entityInventory.stopOpen(player);
    }

    @Override
    public MouthMenu create(int syncId, Inventory inventory, MouthData data) {
        return new MouthMenu(syncId, inventory, data);
    }
}