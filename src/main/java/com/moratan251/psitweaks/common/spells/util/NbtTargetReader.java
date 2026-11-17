package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.ContextualValue;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Reads the NBT of the targets of the NBT pieces: an Entity (its saved data without the id, plus SelectedItem for a
 * player, as vanilla NBT predicates see it), or a Contextual Value such as an Item (the saved stack) or a Block (its
 * block entity). The NBT of each target is saved once per spell context and reused by every NBT piece and list
 * element of that cast; the cache lives in the context's custom data and goes away with it. Callers must not modify
 * the returned tag.
 */
public final class NbtTargetReader {
    private static final String CACHE_KEY = Psitweaks.MOD_ID + ":nbt_snapshots";

    private NbtTargetReader() {
    }

    /** The target's NBT; empty for other values and for targets without NBT, such as a block without a block entity. */
    public static Optional<CompoundTag> read(SpellContext context, Object target) throws SpellRuntimeException {
        if (target instanceof Entity entity) {
            ReadOnlyTargets.verify(context, entity);
        } else if (!(target instanceof ContextualValue)) {
            return Optional.empty();
        }

        Map<Object, Optional<CompoundTag>> cache = cache(context);
        Optional<CompoundTag> cached = cache.get(target);
        if (cached != null) {
            return cached;
        }
        Optional<CompoundTag> read = target instanceof Entity entity
                ? Optional.of(NbtPredicate.getEntityTagToCompare(entity))
                : ((ContextualValue) target).getNbt(context);
        cache.put(target, read);
        return read;
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Optional<CompoundTag>> cache(SpellContext context) {
        if (context.customData.get(CACHE_KEY) instanceof IdentityHashMap<?, ?> existing) {
            return (Map<Object, Optional<CompoundTag>>) existing;
        }
        Map<Object, Optional<CompoundTag>> created = new IdentityHashMap<>();
        context.customData.put(CACHE_KEY, created);
        return created;
    }
}
