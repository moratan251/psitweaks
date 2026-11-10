package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;

/**
 * 作動式: 加熱領域（仮、Heat Zone）。範囲の中の生物に火をつけ、氷を溶かす。Create があれば一括燻製、Mekanism があれば
 * 熱を受け取るブロックに熱を送る（{@link EntityHeatZone}）。「威力」（定数、最大 10）は領域の温度で、燃やす長さと
 * Mekanism の機械を温める上限の温度、氷炎地獄の強さを決める（2026-11-11 ユーザー）。
 */
public class PieceTrickHeatZone extends PieceTrickZoneBase<EntityHeatZone> {
    // 仮の数値: 規模 = 10 + 広さ × 威力 / 25、コスト = 50 + 広さ × 威力 × 2 / 5（広さ = サイズ²。威力 5 で 威力のなかった頃と同じ）
    private static final int POTENCY_BASE = 10;
    private static final int POTENCY_DIVISOR = 25;
    private static final int COST_BASE = 50;
    private static final int COST_MULTIPLIER = 2;
    private static final int COST_DIVISOR = 5;

    private SpellParam<Number> power;

    public PieceTrickHeatZone(Spell spell) {
        super(spell, EntityHeatZone.class);
        setStatLabel(EnumSpellStat.POTENCY, areaLabel().mul(new StatLabel(SpellParam.GENERIC_NAME_POWER, true))
                .div(POTENCY_DIVISOR).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(new StatLabel(SpellParam.GENERIC_NAME_POWER, true))
                .mul(COST_MULTIPLIER).div(COST_DIVISOR).add(COST_BASE).floor());
    }

    @Override
    protected void addZoneParams() {
        power = addPowerParam();
    }

    @Override
    protected int potency(int area) throws SpellCompilationException {
        return POTENCY_BASE + (int) Math.floor(area * constantPower(power) / POTENCY_DIVISOR);
    }

    @Override
    protected int cost(int area) throws SpellCompilationException {
        return COST_BASE + (int) Math.floor(area * constantPower(power) * COST_MULTIPLIER / COST_DIVISOR);
    }

    @Override
    protected EntityHeatZone createZone(ServerLevel level) {
        return new EntityHeatZone(level);
    }

    @Override
    protected void configure(EntityHeatZone zone, SpellContext context) throws SpellRuntimeException {
        zone.setPower(powerValue(context, power));
    }
}
