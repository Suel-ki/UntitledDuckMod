package net.untitledduckmod.common.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record MouthData(int holderType, int entityId) {
    public static final PacketCodec<RegistryByteBuf, MouthData> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, MouthData::holderType,
            PacketCodecs.VAR_INT, MouthData::entityId,
            MouthData::new
    );
}
