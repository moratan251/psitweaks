package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.spell.trick.PieceTrickDie;

public final class OptionalSpellPieceCompat {
    private static final String TAG_KEY_LEGACY = "spellKey";
    private static final String TAG_KEY = "key";
    private static final ResourceLocation PHYSICAL_PROPULSION_ID = Psitweaks.location("trick_physical_propulsion");
    private static final String REMOVAL_MARKER_COMMENT = "psitweaks:remove_optional_physical_propulsion";

    private OptionalSpellPieceCompat() {
    }

    public static boolean shouldRemoveMissingPhysicalPropulsion(CompoundTag cmp) {
        if (SablePhysicsCompat.isLoaded() || cmp == null) {
            return false;
        }

        return matchesSpellPieceId(getSpellPieceId(cmp), PHYSICAL_PROPULSION_ID);
    }

    public static void rewriteSpellPieceId(CompoundTag cmp, ResourceLocation oldId, ResourceLocation newId) {
        String tagKey = getSpellPieceIdTag(cmp);
        if (tagKey == null || oldId == null || newId == null) {
            return;
        }

        if (matchesSpellPieceId(cmp.getString(tagKey), oldId)) {
            cmp.putString(tagKey, newId.toString());
        }
    }

    private static String getSpellPieceId(CompoundTag cmp) {
        String tagKey = getSpellPieceIdTag(cmp);
        return tagKey == null ? "" : cmp.getString(tagKey);
    }

    private static String getSpellPieceIdTag(CompoundTag cmp) {
        if (cmp == null) {
            return null;
        }
        if (cmp.contains(TAG_KEY_LEGACY)) {
            return TAG_KEY_LEGACY;
        }
        if (cmp.contains(TAG_KEY)) {
            return TAG_KEY;
        }
        return null;
    }

    private static boolean matchesSpellPieceId(String id, ResourceLocation expected) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (expected.equals(location)) {
            return true;
        }

        return expected.getPath().equals(id);
    }

    public static SpellPiece createRemovalMarker(Spell spell) {
        SpellPiece marker = new PieceTrickDie(spell == null ? SpellPiece.dummySpell : spell);
        marker.comment = REMOVAL_MARKER_COMMENT;
        return marker;
    }

    public static void removeMarkedPieces(SpellGrid grid) {
        if (grid == null) {
            return;
        }

        for (int x = 0; x < grid.gridData.length; x++) {
            SpellPiece[] column = grid.gridData[x];
            for (int y = 0; y < column.length; y++) {
                if (isRemovalMarker(column[y])) {
                    column[y] = null;
                }
            }
        }
    }

    public static boolean isRemovalMarker(SpellPiece piece) {
        return piece instanceof PieceTrickDie && REMOVAL_MARKER_COMMENT.equals(piece.comment);
    }
}
