package com.moratan251.psitweaks;

import com.moratan251.psitweaks.client.spells.PsitweaksClientSpells;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.items.component.ComponentStats;
import com.moratan251.psitweaks.common.items.PsitweaksItemCapabilities;
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
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.neoforged.neoforge.registries.DeferredItem;
import org.slf4j.Logger;
import vazkii.psi.api.spell.ISpellAcceptor;

@Mod(Psitweaks.MOD_ID)
public class Psitweaks {
    public static final String MOD_ID = "psitweaks";
    // TODO(port): Remove this alias after Config.java is replaced with the 1.20.1 PsitweaksConfig port.
    public static final String MODID = MOD_ID;

    private static final Logger LOGGER = LogUtils.getLogger();

    public Psitweaks(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "psitweaks-common.toml");

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(PsitweaksItemCapabilities::registerCapabilities);
        // TODO(port): Re-enable Mekanism IMC after PsitweaksModules is ported.
        // modEventBus.addListener(this::enqueueIMC);

        PsitweaksItems.register(modEventBus);
        PsitweaksBlocks.register(modEventBus);
        PsitweaksChemicals.register(modEventBus);
        PsitweaksEffects.register(modEventBus);
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

        event.enqueueWork(ComponentStats::registerAssemblyStats);

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
        private static final ResourceLocation ACTIVE_PROPERTY = Psitweaks.location("active");

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("PsiTweaks client setup");

            event.enqueueWork(() -> registerActiveSpellProperties(
                    PsitweaksItems.ADVANCED_SPELL_BULLET,
                    PsitweaksItems.ADVANCED_SPELL_BULLET_LOOP,
                    PsitweaksItems.ADVANCED_SPELL_BULLET_MINE,
                    PsitweaksItems.ADVANCED_SPELL_BULLET_CHARGE,
                    PsitweaksItems.ADVANCED_SPELL_BULLET_GRENADE,
                    PsitweaksItems.ADVANCED_SPELL_BULLET_PROJECTILE,
                    PsitweaksItems.ADVANCED_SPELL_BULLET_CIRCLE,
                    PsitweaksItems.RESONANT_SPELL_BULLET,
                    PsitweaksItems.RESONANT_SPELL_BULLET_LOOP,
                    PsitweaksItems.RESONANT_SPELL_BULLET_MINE,
                    PsitweaksItems.RESONANT_SPELL_BULLET_CHARGE,
                    PsitweaksItems.RESONANT_SPELL_BULLET_GRENADE,
                    PsitweaksItems.RESONANT_SPELL_BULLET_PROJECTILE,
                    PsitweaksItems.RESONANT_SPELL_BULLET_CIRCLE,
                    PsitweaksItems.SUBLIMATED_SPELL_BULLET,
                    PsitweaksItems.SUBLIMATED_SPELL_BULLET_LOOP,
                    PsitweaksItems.SUBLIMATED_SPELL_BULLET_MINE,
                    PsitweaksItems.SUBLIMATED_SPELL_BULLET_CHARGE,
                    PsitweaksItems.SUBLIMATED_SPELL_BULLET_GRENADE,
                    PsitweaksItems.SUBLIMATED_SPELL_BULLET_PROJECTILE,
                    PsitweaksItems.SUBLIMATED_SPELL_BULLET_CIRCLE,
                    PsitweaksItems.AWAKENED_SPELL_BULLET,
                    PsitweaksItems.AWAKENED_SPELL_BULLET_LOOP,
                    PsitweaksItems.AWAKENED_SPELL_BULLET_MINE,
                    PsitweaksItems.AWAKENED_SPELL_BULLET_CHARGE,
                    PsitweaksItems.AWAKENED_SPELL_BULLET_GRENADE,
                    PsitweaksItems.AWAKENED_SPELL_BULLET_PROJECTILE,
                    PsitweaksItems.AWAKENED_SPELL_BULLET_CIRCLE,
                    PsitweaksItems.TRANSCENDENT_SPELL_BULLET,
                    PsitweaksItems.TRANSCENDENT_SPELL_BULLET_LOOP,
                    PsitweaksItems.TRANSCENDENT_SPELL_BULLET_MINE,
                    PsitweaksItems.TRANSCENDENT_SPELL_BULLET_CHARGE,
                    PsitweaksItems.TRANSCENDENT_SPELL_BULLET_GRENADE,
                    PsitweaksItems.TRANSCENDENT_SPELL_BULLET_PROJECTILE,
                    PsitweaksItems.TRANSCENDENT_SPELL_BULLET_CIRCLE
            ));
        }

        @SafeVarargs
        private static void registerActiveSpellProperties(DeferredItem<? extends Item>... items) {
            for (DeferredItem<? extends Item> item : items) {
                ItemProperties.register(item.get(), ACTIVE_PROPERTY, (stack, level, entity, seed) -> hasSpell(stack));
            }
        }

        private static float hasSpell(ItemStack stack) {
            return ISpellAcceptor.hasSpell(stack) ? 1.0F : 0.0F;
        }
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
