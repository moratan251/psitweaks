package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
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
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorBlockList extends PieceSelector {
    private SpellParam<VectorListWrapper> positions;

    public PieceSelectorBlockList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(positions = new ParamVectorListWrapper(SpellParam.GENERIC_NAME_LIST,
                PsitweaksSpellParams.VECTOR_LIST_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        VectorListWrapper list = getRequired(getParamValue(context, positions));
        List<BlockValue> blocks = new ArrayList<>(list.size());
        for (Vector3 vector : list) {
            blocks.add(BlockValueHelper.snapshotVector(context, vector));
        }
        return BlockListWrapper.make(blocks);
    }

    @Override
    public Class<?> getEvaluationType() {
        return BlockListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.block_list");
    }

    private static <T> T getRequired(T value) throws SpellRuntimeException {
        if (value == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        return value;
    }
}

