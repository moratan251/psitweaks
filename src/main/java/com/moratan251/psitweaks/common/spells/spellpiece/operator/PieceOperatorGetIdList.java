package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.SpellParamContextualValueList;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorGetIdList extends PieceOperator {
    private SpellParam<Object> list;

    public PieceOperatorGetIdList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new SpellParamContextualValueList(SpellParam.GENERIC_NAME_LIST,
                PsitweaksSpellParams.CONTEXTUAL_VALUE_LIST_COLOR, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, list);
        PsitweaksListAdapter<Object> adapter = PsitweaksListAdapters.findAdapter(source.getClass()).orElseThrow(
                () -> new SpellRuntimeException(SpellRuntimeException.NULL_TARGET)
        );
        List<String> result = new ArrayList<>();

        int size = adapter.size(source);
        for (int i = 0; i < size; i++) {
            result.add(RegistryIdTargetHelper.getRegistryId(context, adapter.get(source, i)));
        }
        return StringListWrapper.make(result);
    }

    @Override
    public Class<?> getEvaluationType() {
        return StringListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string_list");
    }
}
