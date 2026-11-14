package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySilenceZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

/**
 * 作動式: 遮音領域（仮、Silence Zone、2026-11-11 ユーザー）。範囲の中の音を鳴らさず、振動を通さない
 * （{@link EntitySilenceZone}）。
 */
public class PieceTrickSilenceZone extends PieceTrickZoneBase<EntitySilenceZone> {
    // 仮の数値: 規模 = 10 + 広さ（2026-11-14）、コスト = 40 + 広さ（広さ = サイズ²。湿潤領域と同じ）
    private static final int COST_BASE = 40;

    public PieceTrickSilenceZone(Spell spell) {
        super(spell, EntitySilenceZone.class);
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
    protected EntitySilenceZone createZone(ServerLevel level) {
        return new EntitySilenceZone(level);
    }

    @Override
    protected void configure(EntitySilenceZone zone, SpellContext context) {
    }
}
