package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.compat.SablePhysicsCompat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickPhysicalPropulsion extends PieceTrick {
    private static final double MIN_POWER = 0.1D;
    private static final double MIN_RAY_LENGTH_SQUARED = 1.0E-8D;
    private static final double POTENCY_BASE = 80.0D;
    private static final double POTENCY_PER_POWER = 80.0D;
    private static final double COST_BASE = 100.0D;
    private static final double COST_PER_POWER = 300.0D;

    private SpellParam<Vector3> position;
    private SpellParam<Vector3> ray;
    private SpellParam<Number> power;

    public PieceTrickPhysicalPropulsion(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY,
                new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER).mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST,
                new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER).mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(ray = new ParamVector(SpellParam.GENERIC_NAME_RAY, SpellParam.CYAN, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = getParamEvaluation(power);
        if (powerVal == null || powerVal <= 0.0D) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        double clampedPower = Math.max(MIN_POWER, powerVal);
        meta.addStat(EnumSpellStat.POTENCY, (int) (POTENCY_BASE + clampedPower * POTENCY_PER_POWER));
        meta.addStat(EnumSpellStat.COST, (int) (COST_BASE + clampedPower * COST_PER_POWER));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        Vector3 rayVal = getParamValue(context, ray);
        Number powerVal = getParamValue(context, power);

        if (positionVal == null || rayVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (powerVal.doubleValue() <= 0.0D) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Vec3 direction = rayVal.toVec3D();
        if (direction.lengthSqr() < MIN_RAY_LENGTH_SQUARED) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }

        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        SablePhysicsCompat.applyImpulseAlongRay(level, positionVal, direction, Math.max(MIN_POWER, powerVal.doubleValue()), context.caster);
        return null;
    }
}
