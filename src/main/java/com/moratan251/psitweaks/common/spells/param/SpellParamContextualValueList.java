package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import com.moratan251.psitweaks.api.PsitweaksValueKind;
import com.moratan251.psitweaks.api.value.ContextualValue;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

public class SpellParamContextualValueList extends SpellParam<Object> {
    public SpellParamContextualValueList(String name, int color, boolean canDisable) {
        super(name, color, canDisable);
    }

    @Override
    protected Class<Object> getRequiredType() {
        return Object.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.contextual_value_list");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        Class<?> type = piece.getEvaluationType();
        return type != null && canAcceptListType(type);
    }

    public static boolean canAcceptListType(Class<?> type) {
        Optional<PsitweaksListAdapter<Object>> adapter;
        try {
            adapter = PsitweaksListAdapters.findAdapter(type);
        } catch (IllegalStateException exception) {
            return false;
        }
        return adapter.filter(SpellParamContextualValueList::isContextualListAdapter).isPresent();
    }

    private static boolean isContextualListAdapter(PsitweaksListAdapter<Object> adapter) {
        PsitweaksModeOption modeOption = adapter.modeOption();
        if (modeOption != null && modeOption.valueKind() == PsitweaksValueKind.CONTEXTUAL) {
            return true;
        }

        Class<?> elementType = adapter.elementType();
        return elementType != null && (ContextualValue.class.isAssignableFrom(elementType)
                || Entity.class.isAssignableFrom(elementType)
                || Vector3.class.isAssignableFrom(elementType));
    }
}
