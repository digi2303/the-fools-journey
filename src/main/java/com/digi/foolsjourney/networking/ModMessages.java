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
                        context.player().sendMessage(Text.of("§c[Error] Not a Beyonder!"), true);
                        return;
                    }

                    if (beyonder.getCooldown() > 0) {
                        long secondsLeft = (long) Math.ceil(beyonder.getCooldown() / 20.0);
                        context.player().sendMessage(Text.of("§c[LotM] Cooldown! Wait " + secondsLeft + "s..."), true);
                        return;
                    }

                    if (!beyonder.isSpiritVisionActive() && beyonder.getSpirituality() < 1.0) {
                        context.player().sendMessage(Text.of("§c[LotM] Not enough spirituality to activate!"), true);
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