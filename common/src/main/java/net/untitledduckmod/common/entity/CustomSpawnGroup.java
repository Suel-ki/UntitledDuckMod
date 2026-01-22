package net.untitledduckmod.common.entity;

import net.minecraft.world.entity.MobCategory;

// credit to hybrid aquatic for the code
public enum CustomSpawnGroup {
    WATERFOWL("waterfowl", 10, true, false, 128);

    public MobCategory spawnGroup;
    public final String name;
    public final int spawnCap;
    public final boolean peaceful;
    public final boolean rare;
    public final int immediateDespawnRange;

    CustomSpawnGroup(String name, int spawnCap, boolean peaceful, boolean rare, int immediateDespawnRange) {
        this.name = name;
        this.spawnCap = spawnCap;
        this.peaceful = peaceful;
        this.rare = rare;
        this.immediateDespawnRange = immediateDespawnRange;
    }

}
