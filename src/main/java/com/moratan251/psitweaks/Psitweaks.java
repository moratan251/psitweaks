package com.moratan251.psitweaks;

import com.mojang.logging.LogUtils;
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
import net.neoforged.neoforge.common.NeoForge;
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

        // TODO(port): Re-enable registries as their 1.21.1 NeoForge implementations are ported.
        // PsitweaksItems.register(modEventBus);
        // PsitweaksBlocks.register(modEventBus);
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
        // PsitweaksTabs.register(modEventBus);
        // PsitweaksEffects.register(modEventBus);
        // PsitweaksAttributes.register(modEventBus);
        // PsitweaksEntities.register(modEventBus);

        // TODO(port): Re-enable data providers after datagen classes are ported.
        // modEventBus.addListener(this::registerProviders);

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
