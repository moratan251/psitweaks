package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.items.armor.ItemMovalSuitLeggings;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "psitweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlightHandler {

    @SubscribeEvent
    public static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        Player player = event.getEntity();
        GameType oldGameMode = event.getCurrentGameMode();
        GameType newGameMode = event.getNewGameMode();
        if((oldGameMode == GameType.CREATIVE || oldGameMode == GameType.SPECTATOR) && (newGameMode == GameType.ADVENTURE  || newGameMode == GameType.SURVIVAL) ){
            if(player.hasEffect(PsitweaksEffects.FLIGHT.get())) {
                player.getAbilities().flying = true;

            }
        }



    }
}