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
    public static final ResourceKey<DamageType> AIR_BULLET =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("air_bullet"));
    public static final ResourceKey<DamageType> DANCING_BLADES =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("dancing_blades"));
    public static final ResourceKey<DamageType> STARFALL =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("starfall"));
    public static final ResourceKey<DamageType> MICRO_BLACK_HOLE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("micro_black_hole"));
    public static final ResourceKey<DamageType> ACTIVE_AIR_MINE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("active_air_mine"));
    /** 作動式: ファイアサークルの円の中のダメージ。火属性で、術者を攻撃者に持つ（障壁で防げる）。 */
    public static final ResourceKey<DamageType> FLARE_CIRCLE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("flare_circle"));
    /** 作動式: アイスサークルの円の中のダメージ。凍結属性で、術者を攻撃者に持つ（障壁で防げる）。 */
    public static final ResourceKey<DamageType> ICE_CIRCLE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("ice_circle"));

    private PsitweaksDamageTypes() {
    }
}
