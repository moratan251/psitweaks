package com.moratan251.psitweaks.common.spells;

import mekanism.api.radiation.IRadiationManager;
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

public class PieceTrickRadiationInjection extends PieceTrick {

    private static final double MIN_POWER = 0.1;
    private static final double POTENCY_BASE = 400.0;
    private static final double POTENCY_PER_POWER = 100.0;
    private static final double COST_BASE = 200.0;
    private static final double COST_PER_POWER = 400.0;

    private SpellParam<Entity> target;
    private SpellParam<Number> power;

    public PieceTrickRadiationInjection(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psi.spellparam.target", SpellParam.YELLOW, false, false));
        addParam(power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double powerVal = getParamEvaluation(power);
        if (powerVal == null || powerVal <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        powerVal = Math.max(MIN_POWER, powerVal);
        meta.addStat(EnumSpellStat.POTENCY, (int) Math.ceil(POTENCY_BASE + powerVal * POTENCY_PER_POWER));
        meta.addStat(EnumSpellStat.COST, (int) Math.ceil(COST_BASE + powerVal * COST_PER_POWER));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = getParamValue(context, target);
        context.verifyEntity(targetVal);

        if (!(targetVal instanceof LivingEntity livingEntity)) {
            throw new SpellRuntimeException("psi.spellerror.nulltarget");
        }
        if (!context.isInRadius(targetVal)) {
            throw new SpellRuntimeException("psi.spellerror.outsideradius");
        }

        double powerVal = getParamValue(context, power).doubleValue();
        if (powerVal <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        if (context.caster.level().isClientSide) {
            return null;
        }

        if (!IRadiationManager.INSTANCE.isRadiationEnabled()) {
            return null;
        }

        double radiation = Math.max(MIN_POWER, powerVal);
        IRadiationManager.INSTANCE.radiate(livingEntity, radiation);
        return null;
    }
}
