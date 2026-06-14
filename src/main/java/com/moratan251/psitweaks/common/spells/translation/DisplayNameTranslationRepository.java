package com.moratan251.psitweaks.common.spells.translation;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

public final class DisplayNameTranslationRepository {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LANGUAGE_DIRECTORY = "lang";
    private static final String LANGUAGE_PREFIX = LANGUAGE_DIRECTORY + "/";
    private static final String JSON_SUFFIX = ".json";
    private static final ResourceManagerReloadListener CLIENT_RELOAD_LISTENER =
            DisplayNameTranslationRepository::reload;
    private static final ResourceManagerReloadListener SERVER_RELOAD_LISTENER =
            DisplayNameTranslationRepository::reloadFromServerPacks;

    private static volatile Map<LangResourceKey, Map<String, String>> translations = Map.of();

    private DisplayNameTranslationRepository() {
    }

    /**
     * Registers the client resource listener. Call this only on the physical client.
     */
    public static void registerClientReloadListeners(IEventBus modEventBus) {
        modEventBus.addListener(ClientReloadEvents::onRegisterClientReloadListeners);
    }

    /**
     * Registers the server data reload bridge. Call this only on a dedicated server.
     */
    public static void registerServerReloadListeners(IEventBus neoForgeEventBus) {
        neoForgeEventBus.addListener(DisplayNameTranslationRepository::onAddReloadListener);
    }

    public static String getTranslation(String namespace, String language, String key) {
        if (namespace == null || language == null || key == null) {
            return null;
        }
        Map<String, String> languageMap = translations.get(new LangResourceKey(
                namespace.toLowerCase(Locale.ROOT),
                language.toLowerCase(Locale.ROOT)
        ));
        return languageMap == null ? null : languageMap.get(key);
    }

    private static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(SERVER_RELOAD_LISTENER);
    }

    private static void reloadFromServerPacks(ResourceManager serverDataResources) {
        List<PackResources> packs = serverDataResources.listPacks().toList();
        // These PackResources are owned by the server resource manager and must remain open.
        ResourceManager clientResources = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, packs);
        reload(clientResources);
    }

    private static void reload(ResourceManager resourceManager) {
        Map<LangResourceKey, Map<String, String>> loaded = new HashMap<>();
        Map<ResourceLocation, List<Resource>> languageResources = resourceManager.listResourceStacks(
                LANGUAGE_DIRECTORY,
                DisplayNameTranslationRepository::isLanguageResource
        );

        for (Map.Entry<ResourceLocation, List<Resource>> entry : languageResources.entrySet()) {
            ResourceLocation location = entry.getKey();
            String language = languageFromPath(location.getPath());
            if (language == null) {
                continue;
            }

            LangResourceKey resourceKey = new LangResourceKey(location.getNamespace(), language);
            Map<String, String> languageMap = loaded.computeIfAbsent(resourceKey, ignored -> new LinkedHashMap<>());
            for (Resource resource : entry.getValue()) {
                try (InputStream stream = resource.open()) {
                    Language.loadFromJson(stream, languageMap::put);
                } catch (IOException | RuntimeException exception) {
                    LOGGER.warn("Failed to load display-name translations from {} in pack {}",
                            location, resource.sourcePackId(), exception);
                }
            }
        }

        Map<LangResourceKey, Map<String, String>> snapshot = new HashMap<>();
        loaded.forEach((key, value) -> snapshot.put(key, Map.copyOf(value)));
        translations = Map.copyOf(snapshot);
        LOGGER.debug("Loaded {} display-name translation tables", translations.size());
    }

    private static boolean isLanguageResource(ResourceLocation location) {
        return languageFromPath(location.getPath()) != null;
    }

    private static String languageFromPath(String path) {
        if (!path.startsWith(LANGUAGE_PREFIX) || !path.endsWith(JSON_SUFFIX)) {
            return null;
        }
        String language = path.substring(LANGUAGE_PREFIX.length(), path.length() - JSON_SUFFIX.length());
        if (language.isBlank() || language.indexOf('/') >= 0) {
            return null;
        }
        return language.toLowerCase(Locale.ROOT);
    }

    private static final class ClientReloadEvents {
        private static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(CLIENT_RELOAD_LISTENER);
        }
    }

    private record LangResourceKey(String namespace, String language) {
    }
}
