package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.NbtPathHelper;
import com.moratan251.psitweaks.common.spells.util.NbtTargetReader;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * One number per element of an Entity, Item or Block List, in the List's order: the value Selector: NBT Value would
 * give in Number mode (the first match, number tags only), else the Fallback, else 0. The output lines up with the
 * List, so it can be the Values of the value-ranked list operators.
 */
public class PieceSelectorNbtNumberList extends PieceSelector implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";
    private static final List<PsitweaksModeOption> MODES = List.of(
            PsitweaksModeOptions.ENTITY, PsitweaksModeOptions.ITEM, PsitweaksModeOptions.BLOCK);

    private PsitweaksModeOption mode = PsitweaksModeOptions.ENTITY;
    private SpellParam<?> list;
    private SpellParam<String> path;
    private SpellParam<Number> fallback;

    public PieceSelectorNbtNumberList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        rebuildParams(null);
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return MODES;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    @Override
    public void setModeOption(PsitweaksModeOption modeOption) {
        PsitweaksModeOption nextMode = normalizeModeOption(modeOption);
        if (nextMode.id().equals(getModeOption().id())) {
            return;
        }
        Map<String, SpellParam.Side> savedSides = new LinkedHashMap<>();
        paramSides.forEach((param, side) -> savedSides.put(param.name, side));
        mode = nextMode;
        rebuildParams(savedSides);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        mode = normalizeModeOption(PsitweaksModeOptions.byId(tag.getString(TAG_MODE)).orElse(null));
        rebuildParams(null);
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    private PsitweaksListAdapter<Object> adapter() {
        PsitweaksModeOption current = getModeOption();
        return PsitweaksListAdapters.findModeAdapter(current).orElseThrow(
                () -> new IllegalStateException("No PsiTweaks list adapter registered for mode " + current.id()));
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        addParam(list = adapter().createListParam(SpellParam.GENERIC_NAME_LIST, false));
        addParam(path = new ParamString(PsitweaksSpellParams.NBT_PATH, PsitweaksSpellParams.STRING_COLOR, false, false));
        addParam(fallback = new ParamNumber(PsitweaksSpellParams.FALLBACK, SpellParam.PURPLE, true, false));
        if (savedSides != null) {
            for (SpellParam<?> param : params.values()) {
                SpellParam.Side side = savedSides.get(param.name);
                if (side != null) {
                    paramSides.put(param, side);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, (SpellParam<Object>) list);
        String pathValue = getNotNullParamValue(context, path);
        Number fallbackValue = getParamValue(context, fallback);
        double otherwise = fallbackValue == null ? 0.0D : fallbackValue.doubleValue();
        PsitweaksListAdapter<Object> adapter = adapter();
        int size = adapter.size(source);
        List<Double> numbers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            List<Tag> found = NbtPathHelper.find(NbtTargetReader.read(context, adapter.get(source, i)).orElse(null), pathValue);
            OptionalDouble number = found.isEmpty() ? OptionalDouble.empty() : NbtPathHelper.number(found.get(0));
            numbers.add(number.orElse(otherwise));
        }
        return NumberListWrapper.make(numbers);
    }

    @Override
    public Class<?> getEvaluationType() {
        return NumberListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(PsitweaksModeOptions.NUMBER.listTranslationKey());
    }
}
