package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import vazkii.psi.api.exosuit.IPsiEventArmor;
import vazkii.psi.api.exosuit.PsiArmorEvent;
import vazkii.psi.common.core.handler.PlayerDataHandler;

@Mod.EventBusSubscriber(modid = "psitweaks")
public class PlayerPsiEventHandler extends PlayerDataHandler {

    //private static int tickCount = 0;

    public PlayerPsiEventHandler(){
        super();

    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player && !player.isSpectator()) {
            if (!player.level().isClientSide) {
                long time = player.level().getGameTime();
                if (time % 18 == 0) {
                    PsiArmorEvent.post(new PsiArmorEvent(player, "psitweaks.event.second"));

                }
                PsiArmorEvent.post(new PsiArmorEvent(player, "psitweaks.event.custom_tick"));
            }
        }
    }

    @SubscribeEvent
    public static void onPsiCurioEvent(PsiArmorEvent event) {
        if (!event.getEntity().isSpectator() && ModList.get().isLoaded("curios")) {

            for(int i = 0; i < 4; ++i) {
                ItemStack curio = CuriosSlotChecker.getItemFromSelectedMagicSlot(event.getEntity(), i);
                if(curio.getItem() instanceof IPsiEventArmor handler){
                    handler.onEvent(curio, event);
                }

            }
        }
    }


}

