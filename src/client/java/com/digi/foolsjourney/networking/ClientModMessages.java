package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class ClientModMessages {

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(BeyonderSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var player = MinecraftClient.getInstance().player;

                if (player instanceof IBeyonder beyonder) {
                    beyonder.setSequence(payload.sequence());
                    beyonder.setSpirituality(payload.spirituality());
                    beyonder.setCooldown(payload.cooldown());
                }
            });
        });
    }
}