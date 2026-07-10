package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.api.PsitweaksPlainValues;
import com.moratan251.psitweaks.common.spells.memory.CadPlainMemory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceSelector;

import java.util.UUID;

public class PieceSelectorStoredEntity extends PieceSelector {

    SpellParam<Number> number;

    public PieceSelectorStoredEntity(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.GREEN, false, false));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Entity.class;
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double numberVal = (Double) getParamEvaluation(number);
        if (numberVal != null && numberVal > 0D && numberVal == numberVal.intValue()) {
            meta.addStat(EnumSpellStat.POTENCY, numberVal.intValue() * 6);
        } else {
            throw new SpellCompilationException("psi.spellerror.nonpositiveinteger", x, y);
        }
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int internalSlot = CadPlainMemory.internalSlot(this.getParamValue(context, this.number));
        if (CadPlainMemory.isSlotLocked(context, internalSlot)) {
            throw new SpellRuntimeException("psi.spellerror.lockedmemory");
        }

        String uuidStr = (String) CadPlainMemory.read(context, internalSlot, PsitweaksPlainValues.STRING);
        if (uuidStr.isEmpty()) {
            return null;
        }

        try {
            UUID uuid = UUID.fromString(uuidStr);
            return ((ServerLevel) context.caster.level()).getEntity(uuid);
        } catch (Exception e) {
            return null;
        }
    }
}
