package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import vazkii.psi.api.exosuit.PsiArmorEvent;

public class PsitweaksArmorEvent extends PsiArmorEvent {
    public static final String SECOND = "psitweaks.event.second";

    public PsitweaksArmorEvent(Player player, String type){
        super(player, type);
    }



}
