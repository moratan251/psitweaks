package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.NbtPathHelper;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
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

/**
 * The value at the path, from the first match. String mode gives a String as it is, a number without its type suffix
 * and anything else as SNBT; Number mode gives the value of a number tag only. When nothing usable is found the
 * optional Fallback is given, else "" or 0.
 */
public class PieceSelectorNbtValue extends PieceSelectorNbtBase implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";
    private static final String TAG_PARAMS = "params";
    /** The name the path input had before it became {@link PsitweaksSpellParams#NBT_PATH}. */
    private static final String OLD_PATH_NAME = PsitweaksSpellParams.STRING;
    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.STRING, PsitweaksModeOptions.NUMBER);

    private PsitweaksModeOption mode = PsitweaksModeOptions.STRING;
    private SpellParam<?> fallback;

    public PieceSelectorNbtValue(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean pathOptional() {
        return false;
    }

    @Override
    protected void addAdditionalParams() {
        if (isNumberMode()) {
            addParam(fallback = new ParamNumber(PsitweaksSpellParams.FALLBACK, SpellParam.PURPLE, true, false));
        } else {
            addParam(fallback = new ParamString(PsitweaksSpellParams.FALLBACK, SpellParam.PURPLE, true, false));
        }
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
        CompoundTag params = tag.getCompound(TAG_PARAMS);
        if (params.contains(OLD_PATH_NAME) && !params.contains(PsitweaksSpellParams.NBT_PATH)) {
            tag = tag.copy();
            CompoundTag migrated = tag.getCompound(TAG_PARAMS);
            migrated.put(PsitweaksSpellParams.NBT_PATH, migrated.get(OLD_PATH_NAME));
            migrated.remove(OLD_PATH_NAME);
        }
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    private boolean isNumberMode() {
        return mode != null && mode.id().equals(PsitweaksModeOptions.NUMBER.id());
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        initParams();
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
    public Object execute(SpellContext context) throws SpellRuntimeException {
        List<Tag> found = find(context);
        Tag first = found.isEmpty() ? null : found.get(0);
        if (isNumberMode()) {
            OptionalDouble number = first == null ? OptionalDouble.empty() : NbtPathHelper.number(first);
            if (number.isPresent()) {
                return number.getAsDouble();
            }
            Object value = getParamValue(context, fallback);
            return value instanceof Number fallbackNumber ? fallbackNumber.doubleValue() : 0.0D;
        }
        if (first != null) {
            return NbtPathHelper.text(first);
        }
        Object value = getParamValue(context, fallback);
        return value instanceof String fallbackString ? fallbackString : "";
    }

    @Override
    public Class<?> getEvaluationType() {
        return isNumberMode() ? Double.class : String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(isNumberMode() ? "psi.datatype.number" : "psitweaks.datatype.string");
    }
}
