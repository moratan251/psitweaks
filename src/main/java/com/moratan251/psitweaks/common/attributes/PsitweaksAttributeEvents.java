package com.moratan251.psitweaks.common.attributes;

import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

public final class PsitweaksAttributeEvents {
    private PsitweaksAttributeEvents() {
    }

    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, PsitweaksAttributes.SPELL_DAMAGE_FACTOR);
    }
}
