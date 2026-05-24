package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.operator.PieceOperatorModeListBase;
import com.moratan251.psitweaks.common.spells.util.ModeListOperations;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

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
        if (index < 0 || index >= size(source)) {
            throw new SpellRuntimeException("psi.spellerror.out_of_bounds");
        }

        return elementAt(source, index);
    }

    @Override
    public Class<?> getEvaluationType() {
        return switch (currentMode()) {
            case STRING -> String.class;
            case NUMBER -> Double.class;
            case VECTOR -> Vector3.class;
            case ENTITY -> Entity.class;
            case ITEM -> SpellItemValue.class;
        };
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().elementTranslationKey());
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = ModeListOperations.createListParam(currentMode(), SpellParam.GENERIC_NAME_LIST, false));
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.RED, false, false));
        restoreParamSides(savedSides);
    }

    private int size(Object source) {
        return switch (currentMode()) {
            case STRING -> ((StringListWrapper) source).size();
            case NUMBER -> ((NumberListWrapper) source).size();
            case VECTOR -> ((VectorListWrapper) source).size();
            case ENTITY -> ((EntityListWrapper) source).size();
            case ITEM -> ((SpellItemListWrapper) source).size();
        };
    }

    private Object elementAt(Object source, int index) {
        return switch (currentMode()) {
            case STRING -> ((StringListWrapper) source).get(index);
            case NUMBER -> ((NumberListWrapper) source).get(index);
            case VECTOR -> ((VectorListWrapper) source).get(index);
            case ENTITY -> ((EntityListWrapper) source).get(index);
            case ITEM -> ((SpellItemListWrapper) source).get(index);
        };
    }
}
