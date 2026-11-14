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
 * 作動式: 乱流領域（仮、Turbulence Zone）。範囲の中に渦を巻く暴風を起こし、エンティティを振り回す。強さは「威力」
 * （コストを決めるため定数）。Mekanism Generators の風力発電機は範囲の中なら屋内でも発電する（{@link EntityTurbulenceZone}）。
 */
public class PieceTrickTurbulenceZone extends PieceTrickZoneBase<EntityTurbulenceZone> {
    /** 威力の上限（仮）。 */
    public static final double MAX_POWER = MAX_RESOURCE_ZONE_POWER;
    // 仮の数値: 規模 = 10 + ⌊広さ × 威力 × 0.27⌋（威力のある領域で共通、2026-11-14）、コスト = 40 + 広さ × 威力（広さ = サイズ²）
    private static final int COST_BASE = 40;

    private SpellParam<Number> power;

    public PieceTrickTurbulenceZone(Spell spell) {
        super(spell, EntityTurbulenceZone.class);
        setStatLabel(EnumSpellStat.POTENCY, poweredPotencyLabel(MAX_RESOURCE_ZONE_POWER));
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(powerLabel(MAX_RESOURCE_ZONE_POWER)).add(COST_BASE).floor());
    }

    /** 総量を持つ領域なので、威力は 10 より上も入れられる（2026-11-15 ユーザー）。 */
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

    /** コスト（30 秒）: 40 + ⌊広さ × 威力⌋。 */
    public static int costFor(int area, double power) {
        return COST_BASE + (int) Math.floor(area * power);
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
