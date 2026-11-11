package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityGravityZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;

/**
 * 作動式: 加重領域（仮、Gravity Zone）。範囲の中の生物に、「方向」の向きへ「威力」に応じた運動を毎 tick 足し続ける。
 * 方向は向きだけを使い、長さは関係ない。威力はコストを決めるため定数。
 */
public class PieceTrickGravityZone extends PieceTrickZoneBase<EntityGravityZone> {
    /** 威力の上限（仮）。 */
    public static final double MAX_POWER = 10.0;
    // 仮の数値: 規模 = 10 + 広さ × 威力 / 10、コスト = 40 + 広さ × 威力（広さ = サイズ²）
    private static final int POTENCY_BASE = 10;
    private static final int POTENCY_AREA_DIVISOR = 10;
    private static final int COST_BASE = 40;

    private SpellParam<Vector3> direction;
    private SpellParam<Number> power;

    public PieceTrickGravityZone(Spell spell) {
        super(spell, EntityGravityZone.class);
        setStatLabel(EnumSpellStat.POTENCY, areaLabel().mul(new StatLabel(SpellParam.GENERIC_NAME_POWER, true))
                .div(POTENCY_AREA_DIVISOR).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(new StatLabel(SpellParam.GENERIC_NAME_POWER, true))
                .add(COST_BASE).floor());
    }

    @Override
    protected void addZoneParams() {
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    /** 範囲・サイズ・方向・威力で入力の 4 つが埋まるので、「時間」は持たない（30 秒のまま）。 */
    @Override
    protected boolean hasTimeParam() {
        return false;
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
    protected EntityGravityZone createZone(ServerLevel level) {
        return new EntityGravityZone(level);
    }

    @Override
    protected void configure(EntityGravityZone zone, SpellContext context) throws SpellRuntimeException {
        Vector3 dir = getNotNullParamValue(context, direction);
        if (dir.isZero()) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        double powerValue = getNotNullParamValue(context, power).doubleValue();
        if (powerValue <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        zone.configurePush(dir.toVec3D(), (float) Math.min(MAX_POWER, powerValue));
    }
}
