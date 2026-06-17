package com.moratan251.psitweaks.api.value;

import java.util.Optional;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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

    default Set<ResourceLocation> getTagIds(SpellContext context) throws SpellRuntimeException {
        return Set.of();
    }
}
