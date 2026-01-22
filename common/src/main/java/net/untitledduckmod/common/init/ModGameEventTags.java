package net.untitledduckmod.common.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;
import net.untitledduckmod.DuckMod;

public class ModGameEventTags {
    public static final TagKey<GameEvent> DUCK_CAN_LISTEN = create("duck_can_listen");

    private static TagKey<GameEvent> create(String name) {
        return TagKey.create(Registries.GAME_EVENT, DuckMod.id(name));
    }
}
