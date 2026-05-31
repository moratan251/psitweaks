package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceSelectorNbt extends PieceSelectorNbtBase {
    public PieceSelectorNbt(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        NbtTargetHelper.TargetData data = getTargetData(context);
        List<String> keys = NbtTargetHelper.sortedKeys(data);

        List<String> values = new ArrayList<>(keys.size());
        for (String key : keys) {
            Tag value = data.tag().get(key);
            if (value != null) {
                values.add(key + ":" + value);
            }
        }

        return StringListWrapper.make(values);
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
