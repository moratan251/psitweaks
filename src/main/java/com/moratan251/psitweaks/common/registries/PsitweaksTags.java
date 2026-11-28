package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class PsitweaksTags {
    private PsitweaksTags() {
    }

    public static final class DamageTypes {
        /** Damage that Trick: Barrier never absorbs (void, /kill, starvation, drowning and similar). */
        public static final TagKey<DamageType> BYPASSES_BARRIER =
                TagKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("bypasses_barrier"));
        /**
         * Damage that bypasses armor but that Trick: Hardening still caps (falling, hitting a wall while gliding). Hardening
         * caps damage that armor reduces and the damage in this tag.
         */
        public static final TagKey<DamageType> HARDENING_REDUCES =
                TagKey.create(Registries.DAMAGE_TYPE, Psitweaks.location("hardening_reduces"));

        private DamageTypes() {
        }
    }
}
