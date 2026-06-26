package net.untitledduckmod.common.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MouthData(int holderType, int entityId) {
    public static final StreamCodec<RegistryFriendlyByteBuf, MouthData> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MouthData::holderType,
            ByteBufCodecs.VAR_INT, MouthData::entityId,
            MouthData::new
    );
}
