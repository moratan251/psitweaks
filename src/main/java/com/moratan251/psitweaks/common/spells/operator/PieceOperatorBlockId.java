package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamBlockValue;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorBlockId extends PieceOperator {
    private SpellParam<BlockValue> target;

    public PieceOperatorBlockId(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamBlockValue(SpellParam.GENERIC_NAME_TARGET, PsitweaksSpellParams.BLOCK_COLOR,
                false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockValue block = getNotNullParamValue(context, target);
        return StringSpellHelper.clamp(block.blockId().toString());
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }
}
