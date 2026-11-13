package com.moratan251.psitweaksqol.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * いまの言語設定に関係なく、en_us の訳を引く。ピースの英語名での検索と、英語名順の並べ替えに使う。
 * 訳は翻訳キーの名前空間（最初の「.」まで）ごとに初めて引いたときに読み、リソースの再読み込みで捨てる。
 */
public final class EnglishTranslations {
    private static final String ENGLISH_LANG_FILE = "lang/en_us.json";
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    private static ResourceManager cachedResourceManager;

    private EnglishTranslations() {
    }

    /** 英語の訳。見つからなければ翻訳キーをそのまま返す。 */
    public static String get(String translationKey) {
        int separatorIndex = translationKey.indexOf('.');
        if (separatorIndex <= 0) {
            return translationKey;
        }

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        if (cachedResourceManager != resourceManager) {
            cachedResourceManager = resourceManager;
            TRANSLATIONS.clear();
        }

        String namespace = translationKey.substring(0, separatorIndex);
        Map<String, String> translations = TRANSLATIONS.computeIfAbsent(
                namespace,
                key -> load(resourceManager, key)
        );
        return translations.getOrDefault(translationKey, translationKey);
    }

    /** その名前空間の en_us をすべてのリソースパックから読み、1 つにまとめる。 */
    private static Map<String, String> load(ResourceManager resourceManager, String namespace) {
        ResourceLocation langResource = ResourceLocation.fromNamespaceAndPath(namespace, ENGLISH_LANG_FILE);
        List<Resource> resources = resourceManager.getResourceStack(langResource);
        if (resources.isEmpty()) {
            return Map.of();
        }

        Map<String, String> translations = new HashMap<>();
        for (Resource resource : resources) {
            try (Reader reader = resource.openAsReader()) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                if (!jsonElement.isJsonObject()) {
                    continue;
                }

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    JsonElement value = entry.getValue();
                    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                        translations.put(entry.getKey(), value.getAsString());
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (translations.isEmpty()) {
            return Map.of();
        }
        return translations;
    }
}
