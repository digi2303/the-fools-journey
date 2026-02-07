package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.TheFoolsJourney;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BeyonderSyncPayload(int sequence, double spirituality, boolean active, int cooldown, double digestion) implements CustomPayload {

    public static final CustomPayload.Id<BeyonderSyncPayload> ID = new CustomPayload.Id<>(Identifier.of(TheFoolsJourney.MOD_ID, "beyonder_sync"));

    public static final PacketCodec<RegistryByteBuf, BeyonderSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, BeyonderSyncPayload::sequence,
            PacketCodecs.DOUBLE, BeyonderSyncPayload::spirituality,
            PacketCodecs.BOOL, BeyonderSyncPayload::active,
            PacketCodecs.INTEGER, BeyonderSyncPayload::cooldown,
            PacketCodecs.DOUBLE, BeyonderSyncPayload::digestion,
            BeyonderSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}