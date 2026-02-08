package com.digi.foolsjourney.datagen;

import com.digi.foolsjourney.registry.ModDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeTagProvider extends FabricTagProvider<DamageType> {
    public ModDamageTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR)
                .add(ModDamageTypes.LOSS_OF_CONTROL);

        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_INVULNERABILITY)
                .add(ModDamageTypes.LOSS_OF_CONTROL);
    }
}