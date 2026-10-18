package com.moratan251.psitweaks.common.spells.param;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/** A target that is one Entity or an Entity List, read untyped; {@link #targets} turns either into a list. */
public class ParamEntityOrEntityList extends SpellParam<Object> {
    public ParamEntityOrEntityList(String name, int color, boolean canDisable) {
        super(name, color, canDisable);
    }

    @Override
    public Class<Object> getRequiredType() {
        return Object.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.entity_or_entity_list");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        Class<?> type = piece.getEvaluationType();
        return type != null && (Entity.class.isAssignableFrom(type) || EntityListWrapper.class.isAssignableFrom(type));
    }

    /**
     * The entities of {@code value}, each checked like Psi checks a single target: it must exist, must not be immune
     * to spells and must be within the spell radius.
     */
    public static List<Entity> targets(SpellContext context, Object value) throws SpellRuntimeException {
        List<Entity> entities;
        if (value instanceof EntityListWrapper list) {
            entities = new ArrayList<>(list.size());
            list.forEach(entities::add);
        } else if (value instanceof Entity entity) {
            entities = List.of(entity);
        } else {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        for (Entity entity : entities) {
            context.verifyEntity(entity);
            if (!context.isInRadius(entity)) {
                throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
            }
        }
        return entities;
    }
}
