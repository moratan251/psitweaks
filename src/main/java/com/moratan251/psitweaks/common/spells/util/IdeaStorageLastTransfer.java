package com.moratan251.psitweaks.common.spells.util;

import vazkii.psi.api.spell.SpellContext;

/**
 * Amount moved by the most recent ideaspace transfer trick of the current cast, in the storage's own units
 * (items: count, fluids/chemicals: mB, FE: FE). Kept in {@link SpellContext#customData}, so it lives exactly
 * as long as the cast and is never shared between casts or players.
 */
public final class IdeaStorageLastTransfer {
    public static final String CONTEXT_KEY = "psitweaks:idea_storage_last_transfer";

    private IdeaStorageLastTransfer() {
    }

    /** Called with 0 when a transfer trick starts, then with the actual amount once it finishes. */
    public static void record(SpellContext context, long amount) {
        if (context != null) {
            context.customData.put(CONTEXT_KEY, Math.max(0L, amount));
        }
    }

    public static long get(SpellContext context) {
        return context != null && context.customData.get(CONTEXT_KEY) instanceof Long amount ? amount : 0L;
    }
}
