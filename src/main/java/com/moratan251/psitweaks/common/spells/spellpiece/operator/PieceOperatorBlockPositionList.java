package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamBlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorBlockPositionList extends PsitweaksPieceOperator {
    private SpellParam<BlockListWrapper> target;

    public PieceOperatorBlockPositionList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamBlockListWrapper(SpellParam.GENERIC_NAME_LIST,
                PsitweaksSpellParams.BLOCK_LIST_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockListWrapper list = getNotNullParamValue(context, target);
        List<Vector3> vectors = new ArrayList<>(list.size());
        for (BlockValue block : list) {
            vectors.add(block.positionVector());
        }
        return VectorListWrapper.make(vectors);
    }

    @Override
    public Class<?> getEvaluationType() {
        return VectorListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.vector_list");
    }
}
