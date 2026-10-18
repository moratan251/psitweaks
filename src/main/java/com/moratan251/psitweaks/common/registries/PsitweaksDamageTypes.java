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
    public static final ResourceKey<DamageType> CHAIN_LIGHTNING =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("chain_lightning"));
    public static final ResourceKey<DamageType> MINE_GENESIS =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("mine_genesis"));
    public static final ResourceKey<DamageType> BURST =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("burst"));
    public static final ResourceKey<DamageType> KAMAITACHI =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("kamaitachi"));
    public static final ResourceKey<DamageType> DANCING_BLADES =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("dancing_blades"));
    public static final ResourceKey<DamageType> STARFALL =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("starfall"));
    public static final ResourceKey<DamageType> GRAVITY_COLLAPSE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("gravity_collapse"));
    public static final ResourceKey<DamageType> ACTIVE_AIR_MINE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("active_air_mine"));

    private PsitweaksDamageTypes() {
    }
}
