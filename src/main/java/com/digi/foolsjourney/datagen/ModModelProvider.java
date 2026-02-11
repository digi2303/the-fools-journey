package com.digi.foolsjourney.datagen;

import com.digi.foolsjourney.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.SEER_POTION, Models.GENERATED);
        itemModelGenerator.register(ModItems.CLOWN_POTION, Models.GENERATED);
        itemModelGenerator.register(ModItems.SPIRIT_PENDULUM, Models.GENERATED);
        itemModelGenerator.register(ModItems.THROWING_CARD, Models.GENERATED);
        itemModelGenerator.register(ModItems.MAGICIAN_POTION, Models.GENERATED);
        itemModelGenerator.register(ModItems.FACELESS_POTION, Models.GENERATED);
    }
}