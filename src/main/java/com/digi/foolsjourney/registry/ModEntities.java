package com.digi.foolsjourney.registry;

import com.digi.foolsjourney.TheFoolsJourney;
import com.digi.foolsjourney.entity.custom.ThrownCardEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<ThrownCardEntity> THROWN_CARD = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(TheFoolsJourney.MOD_ID, "thrown_card"),
            EntityType.Builder.<ThrownCardEntity>create(ThrownCardEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10)
                    .build()
    );

    public static void registerModEntities() {
        TheFoolsJourney.LOGGER.info("Registering Mod Entities for " + TheFoolsJourney.MOD_ID);
    }
}