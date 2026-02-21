package com.moratan251.psitweaks.mixin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.widget.PiecePanelWidget;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mixin(value = PiecePanelWidget.class, remap = false)
public abstract class PiecePanelWidgetEnglishSearchMixin {
    @Unique
    private static final String ENGLISH_LANG_FILE = "lang/en_us.json";
    @Unique
    private static final Map<String, Map<String, String>> ENGLISH_TRANSLATIONS = new HashMap<>();
    @Unique
    private static ResourceManager psitweaks$cachedResourceManager;

    // Reuse Psi's internal token scoring to keep ranking behavior consistent.
    @Invoker("rankTextToken")
    protected abstract int psitweaks$invokeRankTextToken(String text, String token);

    // If localized search misses, rerank with an English-name fallback.
    @Inject(method = "ranking", at = @At("RETURN"), cancellable = true)
    private void psitweaks$includeEnglishNameSearch(String searchText, SpellPiece piece, CallbackInfoReturnable<Integer> cir) {
        int currentRanking = cir.getReturnValue();
        // Preserve the original result when already matched or query is empty.
        if (currentRanking > 0 || searchText == null || searchText.isBlank()) {
            return;
        }

        String localizedName = I18n.get(piece.getUnlocalizedName()).toLowerCase(Locale.ROOT);
        String localizedDesc = I18n.get(piece.getUnlocalizedDesc()).toLowerCase(Locale.ROOT);

        String englishName = psitweaks$getEnglishTranslation(piece.getUnlocalizedName()).toLowerCase(Locale.ROOT);
        String englishDesc = psitweaks$getEnglishTranslation(piece.getUnlocalizedDesc()).toLowerCase(Locale.ROOT);

        if (englishName.equals(piece.getUnlocalizedName().toLowerCase(Locale.ROOT))) {
            englishName = "";
        }
        if (englishDesc.equals(piece.getUnlocalizedDesc().toLowerCase(Locale.ROOT))) {
            englishDesc = "";
        }

        if (englishName.isEmpty() && englishDesc.isEmpty()) {
            return;
        }

        int englishRanking = psitweaks$calculateBilingualRanking(
                searchText.toLowerCase(Locale.ROOT),
                piece,
                localizedName,
                localizedDesc,
                englishName,
                englishDesc
        );
        if (englishRanking > 0) {
            cir.setReturnValue(englishRanking);
        }
    }

    @Unique
    // Mirror Psi ranking rules while evaluating localized and English strings together.
    private int psitweaks$calculateBilingualRanking(
            String searchText,
            SpellPiece piece,
            String localizedName,
            String localizedDesc,
            String englishName,
            String englishDesc
    ) {
        int ranking = 0;
        String[] tokens = searchText.split("\\s+");

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }

            // Input type filter syntax: in:<type>.
            if (token.startsWith("in:")) {
                String requiredTypeToken = token.substring(3);
                if (requiredTypeToken.isEmpty()) {
                    continue;
                }

                int bestMatch = 0;
                for (SpellParam param : piece.params.values()) {
                    String requiredType = param.getRequiredTypeString().getString().toLowerCase(Locale.ROOT);
                    bestMatch = Math.max(bestMatch, psitweaks$invokeRankTextToken(requiredType, requiredTypeToken));
                }
                if (bestMatch <= 0) {
                    return 0;
                }

                ranking += bestMatch;
                continue;
            }

            // Output type filter syntax: out:<type>.
            if (token.startsWith("out:")) {
                String outputTypeToken = token.substring(4);
                if (outputTypeToken.isEmpty()) {
                    continue;
                }

                String outputType = piece.getEvaluationTypeString().getString().toLowerCase(Locale.ROOT);
                int outputMatch = psitweaks$invokeRankTextToken(outputType, outputTypeToken);
                if (outputMatch <= 0) {
                    return 0;
                }

                ranking += outputMatch;
                continue;
            }

            // Piece id filter syntax: @<piece_path>.
            if (token.startsWith("@")) {
                String pieceIdToken = token.substring(1);
                if (pieceIdToken.isEmpty()) {
                    continue;
                }

                ResourceLocation pieceKey = PsiAPI.getSpellPieceKey(piece.getClass());
                String piecePath = pieceKey == null ? null : pieceKey.getPath();
                if (piecePath == null) {
                    return 0;
                }

                int pathMatch = psitweaks$invokeRankTextToken(piecePath, pieceIdToken);
                if (pathMatch <= 0) {
                    return 0;
                }

                ranking += pathMatch;
                continue;
            }

            // Regular token matching uses the better score of localized vs English text.
            int localizedNameRank = psitweaks$invokeRankTextToken(localizedName, token);
            int englishNameRank = psitweaks$invokeRankTextToken(englishName, token);
            int nameRank = Math.max(localizedNameRank, englishNameRank);

            int localizedDescRank = psitweaks$invokeRankTextToken(localizedDesc, token);
            int englishDescRank = psitweaks$invokeRankTextToken(englishDesc, token);
            int descRank = Math.max(localizedDescRank, englishDescRank);
            ranking += nameRank;
            if (nameRank <= 0 && descRank <= 0) {
                return 0;
            }

            ranking += descRank / 2;
        }

        return ranking;
    }

    @Unique
    // Resolve a key from en_us.json of the same namespace as the translation key.
    private static String psitweaks$getEnglishTranslation(String translationKey) {
        int separatorIndex = translationKey.indexOf('.');
        if (separatorIndex <= 0) {
            return translationKey;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ResourceManager resourceManager = minecraft.getResourceManager();

        // Clear cached language maps after resource reloads.
        if (psitweaks$cachedResourceManager != resourceManager) {
            psitweaks$cachedResourceManager = resourceManager;
            ENGLISH_TRANSLATIONS.clear();
        }

        String namespace = translationKey.substring(0, separatorIndex);
        Map<String, String> translations = ENGLISH_TRANSLATIONS.computeIfAbsent(
                namespace,
                key -> psitweaks$loadEnglishTranslations(resourceManager, key)
        );
        return translations.getOrDefault(translationKey, translationKey);
    }

    @Unique
    // Merge all en_us language resources for the namespace into one lookup map.
    private static Map<String, String> psitweaks$loadEnglishTranslations(ResourceManager resourceManager, String namespace) {
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
