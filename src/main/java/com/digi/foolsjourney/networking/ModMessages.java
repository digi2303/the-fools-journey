package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;

public class ModMessages {

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(SpiritVisionPayload.ID, SpiritVisionPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(BeyonderSyncPayload.ID, BeyonderSyncPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SpiritVisionPayload.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                if (context.player() instanceof IBeyonder beyonder) {
                    if (beyonder.getSequence() == -1) return;
                    if (beyonder.getCooldown() > 0) return;
                    if (!beyonder.isSpiritVisionActive() && beyonder.getSpirituality() < 1.0) return;

                    boolean newState = !beyonder.isSpiritVisionActive();
                    beyonder.setSpiritVision(newState);
                }
            });
        });
    }
}