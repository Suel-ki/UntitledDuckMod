package net.untitledduckmod.common;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.untitledduckmod.common.entity.WaterfowlEggEntity;
import net.untitledduckmod.common.entity.WaterfowlEntity;

public class CommonSetup {
    public static void setupDispenserProjectile(Item item, EntityType<? extends ThrownItemEntity> thrownEntity, EntityType<? extends WaterfowlEntity> mobType) {
        // Setup projectile spawning for dispensers
        DispenserBlock.registerBehavior(item, new ProjectileDispenserBehavior() {
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                return Util.make(new WaterfowlEggEntity(thrownEntity, world, position.getX(), position.getY(), position.getZ(), mobType), (entity) -> {
                    entity.setItem(stack);
                });
            }
        });
    }
}
