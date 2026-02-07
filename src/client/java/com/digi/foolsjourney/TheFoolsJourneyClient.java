package com.digi.foolsjourney;

import com.digi.foolsjourney.client.DigestionHudOverlay;
import com.digi.foolsjourney.client.ModKeyBindings;
import com.digi.foolsjourney.client.SpiritualityHudOverlay;
import com.digi.foolsjourney.networking.ClientModMessages;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class TheFoolsJourneyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModKeyBindings.register();

		HudRenderCallback.EVENT.register(new SpiritualityHudOverlay());

		HudRenderCallback.EVENT.register(new DigestionHudOverlay());

		ClientModMessages.registerS2CPackets();
	}
}