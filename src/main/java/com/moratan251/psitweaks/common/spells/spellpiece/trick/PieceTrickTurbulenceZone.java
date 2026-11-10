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
    public static final double MAX_POWER = 10.0;
    // 仮の数値: 規模 = 10 + 広さ × 威力 / 10、コスト = 40 + 広さ × 威力（広さ = サイズ²）
    private static final int POTENCY_BASE = 10;
    private static final int POTENCY_AREA_DIVISOR = 10;
    private static final int COST_BASE = 40;

    private SpellParam<Number> power;

    public PieceTrickTurbulenceZone(Spell spell) {
        super(spell, EntityTurbulenceZone.class);
        setStatLabel(EnumSpellStat.POTENCY, areaLabel().mul(new StatLabel(SpellParam.GENERIC_NAME_POWER, true))
                .div(POTENCY_AREA_DIVISOR).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(new StatLabel(SpellParam.GENERIC_NAME_POWER, true))
                .add(COST_BASE).floor());
    }

    @Override
    protected void addZoneParams() {
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    protected int potency(int area) throws SpellCompilationException {
        return POTENCY_BASE + (int) Math.floor(area * constantPower() / POTENCY_AREA_DIVISOR);
    }

    @Override
    protected int cost(int area) throws SpellCompilationException {
        return COST_BASE + (int) Math.floor(area * constantPower());
    }

    private double constantPower() throws SpellCompilationException {
        Object value = getParamEvaluation(power);
        if (!(value instanceof Number number) || number.doubleValue() <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return Math.min(MAX_POWER, number.doubleValue());
    }

    @Override
    protected EntityTurbulenceZone createZone(ServerLevel level) {
        return new EntityTurbulenceZone(level);
    }

    @Override
    protected void configure(EntityTurbulenceZone zone, SpellContext context) throws SpellRuntimeException {
        double powerValue = getNotNullParamValue(context, power).doubleValue();
        if (powerValue <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        zone.configureWind((float) Math.min(MAX_POWER, powerValue));
    }
}
