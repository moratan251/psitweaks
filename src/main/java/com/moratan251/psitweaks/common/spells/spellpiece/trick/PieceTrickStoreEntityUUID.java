package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import com.moratan251.psitweaks.common.spells.memory.CadPlainMemory;
import com.moratan251.psitweaks.common.spells.util.ReadOnlyTargets;
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
        setStatLabel(EnumSpellStat.PROJECTION, new StatLabel(0.0D));
    }


    public void initParams() {
        addParam(this.number = new ParamNumber("psi.spellparam.number", SpellParam.GREEN, false, false));
        addParam(this.target = new ParamEntity("psi.spellparam.target", SpellParam.BLUE, false, false));
    }

    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
        Double numberVal = (Double)this.getParamEvaluation(this.number);
        if (numberVal != null && !(numberVal <= (double)0.0F) && numberVal == (double)numberVal.intValue()) {
            meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(numberVal * 8.0));
        } else {
            throw new SpellCompilationException("psi.spellerror.nonpositiveinteger", this.x, this.y);
        }
    }

    public Object execute(SpellContext context) throws SpellRuntimeException {
        int internalSlot = CadPlainMemory.internalSlot(this.getParamValue(context, this.number));
        Entity target = this.getParamValue(context, this.target);
        // Only remembers which entity it was, so bosses and riders may be stored too.
        ReadOnlyTargets.verify(context, target);

        if (CadPlainMemory.isSlotLocked(context, internalSlot)) {
            return null;
        }

        // Selector: Stored Entity finds an autonomous caster by its machine key, not by its owner's UUID.
        CadPlainMemory.store(context, internalSlot, CasterIds.idOf(target).toString());
        CadPlainMemory.markSlotLocked(context, internalSlot);
        return null;
    }
}

