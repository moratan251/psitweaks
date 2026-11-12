package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityChargedZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

/**
 * 作動式: 荷電領域（仮、Charged Zone、2026-11-12 ユーザー）。範囲の中のエンティティ（持ち物）に FE を少しずつ送り、AE2 の
 * チャージ加工をし、乱流領域と重なると雷雲になる（{@link EntityChargedZone}）。FE の量は湿潤領域の一括洗浄と同じく、置いた時点の
 * 事象干渉力で決まり、威力の入力は持たない。
 */
public class PieceTrickChargedZone extends PieceTrickZoneBase<EntityChargedZone> {
    // 仮の数値: 規模 = 10 + 広さ / 5、コスト = 50 + 広さ × 2（広さ = サイズ²。領域干渉と同じ）
    private static final int POTENCY_BASE = 10;
    private static final int POTENCY_AREA_DIVISOR = 5;
    private static final int COST_BASE = 50;
    private static final int COST_PER_AREA = 2;

    public PieceTrickChargedZone(Spell spell) {
        super(spell, EntityChargedZone.class);
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
    protected EntityChargedZone createZone(ServerLevel level) {
        return new EntityChargedZone(level);
    }

    @Override
    protected void configure(EntityChargedZone zone, SpellContext context) {
    }
}
