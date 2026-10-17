package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.combat.BarrierShields;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Raises a shield of (10 x Power) points on the target for Time seconds. Incoming damage is taken from the shield
 * first. The parameters keep the names and colours of the old effect-based version so saved spells still load.
 */
public class PieceTrickBarrier extends PieceTrick {
    private static final double POTENCY_BASE = 20.0;
    private static final double POTENCY_PER_POWER_SQUARED = 15.0;
    private static final double POTENCY_PER_SECOND = 1.0;
    private static final double COST_BASE = 40.0;
    private static final double COST_PER_POWER_SQUARED = 60.0;
    private static final double COST_PER_SECOND = 4.0;

    private SpellParam<Entity> target;
    private SpellParam<Number> power;
    private SpellParam<Number> time;

    public PieceTrickBarrier(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel("psi.spellparam.power", true).square().mul(POTENCY_PER_POWER_SQUARED)
                .add(new StatLabel("psi.spellparam.time", true).mul(POTENCY_PER_SECOND)).add(POTENCY_BASE));
        setStatLabel(EnumSpellStat.COST, new StatLabel("psi.spellparam.power", true).square().mul(COST_PER_POWER_SQUARED)
                .add(new StatLabel("psi.spellparam.time", true).mul(COST_PER_SECOND)).add(COST_BASE));
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psi.spellparam.target", 13814826, false, false));
        addParam(power = new ParamNumber("psi.spellparam.power", 13773354, false, true));
        addParam(time = new ParamNumber("psi.spellparam.time", 2774482, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        int powerVal = CombatTrickStats.positiveInteger(getParamEvaluation(power), x, y);
        int timeVal = CombatTrickStats.positiveInteger(getParamEvaluation(time), x, y);
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(
                POTENCY_BASE + POTENCY_PER_POWER_SQUARED * powerVal * (double) powerVal + POTENCY_PER_SECOND * timeVal));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(
                COST_BASE + COST_PER_POWER_SQUARED * powerVal * (double) powerVal + COST_PER_SECOND * timeVal));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = getParamValue(context, target);
        if (CombatSpellHelper.skipFallenTarget(context, targetVal)) {
            return null;
        }
        context.verifyEntity(targetVal);
        if (!(targetVal instanceof LivingEntity living)) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (!context.isInRadius(targetVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (living.level().isClientSide) {
            return null;
        }
        int powerVal = Math.max(1, getParamValue(context, power).intValue());
        int seconds = Math.max(1, getParamValue(context, time).intValue());
        float amount = (float) (powerVal * BarrierShields.SHIELD_PER_POWER
                * PsitweaksConfig.COMMON.barrierShieldMultiplier.get());
        BarrierShields.raise(living, amount, seconds * 20, CombatSpellHelper.spellColor(context.caster));
        return null;
    }
}
