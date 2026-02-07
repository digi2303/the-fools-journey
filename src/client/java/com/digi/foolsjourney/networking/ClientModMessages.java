package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

public class ClientModMessages {

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(BeyonderSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var player = MinecraftClient.getInstance().player;

                if (player instanceof IBeyonder beyonder) {
                    boolean wasActive = beyonder.isSpiritVisionActive();

                    beyonder.setSequence(payload.sequence());
                    beyonder.setSpirituality(payload.spirituality());
                    beyonder.setCooldown(payload.cooldown());
                    beyonder.setSpiritVision(payload.active());

                    // YENİ: Digestion verisini işle
                    beyonder.setDigestion(payload.digestion());

                    if (wasActive != payload.active()) {
                        if (payload.active()) {
                            player.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.6f, 1.5f);
                        } else {
                            player.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.6f, 1.2f);
                        }
                    }
                }
            });
        });
    }
}