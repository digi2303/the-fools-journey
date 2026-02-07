package com.digi.foolsjourney;

import com.digi.foolsjourney.registry.ModItems;
import com.digi.foolsjourney.registry.ModItemGroups;
import com.digi.foolsjourney.networking.ModMessages;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheFoolsJourney implements ModInitializer {
	public static final String MOD_ID = "foolsjourney";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing The Fool's Journey mod...");

		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();

		ModMessages.registerPayloads();
		ModMessages.registerServerReceivers();
	}
}