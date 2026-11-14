package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityInterferenceZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

/**
 * 作動式: 領域干渉（仮、Interference Zone）。置いた時点の事象干渉力より低い他者の Psi の作動式を、範囲の中で効かなくし、
 * 範囲の中では術者以外のエンダー系のテレポートを止める（{@link EntityInterferenceZone}）。「威力」（省略できる 0〜10 の定数、
 * 2026-11-14 ユーザー）は、他人の領域との奪い合いで事象干渉力に 威力 × 5% を上乗せする。
 */
public class PieceTrickInterferenceZone extends PieceTrickZoneBase<EntityInterferenceZone> {
    // 仮の数値（2026-11-14 ユーザー）: 規模 = 10 + ⌊広さ × 1.35 ×（1 + 威力 / 10）⌋、コスト = 50 + 広さ × 2 ×（1 + 威力 / 10）。威力は必須の 1〜10
    // （広さ = サイズ²）。威力を省けば 0。
    private static final int POTENCY_BASE = 10;
    /** 規模の広さあたりの係数（2026-11-14 ユーザー: 威力 10・サイズ 16 で威力のある領域と同じ 701）。 */
    private static final double POTENCY_PER_AREA = 1.35;
    private static final int COST_BASE = 50;
    private static final int COST_PER_AREA = 2;

    /** 威力の上限。 */
    public static final double MAX_POWER = 10.0;

    private SpellParam<Number> power;

    public PieceTrickInterferenceZone(Spell spell) {
        super(spell, EntityInterferenceZone.class);
        setStatLabel(EnumSpellStat.POTENCY, areaLabel().mul(POTENCY_PER_AREA).mul(powerFactorLabel()).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(COST_PER_AREA).mul(powerFactorLabel()).add(COST_BASE).floor());
    }

    @Override
    protected void addZoneParams() {
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    /** 威力の補正のラベル:（威力 / 10 + 1）。 */
    private static StatLabel powerFactorLabel() {
        return powerLabel().div(MAX_POWER).add(1).parenthesize();
    }

    /** 威力の補正: 1 + 威力 / 10（威力は術式を組んだ時点の定数、1〜10、2026-11-14 から必須）。 */
    private double powerFactor() throws SpellCompilationException {
        return 1.0 + constantPower(power) / MAX_POWER;
    }

    @Override
    protected int potency(int area) throws SpellCompilationException {
        return POTENCY_BASE + (int) Math.floor(area * POTENCY_PER_AREA * powerFactor());
    }

    @Override
    protected int cost(int area) throws SpellCompilationException {
        return COST_BASE + (int) Math.floor(area * COST_PER_AREA * powerFactor());
    }

    @Override
    protected int runtimeCost(int size, SpellContext context) throws SpellRuntimeException {
        return COST_BASE + (int) Math.floor(areaOf(size) * COST_PER_AREA * (1.0 + powerValue(context, power) / MAX_POWER));
    }

    @Override
    protected EntityInterferenceZone createZone(ServerLevel level) {
        return new EntityInterferenceZone(level);
    }

    @Override
    protected void configure(EntityInterferenceZone zone, SpellContext context) throws SpellRuntimeException {
        zone.configurePower(powerValue(context, power));
    }
}
