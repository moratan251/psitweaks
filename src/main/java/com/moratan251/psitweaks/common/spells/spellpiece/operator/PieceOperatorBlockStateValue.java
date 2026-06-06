package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamBlockValue;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.util.Map.Entry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.Property;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorBlockStateValue extends PieceOperator {
    private SpellParam<?> target;
    private SpellParam<String> propertyName;

    public PieceOperatorBlockStateValue(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamBlockValue(SpellParam.GENERIC_NAME_TARGET, PsitweaksSpellParams.BLOCK_COLOR,
                false, false));
        addParam(propertyName = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR,
                false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockValue block = BlockValueHelper.getBlockValue(this, context, target);
        String requestedName = StringSpellHelper.sanitize(getNotNullParamValue(context, propertyName)).trim();
        if (requestedName.isEmpty()) {
            return "";
        }

        return block.state().getValues().entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals(requestedName))
                .findFirst()
                .map(PieceOperatorBlockStateValue::formatValue)
                .orElse("");
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }

    private static String formatValue(Entry<Property<?>, Comparable<?>> entry) {
        return getName(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }
}
