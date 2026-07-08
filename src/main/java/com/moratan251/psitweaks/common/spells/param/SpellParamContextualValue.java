package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.api.value.ContextualValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

public class SpellParamContextualValue extends SpellParam<Object> {
    public SpellParamContextualValue(String name, int color, boolean canDisable) {
        super(name, color, canDisable);
    }

    @Override
    protected Class<Object> getRequiredType() {
        return Object.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.contextual_value");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        Class<?> type = piece.getEvaluationType();
        return type != null && (ContextualValue.class.isAssignableFrom(type)
                || Entity.class.isAssignableFrom(type)
                || Vector3.class.isAssignableFrom(type));
    }
}
