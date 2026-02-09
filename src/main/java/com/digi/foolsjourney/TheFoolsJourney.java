package com.digi.foolsjourney;

import com.digi.foolsjourney.command.ModCommands;
import com.digi.foolsjourney.event.CauldronBrewing;
import com.digi.foolsjourney.registry.ModEntities;
import com.digi.foolsjourney.registry.ModItems;
import com.digi.foolsjourney.registry.ModItemGroups;
import com.digi.foolsjourney.networking.ModMessages;
import com.digi.foolsjourney.util.IBeyonder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
		ModEntities.registerModEntities();

		ModMessages.registerPayloads();
		ModMessages.registerServerReceivers();

		CauldronBrewing.register();

		CommandRegistrationCallback.EVENT.register(ModCommands::register);

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (oldPlayer instanceof IBeyonder oldBeyonder && newPlayer instanceof IBeyonder newBeyonder) {
				newBeyonder.setSequence(oldBeyonder.getSequence());
				newBeyonder.setDigestion(oldBeyonder.getDigestion());
				newBeyonder.setSpirituality(oldBeyonder.getSpirituality());

				newBeyonder.setSpiritVision(false);

				newBeyonder.setCooldown(0);

				newBeyonder.syncBeyonderData();
			}
		});
	}
}