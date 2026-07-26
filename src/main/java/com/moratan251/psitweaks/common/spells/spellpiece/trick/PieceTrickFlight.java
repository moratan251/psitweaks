package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.effects.FlightPsiCostProfile;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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

public class PieceTrickFlight extends PieceTrick {
    private SpellParam<Entity> target;
    private SpellParam<Number> power;
    private SpellParam<Number> time;

    public PieceTrickFlight(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel("psi.spellparam.power", true)
                .mul(200)
                .add(new StatLabel("psi.spellparam.time", true).mul(0.1))
                .add(350)
                .floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel("psi.spellparam.power", true)
                .mul(50)
                .add("psi.spellparam.time", true)
                .add(100)
                .floor());
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(
                SpellParam.GENERIC_NAME_TARGET,
                SpellParam.YELLOW,
                false,
                false
        ));
        addParam(power = new ParamNumber(
                SpellParam.GENERIC_NAME_POWER,
                SpellParam.RED,
                false,
                true
        ));
        addParam(time = new ParamNumber(
                SpellParam.GENERIC_NAME_TIME,
                SpellParam.BLUE,
                false,
                true
        ));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double powerValue = getParamEvaluation(power);
        Double timeValue = getParamEvaluation(time);
        if (!isPositiveInteger(powerValue) || !isPositiveInteger(timeValue)) {
            throw new SpellCompilationException(
                    SpellCompilationException.NON_POSITIVE_INTEGER,
                    x,
                    y
            );
        }

        meta.addStat(
                EnumSpellStat.POTENCY,
                getPotency(powerValue.intValue(), timeValue.intValue())
        );
        meta.addStat(
                EnumSpellStat.COST,
                getCost(powerValue.intValue(), timeValue.intValue())
        );
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetValue = getParamValue(context, target);
        context.verifyEntity(targetValue);
        if (!(targetValue instanceof LivingEntity living)) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (!context.isInRadius(targetValue)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        int powerValue = getParamValue(context, power).intValue();
        int timeValue = getParamValue(context, time).intValue();
        MobEffectInstance effect = new MobEffectInstance(
                getPotion(),
                Math.max(1, timeValue) * 20,
                Math.max(0, powerValue - 1)
        );

        boolean serverSide = !living.level().isClientSide;
        FlightPsiCostProfile previousProfile = serverSide
                ? FlightPsiCostProfile.load(living)
                : FlightPsiCostProfile.DEFAULT;
        boolean added = living.addEffect(effect);
        if (serverSide) {
            if (added) {
                FlightPsiCostProfile.fromContext(context).save(living);
            } else {
                previousProfile.save(living);
            }
        }
        return null;
    }

    public Holder<MobEffect> getPotion() {
        return PsitweaksEffects.FLIGHT;
    }

    public int getCost(int power, int time) throws SpellCompilationException {
        return (int) multiplySafe(100.0 + power * 50.0 + time, 1.0);
    }

    public int getPotency(int power, int time) throws SpellCompilationException {
        return (int) multiplySafe(350.0 + power * 200.0 + time * 0.1, 1.0);
    }

    private static boolean isPositiveInteger(Double value) {
        return value != null && value > 0.0 && value.doubleValue() == value.intValue();
    }
}
