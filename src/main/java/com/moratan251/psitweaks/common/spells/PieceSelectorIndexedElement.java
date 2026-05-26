package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.operator.PieceOperatorModeListBase;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;

public class PieceSelectorIndexedElement extends PieceOperatorModeListBase {
    private SpellParam<?> list;
    private SpellParam<Number> number;

    public PieceSelectorIndexedElement(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1.0D));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int index = getNotNullParamValue(context, number).intValue();
        Object source = getNotNullParamValue(context, typed(list));
        int size = currentAdapter().size(source);
        int normalizedIndex = normalizeIndex(index, size);
        if (normalizedIndex < 0 || normalizedIndex >= size) {
            throw new SpellRuntimeException("psi.spellerror.out_of_bounds");
        }

        return currentAdapter().get(source, normalizedIndex);
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentAdapter().elementType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().elementTranslationKey());
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = currentAdapter().createListParam(SpellParam.GENERIC_NAME_LIST, false));
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.RED, false, false));
        restoreParamSides(savedSides);
    }

    private static int normalizeIndex(int index, int size) {
        return index < 0 ? size + index : index;
    }
}
