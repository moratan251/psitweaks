package com.moratan251.psitweaks.common.attributes;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PsitweaksAttributeEvents {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, PsitweaksAttributes.SPELL_DAMAGE_FACTOR.get());
        event.add(EntityType.PLAYER, PsitweaksAttributes.MAX_PSI_BONUS.get());
        event.add(EntityType.PLAYER, PsitweaksAttributes.PSI_REGEN_BONUS.get());
    }
}
