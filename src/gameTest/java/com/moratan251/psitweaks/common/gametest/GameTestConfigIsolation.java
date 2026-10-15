package com.moratan251.psitweaks.common.gametest;

import com.electronwill.nightconfig.core.file.FileWatcher;
import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 1.20.1 only. Forge saves the config file on every {@code ConfigValue.set} and reloads it on a watcher thread, so a
 * test that changes a storage limit and restores it can race that reload: the reload brings the temporary value back,
 * and the next save keeps it in the file for later runs. NeoForge 1.21.1 saves only on request, so the tests there have
 * no such race. The game test server stops watching this mod's common config before any test runs.
 */
@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class GameTestConfigIsolation {
    private GameTestConfigIsolation() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (!(event.getServer() instanceof GameTestServer)) {
            return;
        }
        for (ModConfig config : ConfigTracker.INSTANCE.configSets().get(ModConfig.Type.COMMON)) {
            if (Psitweaks.MOD_ID.equals(config.getModId())) {
                FileWatcher.defaultInstance().removeWatch(config.getFullPath());
            }
        }
    }
}
