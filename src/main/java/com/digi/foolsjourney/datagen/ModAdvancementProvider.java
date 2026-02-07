package com.digi.foolsjourney.datagen;

import com.digi.foolsjourney.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.ConsumeItemCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends FabricAdvancementProvider {
    public ModAdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup registryLookup, Consumer<AdvancementEntry> consumer) {

        AdvancementEntry rootAdvancement = Advancement.Builder.create()
                .display(
                        ModItems.SEER_POTION,
                        Text.translatable("advancement.foolsjourney.root.title"),
                        Text.translatable("advancement.foolsjourney.root.desc"),
                        Identifier.of("minecraft", "textures/block/deepslate_bricks.png"),
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("consumed_seer_potion", ConsumeItemCriterion.Conditions.item(ModItems.SEER_POTION))
                .build(consumer, "foolsjourney/root");

        AdvancementEntry pendulumAdvancement = Advancement.Builder.create()
                .parent(rootAdvancement)
                .display(
                        ModItems.SPIRIT_PENDULUM,
                        Text.translatable("advancement.foolsjourney.pendulum.title"),
                        Text.translatable("advancement.foolsjourney.pendulum.desc"),
                        null,
                        AdvancementFrame.GOAL,
                        true,
                        true,
                        false
                )
                .criterion("has_pendulum", InventoryChangedCriterion.Conditions.items(ModItems.SPIRIT_PENDULUM))
                .build(consumer, "foolsjourney/get_pendulum");
    }
}