package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

/**
 * 作動式: 加熱領域（仮、Heat Zone）。範囲の中の生物に火をつけ、氷を溶かす。Create があれば一括燻製、Mekanism があれば
 * 熱を受け取るブロックに熱を送る（{@link EntityHeatZone}）。
 */
public class PieceTrickHeatZone extends PieceTrickZoneBase<EntityHeatZone> {
    // 仮の数値: 規模 = 10 + 広さ / 5、コスト = 50 + 広さ × 2（広さ = サイズ²）
    private static final int POTENCY_BASE = 10;
    private static final int POTENCY_AREA_DIVISOR = 5;
    private static final int COST_BASE = 50;
    private static final int COST_PER_AREA = 2;

    public PieceTrickHeatZone(Spell spell) {
        super(spell, EntityHeatZone.class);
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
    protected EntityHeatZone createZone(ServerLevel level) {
        return new EntityHeatZone(level);
    }

    @Override
    protected void configure(EntityHeatZone zone, SpellContext context) {
    }
}
