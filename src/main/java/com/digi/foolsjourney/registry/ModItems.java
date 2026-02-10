package com.digi.foolsjourney.registry;

import com.digi.foolsjourney.item.custom.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item SEER_POTION = registerItem("seer_potion",
            new SeerPotionItem(new Item.Settings().maxCount(1)));

    public static final Item CLOWN_POTION = registerItem("clown_potion",
            new ClownPotionItem(new Item.Settings().maxCount(1)));

    public static final Item SPIRIT_PENDULUM = registerItem("spirit_pendulum",
            new SpiritPendulumItem(new Item.Settings().maxCount(1)));

    public static final Item THROWING_CARD = registerItem("throwing_card",
            new ThrowingCardItem(new Item.Settings().maxCount(64)));

    public static final Item MAGICIAN_POTION = registerItem("magician_potion",
            new MagicianPotionItem(new Item.Settings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of("foolsjourney", name), item);
    }

    public static void registerModItems() {
        System.out.println("Registering Mod Items for foolsjourney");
    }
}