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
        event.enqueueWork(() -> {
            registerAssemblyStats();
            registerCoreStats();
        });
    }

    private static void registerAssemblyStats() {
        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION.get(),
                EnumCADStat.EFFICIENCY,
                250
        );
        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION.get(),
                EnumCADStat.POTENCY,
                60
        );


        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL.get(),
                EnumCADStat.EFFICIENCY,
                110

        );
        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL.get(),
                EnumCADStat.POTENCY,
                640//450
        );

        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL.get(),
                EnumCADStat.EFFICIENCY,
                125
        );
        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL.get(),
                EnumCADStat.POTENCY,
                1050//550


        );

        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA.get(),
                EnumCADStat.EFFICIENCY,
                150
        );
        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA.get(),
                EnumCADStat.POTENCY,
                1600//750
        );

        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA.get(),
                EnumCADStat.EFFICIENCY,
                175
        );
        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA.get(),
                EnumCADStat.POTENCY,
                1400//640
        );

        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL.get(),
                EnumCADStat.EFFICIENCY,
                200
        );
        ItemCADComponent.addStatToStack(
                PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL.get(),
                EnumCADStat.POTENCY,
                2000
        );




    }

    private static void registerCoreStats() {
        ItemCADComponent.addStatToStack(PsitweaksItems.CAD_CORE_ABSORPTIVE.get(), EnumCADStat.PROJECTION, 10);
        ItemCADComponent.addStatToStack(PsitweaksItems.CAD_CORE_ABSORPTIVE.get(), EnumCADStat.COMPLEXITY, 46);
        ItemCADComponent.addStatToStack(PsitweaksItems.CAD_CORE_ULTRACLOCKED.get(), EnumCADStat.PROJECTION, 9);
        ItemCADComponent.addStatToStack(PsitweaksItems.CAD_CORE_ULTRACLOCKED.get(), EnumCADStat.COMPLEXITY, 54);
    }

    // 他の registerXXXStats は同様にここへ
}
