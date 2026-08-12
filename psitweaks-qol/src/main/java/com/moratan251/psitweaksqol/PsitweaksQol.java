package com.moratan251.psitweaksqol;

import com.moratan251.psitweaksqol.client.config.PsitweaksQolConfig;
import com.moratan251.psitweaksqol.client.event.PsitweaksQolClientGuiEvents;
import com.moratan251.psitweaksqol.datagen.providers.PsitweaksQolLanguageProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;

@Mod(value = PsitweaksQol.MOD_ID, dist = Dist.CLIENT)
public class PsitweaksQol {
    public static final String MOD_ID = "psitweaks_qol";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PsitweaksQol(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                PsitweaksQolConfig.CLIENT_SPEC,
                "psitweaks_qol-client.toml"
        );
        PsitweaksQolClientGuiEvents.register(NeoForge.EVENT_BUS);
        modEventBus.addListener(this::registerProviders);
    }

    private void registerProviders(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new PsitweaksQolLanguageProvider(packOutput, "en_us"));
        generator.addProvider(event.includeClient(), new PsitweaksQolLanguageProvider(packOutput, "ja_jp"));
    }
}
