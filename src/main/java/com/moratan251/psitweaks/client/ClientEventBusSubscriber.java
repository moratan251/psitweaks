package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "psitweaks", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventBusSubscriber {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 引き絞り進行度 (0.0 ~ 1.0)
            ItemProperties.register(PsitweaksItems.PSIMETAL_BOW.get(),
                    ResourceLocation.parse("pull"),
                    (stack, world, entity, seed) -> {
                        if (entity == null) return 0.0F;
                        if (entity.getUseItem() != stack) return 0.0F;
                        return (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F;
                    });

            // 弓を構えているかどうか
            ItemProperties.register(PsitweaksItems.PSIMETAL_BOW.get(),
                    ResourceLocation.parse("pulling"),
                    (stack, world, entity, seed) ->
                            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
            );
        });
    }
}