package com.moratan251.psitweaks.common.spells;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickDispel extends PieceTrick {
    SpellParam<Entity> target;

    public PieceTrickDispel(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY,new StatLabel((double)90.0F));
        this.setStatLabel(EnumSpellStat.COST, new StatLabel((double)300.0F));
    }

    public void initParams() {
        this.addParam(this.target = new ParamEntity("psi.spellparam.target", SpellParam.YELLOW, false, false));
    }

    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, 90);
        meta.addStat(EnumSpellStat.COST, 300);
    }

    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = (Entity)this.getParamValue(context, this.target);
        context.verifyEntity(targetVal);
        if (!(targetVal instanceof LivingEntity)) {
            throw new SpellRuntimeException("psi.spellerror.nulltarget");
        } else if (!context.isInRadius(targetVal)) {
            throw new SpellRuntimeException("psi.spellerror.outsideradius");
        } else {
            ((LivingEntity)targetVal).removeAllEffects();

            return null;
        }
    }
}