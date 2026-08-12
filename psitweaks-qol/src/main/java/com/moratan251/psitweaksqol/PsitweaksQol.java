package com.moratan251.psitweaksqol;

import com.moratan251.psitweaksqol.client.config.PsitweaksQolConfig;
import com.moratan251.psitweaksqol.datagen.providers.PsitweaksQolLanguageProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(PsitweaksQol.MOD_ID)
public class PsitweaksQol {
    public static final String MOD_ID = "psitweaks_qol";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public PsitweaksQol(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                PsitweaksQolConfig.CLIENT_SPEC,
                "psitweaks_qol-client.toml"
        );

        modEventBus.addListener(this::registerProviders);
    }

    private void registerProviders(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();

        gen.addProvider(event.includeClient(), new PsitweaksQolLanguageProvider(packOutput, "en_us"));
        gen.addProvider(event.includeClient(), new PsitweaksQolLanguageProvider(packOutput, "ja_jp"));
    }
}
