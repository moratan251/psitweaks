package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.Psitweaks;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.spell.trick.PieceTrickDie;
import org.jetbrains.annotations.Nullable;

public final class OptionalSpellPieceCompat {
    private static final String TAG_KEY_LEGACY = "spellKey";
    private static final String TAG_KEY = "key";
    private static final ResourceLocation PHYSICAL_PROPULSION_ID = Psitweaks.location("trick_physical_propulsion");
    private static final String REMOVAL_MARKER_COMMENT = "psitweaks:remove_optional_physical_propulsion";
    /** Pieces registered only with Mekanism (see {@code PsitweaksSpells}). */
    private static final List<ResourceLocation> MEKANISM_PIECE_IDS = List.of(
            Psitweaks.location("selector_autonomous_caster_position"),
            Psitweaks.location("trick_radiation_injection"),
            Psitweaks.location("trick_radiation_filter"),
            Psitweaks.location("trick_cure_radiation"));

    /**
     * Pieces whose ID changed during 1.0.0 development (old ID → new ID). Psi cannot read an unknown namespaced ID
     * (it fails the whole player or chunk), so spells saved before the rename load under the new ID.
     */
    private static final Map<ResourceLocation, ResourceLocation> RENAMED_PIECES = Map.of(
            Psitweaks.location("trick_kamaitachi"), Psitweaks.location("trick_air_bullet"),
            Psitweaks.location("trick_gravity_collapse"), Psitweaks.location("trick_micro_black_hole"));

    /**
     * Pieces removed from the mod. Spells saved while they existed load with the piece taken out of the grid, like a
     * missing optional piece, instead of failing the whole player or chunk.
     */
    private static final List<ResourceLocation> REMOVED_PIECE_IDS = List.of(
            // Trick: Set SpellGram Follow Target (removed in 1.0.0 with the SpellGram rework).
            Psitweaks.location("trick_set_spellgram_follow_target"));

    private OptionalSpellPieceCompat() {
    }

    /** A copy of {@code cmp} under the piece's new ID when it was saved under a renamed one, otherwise null. */
    @Nullable
    public static CompoundTag renamedCopy(CompoundTag cmp) {
        String tagKey = getSpellPieceIdTag(cmp);
        if (tagKey == null) {
            return null;
        }
        String id = cmp.getString(tagKey);
        for (Map.Entry<ResourceLocation, ResourceLocation> rename : RENAMED_PIECES.entrySet()) {
            if (matchesSpellPieceId(id, rename.getKey())) {
                CompoundTag copy = cmp.copy();
                copy.putString(tagKey, rename.getValue().toString());
                return copy;
            }
        }
        return null;
    }

    /**
     * Whether the piece belongs to an optional mod that is not loaded (Sable for Trick: Physical Propulsion, Mekanism
     * for the radiation tricks and Selector: Autonomous Caster Position). Such a piece is not registered, and Psi would
     * fail the whole spell (or the player holding it), so it is read as a marker and removed from the grid. A spell
     * saved while the mod is missing keeps the piece removed. Pieces removed from the mod ({@link #REMOVED_PIECE_IDS})
     * are taken out the same way.
     */
    public static boolean shouldRemoveMissingOptionalPiece(CompoundTag cmp) {
        if (cmp == null) {
            return false;
        }
        String id = getSpellPieceId(cmp);
        for (ResourceLocation removedPiece : REMOVED_PIECE_IDS) {
            if (matchesSpellPieceId(id, removedPiece)) {
                return true;
            }
        }
        if (!SablePhysicsCompat.isLoaded() && matchesSpellPieceId(id, PHYSICAL_PROPULSION_ID)) {
            return true;
        }
        if (!MekanismCompat.isMekanismLoaded()) {
            for (ResourceLocation mekanismPiece : MEKANISM_PIECE_IDS) {
                if (matchesSpellPieceId(id, mekanismPiece)) {
                    return true;
                }
            }
        }
        return false;
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
        SpellPiece marker = vazkii.psi.api.PsiAPI.SPELL_PIECE_REGISTRY.get(vazkii.psi.api.PsiAPI.location("trick_die")).create(spell == null ? SpellPiece.dummySpell : spell);
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
