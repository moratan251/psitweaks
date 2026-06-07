package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.SablePhysicsCompat;
import com.moratan251.psitweaks.common.spells.spellpiece.constant.*;
import com.moratan251.psitweaks.common.spells.spellpiece.etc.*;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.*;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.*;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.*;
import java.util.Collection;
import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.SpellPiece;

public final class PsitweaksSpells {
    public static final DeferredRegister<Class<? extends SpellPiece>> SPELL_PIECES =
            DeferredRegister.create(PsiAPI.SPELL_PIECE_REGISTRY_TYPE_KEY, Psitweaks.MOD_ID);
    public static final DeferredRegister<Collection<Class<? extends SpellPiece>>> ADVANCEMENT_GROUPS =
            DeferredRegister.create(PsiAPI.ADVANCEMENT_GROUP_REGISTRY_KEY, Psitweaks.MOD_ID);

    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickExplodeNoDestroy>> TRICK_EXPLODE_NO_DESTROY =
            registerPiece("trick_explode_no_destroy", PieceTrickExplodeNoDestroy.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickParade>> TRICK_PARADE =
            registerPiece("trick_parade", PieceTrickParade.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickFlight>> TRICK_FLIGHT =
            registerPiece("trick_flight", PieceTrickFlight.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickHardening>> TRICK_HARDENING =
            registerPiece("trick_hardening", PieceTrickHardening.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickBarrier>> TRICK_BARRIER =
            registerPiece("trick_barrier", PieceTrickBarrier.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickInteractBlock>> TRICK_INTERACT_BLOCK =
            registerPiece("trick_interact_block", PieceTrickInteractBlock.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickFreezeBlock>> TRICK_FREEZE_BLOCK =
            registerPiece("trick_freeze_block", PieceTrickFreezeBlock.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickMeltBlock>> TRICK_MELT_BLOCK =
            registerPiece("trick_melt_block", PieceTrickMeltBlock.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickStoreEntityUUID>> TRICK_STORE_ENTITY =
            registerPiece("trick_store_entity", PieceTrickStoreEntityUUID.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorStoredEntity>> SELECTOR_STORED_ENTITY =
            registerPiece("selector_stored_entity", PieceSelectorStoredEntity.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickStoreValue>> TRICK_STORE_VALUE =
            registerPiece("trick_store_value", PieceTrickStoreValue.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorStoredValue>> SELECTOR_STORED_VALUE =
            registerPiece("selector_stored_value", PieceSelectorStoredValue.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorNearbySpellGram>> SELECTOR_NEARBY_SPELLGRAM =
            registerPiece("selector_nearby_spellgram", PieceSelectorNearbySpellGram.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickDispel>> TRICK_DISPEL =
            registerPiece("trick_dispel", PieceTrickDispel.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickDispelBeneficial>> TRICK_DISPEL_BENEFICIAL =
            registerPiece("trick_dispel_beneficial", PieceTrickDispelBeneficial.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickDispelNonBeneficial>> TRICK_DISPEL_NON_BENEFICIAL =
            registerPiece("trick_dispel_non_beneficial", PieceTrickDispelNonBeneficial.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickCocytus>> TRICK_COCYTUS =
            registerPiece("trick_cocytus", PieceTrickCocytus.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickSupplyFE>> TRICK_SUPPLY_FE =
            registerPiece("trick_supply_fe", PieceTrickSupplyFE.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickTimeAccelerate>> TRICK_TIME_ACCELERATE =
            registerPiece("trick_time_accelerate", PieceTrickTimeAccelerate.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickPhononMaser>> TRICK_PHONON_MASER =
            registerPiece("trick_phonon_maser", PieceTrickPhononMaser.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickMeteorLine>> TRICK_METEOR_LINE =
            registerPiece("trick_meteor_line", PieceTrickMeteorLine.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickSupremeInfusion>> TRICK_SUPREME_INFUSION =
            registerPiece("trick_supreme_infusion", PieceTrickSupremeInfusion.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickMolecularDivider>> TRICK_MOLECULAR_DIVIDER =
            registerPiece("trick_molecular_divider", PieceTrickMolecularDivider.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickBreakBlockFortune>> TRICK_BREAK_FORTUNE =
            registerPiece("trick_break_fortune", PieceTrickBreakBlockFortune.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickBreakBlockSilk>> TRICK_BREAK_SILK =
            registerPiece("trick_break_silk", PieceTrickBreakBlockSilk.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickAquaCutter>> TRICK_AQUA_CUTTER =
            registerPiece("trick_aqua_cutter", PieceTrickAquaCutter.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickBlazeBall>> TRICK_BLAZE_BALL =
            registerPiece("trick_blaze_ball", PieceTrickBlazeBall.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickActiveAirMine>> TRICK_ACTIVE_AIR_MINE =
            registerPiece("trick_active_air_mine", PieceTrickActiveAirMine.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickFlareCircle>> TRICK_FLARE_CIRCLE =
            registerPiece("trick_flare_circle", PieceTrickFlareCircle.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickIceCircle>> TRICK_ICE_CIRCLE =
            registerPiece("trick_ice_circle", PieceTrickIceCircle.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickSetSpellGramFollowTarget>> TRICK_SET_SPELLGRAM_FOLLOW_TARGET =
            registerPiece("trick_set_spellgram_follow_target", PieceTrickSetSpellGramFollowTarget.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickDieFlex>> TRICK_DIE_FLEX =
            registerPiece("trick_die_flex", PieceTrickDieFlex.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickJump>> TRICK_JUMP =
            registerPiece("trick_jump", PieceTrickJump.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickSwitch>> TRICK_SWITCH =
            registerPiece("trick_switch", PieceTrickSwitch.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceJumpAnchor>> JUMP_ANCHOR =
            registerPiece("jump_anchor", PieceJumpAnchor.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickRadiationInjection>> TRICK_RADIATION_INJECTION =
            registerPiece("trick_radiation_injection", PieceTrickRadiationInjection.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickRadiationFilter>> TRICK_RADIATION_FILTER =
            registerPiece("trick_radiation_filter", PieceTrickRadiationFilter.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickCureRadiation>> TRICK_CURE_RADIATION =
            registerPiece("trick_cure_radiation", PieceTrickCureRadiation.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickGuillotine>> TRICK_GUILLOTINE =
            registerPiece("trick_guillotine", PieceTrickGuillotine.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceTrickMaterialMutation>> TRICK_MATERIAL_MUTATION =
            registerPiece("trick_material_mutation", PieceTrickMaterialMutation.class);
    static {
        if (SablePhysicsCompat.isLoaded()) {
            registerPiece("trick_physical_propulsion", PieceTrickPhysicalPropulsion.class);
        }
    }
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorTan>> OPERATOR_TAN =
            registerPiece("operator_tan", PieceOperatorTan.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorAtan>> OPERATOR_ATAN =
            registerPiece("operator_atan", PieceOperatorAtan.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorSinh>> OPERATOR_SINH =
            registerPiece("operator_sinh", PieceOperatorSinh.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorCosh>> OPERATOR_COSH =
            registerPiece("operator_cosh", PieceOperatorCosh.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorTanh>> OPERATOR_TANH =
            registerPiece("operator_tanh", PieceOperatorTanh.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorGreaterThan>> OPERATOR_GREATER_THAN =
            registerPiece("operator_greater_than", PieceOperatorGreaterThan.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorGreaterThanOrEqual>> OPERATOR_GREATER_THAN_OR_EQUAL =
            registerPiece("operator_greater_than_or_equal", PieceOperatorGreaterThanOrEqual.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorEqual>> OPERATOR_EQUAL =
            registerPiece("operator_equal", PieceOperatorEqual.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceConstantString>> CONSTANT_STRING =
            registerPiece("constant_string", PieceConstantString.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorFromString>> OPERATOR_FROM_STRING =
            registerPiece("operator_from_string", PieceOperatorFromString.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListFromStringList>> OPERATOR_LIST_FROM_STRING_LIST =
            registerPiece("operator_list_from_string_list", PieceOperatorListFromStringList.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorToString>> OPERATOR_TO_STRING =
            registerPiece("operator_to_string", PieceOperatorToString.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListToStringList>> OPERATOR_LIST_TO_STRING_LIST =
            registerPiece("operator_list_to_string_list", PieceOperatorListToStringList.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorGetId>> OPERATOR_GET_ID =
            registerPiece("operator_get_id", PieceOperatorGetId.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorGetIdList>> OPERATOR_GET_ID_LIST =
            registerPiece("operator_get_id_list", PieceOperatorGetIdList.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorBlock>> SELECTOR_BLOCK =
            registerPiece("selector_block", PieceSelectorBlock.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorBlockState>> OPERATOR_BLOCK_STATE =
            registerPiece("operator_block_state", PieceOperatorBlockState.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorBlockStateValue>> OPERATOR_BLOCK_STATE_VALUE =
            registerPiece("operator_block_state_value", PieceOperatorBlockStateValue.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorBlockStateEntries>> OPERATOR_BLOCK_STATE_ENTRIES =
            registerPiece("operator_block_state_entries", PieceOperatorBlockStateEntries.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorTagList>> OPERATOR_TAG_LIST =
            registerPiece("operator_tag_list", PieceOperatorTagList.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorBlockPosition>> OPERATOR_BLOCK_POSITION =
            registerPiece("operator_block_position", PieceOperatorBlockPosition.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorOnlinePlayers>> SELECTOR_ONLINE_PLAYERS =
            registerPiece("selector_online_players", PieceSelectorOnlinePlayers.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorHeldItem>> SELECTOR_HELD_ITEM =
            registerPiece("selector_held_item", PieceSelectorHeldItem.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorSelectedSlotItem>> SELECTOR_SELECTED_SLOT_ITEM =
            registerPiece("selector_selected_slot_item", PieceSelectorSelectedSlotItem.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorEntitySlotItem>> SELECTOR_ENTITY_SLOT_ITEM =
            registerPiece("selector_entity_slot_item", PieceSelectorEntitySlotItem.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorInternalSlotItem>> SELECTOR_INTERNAL_SLOT_ITEM =
            registerPiece("selector_internal_slot_item", PieceSelectorInternalSlotItem.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorItemCount>> OPERATOR_ITEM_COUNT =
            registerPiece("operator_item_count", PieceOperatorItemCount.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorItemSlot>> OPERATOR_ITEM_SLOT =
            registerPiece("operator_item_slot", PieceOperatorItemSlot.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorItemTotalCount>> OPERATOR_ITEM_TOTAL_COUNT =
            registerPiece("operator_item_total_count", PieceOperatorItemTotalCount.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorHeldItems>> SELECTOR_HELD_ITEMS =
            registerPiece("selector_held_items", PieceSelectorHeldItems.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorInternalItems>> SELECTOR_INTERNAL_ITEMS =
            registerPiece("selector_internal_items", PieceSelectorInternalItems.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorIndexedElement>> SELECTOR_INDEXED_ELEMENT =
            registerPiece("selector_indexed_element", PieceSelectorIndexedElement.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorNbt>> SELECTOR_NBT =
            registerPiece("selector_nbt", PieceSelectorNbt.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorNbtKeys>> SELECTOR_NBT_KEYS =
            registerPiece("selector_nbt_keys", PieceSelectorNbtKeys.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceSelectorNbtValue>> SELECTOR_NBT_VALUE =
            registerPiece("selector_nbt_value", PieceSelectorNbtValue.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorStringPartialMatch>> OPERATOR_STRING_PARTIAL_MATCH =
            registerPiece("operator_string_partial_match", PieceOperatorStringPartialMatch.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorStringConcat>> OPERATOR_STRING_CONCAT =
            registerPiece("operator_string_concat", PieceOperatorStringConcat.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorStringSplit>> OPERATOR_STRING_SPLIT =
            registerPiece("operator_string_split", PieceOperatorStringSplit.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorStringListJoin>> OPERATOR_STRING_LIST_JOIN =
            registerPiece("operator_string_list_join", PieceOperatorStringListJoin.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorPlayerName>> OPERATOR_PLAYER_NAME =
            registerPiece("operator_player_name", PieceOperatorPlayerName.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListSearch>> OPERATOR_LIST_SEARCH =
            registerPiece("operator_list_search", PieceOperatorListSearch.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListSearchExclude>> OPERATOR_LIST_SEARCH_EXCLUDE =
            registerPiece("operator_list_search_exclude", PieceOperatorListSearchExclude.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorRandomElement>> OPERATOR_RANDOM_ELEMENT =
            registerPiece("operator_random_element", PieceOperatorRandomElement.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListAdd>> OPERATOR_LIST_ADD =
            registerPiece("operator_list_add", PieceOperatorListAdd.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListRemove>> OPERATOR_LIST_REMOVE =
            registerPiece("operator_list_remove", PieceOperatorListRemove.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListSize>> OPERATOR_LIST_SIZE =
            registerPiece("operator_list_size", PieceOperatorListSize.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListExclusion>> OPERATOR_LIST_EXCLUSION =
            registerPiece("operator_list_exclusion", PieceOperatorListExclusion.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListIntersection>> OPERATOR_LIST_INTERSECTION =
            registerPiece("operator_list_intersection", PieceOperatorListIntersection.class);
    public static final DeferredHolder<Class<? extends SpellPiece>, Class<PieceOperatorListConcatenation>> OPERATOR_LIST_CONCATENATION =
            registerPiece("operator_list_concatenation", PieceOperatorListConcatenation.class);

    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> EXPLODE_NO_DESTROY =
            registerGroup("trick_explode_no_destroy", PieceTrickExplodeNoDestroy.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> PARADE =
            registerGroup("trick_parade", PieceTrickParade.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> FLIGHT =
            registerGroup("trick_flight", PieceTrickFlight.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> HARDENING =
            registerGroup("trick_hardening", PieceTrickHardening.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> BARRIER =
            registerGroup("trick_barrier", PieceTrickBarrier.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> INTERACT_BLOCK =
            registerGroup("trick_interact_block", PieceTrickInteractBlock.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> FREEZE_BLOCK =
            registerGroup("trick_freeze_block", PieceTrickFreezeBlock.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> MELT_BLOCK =
            registerGroup("trick_melt_block", PieceTrickMeltBlock.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> STORE_ENTITY =
            registerGroup("trick_store_entity", PieceTrickStoreEntityUUID.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> STORED_ENTITY =
            registerGroup("selector_stored_entity", PieceSelectorStoredEntity.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> STORE_VALUE =
            registerGroup("trick_store_value", PieceTrickStoreValue.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> STORED_VALUE =
            registerGroup("selector_stored_value", PieceSelectorStoredValue.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> NEARBY_SPELLGRAM =
            registerGroup("selector_nearby_spellgram", PieceSelectorNearbySpellGram.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> DISPEL =
            registerGroup("trick_dispel", PieceTrickDispel.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> DISPEL_BENEFICIAL =
            registerGroup("trick_dispel_beneficial", PieceTrickDispelBeneficial.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> DISPEL_NON_BENEFICIAL =
            registerGroup("trick_dispel_non_beneficial", PieceTrickDispelNonBeneficial.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> COCYTUS =
            registerGroup("trick_cocytus", PieceTrickCocytus.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> SUPPLY_FE =
            registerGroup("trick_supply_fe", PieceTrickSupplyFE.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> TIME_ACCELERATE =
            registerGroup("trick_time_accelerate", PieceTrickTimeAccelerate.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> PHONON_MASER =
            registerGroup("trick_phonon_maser", PieceTrickPhononMaser.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> METEOR_LINE =
            registerGroup("trick_meteor_line", PieceTrickMeteorLine.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> SUPREME_INFUSION =
            registerGroup("trick_supreme_infusion", PieceTrickSupremeInfusion.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> MOLECULAR_DIVIDER =
            registerGroup("trick_molecular_divider", PieceTrickMolecularDivider.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> BREAK_FORTUNE =
            registerGroup("trick_break_fortune", PieceTrickBreakBlockFortune.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> BREAK_SILK =
            registerGroup("trick_break_silk", PieceTrickBreakBlockSilk.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> AQUA_CUTTER =
            registerGroup("trick_aqua_cutter", PieceTrickAquaCutter.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> BLAZE_BALL =
            registerGroup("trick_blaze_ball", PieceTrickBlazeBall.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> ACTIVE_AIR_MINE =
            registerGroup("trick_active_air_mine", PieceTrickActiveAirMine.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> FLARE_CIRCLE =
            registerGroup("trick_flare_circle", PieceTrickFlareCircle.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> ICE_CIRCLE =
            registerGroup("trick_ice_circle", PieceTrickIceCircle.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> SET_SPELLGRAM_FOLLOW_TARGET =
            registerGroup("trick_set_spellgram_follow_target", PieceTrickSetSpellGramFollowTarget.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> DIE_FLEX =
            registerGroup("trick_die_flex", PieceTrickDieFlex.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> JUMP =
            registerGroup("trick_jump", PieceTrickJump.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> SWITCH =
            registerGroup("trick_switch", PieceTrickSwitch.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> JUMP_ANCHOR_GROUP =
            registerGroup("jump_anchor", PieceJumpAnchor.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> RADIATION_INJECTION =
            registerGroup("trick_radiation_injection", PieceTrickRadiationInjection.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> RADIATION_FILTER =
            registerGroup("trick_radiation_filter", PieceTrickRadiationFilter.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> CURE_RADIATION =
            registerGroup("trick_cure_radiation", PieceTrickCureRadiation.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> GUILLOTINE =
            registerGroup("trick_guillotine", PieceTrickGuillotine.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> MATERIAL_MUTATION =
            registerGroup("trick_material_mutation", PieceTrickMaterialMutation.class);
    static {
        if (SablePhysicsCompat.isLoaded()) {
            registerGroup("trick_physical_propulsion", PieceTrickPhysicalPropulsion.class);
        }
    }
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> TAN =
            registerGroup("operator_tan", PieceOperatorTan.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> ATAN =
            registerGroup("operator_atan", PieceOperatorAtan.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> SINH =
            registerGroup("operator_sinh", PieceOperatorSinh.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> COSH =
            registerGroup("operator_cosh", PieceOperatorCosh.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> TANH =
            registerGroup("operator_tanh", PieceOperatorTanh.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> GREATER_THAN =
            registerGroup("operator_greater_than", PieceOperatorGreaterThan.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> GREATER_THAN_OR_EQUAL =
            registerGroup("operator_greater_than_or_equal", PieceOperatorGreaterThanOrEqual.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> EQUAL =
            registerGroup("operator_equal", PieceOperatorEqual.class);
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> STRING =
            ADVANCEMENT_GROUPS.register("string", () -> List.of(
                    PieceConstantString.class,
                    PieceOperatorFromString.class,
                    PieceOperatorListFromStringList.class,
                    PieceOperatorToString.class,
                    PieceOperatorListToStringList.class,
                    PieceOperatorGetId.class,
                    PieceOperatorGetIdList.class,
                    PieceSelectorOnlinePlayers.class,
                    PieceSelectorHeldItems.class,
                    PieceSelectorInternalItems.class,
                    PieceSelectorIndexedElement.class,
                    PieceSelectorNbt.class,
                    PieceSelectorNbtKeys.class,
                    PieceSelectorNbtValue.class,
                    PieceOperatorTagList.class,
                    PieceOperatorStringPartialMatch.class,
                    PieceOperatorStringConcat.class,
                    PieceOperatorStringSplit.class,
                    PieceOperatorStringListJoin.class,
                    PieceOperatorPlayerName.class,
                    PieceOperatorListSearch.class,
                    PieceOperatorListSearchExclude.class,
                    PieceOperatorRandomElement.class,
                    PieceOperatorListAdd.class,
                    PieceOperatorListRemove.class,
                    PieceOperatorListSize.class,
                    PieceOperatorListExclusion.class,
                    PieceOperatorListIntersection.class,
                    PieceOperatorListConcatenation.class
            ));
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> ITEM =
            ADVANCEMENT_GROUPS.register("item", () -> List.of(
                    PieceSelectorHeldItem.class,
                    PieceSelectorSelectedSlotItem.class,
                    PieceSelectorEntitySlotItem.class,
                    PieceSelectorInternalSlotItem.class,
                    PieceOperatorItemCount.class,
                    PieceOperatorItemSlot.class,
                    PieceOperatorItemTotalCount.class
            ));
    public static final DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> BLOCK =
            ADVANCEMENT_GROUPS.register("block", () -> List.of(
                    PieceSelectorBlock.class,
                    PieceOperatorBlockState.class,
                    PieceOperatorBlockStateValue.class,
                    PieceOperatorBlockStateEntries.class,
                    PieceOperatorBlockPosition.class
            ));

    private PsitweaksSpells() {
    }

    private static <T extends SpellPiece> DeferredHolder<Class<? extends SpellPiece>, Class<T>> registerPiece(String id, Class<T> pieceClass) {
        return SPELL_PIECES.register(id, () -> pieceClass);
    }

    private static DeferredHolder<Collection<Class<? extends SpellPiece>>, Collection<Class<? extends SpellPiece>>> registerGroup(
            String id, Class<? extends SpellPiece> pieceClass) {
        return ADVANCEMENT_GROUPS.register(id, () -> List.of(pieceClass));
    }

    public static void register(IEventBus eventBus) {
        SPELL_PIECES.register(eventBus);
        ADVANCEMENT_GROUPS.register(eventBus);
    }
}
