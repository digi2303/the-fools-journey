package com.digi.foolsjourney.registry;

import com.digi.foolsjourney.TheFoolsJourney;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> LOSS_OF_CONTROL = RegistryKey.of(
            RegistryKeys.DAMAGE_TYPE,
            Identifier.of(TheFoolsJourney.MOD_ID, "loss_of_control")
    );

    public static void bootstrap(Registerable<DamageType> context) {
    }
}