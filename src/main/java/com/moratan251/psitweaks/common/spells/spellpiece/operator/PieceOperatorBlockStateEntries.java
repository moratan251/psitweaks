package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamBlockValue;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.Comparator;
import java.util.Map.Entry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.Property;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorBlockStateEntries extends PieceOperator {
    private SpellParam<?> target;

    public PieceOperatorBlockStateEntries(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamBlockValue(SpellParam.GENERIC_NAME_TARGET, PsitweaksSpellParams.BLOCK_COLOR,
                false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockValue block = BlockValueHelper.getBlockValue(this, context, target);
        return StringListWrapper.make(block.state().getValues().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                .map(PieceOperatorBlockStateEntries::formatEntry)
                .toList());
    }

    @Override
    public Class<?> getEvaluationType() {
        return StringListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string_list");
    }

    private static String formatEntry(Entry<Property<?>, Comparable<?>> entry) {
        Property<?> property = entry.getKey();
        return property.getName() + ":" + getName(property, entry.getValue());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }
}
