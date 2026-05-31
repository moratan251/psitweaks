package com.moratan251.psitweaks.client.spells;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.SablePhysicsCompat;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import vazkii.psi.api.ClientPsiAPI;

public final class PsitweaksClientSpells {
    public static final DeferredRegister<Material> SPELL_PIECE_MATERIALS =
            DeferredRegister.create(ClientPsiAPI.SPELL_PIECE_MATERIAL, Psitweaks.MOD_ID);

    public static final DeferredHolder<Material, Material> TRICK_EXPLODE_NO_DESTROY =
            registerPieceMaterial("trick_explode_no_destroy");
    public static final DeferredHolder<Material, Material> TRICK_PARADE =
            registerPieceMaterial("trick_parade");
    public static final DeferredHolder<Material, Material> TRICK_FLIGHT =
            registerPieceMaterial("trick_flight");
    public static final DeferredHolder<Material, Material> TRICK_HARDENING =
            registerPieceMaterial("trick_hardening");
    public static final DeferredHolder<Material, Material> TRICK_BARRIER =
            registerPieceMaterial("trick_barrier");
    public static final DeferredHolder<Material, Material> TRICK_INTERACT_BLOCK =
            registerPieceMaterial("trick_interact_block");
    public static final DeferredHolder<Material, Material> TRICK_FREEZE_BLOCK =
            registerPieceMaterial("trick_freeze_block");
    public static final DeferredHolder<Material, Material> TRICK_MELT_BLOCK =
            registerPieceMaterial("trick_melt_block");
    public static final DeferredHolder<Material, Material> TRICK_STORE_ENTITY =
            registerPieceMaterial("trick_store_entity");
    public static final DeferredHolder<Material, Material> SELECTOR_STORED_ENTITY =
            registerPieceMaterial("selector_stored_entity");
    public static final DeferredHolder<Material, Material> SELECTOR_NEARBY_SPELLGRAM =
            registerPieceMaterial("selector_nearby_spellgram");
    public static final DeferredHolder<Material, Material> TRICK_DISPEL =
            registerPieceMaterial("trick_dispel");
    public static final DeferredHolder<Material, Material> TRICK_DISPEL_BENEFICIAL =
            registerPieceMaterial("trick_dispel_beneficial");
    public static final DeferredHolder<Material, Material> TRICK_DISPEL_NON_BENEFICIAL =
            registerPieceMaterial("trick_dispel_non_beneficial");
    public static final DeferredHolder<Material, Material> TRICK_COCYTUS =
            registerPieceMaterial("trick_cocytus");
    public static final DeferredHolder<Material, Material> TRICK_SUPPLY_FE =
            registerPieceMaterial("trick_supply_fe");
    public static final DeferredHolder<Material, Material> TRICK_TIME_ACCELERATE =
            registerPieceMaterial("trick_time_accelerate");
    public static final DeferredHolder<Material, Material> TRICK_PHONON_MASER =
            registerPieceMaterial("trick_phonon_maser");
    public static final DeferredHolder<Material, Material> TRICK_METEOR_LINE =
            registerPieceMaterial("trick_meteor_line");
    public static final DeferredHolder<Material, Material> TRICK_SUPREME_INFUSION =
            registerPieceMaterial("trick_supreme_infusion");
    public static final DeferredHolder<Material, Material> TRICK_MOLECULAR_DIVIDER =
            registerPieceMaterial("trick_molecular_divider");
    public static final DeferredHolder<Material, Material> TRICK_BREAK_FORTUNE =
            registerPieceMaterial("trick_break_fortune");
    public static final DeferredHolder<Material, Material> TRICK_BREAK_SILK =
            registerPieceMaterial("trick_break_silk");
    public static final DeferredHolder<Material, Material> TRICK_AQUA_CUTTER =
            registerPieceMaterial("trick_aqua_cutter");
    public static final DeferredHolder<Material, Material> TRICK_BLAZE_BALL =
            registerPieceMaterial("trick_blaze_ball");
    public static final DeferredHolder<Material, Material> TRICK_ACTIVE_AIR_MINE =
            registerPieceMaterial("trick_active_air_mine");
    public static final DeferredHolder<Material, Material> TRICK_FLARE_CIRCLE =
            registerPieceMaterial("trick_flare_circle");
    public static final DeferredHolder<Material, Material> TRICK_ICE_CIRCLE =
            registerPieceMaterial("trick_ice_circle");
    public static final DeferredHolder<Material, Material> TRICK_SET_SPELLGRAM_FOLLOW_TARGET =
            registerPieceMaterial("trick_set_spellgram_follow_target");
    public static final DeferredHolder<Material, Material> TRICK_DIE_FLEX =
            registerPieceMaterial("trick_die_flex");
    public static final DeferredHolder<Material, Material> TRICK_JUMP =
            registerPieceMaterial("trick_jump");
    public static final DeferredHolder<Material, Material> TRICK_SWITCH =
            registerPieceMaterial("trick_switch");
    public static final DeferredHolder<Material, Material> JUMP_ANCHOR =
            registerPieceMaterial("jump_anchor");
    public static final DeferredHolder<Material, Material> TRICK_RADIATION_INJECTION =
            registerPieceMaterial("trick_radiation_injection");
    public static final DeferredHolder<Material, Material> TRICK_RADIATION_FILTER =
            registerPieceMaterial("trick_radiation_filter");
    public static final DeferredHolder<Material, Material> TRICK_CURE_RADIATION =
            registerPieceMaterial("trick_cure_radiation");
    public static final DeferredHolder<Material, Material> TRICK_GUILLOTINE =
            registerPieceMaterial("trick_guillotine");
    public static final DeferredHolder<Material, Material> TRICK_MATERIAL_MUTATION =
            registerPieceMaterial("trick_material_mutation");
    static {
        if (SablePhysicsCompat.isLoaded()) {
            registerPieceMaterial("trick_physical_propulsion");
        }
    }
    public static final DeferredHolder<Material, Material> OPERATOR_TAN =
            registerPieceMaterial("operator_tan");
    public static final DeferredHolder<Material, Material> OPERATOR_ATAN =
            registerPieceMaterial("operator_atan");
    public static final DeferredHolder<Material, Material> OPERATOR_SINH =
            registerPieceMaterial("operator_sinh");
    public static final DeferredHolder<Material, Material> OPERATOR_COSH =
            registerPieceMaterial("operator_cosh");
    public static final DeferredHolder<Material, Material> OPERATOR_TANH =
            registerPieceMaterial("operator_tanh");
    public static final DeferredHolder<Material, Material> OPERATOR_GREATER_THAN =
            registerPieceMaterial("operator_greater_than");
    public static final DeferredHolder<Material, Material> OPERATOR_GREATER_THAN_OR_EQUAL =
            registerPieceMaterial("operator_greater_than_or_equal");
    public static final DeferredHolder<Material, Material> OPERATOR_EQUAL =
            registerPieceMaterial("operator_equal");
    public static final DeferredHolder<Material, Material> CONSTANT_STRING =
            registerPieceMaterial("constant_string");
    public static final DeferredHolder<Material, Material> OPERATOR_FROM_STRING =
            registerPieceMaterial("operator_from_string", "operator_from_string");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_FROM_STRING_LIST =
            registerPieceMaterial("operator_list_from_string_list", "operator_list_from_string_list");
    public static final DeferredHolder<Material, Material> OPERATOR_TO_STRING =
            registerPieceMaterial("operator_to_string", "operator_to_string");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_TO_STRING_LIST =
            registerPieceMaterial("operator_list_to_string_list", "operator_list_to_string_list");
    public static final DeferredHolder<Material, Material> SELECTOR_BLOCK =
            registerPieceMaterial("selector_block");
    public static final DeferredHolder<Material, Material> OPERATOR_BLOCK_ID =
            registerPieceMaterial("operator_block_id");
    public static final DeferredHolder<Material, Material> OPERATOR_BLOCK_HAS_TAG =
            registerPieceMaterial("operator_block_has_tag");
    public static final DeferredHolder<Material, Material> OPERATOR_BLOCK_POSITION =
            registerPieceMaterial("operator_block_position");
    public static final DeferredHolder<Material, Material> SELECTOR_ONLINE_PLAYERS =
            registerPieceMaterial("selector_online_players");
    public static final DeferredHolder<Material, Material> SELECTOR_HELD_ITEM =
            registerPieceMaterial("selector_held_item");
    public static final DeferredHolder<Material, Material> SELECTOR_SELECTED_SLOT_ITEM =
            registerPieceMaterial("selector_selected_slot_item");
    public static final DeferredHolder<Material, Material> SELECTOR_HELD_ITEMS =
            registerPieceMaterial("selector_held_items");
    public static final DeferredHolder<Material, Material> SELECTOR_INTERNAL_ITEMS =
            registerPieceMaterial("selector_internal_items");
    public static final DeferredHolder<Material, Material> SELECTOR_INDEXED_ELEMENT =
            registerPieceMaterial("selector_indexed_element", "psi", "operator_list_index");
    public static final DeferredHolder<Material, Material> SELECTOR_NBT =
            registerPieceMaterial("selector_nbt");
    public static final DeferredHolder<Material, Material> SELECTOR_NBT_KEYS =
            registerPieceMaterial("selector_nbt_keys");
    public static final DeferredHolder<Material, Material> SELECTOR_NBT_VALUE =
            registerPieceMaterial("selector_nbt_value");
    public static final DeferredHolder<Material, Material> OPERATOR_STRING_PARTIAL_MATCH =
            registerPieceMaterial("operator_string_partial_match");
    public static final DeferredHolder<Material, Material> OPERATOR_STRING_CONCAT =
            registerPieceMaterial("operator_string_concat");
    public static final DeferredHolder<Material, Material> OPERATOR_PLAYER_NAME =
            registerPieceMaterial("operator_player_name");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_SEARCH =
            registerPieceMaterial("operator_list_search");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_SEARCH_EXCLUDE =
            registerPieceMaterial("operator_list_search_exclude");
    public static final DeferredHolder<Material, Material> OPERATOR_RANDOM_ELEMENT =
            registerPieceMaterial("operator_random_element", "operator_random_string");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_ADD =
            registerPieceMaterial("operator_list_add");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_REMOVE =
            registerPieceMaterial("operator_list_remove");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_SIZE =
            registerPieceMaterial("operator_list_size");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_EXCLUSION =
            registerPieceMaterial("operator_list_exclusion");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_INTERSECTION =
            registerPieceMaterial("operator_list_intersection");
    public static final DeferredHolder<Material, Material> OPERATOR_LIST_CONCATENATION =
            registerPieceMaterial("operator_list_concatenation");
    public static final DeferredHolder<Material, Material> MODE_STRING =
            registerPieceMaterial("mode/string");
    public static final DeferredHolder<Material, Material> MODE_NUMBER =
            registerPieceMaterial("mode/number");
    public static final DeferredHolder<Material, Material> MODE_VECTOR =
            registerPieceMaterial("mode/vector");
    public static final DeferredHolder<Material, Material> MODE_ENTITY =
            registerPieceMaterial("mode/entity");
    public static final DeferredHolder<Material, Material> MODE_ITEM =
            registerPieceMaterial("mode/item");
    public static final DeferredHolder<Material, Material> MODE_BLOCK =
            registerPieceMaterial("mode/block");

    private PsitweaksClientSpells() {
    }

    public static void register(IEventBus eventBus) {
        SPELL_PIECE_MATERIALS.register(eventBus);
    }

    private static DeferredHolder<Material, Material> registerPieceMaterial(String id) {
        return registerPieceMaterial(id, id);
    }

    private static DeferredHolder<Material, Material> registerPieceMaterial(String id, String textureId) {
        return SPELL_PIECE_MATERIALS.register(id,
                () -> new Material(InventoryMenu.BLOCK_ATLAS, Psitweaks.location("spell/" + textureId)));
    }

    private static DeferredHolder<Material, Material> registerPieceMaterial(String id, String textureNamespace,
            String textureId) {
        return SPELL_PIECE_MATERIALS.register(id,
                () -> new Material(InventoryMenu.BLOCK_ATLAS,
                        ResourceLocation.fromNamespaceAndPath(textureNamespace, "spell/" + textureId)));
    }
}
