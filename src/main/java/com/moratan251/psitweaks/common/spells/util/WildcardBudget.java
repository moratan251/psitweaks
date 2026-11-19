package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * How many character comparisons the pattern matching of one cast may still make. Operator: Partial Match and the
 * List Keep/Remove Matches, Name Matches and NBT Matches pieces share one budget per spell context, kept in its custom
 * data, so the matching work of a cast stays bounded however many of those pieces it runs. # tag patterns are not
 * counted: they compare short tag IDs and keep the answer for each one. Running out is a spell error. Not thread-safe:
 * use a budget within one cast.
 */
public final class WildcardBudget {
    public static final String ERROR_TOO_MANY_COMPARISONS = "psitweaks.spellerror.wildcard_too_many_comparisons";
    private static final String CACHE_KEY = Psitweaks.MOD_ID + ":wildcard_budget";

    private long remaining;

    private WildcardBudget(long comparisons) {
        this.remaining = comparisons;
    }

    /** The budget of the cast, starting from the configured number of comparisons the first time a piece asks. */
    public static WildcardBudget forCast(SpellContext context) {
        if (context.customData.get(CACHE_KEY) instanceof WildcardBudget existing) {
            return existing;
        }
        WildcardBudget created = new WildcardBudget(PsitweaksConfig.COMMON.wildcardMaxComparisonsPerCast.get());
        context.customData.put(CACHE_KEY, created);
        return created;
    }

    /** A budget of its own with the given number of comparisons, not tied to any cast. */
    public static WildcardBudget of(long comparisons) {
        return new WildcardBudget(comparisons);
    }

    /** The comparisons left; negative once a match has gone past the budget. */
    public long remaining() {
        return remaining;
    }

    /** Takes comparisons from the budget; false once the budget has gone below zero. */
    boolean spend(long comparisons) {
        remaining -= comparisons;
        return remaining >= 0;
    }

    void throwIfSpent() throws SpellRuntimeException {
        if (remaining < 0) {
            throw new SpellRuntimeException(ERROR_TOO_MANY_COMPARISONS);
        }
    }
}
