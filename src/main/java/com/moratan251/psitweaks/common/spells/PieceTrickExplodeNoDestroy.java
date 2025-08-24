package com.moratan251.psitweaks.common.spells;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.spell.trick.PieceTrickExplode;


public class PieceTrickExplodeNoDestroy extends PieceTrick {
    SpellParam<Vector3> position;
    SpellParam<Number> power;

    public PieceTrickExplodeNoDestroy(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY, (new StatLabel("psi.spellparam.power", true)).max((double)0.5F).mul((double)70.0F).floor());
        this.setStatLabel(EnumSpellStat.COST, (new StatLabel("psi.spellparam.power", true)).max((double)0.5F).mul((double)210.0F).floor());
    }

    public void initParams() {
        this.addParam(this.position = new ParamVector("psi.spellparam.position", SpellParam.BLUE, false, false));
        this.addParam(this.power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
    }

    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double powerVal = (Double)this.getParamEvaluation(this.power);
        if (powerVal != null && !(powerVal <= (double)0.0F)) {
            powerVal = Math.max((double)0.5F, powerVal);
            meta.addStat(EnumSpellStat.POTENCY, (int)(powerVal * (double)70.0F));
            meta.addStat(EnumSpellStat.COST, (int)(powerVal * (double)210.0F));
        } else {
            throw new SpellCompilationException("psi.spellerror.nonpositivevalue", this.x, this.y);
        }
    }

    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = (Vector3)this.getParamValue(context, this.position);
        double powerVal = ((Number)this.getParamValue(context, this.power)).doubleValue();
        if (positionVal == null) {
            throw new SpellRuntimeException("psi.spellerror.nullvector");
        } else if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException("psi.spellerror.outsideradius");
        } else {
            BlockPos pos = positionVal.toBlockPos();
            context.focalPoint.getCommandSenderWorld().explode(context.focalPoint, positionVal.x, positionVal.y, positionVal.z, (float)powerVal, Level.ExplosionInteraction.NONE);
            return null;
        }
    }

}