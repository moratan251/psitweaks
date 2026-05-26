package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.ModeStringParsingHelper;
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
        return ModeStringParsingHelper.elementFromString(currentMode(), value);
    }

    @Override
    public Class<?> getEvaluationType() {
        return switch (currentMode()) {
            case STRING -> String.class;
            case NUMBER -> Double.class;
            case VECTOR -> Vector3.class;
            case ENTITY, ITEM, BLOCK -> Object.class;
        };
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().elementTranslationKey());
    }

    @Override
    protected List<ListElementMode> availableElementModes() {
        return ListElementMode.plainModes();
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(string = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR, false,
                false));
        restoreParamSides(savedSides);
    }
}
