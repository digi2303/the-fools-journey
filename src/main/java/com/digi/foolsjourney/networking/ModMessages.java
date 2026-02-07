package com.digi.foolsjourney.networking;

import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;

public class ModMessages {
    public static void registerGlobalReceivers() {
        PayloadTypeRegistry.playC2S().register(SpiritVisionPayload.ID, SpiritVisionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SpiritVisionPayload.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                if (context.player() instanceof IBeyonder beyonder) {

                    if (beyonder.getSequence() == -1) {
                        context.player().sendMessage(Text.of("§c[Error] You are not a Beyonder yet!"), true);
                        return;
                    }

                    boolean newState = !beyonder.isSpiritVisionActive();
                    beyonder.setSpiritVision(newState);

                    String status = newState ? "§a[ON]" : "§c[OFF]";
                    context.player().sendMessage(Text.of("§5[LotM] Spirit Vision: " + status), true);
                }
            });
        });
    }
}