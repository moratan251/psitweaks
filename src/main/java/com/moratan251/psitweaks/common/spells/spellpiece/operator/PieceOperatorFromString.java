package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.PsitweaksModeOption;
import com.moratan251.psitweaks.api.PsitweaksModeOptions;
import com.moratan251.psitweaks.api.PsitweaksPlainValues;
import com.moratan251.psitweaks.api.PsitweaksValueKind;
import com.moratan251.psitweaks.api.value.PlainValueType;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceOperatorFromString extends PieceOperatorModeConversionBase {
    private SpellParam<String> string;

    public PieceOperatorFromString(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String value = getNotNullParamValue(context, string);
        PsitweaksModeOption mode = currentMode();

        // Number/Vector modes preserve the legacy fallback behavior from ListAdapter implementations.
        if (mode.id().equals(PsitweaksModeOptions.NUMBER.id())) {
            return StringSpellHelper.parseFiniteDouble(value).orElse(0D);
        }
        if (mode.id().equals(PsitweaksModeOptions.VECTOR.id())) {
            return StringSpellHelper.parseVector(value).orElseGet(() -> Vector3.zero.copy());
        }

        return currentPlainType().parse(value)
                .orElseThrow(() -> new SpellRuntimeException("psitweaks.spellerror.plain_memory_parse"));
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentPlainType().valueClass();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().elementTranslationKey());
    }

    @Override
    protected List<PsitweaksModeOption> availableModeOptions() {
        return PsitweaksPlainValues.snapshot().stream()
                .map(PlainValueType::modeOption)
                .toList();
    }

    @Override
    protected PsitweaksValueKind modeOptionKindFilter() {
        return PsitweaksValueKind.PLAIN;
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(string = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR, false,
                false));
        restoreParamSides(savedSides);
    }
}
