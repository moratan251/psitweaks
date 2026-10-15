package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/**
 * Loads spells saved before the ideaspace pieces were merged into one piece per reading or direction with a resource
 * mode. A piece saved under an old ID is rewritten to the new ID and mode, and the wire of its old String or Item
 * input moves to the merged input. The slot tricks keep their IDs and only move that wire. The saved tag itself is
 * never changed; {@link #migrate} works on a copy.
 */
public final class IdeaStoragePieceMigration {
    private static final String TAG_KEY_LEGACY = "spellKey";
    private static final String TAG_KEY = "key";
    private static final String TAG_PARAMS = "params";
    private static final String TAG_MODE = "psitweaksMode";
    private static final String TAG_LEGACY_TRANSFER = "psitweaksTransferMode";
    private static final String NUMBER = "psi.spellparam.number";
    private static final String POWER = "psi.spellparam.power";

    /** Where an old piece goes: the new piece, its mode (null for the slot tricks) and the merged input's name. */
    private record Target(ResourceLocation id, String mode, String input, boolean itemTrick) {
    }

    private static final Map<String, Target> TARGETS = new HashMap<>();

    static {
        String[] kinds = {"item", "fluid", "chemical"};
        for (String kind : kinds) {
            selector(kind + "_amount", "selector_idea_storage_amount", kind);
            selector(kind + "_free_capacity", "selector_idea_storage_free_capacity", kind);
            selector(kind + "_type_count", "selector_idea_storage_type_count", kind);
            trick("deposit_" + kind, "trick_idea_storage_deposit", kind);
            trick("withdraw_" + kind, "trick_idea_storage_withdraw", kind);
        }
        selector("energy", "selector_idea_storage_amount", "energy");
        selector("energy_free_capacity", "selector_idea_storage_free_capacity", "energy");
        trick("absorb_fe", "trick_idea_storage_deposit", "energy");
        trick("supply_fe", "trick_idea_storage_withdraw", "energy");
        for (String slot : new String[] {"trick_idea_storage_deposit_slot", "trick_idea_storage_withdraw_slot"}) {
            TARGETS.put(slot, new Target(Psitweaks.location(slot), null, PsitweaksSpellParams.FILTER, false));
        }
    }

    private IdeaStoragePieceMigration() {
    }

    private static void selector(String oldSuffix, String newId, String kind) {
        TARGETS.put("selector_idea_storage_" + oldSuffix,
                new Target(Psitweaks.location(newId), "resource_" + kind, PsitweaksSpellParams.RESOURCE, false));
    }

    private static void trick(String oldSuffix, String newId, String kind) {
        TARGETS.put("trick_idea_storage_" + oldSuffix,
                new Target(Psitweaks.location(newId), "resource_" + kind, PsitweaksSpellParams.FILTER, kind.equals("item")));
    }

    /** A migrated copy of a saved piece, or null when the piece needs no change. */
    public static CompoundTag migrate(CompoundTag cmp) {
        if (cmp == null) {
            return null;
        }
        String tagKey = cmp.contains(TAG_KEY_LEGACY) ? TAG_KEY_LEGACY : cmp.contains(TAG_KEY) ? TAG_KEY : null;
        if (tagKey == null) {
            return null;
        }
        Target target = TARGETS.get(oldPath(cmp.getString(tagKey)));
        if (target == null) {
            return null;
        }
        CompoundTag params = cmp.getCompound(TAG_PARAMS);
        boolean renamed = !Psitweaks.location(oldPath(cmp.getString(tagKey))).equals(target.id());
        if (!renamed && (params.contains(target.input()) || !hasOldInput(params))) {
            return null;
        }

        CompoundTag copy = cmp.copy();
        CompoundTag copiedParams = copy.getCompound(TAG_PARAMS);
        moveInput(copiedParams, target.input());
        if (target.itemTrick()) {
            // The item tricks once named their quantity Power; keep that wire on Number.
            if (!copiedParams.contains(NUMBER) && !copiedParams.contains("_number")) {
                String oldKey = copiedParams.contains(POWER, Tag.TAG_ANY_NUMERIC) ? POWER : "_power";
                if (copiedParams.contains(oldKey, Tag.TAG_ANY_NUMERIC)) {
                    copiedParams.putInt(NUMBER, copiedParams.getInt(oldKey));
                }
            }
        }
        copy.put(TAG_PARAMS, copiedParams);
        if (renamed) {
            copy.putString(tagKey, target.id().toString());
            String mode = target.mode();
            if ("exact".equals(copy.getString(TAG_LEGACY_TRANSFER)) && !mode.equals("resource_energy")) {
                mode += "_exact";
            }
            copy.putString(TAG_MODE, mode);
        } else {
            copy.remove(TAG_MODE);
        }
        copy.remove(TAG_LEGACY_TRANSFER);
        return copy;
    }

    private static boolean hasOldInput(CompoundTag params) {
        return params.contains(PsitweaksSpellParams.STRING) || params.contains(PsitweaksSpellParams.ITEM);
    }

    /** Only one of the old String and Item inputs existed at a time; whichever was saved becomes the merged input. */
    private static void moveInput(CompoundTag params, String input) {
        for (String old : new String[] {PsitweaksSpellParams.STRING, PsitweaksSpellParams.ITEM}) {
            if (params.contains(old, Tag.TAG_ANY_NUMERIC)) {
                if (!params.contains(input)) {
                    params.putInt(input, params.getInt(old));
                }
                params.remove(old);
            }
        }
    }

    /** The piece path of a saved ID, which may lack the namespace. Other mods' IDs never match. */
    private static String oldPath(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return "";
        }
        if (id.indexOf(':') < 0 || location.getNamespace().equals(Psitweaks.MOD_ID)) {
            return location.getPath();
        }
        return "";
    }
}
