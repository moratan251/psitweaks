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
 * 熱を受け取るブロックに熱を送る（{@link EntityHeatZone}）。「威力」（定数）は領域の温度で、燃やす長さと
 * Mekanism の機械を温める上限の温度を決める。
 */
public class PieceTrickHeatZone extends PieceTrickZoneBase<EntityHeatZone> {
    // 仮の数値: 規模 = 10 + ⌊広さ × 威力 × 0.27⌋（威力のある領域で共通）、コスト = 50 + ⌊広さ × max(威力 × 2 / 5, 2)⌋（広さ = サイズ²）
    private static final int COST_BASE = 50;
    private static final int COST_MULTIPLIER = 2;
    private static final int COST_DIVISOR = 5;

    private SpellParam<Number> power;

    public PieceTrickHeatZone(Spell spell) {
        super(spell, EntityHeatZone.class);
        setStatLabel(EnumSpellStat.POTENCY, poweredPotencyLabel());
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(perAreaCostWithFloorLabel(
                powerLabel(MAX_RESOURCE_ZONE_POWER).mul(COST_MULTIPLIER).div(COST_DIVISOR))).add(COST_BASE).floor());
    }

    /** 総量を持つ領域なので、威力は 10 より上も入れられる。 */
    @Override
    protected double maxPower() {
        return MAX_RESOURCE_ZONE_POWER;
    }

    @Override
    protected void addZoneParams() {
        power = addPowerParam();
    }

    @Override
    protected int potency(int area) throws SpellCompilationException {
        return poweredPotency(area, constantPower(power));
    }

    @Override
    protected int cost(int area) throws SpellCompilationException {
        return costFor(area, constantPower(power));
    }

    /** コスト（30 秒）: 50 + ⌊広さ × max(威力 × 2 / 5, 2)⌋（2 は {@link #MIN_COST_PER_AREA}。威力 5 より下は威力 5 と同じ）。 */
    public static int costFor(int area, double power) {
        return COST_BASE + withCostFloor(area, area * power * COST_MULTIPLIER / COST_DIVISOR);
    }

    @Override
    protected int runtimeCost(int size, SpellContext context) throws SpellRuntimeException {
        return costFor(areaOf(size), powerValue(context, power));
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
