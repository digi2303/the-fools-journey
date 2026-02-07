package com.digi.foolsjourney;

import com.digi.foolsjourney.client.ModKeyBindings;
import net.fabricmc.api.ClientModInitializer;

public class TheFoolsJourneyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModKeyBindings.register();
	}
}