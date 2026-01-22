package net.untitledduckmod;

import net.minecraft.resources.Identifier;
import net.untitledduckmod.common.CommonSetup;
import net.untitledduckmod.common.init.*;
import net.untitledduckmod.common.platform.Services;

public class DuckMod {
    public static final String MOD_ID = "untitledduckmod";

    public static void preInit() {
        Services.CONFIG.setup();
    }

    public static void postInit() {
    }

    public static void postEntityInit() {
        CommonSetup.setupDispenserProjectile(ModItems.DUCK_EGG.get());
        CommonSetup.setupDispenserProjectile(ModItems.GOOSE_EGG.get());
    }

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

    public static String stringID(String name) {
        return MOD_ID + ":" + name;
    }
}