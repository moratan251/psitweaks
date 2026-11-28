package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

/**
 * Outputs If True when the absolute value of Condition is 1 or more, otherwise If False (the threshold of Trick: Jump
 * and Psi's Trick: Die). The mode picks the type of the two values. Both inputs are evaluated before this piece runs,
 * so an error in the unpicked input still stops the spell.
 */
public class PieceOperatorConditionalSelect extends PieceOperatorFixedModeBase {
    public static final double TRUE_THRESHOLD = 1.0D;

    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.STRING,
            PsitweaksModeOptions.NUMBER, PsitweaksModeOptions.VECTOR, PsitweaksModeOptions.MATRIX,
            PsitweaksModeOptions.ENTITY, PsitweaksModeOptions.ITEM, PsitweaksModeOptions.BLOCK);

    private SpellParam<Number> condition;
    private SpellParam<?> ifTrue;
    private SpellParam<?> ifFalse;

    public PieceOperatorConditionalSelect(Spell spell) {
        super(spell);
    }

    @Override
    protected List<PsitweaksModeOption> modes() {
        return MODES;
    }

    @Override
    protected PsitweaksModeOption defaultMode() {
        return PsitweaksModeOptions.NUMBER;
    }

    @Override
    protected void addModeParams() {
        addParam(condition = new ParamNumber(PsitweaksSpellParams.CONDITION, SpellParam.BLUE, false, false));
        addParam(ifTrue = valueParam(PsitweaksSpellParams.IF_TRUE));
        addParam(ifFalse = valueParam(PsitweaksSpellParams.IF_FALSE));
    }

    private SpellParam<?> valueParam(String name) {
        if (isMode(PsitweaksModeOptions.MATRIX)) {
            return new ParamMatrix(name, PsitweaksSpellParams.MATRIX_COLOR, false, false);
        }
        return PsitweaksListAdapters.createElementParam(getModeOption(), name, false);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double value = getNotNullParamValue(context, condition).doubleValue();
        return getParamValue(context, typed(isTrue(value) ? ifTrue : ifFalse));
    }

    public static boolean isTrue(double condition) {
        return Math.abs(condition) >= TRUE_THRESHOLD;
    }

    @Override
    public Class<?> getEvaluationType() {
        if (isMode(PsitweaksModeOptions.MATRIX)) {
            return MatrixValue.class;
        }
        return PsitweaksListAdapters.elementType(getModeOption()).orElse(Object.class);
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(getModeOption().elementTranslationKey());
    }
}
