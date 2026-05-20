package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public class PieceOperatorEntityListToStringList extends PieceOperator {
    private SpellParam<EntityListWrapper> list;

    public PieceOperatorEntityListToStringList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new ParamEntityListWrapper(SpellParam.GENERIC_NAME_LIST, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        EntityListWrapper source = getNotNullParamValue(context, list);
        List<String> result = new ArrayList<>();

        for (Entity entity : source) {
            result.add(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
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
