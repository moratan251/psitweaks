package com.moratan251.psitweaks.common.entities.SpellGram.region;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * 領域型の魔法式オブジェクトに付く性質（試作）。
 * 効き目は {@link SpellGramPropertyEffects} が持つ。名前・色・粒子だけをここに置く。
 */
public enum SpellGramProperty {
    FIRE(0xFF6A1A, ParticleTypes.FLAME),
    ICE(0x00CCFF, ParticleTypes.SNOWFLAKE),
    WET(0x3A6BFF, ParticleTypes.FALLING_WATER);

    private final int color;
    private final ParticleOptions particle;

    SpellGramProperty(int color, ParticleOptions particle) {
        this.color = color;
        this.particle = particle;
    }

    public int color() {
        return color;
    }

    public ParticleOptions particle() {
        return particle;
    }

    public int bit() {
        return 1 << ordinal();
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** 同じ魔法式の中で打ち消し合い、水蒸気（湿潤）になる相手。 */
    @Nullable
    public SpellGramProperty opposite() {
        return switch (this) {
            case FIRE -> ICE;
            case ICE -> FIRE;
            case WET -> null;
        };
    }

    @Nullable
    public static SpellGramProperty byId(String id) {
        for (SpellGramProperty property : values()) {
            if (property.id().equals(id)) {
                return property;
            }
        }
        return null;
    }
}
