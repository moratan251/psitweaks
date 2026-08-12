package com.moratan251.psitweaksqol.client.gui;

import com.moratan251.psitweaksqol.client.config.PsitweaksQolConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.SpellPiece;

public final class PieceBookmarkManager {
    private static volatile List<? extends String> cachedConfigValues;
    private static volatile Set<String> cachedBookmarkIds = Set.of();

    private PieceBookmarkManager() {
    }

    public static boolean isBookmarked(SpellPiece piece) {
        return isBookmarked(piece, getBookmarkIdSnapshot());
    }

    public static boolean isBookmarked(SpellPiece piece, Set<String> bookmarkIds) {
        ResourceLocation pieceId = getPieceId(piece);
        return pieceId != null && bookmarkIds.contains(pieceId.toString());
    }

    public static boolean toggle(SpellPiece piece) {
        ResourceLocation pieceId = getPieceId(piece);
        if (pieceId == null) {
            return false;
        }

        Set<String> bookmarkIds = getBookmarkIds();
        String pieceIdValue = pieceId.toString();
        if (!bookmarkIds.remove(pieceIdValue)) {
            bookmarkIds.add(pieceIdValue);
        }

        save(bookmarkIds);
        return true;
    }

    private static ResourceLocation getPieceId(SpellPiece piece) {
        return PsiAPI.getSpellPieceKey(piece.getClass());
    }

    public static Set<String> getBookmarkIds() {
        return new LinkedHashSet<>(getBookmarkIdSnapshot());
    }

    private static Set<String> getBookmarkIdSnapshot() {
        List<? extends String> configValues = PsitweaksQolConfig.CLIENT.pieceBookmarks.get();
        if (configValues == cachedConfigValues) {
            return cachedBookmarkIds;
        }
        return rebuildBookmarkIdSnapshot(configValues);
    }

    private static synchronized Set<String> rebuildBookmarkIdSnapshot(List<? extends String> configValues) {
        if (configValues == cachedConfigValues) {
            return cachedBookmarkIds;
        }

        Set<String> bookmarkIds = new LinkedHashSet<>();
        for (String value : configValues) {
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id != null) {
                bookmarkIds.add(id.toString());
            }
        }

        cachedBookmarkIds = Collections.unmodifiableSet(bookmarkIds);
        cachedConfigValues = configValues;
        return cachedBookmarkIds;
    }

    private static void save(Set<String> bookmarkIds) {
        PsitweaksQolConfig.CLIENT.pieceBookmarks.set(new ArrayList<>(bookmarkIds));
        cachedConfigValues = null;
        PsitweaksQolConfig.CLIENT_SPEC.save();
    }
}
