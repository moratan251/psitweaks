package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/** Psi 110 replaces the old advancement group registry with synced datapack groups. */
public final class PsitweaksSpellPieceGroupProvider implements DataProvider {
    private final PackOutput.PathProvider paths;

    public PsitweaksSpellPieceGroupProvider(PackOutput output) {
        paths = output.createPathProvider(PackOutput.Target.DATA_PACK, "psi/spell_piece_group");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        group(output, saves, "trick_explode_no_destroy", "trick_explode_no_destroy");
        group(output, saves, "trick_parade", "trick_parade");
        group(output, saves, "trick_flight", "trick_flight");
        group(output, saves, "trick_hardening", "trick_hardening");
        group(output, saves, "trick_barrier", "trick_barrier");
        group(output, saves, "trick_interact_block", "trick_interact_block");
        group(output, saves, "trick_freeze_block", "trick_freeze_block");
        group(output, saves, "trick_melt_block", "trick_melt_block");
        group(output, saves, "trick_pulsar", "trick_pulsar", "trick_pulsar_sequence", "trick_pulsar_light");
        group(output, saves, "trick_store_entity", "trick_store_entity");
        group(output, saves, "selector_stored_entity", "selector_stored_entity");
        group(output, saves, "trick_store_value", "trick_store_value");
        group(output, saves, "selector_stored_value", "selector_stored_value");
        group(output, saves, "selector_nearby_spellgram", "selector_nearby_spellgram");
        group(output, saves, "trick_dispel", "trick_dispel");
        group(output, saves, "trick_dispel_beneficial", "trick_dispel_beneficial");
        group(output, saves, "trick_dispel_non_beneficial", "trick_dispel_non_beneficial");
        group(output, saves, "trick_cocytus", "trick_cocytus");
        group(output, saves, "trick_supply_fe", "trick_supply_fe");
        group(output, saves, "trick_time_accelerate", "trick_time_accelerate");
        group(output, saves, "trick_phonon_maser", "trick_phonon_maser");
        group(output, saves, "trick_meteor_line", "trick_meteor_line");
        group(output, saves, "trick_supreme_infusion", "trick_supreme_infusion");
        group(output, saves, "trick_molecular_divider", "trick_molecular_divider");
        group(output, saves, "trick_break_fortune", "trick_break_fortune");
        group(output, saves, "trick_break_silk", "trick_break_silk");
        group(output, saves, "trick_aqua_cutter", "trick_aqua_cutter");
        group(output, saves, "trick_dry_meteor", "trick_dry_meteor");
        group(output, saves, "trick_blaze_ball", "trick_blaze_ball");
        group(output, saves, "trick_active_air_mine", "trick_active_air_mine");
        group(output, saves, "trick_flare_circle", "trick_flare_circle");
        group(output, saves, "trick_ice_circle", "trick_ice_circle");
        group(output, saves, "trick_set_spellgram_follow_target", "trick_set_spellgram_follow_target");
        group(output, saves, "trick_die_flex", "trick_die_flex");
        group(output, saves, "trick_jump", "trick_jump");
        group(output, saves, "trick_jump_flex", "trick_jump_flex");
        group(output, saves, "trick_switch", "trick_switch");
        group(output, saves, "trick_switch_flex", "trick_switch_flex");
        group(output, saves, "jump_anchor", "jump_anchor");
        group(output, saves, "safety", "safety");
        group(output, saves, "trick_radiation_injection", "trick_radiation_injection");
        group(output, saves, "trick_radiation_filter", "trick_radiation_filter");
        group(output, saves, "trick_cure_radiation", "trick_cure_radiation");
        group(output, saves, "trick_guillotine", "trick_guillotine");
        group(output, saves, "trick_material_mutation", "trick_material_mutation");
        group(output, saves, "trick_pull_item", "trick_pull_item");
        group(output, saves, "trick_send_item", "trick_send_item");
        group(output, saves, "trick_mass_block_break", "trick_mass_block_break");
        group(output, saves, "idea_storage", "trick_idea_storage_view", "trick_idea_storage_absorb_fe", "trick_idea_storage_supply_fe", "selector_idea_storage_energy");
        group(output, saves, "operator_alive", "operator_alive");
        group(output, saves, "operator_weak_raycast", "operator_weak_raycast");
        group(output, saves, "operator_weak_raycast_axis", "operator_weak_raycast_axis");
        group(output, saves, "operator_strong_raycast", "operator_strong_raycast");
        group(output, saves, "operator_strong_raycast_axis", "operator_strong_raycast_axis");
        group(output, saves, "macro_caster_raycast", "macro_caster_raycast");
        group(output, saves, "macro_caster_raycast_axis", "macro_caster_raycast_axis");
        group(output, saves, "macro_caster_strong_raycast", "macro_caster_strong_raycast");
        group(output, saves, "macro_caster_strong_raycast_axis", "macro_caster_strong_raycast_axis");
        group(output, saves, "macro_caster_weak_raycast", "macro_caster_weak_raycast");
        group(output, saves, "macro_caster_weak_raycast_axis", "macro_caster_weak_raycast_axis");
        group(output, saves, "macro_caster_axial_offset", "macro_caster_axial_offset");
        group(output, saves, "macro_caster_axial_rotation", "macro_caster_axial_rotation");
        group(output, saves, "macro_caster_axial_offset_3d", "macro_caster_axial_offset_3d");
        group(output, saves, "macro_caster_axial_rotation_3d", "macro_caster_axial_rotation_3d");
        group(output, saves, "macro_face_axial_offset", "macro_face_axial_offset");
        group(output, saves, "macro_face_axial_rotation", "macro_face_axial_rotation");
        group(output, saves, "trick_physical_propulsion", "trick_physical_propulsion");
        group(output, saves, "operator_tan", "operator_tan");
        group(output, saves, "operator_atan", "operator_atan");
        group(output, saves, "operator_sinh", "operator_sinh");
        group(output, saves, "operator_cosh", "operator_cosh");
        group(output, saves, "operator_tanh", "operator_tanh");
        group(output, saves, "operator_greater_than", "operator_greater_than");
        group(output, saves, "operator_greater_than_or_equal", "operator_greater_than_or_equal");
        group(output, saves, "operator_equal", "operator_equal");
        group(output, saves, "string", "constant_string", "operator_format_string", "operator_from_string", "operator_list_from_string_list", "operator_number_list_to_vector", "operator_vector_to_number_list", "operator_to_string", "operator_list_to_string_list", "operator_get_id", "operator_get_id_list", "selector_display_name", "selector_display_name_list", "selector_effect_list", "selector_online_players", "selector_held_items", "selector_internal_items", "selector_indexed_element", "selector_nbt", "selector_nbt_keys", "selector_nbt_value", "operator_tag_list", "operator_string_partial_match", "operator_string_starts_with", "operator_string_ends_with", "operator_string_concat", "operator_string_split", "operator_string_slice", "operator_string_length", "operator_string_replace", "operator_string_trim", "operator_string_list_join", "operator_player_name", "operator_list_search", "operator_list_search_exclude", "operator_random_element", "operator_list_add", "operator_list_remove", "operator_list_remove_indices", "operator_list_insert", "operator_list_slice", "operator_list_size", "operator_list_exclusion", "operator_list_intersection", "operator_list_concatenation");
        group(output, saves, "item", "selector_held_item", "selector_selected_slot_item", "selector_entity_slot_item", "selector_internal_slot_item", "operator_item_count", "operator_item_slot", "operator_item_total_count");
        group(output, saves, "block", "selector_block", "selector_block_list", "operator_block_state", "operator_block_state_value", "operator_block_state_entries", "operator_block_position", "operator_block_position_list");
        group(output, saves, "matrix", "operator_matrix_add", "operator_matrix_subtract", "operator_matrix_multiply", "operator_matrix_scalar_multiply", "operator_matrix_transpose", "operator_matrix_determinant", "operator_matrix_inverse", "operator_matrix_extract_row", "operator_matrix_extract_column", "operator_matrix_element", "operator_matrix_row_count", "operator_matrix_column_count", "operator_matrix_multiply_vector", "operator_matrix_column_from_list", "operator_matrix_flatten", "operator_matrix_identity", "operator_matrix_zero", "operator_matrix_diagonal", "operator_matrix_replace_column", "operator_matrix_replace_row", "operator_matrix_replace_element", "operator_matrix_delete_row", "operator_matrix_delete_column", "operator_matrix_transform_vector", "operator_matrix_linear_part", "operator_matrix_cuboid_region", "operator_region_vector_list", "operator_inside_region", "operator_outside_region");
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    private void group(CachedOutput output, List<CompletableFuture<?>> saves, String id, String... members) {
        JsonObject json = new JsonObject();
        json.addProperty("main", Psitweaks.location(members[0]).toString());
        JsonArray pieces = new JsonArray();
        for (int i = 1; i < members.length; i++) {
            pieces.add(Psitweaks.location(members[i]).toString());
        }
        json.add("pieces", pieces);
        // These definitions use IDs only; optional pieces need not be loaded during datagen.
        String requiredMod = switch (id) {
            case "trick_radiation_injection", "trick_radiation_filter", "trick_cure_radiation" -> "mekanism";
            case "trick_physical_propulsion" -> "sable";
            default -> null;
        };
        if (requiredMod != null) {
            JsonObject condition = new JsonObject();
            condition.addProperty("type", "neoforge:mod_loaded");
            condition.addProperty("modid", requiredMod);
            JsonArray conditions = new JsonArray();
            conditions.add(condition);
            json.add("neoforge:conditions", conditions);
        }
        saves.add(DataProvider.saveStable(output, json, paths.json(Psitweaks.location(id))));
    }

    @Override
    public String getName() {
        return "Psitweaks spell piece groups";
    }
}
