package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.NbtPathHelper;
import com.moratan251.psitweaks.common.spells.util.NbtTargetReader;
import com.moratan251.psitweaksqol.api.PsitweaksValueKind;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Filters an Entity, Item or Block List by the NBT values its elements have at a path. Each element's NBT is read
 * through {@link NbtTargetReader} and the path through {@link NbtPathHelper}; the subclass decides from the values the
 * path reaches whether the element stays. The output keeps the List's order (an Entity List takes Psi's own order).
 */
public abstract class PieceOperatorListNbtBase extends PieceOperatorModeListBase {
    private SpellParam<?> list;
    private SpellParam<String> path;

    protected PieceOperatorListNbtBase(Spell spell) {
        super(spell);
    }

    @Override
    protected PsitweaksValueKind modeOptionKindFilter() {
        return PsitweaksValueKind.CONTEXTUAL;
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = currentAdapter().createListParam(SpellParam.GENERIC_NAME_LIST, false));
        addParam(path = new ParamString(PsitweaksSpellParams.NBT_PATH, PsitweaksSpellParams.STRING_COLOR, false, false));
        addFilterParams();
        restoreParamSides(savedSides);
    }

    /** Adds the inputs after the List and the NBT Path. */
    protected abstract void addFilterParams();

    /** Reads this cast's other inputs and returns the test for the values one element's path reaches. */
    protected abstract ElementTest prepare(SpellContext context) throws SpellRuntimeException;

    @FunctionalInterface
    protected interface ElementTest {
        boolean keeps(List<Tag> found);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        String pathValue = getNotNullParamValue(context, path);
        ElementTest test = prepare(context);
        PsitweaksListAdapter<Object> adapter = currentAdapter();
        List<Object> result = new ArrayList<>();

        int size = adapter.size(source);
        for (int i = 0; i < size; i++) {
            Object value = adapter.get(source, i);
            if (test.keeps(NbtPathHelper.find(NbtTargetReader.read(context, value).orElse(null), pathValue))) {
                result.add(value);
            }
        }
        return adapter.add(adapter.emptyList(), result);
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentListType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().listTranslationKey());
    }
}
