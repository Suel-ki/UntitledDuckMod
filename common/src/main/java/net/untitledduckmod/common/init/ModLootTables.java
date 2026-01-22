package net.untitledduckmod.common.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.untitledduckmod.DuckMod;

public class ModLootTables {

    public static final ResourceKey<LootTable> DUCK_LAY = register("gameplay/duck_lay");
    public static final ResourceKey<LootTable> GOOSE_LAY = register("gameplay/goose_lay");

    private static ResourceKey<LootTable> register(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, DuckMod.id(name));
    }

}
