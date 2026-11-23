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

        private DamageTypes() {
        }
    }
}
