package com.moratan251.psitweaks.api.value;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Marker for spell values that carry world or game-context data beyond a plain
 * scalar representation.
 */
public interface ContextualValue {
    default Optional<CompoundTag> getNbt(SpellContext context) throws SpellRuntimeException {
        return Optional.empty();
    }
}
