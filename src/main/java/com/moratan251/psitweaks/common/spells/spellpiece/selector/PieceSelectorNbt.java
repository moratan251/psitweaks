package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.util.SnbtText;
import com.moratan251.psitweaks.common.spells.wrapper.SpellListLimits;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaksqol.api.StringSpellHelper;
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
 * sorted by key, and "index:value" for a list, with values as SNBT. Anything else gives an empty List. Each entry is
 * written only up to the String length cap ({@link SnbtText}), and no more entries than the String List keeps.
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
        Entries entries = new Entries();
        if (tag instanceof CompoundTag compound) {
            for (String key : PieceSelectorNbtKeys.sortedKeys(compound)) {
                Tag value = compound.get(key);
                if (value != null && !entries.add(key, value)) {
                    break;
                }
            }
        } else if (tag instanceof CollectionTag<?> list) {
            for (int i = 0; i < list.size(); i++) {
                if (!entries.add(Integer.toString(i), list.get(i))) {
                    break;
                }
            }
        }
        return StringListWrapper.make(entries.values);
    }

    /**
     * The "label:value" texts, each as {@link StringListWrapper#make} cleans and cuts it, collected until the String List
     * could take no more (the last one added may be one it drops).
     */
    private static final class Entries {
        private final List<String> values = new ArrayList<>();
        private long characters;

        /** Adds the entry; false once no later entry could be kept. */
        boolean add(String label, Tag value) {
            // The label ends with ':', so cleaning the two parts apart gives the same text as cleaning them together.
            String prefix = StringSpellHelper.sanitize(label + ":");
            String text = prefix + SnbtText.write(value, StringSpellHelper.MAX_STRING_LENGTH - prefix.length()).text();
            values.add(text);
            characters += text.length();
            return values.size() < SpellListLimits.MAX_ELEMENTS && characters <= SpellListLimits.MAX_STRING_LIST_CHARACTERS;
        }
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
