package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Turns mobs that go for a Trick: Parade caster onto the image, and lets attacks on the caster miss by chance. */
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

    // Normal priority: before Trick: Barrier's shield (LOW), which should only pay for hits that land.
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide && Parades.evades(target, event.getSource(), target.getRandom().nextDouble())) {
            event.setCanceled(true);
        }
    }

    /** Mod bus, registered in {@code Psitweaks}: the image is a living entity and needs its attributes. */
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(PsitweaksEntities.PARADE_DECOY.get(), EntityParadeDecoy.createAttributes().build());
    }
}
