package com.moratan251.psitweaks.common.items.component;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.common.item.component.ItemCADComponent;

@EventBusSubscriber(modid = "psitweaks", bus = EventBusSubscriber.Bus.MOD)
public class ComponentStats {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // DeferredRegister の登録処理が終わってから実行する
        // registerCoreStats();
        // registerSocketStats();
        // registerBatteryStats();
        event.enqueueWork(ComponentStats::registerAssemblyStats);
    }

    private static void registerAssemblyStats() {
        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION.get(),
                EnumCADStat.EFFICIENCY,
                65
        );
        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION.get(),
                EnumCADStat.POTENCY,
                400
        );
    }

    // 他の registerXXXStats は同様にここへ
}