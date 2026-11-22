package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Set;

/** 領域が飛び道具を扱うときの共通の判定（乱流領域がそらす・減速領域が遅くする相手）。 */
public final class ZoneProjectiles {
    /** 魔法の弾の mod（Psi・Psitweaks・Ars Nouveau・Iron's Spells）。領域はこれらの弾には触らない。 */
    private static final Set<String> SPELL_PROJECTILE_MODS = Set.of("psi", "psitweaks", "ars_nouveau", "irons_spellbooks");

    private ZoneProjectiles() {
    }

    /** {@code entity} が魔法の弾か（エンティティの種類の名前空間で決める）。 */
    public static boolean isSpellProjectile(Entity entity) {
        return isSpellProjectileType(entity.getType());
    }

    /** 種類 {@code type} が魔法の弾か。 */
    public static boolean isSpellProjectileType(EntityType<?> type) {
        return SPELL_PROJECTILE_MODS.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace());
    }
}
