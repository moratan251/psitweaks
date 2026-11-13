package com.moratan251.psitweaksqol.mixin.client;

import com.moratan251.psitweaksqol.client.gui.EnglishTranslations;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.widget.PiecePanelWidget;

import java.util.Locale;

@Mixin(value = PiecePanelWidget.class, remap = false)
public abstract class PiecePanelWidgetEnglishSearchMixin {
    // Reuse Psi's internal token scoring to keep ranking behavior consistent.
    @Invoker("rankTextToken")
    protected abstract int psitweaks$invokeRankTextToken(String text, String token);

    /**
     * 検索の点を、いまの言語の名前・説明と英語の名前・説明の両方で付け直し、高いほうを採る。
     * 以前は「いまの言語で 1 点も当たらなかったピース」だけを付け直していた。それだと、いまの言語の説明文に
     * たまたま同じ綴りがあるピースは、英語名がぴったり一致していても説明文の低い点のままで、下に沈んでいた。
     * 付け直した点は語ごとに高いほうを採るので、いまの言語だけの点より低くなることはない。
     */
    @Inject(method = "ranking", at = @At("RETURN"), cancellable = true)
    private void psitweaks$includeEnglishNameSearch(String searchText, SpellPiece piece, CallbackInfoReturnable<Integer> cir) {
        int currentRanking = cir.getReturnValue();
        if (searchText == null || searchText.isBlank()) {
            return;
        }

        String localizedName = I18n.get(piece.getUnlocalizedName()).toLowerCase(Locale.ROOT);
        String localizedDesc = I18n.get(piece.getUnlocalizedDesc()).toLowerCase(Locale.ROOT);

        String englishName = EnglishTranslations.get(piece.getUnlocalizedName()).toLowerCase(Locale.ROOT);
        String englishDesc = EnglishTranslations.get(piece.getUnlocalizedDesc()).toLowerCase(Locale.ROOT);

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
        if (englishRanking > currentRanking) {
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

            if (token.startsWith("@")) {
                String pieceIdToken = token.substring(1);
                if (pieceIdToken.isEmpty()) {
                    continue;
                }

                ResourceLocation pieceKey = piece.getRegistryKey();
                String pieceNamespace = pieceKey == null ? null : pieceKey.getNamespace();
                if (pieceNamespace == null) {
                    return 0;
                }

                int namespaceMatch = psitweaks$invokeRankTextToken(pieceNamespace, pieceIdToken);
                if (namespaceMatch <= 0) {
                    return 0;
                }

                ranking += namespaceMatch;
                continue;
            }

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
}
