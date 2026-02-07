package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.TheFoolsJourney;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SpiritVisionPayload() implements CustomPayload {
    public static final CustomPayload.Id<SpiritVisionPayload> ID = new CustomPayload.Id<>(Identifier.of(TheFoolsJourney.MOD_ID, "spirit_vision"));
    public static final PacketCodec<RegistryByteBuf, SpiritVisionPayload> CODEC = PacketCodec.unit(new SpiritVisionPayload());

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}