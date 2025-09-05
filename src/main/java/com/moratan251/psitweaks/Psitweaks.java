package com.moratan251.psitweaks;

import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import com.moratan251.psitweaks.common.handler.NetworkHandler;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.items.PsitweaksTabs;
import com.moratan251.psitweaks.common.items.component.ComponentStats;
import com.moratan251.psitweaks.datagen.providers.PsiTweaksRecipeProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.ISpellAcceptor;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Psitweaks.MOD_ID)
public class Psitweaks {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "psitweaks";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace

    public Psitweaks(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        PsitweaksItems.register(modEventBus);
        PsitweaksChemicals.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        PsitweaksTabs.register(modEventBus);

        PsitweaksEffects.register(modEventBus);

        modEventBus.addListener(this::registerProviders);

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
        }
    }


    private void registerProviders(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new PsiTweaksRecipeProvider(gen.getPackOutput()));
    }
}
