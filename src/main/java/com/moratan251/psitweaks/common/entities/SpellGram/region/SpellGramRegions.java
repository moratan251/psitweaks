package com.moratan251.psitweaks.common.entities.SpellGram.region;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** 領域型を作る作動式・回収で共通の計算（試作）。 */
public final class SpellGramRegions {
    private SpellGramRegions() {
    }

    /** 性質の倍率: 全体倍率 × 個別倍率 × 術者の事象干渉力。付けた時点の値で固定する。 */
    public static double scale(SpellGramProperty property, @Nullable LivingEntity caster) {
        double perProperty = switch (property) {
            case FIRE -> PsitweaksConfig.COMMON.spellGramFireDamageMultiplier.get();
            case ICE -> PsitweaksConfig.COMMON.spellGramIceDamageMultiplier.get();
            case WET -> 1.0D;
        };
        return AttackRules.multiplier(perProperty, caster);
    }

    /** 威力に応じて増える強度（威力 × {@link EntitySpellGramRegion#INTEGRITY_PER_POWER} × 事象干渉力）。 */
    public static float integrity(double power, @Nullable LivingEntity caster) {
        return (float) (power * EntitySpellGramRegion.INTEGRITY_PER_POWER * EntitySpellGramRegion.interferenceOf(caster));
    }

    /** 術者の領域型をすべて解く。解いた数を返す。 */
    public static int recallAll(@Nullable UUID caster) {
        int count = 0;
        for (EntitySpellGramRegion region : SpellGramRegionIndex.objectsOf(caster)) {
            region.end(EntitySpellGramRegion.EndReason.RECALLED);
            count++;
        }
        return count;
    }
}
