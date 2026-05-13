package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.entities.EntityMeteorLineBeam;
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

public class PieceTrickMeteorLine extends PieceTrick {
    private static final double MAX_RANGE = 64.0D;
    private static final double MIN_RAY_LENGTH_SQR = 1.0E-8D;
    private static final int POTENCY = 1428;
    private static final int COST = 12_000;

    private SpellParam<Vector3> position;
    private SpellParam<Vector3> ray;
    private SpellParam<Number> maxDistance;

    public PieceTrickMeteorLine(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(ray = new ParamVector(SpellParam.GENERIC_NAME_RAY, SpellParam.CYAN, false, false));
        addParam(maxDistance = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double maxDistanceValue = getParamEvaluation(maxDistance);
        if (maxDistanceValue != null && maxDistanceValue <= 0.0D) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionValue = getParamValue(context, position);
        Vector3 rayValue = getParamValue(context, ray);
        Number maxDistanceValue = getParamValueOrDefault(context, maxDistance, MAX_RANGE);

        if (positionValue == null || rayValue == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionValue)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Vec3 rawDirection = rayValue.toVec3D();
        if (rawDirection.lengthSqr() < MIN_RAY_LENGTH_SQR) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }

        double distance = maxDistanceValue.doubleValue();
        if (distance <= 0.0D) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        distance = Math.min(MAX_RANGE, distance);

        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        Vec3 start = SableRangeCompat.projectForEffect(level, positionValue);
        Vec3 direction = SableRangeCompat.projectDirectionForEffect(level, positionValue, rayValue);
        if (direction.lengthSqr() < MIN_RAY_LENGTH_SQR) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }

        EntityMeteorLineBeam beam = new EntityMeteorLineBeam(level, context.caster, start, direction, distance);
        beam.setSafeToPlayers(SpellSafetyUtils.hasSafeToPlayers(context));
        level.addFreshEntity(beam);

        return null;
    }
}
