package com.digi.foolsjourney.datagen;

import com.digi.foolsjourney.registry.ModDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.entity.damage.DamageScaling;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeProvider extends FabricDynamicRegistryProvider {
    public ModDamageTypeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        entries.add(ModDamageTypes.LOSS_OF_CONTROL, new DamageType(
                "foolsjourney.loss_of_control",
                DamageScaling.ALWAYS,
                0.1f
        ));
    }

    @Override
    public String getName() {
        return "Fool's Journey Damage Types";
    }
}