package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickSetSpellGramFollowTarget extends PieceTrick {

    private static final String ERROR_NOT_SPELLGRAM_OBJECT = "psitweaks.spellerror.not_spellgram_object";

    private SpellParam<Entity> spellGramTarget;
    private SpellParam<Entity> followTarget;

    public PieceTrickSetSpellGramFollowTarget(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(40));
        setStatLabel(EnumSpellStat.COST, new StatLabel(150));
    }

    @Override
    public void initParams() {
        addParam(spellGramTarget = new ParamEntity("psitweaks.spellparam.spellgram", SpellParam.YELLOW, false, false));
        addParam(followTarget = new ParamEntity("psi.spellparam.target", SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, 40);
        meta.addStat(EnumSpellStat.COST, 150);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity spellGramTargetVal = getParamValue(context, spellGramTarget);
        Entity followTargetVal = getParamValue(context, followTarget);

        context.verifyEntity(spellGramTargetVal);
        context.verifyEntity(followTargetVal);

        if (!context.isInRadius(spellGramTargetVal) || !context.isInRadius(followTargetVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        if (!(spellGramTargetVal instanceof SpellGramObject spellGramObject)) {
            throw new SpellRuntimeException(ERROR_NOT_SPELLGRAM_OBJECT);
        }

        spellGramObject.setFollowTarget(followTargetVal, true);
        return null;
    }
}
