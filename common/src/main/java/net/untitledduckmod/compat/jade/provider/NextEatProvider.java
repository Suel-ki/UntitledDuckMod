package net.untitledduckmod.compat.jade.provider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import net.untitledduckmod.common.platform.Services;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public class NextEatProvider implements IServerDataProvider<EntityAccessor> {

    public static final NextEatProvider INSTANCE = new NextEatProvider();

    static final Identifier UID = DuckMod.id("next_eat");

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (!Services.CONFIG.enableForceEat())
            return;
        if (accessor.getEntity() instanceof WaterfowlEntity entity) {
            if (entity.isEdibleFood(entity.getMainHandItem())) {
                var next = entity.getRandomForceEatTick() - entity.getHeldFoodTick() + 20;
                if (next >= 0) {
                    tag.putInt("NextEat", next);
                }
            }
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    public static class Client implements IEntityComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (!accessor.getServerData().contains("NextEat")) {
                return;
            }
            var nextEat = accessor.getServerData().getInt("NextEat").orElse(0);

            tooltip.add(Component.translatable("untitledduckmod.jade.nextEat", IThemeHelper.get().seconds(nextEat, accessor.tickRate())));
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
