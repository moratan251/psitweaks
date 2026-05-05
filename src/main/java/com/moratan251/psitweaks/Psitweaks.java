package com.moratan251.psitweaks;

import com.moratan251.psitweaks.client.spells.PsitweaksClientSpells;
import com.moratan251.psitweaks.client.gui.machine.GuiMaterialMutator;
import com.moratan251.psitweaks.client.gui.machine.GuiProgramResearcher;
import com.moratan251.psitweaks.client.gui.machine.GuiPortableCADAssembler;
import com.moratan251.psitweaks.client.gui.machine.GuiPsionicGenerator;
import com.moratan251.psitweaks.client.gui.machine.GuiSculkEroder;
import com.moratan251.psitweaks.client.renderer.EntityTimeAcceleratorRenderer;
import com.moratan251.psitweaks.client.renderer.FlareCircleRenderer;
import com.moratan251.psitweaks.client.renderer.IceCircleRenderer;
import com.moratan251.psitweaks.client.renderer.MolecularDividerRenderer;
import com.moratan251.psitweaks.client.renderer.PhononMaserBeamRenderer;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributeEvents;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.items.component.ComponentStats;
import com.moratan251.psitweaks.common.items.armor.ArmorSpellDamageAttributeHandler;
import com.moratan251.psitweaks.common.items.armor.PsitweaksArmorMaterials;
import com.moratan251.psitweaks.common.items.PsitweaksItemCapabilities;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.PsitweaksTabs;
import com.moratan251.psitweaks.common.menu.ModMenuTypes;
import com.moratan251.psitweaks.common.network.PsitweaksNetwork;
import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityCapabilities;
import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismTileEntityTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeSerializers;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import com.moratan251.psitweaks.common.spells.PsitweaksSpells;
import com.moratan251.psitweaks.datagen.providers.MaterialMutationRecipeProvider;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksLootTableProvider;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksMekanismRecipeProvider;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksRecipeProvider;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksTagsProvider;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksWorldgenProvider;
import com.moratan251.psitweaks.datagen.providers.PsitweaksBlockStateProvider;
import com.moratan251.psitweaks.datagen.providers.PsitweaksItemModelProvider;
import com.moratan251.psitweaks.datagen.providers.PsitweaksLanguageProvider;
import com.moratan251.psitweaks.datagen.providers.PsitweaksSpellUnlockProvider;
import com.mojang.logging.LogUtils;
import mekanism.api.MekanismIMC;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.slf4j.Logger;
import vazkii.psi.api.spell.ISpellAcceptor;

@Mod(Psitweaks.MOD_ID)
public class Psitweaks {
    public static final String MOD_ID = "psitweaks";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Psitweaks(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, PsitweaksConfig.COMMON_SPEC, "psitweaks-common.toml");

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(PsitweaksItemCapabilities::registerCapabilities);
        modEventBus.addListener(PsitweaksBlockEntityCapabilities::registerCapabilities);
        modEventBus.addListener(PsitweaksAttributeEvents::onEntityAttributeModification);
        modEventBus.addListener(PsitweaksNetwork::registerPayloadHandlers);

        PsitweaksArmorMaterials.register(modEventBus);
        PsitweaksItems.register(modEventBus);
        PsitweaksBlocks.register(modEventBus);
        PsitweaksChemicals.register(modEventBus);
        PsitweaksEffects.register(modEventBus);
        PsitweaksAttributes.register(modEventBus);
        PsitweaksTabs.register(modEventBus);
        PsitweaksSpells.register(modEventBus);
        PsitweaksEntities.register(modEventBus);
        PsitweaksBlockEntityTypes.register(modEventBus);
        PsitweaksRecipeTypes.register(modEventBus);
        PsitweaksRecipeSerializers.register(modEventBus);
        PsitweaksModules.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PsitweaksClientSpells.register(modEventBus);
        }

        PsitweaksMekanismBlocks.register(modEventBus);
        PsitweaksMekanismTileEntityTypes.register(modEventBus);
        PsitweaksMekanismContainerTypes.register(modEventBus);
        // TODO(port): Re-enable as their 1.21.1 NeoForge implementations are ported.
        // PsitweaksVillagers.register(modEventBus);

        modEventBus.addListener(this::registerProviders);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(ArmorSpellDamageAttributeHandler::onItemAttributeModifier);

        // TODO(port): Re-enable client/server proxy handlers after proxy classes are ported.
        // proxyPsitweaks = dist.isClient() ? new ClientProxyPsitweaks() : new ServerProxyPsitweaks();
        // proxyPsitweaks.registerHandlers();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("PsiTweaks common setup");

        event.enqueueWork(ComponentStats::registerAssemblyStats);
        event.enqueueWork(PsitweaksItems::registerCurioItems);
        event.enqueueWork(this::enqueueIMC);

        // TODO(port): Re-enable after NetworkHandler is ported.
        // NetworkHandler.registerMessages();
    }

    private void enqueueIMC() {
        MekanismIMC.addMekaSuitBodyarmorModules(
                PsitweaksModules.PSYON_SUPPLYING_UNIT,
                PsitweaksModules.PSYON_CAPACITY_UNIT
        );
        MekanismIMC.addMekaSuitHelmetModules(PsitweaksModules.PHENOMENON_INTERFERENCE_ENHANCEMENT_UNIT);
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
        generator.addProvider(event.includeServer(), new MaterialMutationRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), new PsitweaksSpellUnlockProvider(packOutput));
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

            event.enqueueWork(() -> {
                registerPsimetalBowProperties();
                registerActiveSpellProperties(
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
                );
            });
        }

        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.PORTABLE_CAD_ASSEMBLER.get(), GuiPortableCADAssembler::new);
            event.register(PsitweaksMekanismContainerTypes.SCULK_ERODER.get(), GuiSculkEroder::new);
            event.register(PsitweaksMekanismContainerTypes.MATERIAL_MUTATOR.get(), GuiMaterialMutator::new);
            event.register(PsitweaksMekanismContainerTypes.PSIONIC_GENERATOR.get(), GuiPsionicGenerator::new);
            event.register(ModMenuTypes.PROGRAM_RESEARCHER.get(), GuiProgramResearcher::new);
        }

        @SubscribeEvent
        public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(PsitweaksEntities.PHONON_MASER_BEAM.get(), PhononMaserBeamRenderer::new);
            event.registerEntityRenderer(PsitweaksEntities.MOLECULAR_DIVIDER.get(), MolecularDividerRenderer::new);
            event.registerEntityRenderer(PsitweaksEntities.TIME_ACCELERATOR.get(), EntityTimeAcceleratorRenderer::new);
            event.registerEntityRenderer(PsitweaksEntities.BLAZE_BALL.get(), context -> new ThrownItemRenderer<>(context, 0.75F, true));
            event.registerEntityRenderer(PsitweaksEntities.FLARE_CIRCLE.get(), FlareCircleRenderer::new);
            event.registerEntityRenderer(PsitweaksEntities.ICE_CIRCLE.get(), IceCircleRenderer::new);
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

        private static void registerPsimetalBowProperties() {
            ItemProperties.register(PsitweaksItems.PSIMETAL_BOW.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem()
                            && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(PsitweaksItems.PSIMETAL_BOW.get(), ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null || entity.getUseItem() != stack) {
                            return 0.0F;
                        }
                        return (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
                    });
        }
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
