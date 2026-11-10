package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityInterferenceZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

/**
 * 作動式: 領域干渉（仮、Interference Zone）。置いた時点の事象干渉力より低い他者の Psi の作動式と領域を、範囲の中で
 * 効かなくする（{@link EntityInterferenceZone}）。
 */
public class PieceTrickInterferenceZone extends PieceTrickZoneBase<EntityInterferenceZone> {
    // 仮の数値: 規模 = 10 + 広さ / 5、コスト = 50 + 広さ × 2（広さ = サイズ²）
    private static final int POTENCY_BASE = 10;
    private static final int POTENCY_AREA_DIVISOR = 5;
    private static final int COST_BASE = 50;
    private static final int COST_PER_AREA = 2;

    public PieceTrickInterferenceZone(Spell spell) {
        super(spell, EntityInterferenceZone.class);
        setStatLabel(EnumSpellStat.POTENCY, areaLabel().div(POTENCY_AREA_DIVISOR).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(COST_PER_AREA).add(COST_BASE));
    }

    @Override
    protected int potency(int area) {
        return POTENCY_BASE + area / POTENCY_AREA_DIVISOR;
    }

    @Override
    protected int cost(int area) {
        return COST_BASE + area * COST_PER_AREA;
    }

    @Override
    protected EntityInterferenceZone createZone(ServerLevel level) {
        return new EntityInterferenceZone(level);
    }

    @Override
    protected void configure(EntityInterferenceZone zone, SpellContext context) {
    }
}
