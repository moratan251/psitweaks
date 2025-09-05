package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.psi.api.internal.IPlayerData;
import vazkii.psi.common.core.handler.PlayerDataHandler;


@Mod.EventBusSubscriber(modid = "psitweaks")
public class MovalSuitEvent {

 //   private static final String MOVAL_SUIT_FLIGHT = "psitweaks_moval_suit_flight";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        PlayerDataHandler.PlayerData pPlayer = PlayerDataHandler.get(player);

        if(pPlayer.getAvailablePsi() < pPlayer.getTotalPsi()){
            if (player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ItemMovalSuitLeggings) {
                pPlayer.deductPsi(-5,0,true,false);
            }
            if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ItemMovalSuitChestplate) {
                pPlayer.deductPsi(-5,0,true,false);
            }
            if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemMovalSuitHelmet) {
                pPlayer.deductPsi(-5,0,true,false);
            }
            if (player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ItemMovalSuitBoots) {
                pPlayer.deductPsi(-5,0,true,false);
            }
        }

    }

}