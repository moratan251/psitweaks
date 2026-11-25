package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.wrapper.SpellListLimits;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * The keys of the compound at the path (the root when the path is omitted), sorted, or the indexes "0", "1", ... of a
 * list. Anything else gives an empty List.
 */
public class PieceSelectorNbtKeys extends PieceSelectorNbtBase {
    public PieceSelectorNbtKeys(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        List<Tag> found = find(context);
        if (found.isEmpty()) {
            return StringListWrapper.EMPTY;
        }
        Tag tag = found.get(0);
        if (tag instanceof CompoundTag compound) {
            return StringListWrapper.make(sortedKeys(compound));
        }
        if (tag instanceof CollectionTag<?> list) {
            // List に入るのは先頭の MAX_ELEMENTS 個までなので、大きい配列でもそこまでしか作らない。
            int count = Math.min(list.size(), SpellListLimits.MAX_ELEMENTS);
            List<String> indexes = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                indexes.add(Integer.toString(i));
            }
            return StringListWrapper.make(indexes);
        }
        return StringListWrapper.EMPTY;
    }

    static List<String> sortedKeys(CompoundTag compound) {
        List<String> keys = new ArrayList<>(compound.getAllKeys());
        Collections.sort(keys);
        return keys;
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
