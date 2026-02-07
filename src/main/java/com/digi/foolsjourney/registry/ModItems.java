package com.digi.foolsjourney.registry;

import com.digi.foolsjourney.item.custom.SeerPotionItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item SEER_POTION = registerItem("seer_potion",
            new SeerPotionItem(new Item.Settings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of("foolsjourney", name), item);
    }

    public static void registerModItems() {
        System.out.println("Registering Mod Items for foolsjourney");
    }
}