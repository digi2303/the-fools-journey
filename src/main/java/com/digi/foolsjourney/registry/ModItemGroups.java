package com.digi.foolsjourney.registry;

import com.digi.foolsjourney.TheFoolsJourney;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup FOOLS_JOURNEY_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of("foolsjourney", "fools_journey_group"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.SEER_POTION))
                    .displayName(Text.translatable("itemgroup.foolsjourney.fools_journey_group"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.SEER_POTION);
                        entries.add(ModItems.SPIRIT_PENDULUM);
                        entries.add(ModItems.CLOWN_POTION);
                        entries.add(ModItems.THROWING_CARD);
                    })
                    .build());

    public static void registerItemGroups() {
        TheFoolsJourney.LOGGER.info("Registering Item Groups for " + TheFoolsJourney.MOD_ID);
    }
}