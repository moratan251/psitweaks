package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;

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

    public static final class EntityTypes {
        /** Creatures with little liquid inside; Trick: Burst deals reduced damage to them. */
        public static final TagKey<EntityType<?>> BURST_RESISTANT =
                TagKey.create(Registries.ENTITY_TYPE, Psitweaks.location("burst_resistant"));

        private EntityTypes() {
        }
    }
}
