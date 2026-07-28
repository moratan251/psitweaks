package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.client.config.PsitweaksClientConfig;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.SpellPiece;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PieceBookmarkManager {
    private PieceBookmarkManager() {
    }

    public static boolean isBookmarked(SpellPiece piece) {
        return isBookmarked(piece, getBookmarkIds());
    }

    public static boolean isBookmarked(SpellPiece piece, Set<String> bookmarkIds) {
        ResourceLocation pieceId = getPieceId(piece);
        return pieceId != null && bookmarkIds.contains(pieceId.toString());
    }

    public static boolean add(SpellPiece piece) {
        ResourceLocation pieceId = getPieceId(piece);
        if (pieceId == null) {
            return false;
        }

        Set<String> bookmarkIds = getBookmarkIds();
        if (!bookmarkIds.add(pieceId.toString())) {
            return false;
        }

        save(bookmarkIds);
        return true;
    }

    public static boolean remove(SpellPiece piece) {
        ResourceLocation pieceId = getPieceId(piece);
        if (pieceId == null) {
            return false;
        }

        Set<String> bookmarkIds = getBookmarkIds();
        if (!bookmarkIds.remove(pieceId.toString())) {
            return false;
        }

        save(bookmarkIds);
        return true;
    }

    private static ResourceLocation getPieceId(SpellPiece piece) {
        return PsiAPI.SPELL_PIECE_REGISTRY.getKey(piece.getClass());
    }

    public static Set<String> getBookmarkIds() {
        Set<String> bookmarkIds = new LinkedHashSet<>();
        for (String value : PsitweaksClientConfig.CLIENT.pieceBookmarks.get()) {
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id != null) {
                bookmarkIds.add(id.toString());
            }
        }
        return bookmarkIds;
    }

    private static void save(Set<String> bookmarkIds) {
        List<String> values = new ArrayList<>(bookmarkIds);
        PsitweaksClientConfig.CLIENT.pieceBookmarks.set(values);
        PsitweaksClientConfig.CLIENT_SPEC.save();
    }
}
