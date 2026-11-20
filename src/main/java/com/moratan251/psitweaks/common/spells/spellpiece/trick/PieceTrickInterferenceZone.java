package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityInterferenceZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

/**
 * 作動式: 領域干渉（仮、Interference Zone）。置いた時点の事象干渉力より低い他者の Psi の作動式を、範囲の中で効かなくし、
 * 範囲の中では術者以外のエンダー系のテレポートを止める（{@link EntityInterferenceZone}）。
 */
public class PieceTrickInterferenceZone extends PieceTrickZoneBase<EntityInterferenceZone> {
    // 仮の数値: 規模 = 10 + 広さ、コスト = 40 + 広さ（広さ = サイズ²。遮音領域と同じ）
    private static final int COST_BASE = 40;

    public PieceTrickInterferenceZone(Spell spell) {
        super(spell, EntityInterferenceZone.class);
        setStatLabel(EnumSpellStat.POTENCY, areaLabel().add(POTENCY_BASE));
        setStatLabel(EnumSpellStat.COST, areaLabel().add(COST_BASE));
    }

    @Override
    protected int potency(int area) {
        return POTENCY_BASE + area;
    }

    @Override
    protected int cost(int area) {
        return COST_BASE + area;
    }

    @Override
    protected EntityInterferenceZone createZone(ServerLevel level) {
        return new EntityInterferenceZone(level);
    }

    @Override
    protected void configure(EntityInterferenceZone zone, SpellContext context) {
    }
}
