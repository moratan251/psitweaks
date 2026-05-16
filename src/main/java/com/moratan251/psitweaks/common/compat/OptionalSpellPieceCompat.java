package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.spell.trick.PieceTrickDie;

public final class OptionalSpellPieceCompat {
    private static final ResourceLocation PHYSICAL_PROPULSION_ID = Psitweaks.location("trick_physical_propulsion");
    private static final String REMOVAL_MARKER_COMMENT = "psitweaks:remove_optional_physical_propulsion";

    private OptionalSpellPieceCompat() {
    }

    public static boolean shouldRemoveMissingPhysicalPropulsion(CompoundTag cmp) {
        if (SablePhysicsCompat.isLoaded() || cmp == null) {
            return false;
        }

        String id = cmp.contains("spellKey") ? cmp.getString("spellKey") : cmp.getString("key");
        return PHYSICAL_PROPULSION_ID.equals(ResourceLocation.tryParse(id));
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
