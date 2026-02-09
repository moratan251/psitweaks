package com.moratan251.psitweaks;

import com.moratan251.psitweaks.client.proxy.ClientProxyPsitweaks;
import com.moratan251.psitweaks.client.renderer.EmptyRenderer;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.chemicals.PsitweaksGases;
import com.moratan251.psitweaks.common.chemicals.PsitweaksInfuseTypes;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.handler.NetworkHandler;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismTileEntityTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeSerializers;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.items.PsitweaksTabs;
//import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import com.moratan251.psitweaks.common.proxy.IProxyPsitweaks;
import com.moratan251.psitweaks.common.proxy.ServerProxyPsitweaks;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksRecipeProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.ISpellAcceptor;

import static net.minecraftforge.fml.loading.FMLEnvironment.dist;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Psitweaks.MOD_ID)
public class Psitweaks {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "psitweaks";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static IProxyPsitweaks proxyPsitweaks;
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace

    @SuppressWarnings("removal")
    public Psitweaks(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PsitweaksConfig.COMMON_SPEC, "psitweaks-common.toml");

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        PsitweaksItems.register(modEventBus);
        PsitweaksBlocks.register(modEventBus);
        PsitweaksMekanismBlocks.register(modEventBus);
        PsitweaksMekanismTileEntityTypes.register(modEventBus);
        PsitweaksMekanismContainerTypes.register(modEventBus);
        PsitweaksRecipeTypes.register(modEventBus);
        PsitweaksRecipeSerializers.register(modEventBus);
        PsitweaksInfuseTypes.register(modEventBus);
        PsitweaksGases.register(modEventBus);
        PsitweaksModules.MODULES.register(modEventBus);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        PsitweaksTabs.register(modEventBus);

        PsitweaksEffects.register(modEventBus);

        modEventBus.addListener(this::registerProviders);

        PsitweaksEntities.register(modEventBus);

        proxyPsitweaks = (IProxyPsitweaks) (dist.isClient() ? new ClientProxyPsitweaks() : new ServerProxyPsitweaks());
        proxyPsitweaks.registerHandlers();

        //ComponentStats.onCommonSetup(modEventBus);


    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        NetworkHandler.registerMessages();

    }



    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = "psitweaks", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // FLASH_RING は RegistryObject<Item>

            ItemProperties.register(PsitweaksItems.FLASH_RING.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.ADVANCED_SPELL_BULLET.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.ADVANCED_SPELL_BULLET_PROJECTILE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.ADVANCED_SPELL_BULLET_CHARGE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.ADVANCED_SPELL_BULLET_MINE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.ADVANCED_SPELL_BULLET_GRENADE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.ADVANCED_SPELL_BULLET_LOOP.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.ADVANCED_SPELL_BULLET_CIRCLE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.RESONANT_SPELL_BULLET.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.RESONANT_SPELL_BULLET_PROJECTILE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.RESONANT_SPELL_BULLET_CHARGE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.RESONANT_SPELL_BULLET_MINE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.RESONANT_SPELL_BULLET_GRENADE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.RESONANT_SPELL_BULLET_LOOP.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.RESONANT_SPELL_BULLET_CIRCLE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.SUBLIMATED_SPELL_BULLET.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.SUBLIMATED_SPELL_BULLET_PROJECTILE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.SUBLIMATED_SPELL_BULLET_CHARGE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.SUBLIMATED_SPELL_BULLET_MINE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.SUBLIMATED_SPELL_BULLET_GRENADE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.SUBLIMATED_SPELL_BULLET_LOOP.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.SUBLIMATED_SPELL_BULLET_CIRCLE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.AWAKENED_SPELL_BULLET.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.AWAKENED_SPELL_BULLET_PROJECTILE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.AWAKENED_SPELL_BULLET_CHARGE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.AWAKENED_SPELL_BULLET_MINE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.AWAKENED_SPELL_BULLET_GRENADE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.AWAKENED_SPELL_BULLET_LOOP.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

            ItemProperties.register(PsitweaksItems.AWAKENED_SPELL_BULLET_CIRCLE.get(),
                    ResourceLocation.fromNamespaceAndPath("psitweaks", "active"),
                    (stack, world, entity, seed) ->
                            stack.getCapability(PsiAPI.SPELL_ACCEPTOR_CAPABILITY)
                                    .map(ISpellAcceptor::containsSpell)
                                    .orElse(false) ? 1f : 0f);

        }


    }


    private void registerProviders(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new PsiTweaksRecipeProvider(gen.getPackOutput()));
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
