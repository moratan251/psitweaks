package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.marker.EntityMeteorLineNight;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.MeteorLineTuning;
import net.minecraft.server.level.ServerLevel;
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

/**
 * Spreads a sphere of night around the position (its core sits at the top), lights stars in its upper half and rains streaks of light from them
 * onto every creature inside that the caster may harm ({@link EntityMeteorLineNight}). The optional Radius defaults to
 * {@link MeteorLineTuning#DEFAULT_RADIUS} and is kept within {@link MeteorLineTuning#MIN_RADIUS} to
 * {@link MeteorLineTuning#MAX_RADIUS}; zero or less does not compile.
 */
public class PieceTrickMeteorLine extends PieceTrick {
    private static final int POTENCY = 1428;
    private static final int COST = 12_000;

    private SpellParam<Vector3> position;
    private SpellParam<Number> radius;

    public PieceTrickMeteorLine(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(radius = new ParamNumber(SpellParam.GENERIC_NAME_RADIUS, SpellParam.GREEN, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double radiusValue = getParamEvaluation(radius);
        if (radiusValue != null && radiusValue <= 0.0D) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionValue = getParamValue(context, position);
        if (positionValue == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionValue)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        double radiusValue = getParamValueOrDefault(context, radius, MeteorLineTuning.DEFAULT_RADIUS).doubleValue();
        if (radiusValue <= 0.0D) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }

        Vec3 center = SableRangeCompat.projectForEffect(level, positionValue);
        EntityMeteorLineNight night = new EntityMeteorLineNight(level, center, MeteorLineTuning.clampRadius(radiusValue));
        night.configure(context.caster, AttackRules.Options.from(context));
        level.addFreshEntity(night);
        return null;
    }
}
