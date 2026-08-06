package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public class PieceOperatorAlive extends PsitweaksPieceOperator {
    private SpellParam<EntityListWrapper> entities;

    public PieceOperatorAlive(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(entities = new ParamEntityListWrapper(SpellParam.GENERIC_NAME_LIST, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        EntityListWrapper source = getNotNullParamValue(context, entities);
        List<Entity> result = new ArrayList<>();
        for (Entity entity : source) {
            if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                result.add(livingEntity);
            }
        }
        return EntityListWrapper.make(result);
    }

    @Override
    public Class<?> getEvaluationType() {
        return EntityListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.entity_list_wrapper");
    }
}

