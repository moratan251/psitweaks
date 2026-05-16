package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public class PieceOperatorEntityListExclude extends PieceOperator {
    private SpellParam<EntityListWrapper> list;
    private SpellParam<String> string;

    public PieceOperatorEntityListExclude(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new ParamEntityListWrapper(SpellParam.GENERIC_NAME_LIST, SpellParam.BLUE, false, false));
        addParam(string = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        EntityListWrapper source = getNotNullParamValue(context, list);
        String targetId = getNotNullParamValue(context, string);
        List<Entity> result = new ArrayList<>();

        for (Entity entity : source) {
            if (!matchesTypeId(entity, targetId)) {
                result.add(entity);
            }
        }

        return EntityListWrapper.make(result);
    }

    @Override
    public Class<?> getEvaluationType() {
        return EntityListWrapper.class;
    }

    private static boolean matchesTypeId(Entity entity, String targetId) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals(targetId);
    }
}
