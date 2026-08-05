package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class PsitweaksDamageTypes {
    public static final ResourceKey<DamageType> METEOR_LINE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("meteor_line"));
    public static final ResourceKey<DamageType> DRY_METEOR =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("dry_meteor"));
    public static final ResourceKey<DamageType> CARBON_POISONING =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("carbon_poisoning"));
    public static final ResourceKey<DamageType> TUNNELER =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("tunneler"));

    private PsitweaksDamageTypes() {
    }
}
