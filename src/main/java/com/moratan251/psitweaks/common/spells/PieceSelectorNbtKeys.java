package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorNbtKeys extends PieceSelector {
    private static final String ID_TAG = "id";

    private SpellParam<Entity> target;

    public PieceSelectorNbtKeys(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psi.spellparam.target", SpellParam.GRAY, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity entity = getNotNullParamValue(context, target);
        context.verifyEntity(entity);

        CompoundTag tag = NbtPredicate.getEntityTagToCompare(entity);
        List<String> keys = new ArrayList<>();
        for (String key : tag.getAllKeys()) {
            if (!ID_TAG.equals(key)) {
                keys.add(key);
            }
        }
        Collections.sort(keys);
        return StringListWrapper.make(keys);
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
