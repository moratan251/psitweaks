package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

/** Turns mobs that go for a Trick: Parade caster (or the placer of an autonomous caster that cast it) onto the lookalike. */
@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class ParadeHandler {
    private ParadeHandler() {
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        EntityParadeDecoy decoy = Parades.lureTarget(event.getEntity(), event.getNewAboutToBeSetTarget());
        if (decoy != null) {
            event.setNewAboutToBeSetTarget(decoy);
        }
    }

    /** Mod bus, registered in {@code Psitweaks}: the lookalike is a living entity and needs its attributes. */
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(PsitweaksEntities.PARADE_DECOY.get(), EntityParadeDecoy.createAttributes().build());
    }
}
