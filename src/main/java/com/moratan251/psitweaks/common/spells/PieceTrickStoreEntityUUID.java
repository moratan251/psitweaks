package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.memory.CadPlainMemory;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickStoreEntityUUID extends PieceTrick {

    SpellParam<Number> number;
    SpellParam<Entity> target;

    public PieceTrickStoreEntityUUID(Spell spell) {
        super(spell);
    }


    public void initParams() {
        addParam(this.number = new ParamNumber("psi.spellparam.number", SpellParam.GREEN, false, false));
        addParam(this.target = new ParamEntity("psi.spellparam.target", SpellParam.BLUE, false, false));
    }

    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
        Double numberVal = (Double)this.getParamEvaluation(this.number);
        if (numberVal != null && !(numberVal <= (double)0.0F) && numberVal == (double)numberVal.intValue()) {
            meta.addStat(EnumSpellStat.POTENCY, numberVal.intValue() * 8);
        } else {
            throw new SpellCompilationException("psi.spellerror.nonpositiveinteger", this.x, this.y);
        }
    }

    public Object execute(SpellContext context) throws SpellRuntimeException {
        int internalSlot = CadPlainMemory.internalSlot(this.getParamValue(context, this.number));
        Entity target = this.getParamValue(context, this.target);
        context.verifyEntity(target);

        if (target == null) {
            throw new SpellRuntimeException("psi.spellerror.nulltarget");
        }

        if (CadPlainMemory.isSlotLocked(context, internalSlot)) {
            return null;
        }

        CadPlainMemory.store(context, internalSlot, target.getUUID().toString());
        CadPlainMemory.markSlotLocked(context, internalSlot);
        return null;
    }
}

