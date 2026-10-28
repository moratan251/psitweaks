package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.SpellGram.region.EntitySpellGramRegion;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramRegions;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 魔法式回収（試作）。対象の自分の領域型を解く。対象をつながないと、自分の領域型をすべて解く。
 * Psi は戻らない。
 */
public class PieceTrickSpellGramRecall extends PieceTrick {
    private SpellParam<Entity> target;

    public PieceTrickSpellGramRecall(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(0));
        setStatLabel(EnumSpellStat.COST, new StatLabel(0));
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psitweaks.spellparam.spellgram", SpellParam.YELLOW, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (context.caster.level().isClientSide) {
            return null;
        }
        Entity targetVal = getParamValue(context, target);
        if (targetVal == null) {
            SpellGramRegions.recallAll(CasterIds.idOf(context.caster));
            return null;
        }
        EntitySpellGramRegion region = PieceTrickSpellGramImbueBase.ownRegion(context, targetVal);
        region.end(EntitySpellGramRegion.EndReason.RECALLED);
        return null;
    }
}
