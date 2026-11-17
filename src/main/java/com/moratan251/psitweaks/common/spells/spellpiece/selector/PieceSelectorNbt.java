package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * The entries of the compound or list at the path (the root when the path is omitted): "key:value" for a compound,
 * sorted by key, and "index:value" for a list, with values as SNBT. Anything else gives an empty List.
 */
public class PieceSelectorNbt extends PieceSelectorNbtBase {
    public PieceSelectorNbt(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        List<Tag> found = find(context);
        if (found.isEmpty()) {
            return StringListWrapper.EMPTY;
        }
        Tag tag = found.get(0);
        List<String> values = new ArrayList<>();
        if (tag instanceof CompoundTag compound) {
            for (String key : PieceSelectorNbtKeys.sortedKeys(compound)) {
                Tag value = compound.get(key);
                if (value != null) {
                    values.add(key + ":" + value);
                }
            }
        } else if (tag instanceof CollectionTag<?> list) {
            for (int i = 0; i < list.size(); i++) {
                values.add(i + ":" + list.get(i));
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
