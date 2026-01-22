package net.untitledduckmod.common;

import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;

public class CommonSetup {
    public static void setupDispenserProjectile(Item item) {
        // Setup projectile spawning for dispensers
        DispenserBlock.registerBehavior(item, new ProjectileDispenseBehavior(item));
    }
}
