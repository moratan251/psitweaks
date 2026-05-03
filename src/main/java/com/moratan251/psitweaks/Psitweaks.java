package com.moratan251.psitweaks;

import com.moratan251.psitweaks.client.spells.PsitweaksClientSpells;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.PsitweaksTabs;
import com.moratan251.psitweaks.common.spells.PsitweaksSpells;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksLootTableProvider;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksMekanismRecipeProvider;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksRecipeProvider;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksTagsProvider;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksWorldgenProvider;
import com.moratan251.psitweaks.datagen.providers.PsitweaksBlockStateProvider;
import com.moratan251.psitweaks.datagen.providers.PsitweaksItemModelProvider;
import com.moratan251.psitweaks.datagen.providers.PsitweaksLanguageProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(Psitweaks.MOD_ID)
public class Psitweaks {
    public static final String MOD_ID = "psitweaks";
    // TODO(port): Remove this alias after Config.java is replaced with the 1.20.1 PsitweaksConfig port.
    public static final String MODID = MOD_ID;

    private static final Logger LOGGER = LogUtils.getLogger();

    public Psitweaks(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "psitweaks-common.toml");

        modEventBus.addListener(this::commonSetup);
        // TODO(port): Re-enable Mekanism IMC after PsitweaksModules is ported.
        // modEventBus.addListener(this::enqueueIMC);

        PsitweaksItems.register(modEventBus);
        PsitweaksBlocks.register(modEventBus);
        PsitweaksChemicals.register(modEventBus);
        PsitweaksTabs.register(modEventBus);
        PsitweaksSpells.register(modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PsitweaksClientSpells.register(modEventBus);
        }

        // TODO(port): Re-enable registries as their 1.21.1 NeoForge implementations are ported.
        // PsitweaksBlockEntityTypes.register(modEventBus);
        // PsitweaksMekanismBlocks.register(modEventBus);
        // PsitweaksMekanismTileEntityTypes.register(modEventBus);
        // PsitweaksMekanismContainerTypes.register(modEventBus);
        // ModMenuTypes.MENUS.register(modEventBus);
        // PsitweaksRecipeTypes.register(modEventBus);
        // PsitweaksRecipeSerializers.register(modEventBus);
        // PsitweaksVillagers.register(modEventBus);
        // PsitweaksInfuseTypes.register(modEventBus);
        // PsitweaksGases.register(modEventBus);
        // PsitweaksSlurries.register(modEventBus);
        // registerTConstructCompat(modEventBus);
        // PsitweaksModules.MODULES.register(modEventBus);
        // PsitweaksEffects.register(modEventBus);
        // PsitweaksAttributes.register(modEventBus);
        // PsitweaksEntities.register(modEventBus);

        modEventBus.addListener(this::registerProviders);

        NeoForge.EVENT_BUS.register(this);

        // TODO(port): Re-enable client/server proxy handlers after proxy classes are ported.
        // proxyPsitweaks = dist.isClient() ? new ClientProxyPsitweaks() : new ServerProxyPsitweaks();
        // proxyPsitweaks.registerHandlers();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("PsiTweaks common setup");

        // TODO(port): Re-enable after NetworkHandler is ported.
        // NetworkHandler.registerMessages();
    }

    private void registerProviders(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new PsitweaksBlockStateProvider(packOutput));
        generator.addProvider(event.includeClient(), new PsitweaksItemModelProvider(packOutput));
        generator.addProvider(event.includeClient(), new PsitweaksLanguageProvider(packOutput, "en_us"));
        generator.addProvider(event.includeClient(), new PsitweaksLanguageProvider(packOutput, "ja_jp"));
        generator.addProvider(event.includeServer(), new PsiTweaksLootTableProvider(packOutput));
        generator.addProvider(event.includeServer(), new PsiTweaksTagsProvider(packOutput));
        generator.addProvider(event.includeServer(), new PsiTweaksRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), new PsiTweaksMekanismRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), new PsiTweaksWorldgenProvider(packOutput));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.debug("PsiTweaks server starting");
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("PsiTweaks client setup");

            // TODO(port): Re-enable active spell item properties after PsitweaksItems and Psi spell capability are ported.
            // registerActiveSpellProperties(...);
        }
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
