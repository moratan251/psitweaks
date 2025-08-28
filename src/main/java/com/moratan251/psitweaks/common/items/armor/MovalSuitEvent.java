package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.psi.common.core.handler.PlayerDataHandler;


@Mod.EventBusSubscriber(modid = "psitweaks")
public class MovalSuitEvent {

    private static final String MOVAL_SUIT_FLIGHT = "psitweaks_moval_suit_flight";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        if (player.level().isClientSide) return;

       // boolean hasLeggings = player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.MOVAL_SUIT_LEGGINGS.get();

        var abilities = player.getAbilities();
        var persistentData = player.getPersistentData();

        if (player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ItemMovalSuitLeggings) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
             //   abilities.flying = true; // すぐ飛べるように
               // abilities.setFlyingSpeed(0.05F); // デフォルト
                player.onUpdateAbilities();
            }
            persistentData.putBoolean(MOVAL_SUIT_FLIGHT, true);
        } else {
            // 以前自作modで飛行を付与していたかどうか確認
            if (persistentData.getBoolean(MOVAL_SUIT_FLIGHT)) {
                persistentData.remove(MOVAL_SUIT_FLIGHT);

                // クリエイティブや他modのmayflyが無ければ解除
                if (!player.isCreative() && !player.isSpectator()) {
                    abilities.mayfly = false;
                    abilities.flying = false;
                    player.onUpdateAbilities();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEquip(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack newStack = event.getTo();
        ItemStack oldStack = event.getFrom();

        if (event.getSlot() == EquipmentSlot.FEET) {

            if (newStack.getItem() instanceof ItemMovalSuitBoots) {
                PlayerDataHandler.get(player).regen += 8;

            }

            if (oldStack.getItem() instanceof ItemMovalSuitBoots) {
                PlayerDataHandler.get(player).regen -= 8;
            }
        }

        if (event.getSlot() == EquipmentSlot.HEAD) {

            if (newStack.getItem() instanceof ItemMovalSuitHelmet) {
                PlayerDataHandler.get(player).regen += 8;
            }

            if (oldStack.getItem() instanceof ItemMovalSuitHelmet) {
                PlayerDataHandler.get(player).regen -= 8;
            }
        }

        if (event.getSlot() == EquipmentSlot.CHEST) {

            if (newStack.getItem() instanceof ItemMovalSuitChestplate) {
                PlayerDataHandler.get(player).regen += 8;
            }

            if (oldStack.getItem() instanceof ItemMovalSuitChestplate) {
                PlayerDataHandler.get(player).regen -= 8;
            }
        }




    }


}