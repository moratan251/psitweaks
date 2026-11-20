package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;

/**
 * 作動式: 乱流領域（仮、Turbulence Zone）。範囲の中に暴風を起こす: 他者の飛び道具をそらし、残留ポーションなどの雲を
 * 吹き散らし、同じ術者のほかの領域と重なると強める（熱乱流・吹雪・暴風雨・雷雲）。中のエンティティは運ばない（運ぶのは
 * 加重領域）。風量（WU）を持ち、Mekanism Generators の風力発電機に風を足す（{@link EntityTurbulenceZone}）。強さは「威力」
 * （コストを決めるため定数）。
 */
public class PieceTrickTurbulenceZone extends PieceTrickZoneBase<EntityTurbulenceZone> {
    /** 威力の上限（仮）。 */
    public static final double MAX_POWER = MAX_RESOURCE_ZONE_POWER;
    // 仮の数値: 規模 = 10 + ⌊広さ × 威力 × 0.27⌋（威力のある領域で共通）、コスト = 50 + ⌊広さ × max(威力 × 2 / 5, 2)⌋（加熱・凍結・荷電と
    // 同じ。広さ = サイズ²）
    private static final int COST_BASE = 50;
    private static final int COST_MULTIPLIER = 2;
    private static final int COST_DIVISOR = 5;

    private SpellParam<Number> power;

    public PieceTrickTurbulenceZone(Spell spell) {
        super(spell, EntityTurbulenceZone.class);
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
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
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
    protected EntityTurbulenceZone createZone(ServerLevel level) {
        return new EntityTurbulenceZone(level);
    }

    @Override
    protected void configure(EntityTurbulenceZone zone, SpellContext context) throws SpellRuntimeException {
        zone.configureWind(powerValue(context, power));
    }
}
