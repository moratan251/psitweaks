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
    // 仮の数値（2026-11-14 ユーザー、今後調整）: 規模 = 10 + 広さ / 5 ×（1 + 威力 / 10）、コスト = 50 + 広さ × 2 ×（1 + 威力 / 10）
    // （広さ = サイズ²）。威力を省けば 0。
    private static final int POTENCY_BASE = 10;
    private static final int POTENCY_AREA_DIVISOR = 5;
    private static final int COST_BASE = 50;
    private static final int COST_PER_AREA = 2;

    /** 威力の上限。 */
    public static final double MAX_POWER = 10.0;

    private SpellParam<Number> power;

    public PieceTrickInterferenceZone(Spell spell) {
        super(spell, EntityInterferenceZone.class);
        setStatLabel(EnumSpellStat.POTENCY, areaLabel().div(POTENCY_AREA_DIVISOR).mul(powerFactorLabel()).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(COST_PER_AREA).mul(powerFactorLabel()).add(COST_BASE).floor());
    }

    @Override
    protected void addZoneParams() {
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, true, true));
    }

    /** 威力の補正のラベル:（威力 / 10 + 1）。 */
    private static StatLabel powerFactorLabel() {
        return new StatLabel(SpellParam.GENERIC_NAME_POWER, true).div(MAX_POWER).add(1).parenthesize();
    }

    /** 威力の補正: 1 + 威力 / 10（威力は術式を組んだ時点の定数、省けば 0）。 */
    private double powerFactor() throws SpellCompilationException {
        Object value = getParamEvaluation(power);
        if (value == null) {
            return 1.0;
        }
        if (!(value instanceof Number number) || number.doubleValue() < 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return 1.0 + Math.min(MAX_POWER, number.doubleValue()) / MAX_POWER;
    }

    @Override
    protected int potency(int area) throws SpellCompilationException {
        return POTENCY_BASE + (int) Math.floor((double) area / POTENCY_AREA_DIVISOR * powerFactor());
    }

    @Override
    protected int cost(int area) throws SpellCompilationException {
        return COST_BASE + (int) Math.floor(area * COST_PER_AREA * powerFactor());
    }

    @Override
    protected EntityInterferenceZone createZone(ServerLevel level) {
        return new EntityInterferenceZone(level);
    }

    @Override
    protected void configure(EntityInterferenceZone zone, SpellContext context) throws SpellRuntimeException {
        Number value = getParamValue(context, power);
        double powerValue = value == null ? 0.0 : value.doubleValue();
        if (powerValue < 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        zone.configurePower((float) Math.min(MAX_POWER, powerValue));
    }
}
