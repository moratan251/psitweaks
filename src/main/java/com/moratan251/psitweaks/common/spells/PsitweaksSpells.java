package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.SablePhysicsCompat;
import com.moratan251.psitweaks.common.spells.spellpiece.constant.*;
import com.moratan251.psitweaks.common.spells.spellpiece.etc.*;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.*;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.*;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellPieceType;

public final class PsitweaksSpells {
    public static final DeferredRegister<SpellPieceType> SPELL_PIECES =
            DeferredRegister.create(PsiAPI.SPELL_PIECE_REGISTRY_TYPE_KEY, Psitweaks.MOD_ID);

    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_EXPLODE_NO_DESTROY =
            registerPiece("trick_explode_no_destroy", PieceTrickExplodeNoDestroy.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_PARADE =
            registerPiece("trick_parade", PieceTrickParade.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_FLIGHT =
            registerPiece("trick_flight", PieceTrickFlight.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_HARDENING =
            registerPiece("trick_hardening", PieceTrickHardening.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_BARRIER =
            registerPiece("trick_barrier", PieceTrickBarrier.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_INTERACT_BLOCK =
            registerPiece("trick_interact_block", PieceTrickInteractBlock.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_FREEZE_BLOCK =
            registerPiece("trick_freeze_block", PieceTrickFreezeBlock.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_MELT_BLOCK =
            registerPiece("trick_melt_block", PieceTrickMeltBlock.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_PULSAR =
            registerPiece("trick_pulsar", PieceTrickConjurePulsar.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_PULSAR_SEQUENCE =
            registerPiece("trick_pulsar_sequence", PieceTrickConjurePulsarSequence.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_PULSAR_LIGHT =
            registerPiece("trick_pulsar_light", PieceTrickConjurePulsarLight.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_STORE_ENTITY =
            registerPiece("trick_store_entity", PieceTrickStoreEntityUUID.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_STORED_ENTITY =
            registerPiece("selector_stored_entity", PieceSelectorStoredEntity.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_STORE_VALUE =
            registerPiece("trick_store_value", PieceTrickStoreValue.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_STORED_VALUE =
            registerPiece("selector_stored_value", PieceSelectorStoredValue.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_NEARBY_SPELLGRAM =
            registerPiece("selector_nearby_spellgram", PieceSelectorNearbySpellGram.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_DISPEL =
            registerPiece("trick_dispel", PieceTrickDispel.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_DISPEL_BENEFICIAL =
            registerPiece("trick_dispel_beneficial", PieceTrickDispelBeneficial.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_DISPEL_NON_BENEFICIAL =
            registerPiece("trick_dispel_non_beneficial", PieceTrickDispelNonBeneficial.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_COCYTUS =
            registerPiece("trick_cocytus", PieceTrickCocytus.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_SUPPLY_FE =
            registerPiece("trick_supply_fe", PieceTrickSupplyFE.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_TIME_ACCELERATE =
            registerPiece("trick_time_accelerate", PieceTrickTimeAccelerate.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_PHONON_MASER =
            registerPiece("trick_phonon_maser", PieceTrickPhononMaser.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_METEOR_LINE =
            registerPiece("trick_meteor_line", PieceTrickMeteorLine.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_SUPREME_INFUSION =
            registerPiece("trick_supreme_infusion", PieceTrickSupremeInfusion.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_MOLECULAR_DIVIDER =
            registerPiece("trick_molecular_divider", PieceTrickMolecularDivider.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_BREAK_FORTUNE =
            registerPiece("trick_break_fortune", PieceTrickBreakBlockFortune.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_BREAK_SILK =
            registerPiece("trick_break_silk", PieceTrickBreakBlockSilk.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_AQUA_CUTTER =
            registerPiece("trick_aqua_cutter", PieceTrickAquaCutter.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_DRY_METEOR =
            registerPiece("trick_dry_meteor", PieceTrickDryMeteor.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_BLAZE_BALL =
            registerPiece("trick_blaze_ball", PieceTrickBlazeBall.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_ACTIVE_AIR_MINE =
            registerPiece("trick_active_air_mine", PieceTrickActiveAirMine.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_FLARE_CIRCLE =
            registerPiece("trick_flare_circle", PieceTrickFlareCircle.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_ICE_CIRCLE =
            registerPiece("trick_ice_circle", PieceTrickIceCircle.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_SET_SPELLGRAM_FOLLOW_TARGET =
            registerPiece("trick_set_spellgram_follow_target", PieceTrickSetSpellGramFollowTarget.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_DIE_FLEX =
            registerPiece("trick_die_flex", PieceTrickDieFlex.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_JUMP =
            registerPiece("trick_jump", PieceTrickJump.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_JUMP_FLEX =
            registerPiece("trick_jump_flex", PieceTrickJumpFlex.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_SWITCH =
            registerPiece("trick_switch", PieceTrickSwitch.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_SWITCH_FLEX =
            registerPiece("trick_switch_flex", PieceTrickSwitchFlex.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> JUMP_ANCHOR =
            registerPiece("jump_anchor", PieceJumpAnchor.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SAFETY =
            registerPiece("safety", PieceSafety.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_RADIATION_INJECTION =
            MekanismCompat.isMekanismLoaded() ? registerPiece("trick_radiation_injection", PieceTrickRadiationInjection.class) : null;
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_RADIATION_FILTER =
            MekanismCompat.isMekanismLoaded() ? registerPiece("trick_radiation_filter", PieceTrickRadiationFilter.class) : null;
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_CURE_RADIATION =
            MekanismCompat.isMekanismLoaded() ? registerPiece("trick_cure_radiation", PieceTrickCureRadiation.class) : null;
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_GUILLOTINE =
            registerPiece("trick_guillotine", PieceTrickGuillotine.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_MATERIAL_MUTATION =
            registerPiece("trick_material_mutation", PieceTrickMaterialMutation.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_PULL_ITEM =
            registerPiece("trick_pull_item", PieceTrickPullItem.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_SEND_ITEM =
            registerPiece("trick_send_item", PieceTrickSendItem.class);
    static {
        if (SablePhysicsCompat.isLoaded()) {
            registerPiece("trick_physical_propulsion", PieceTrickPhysicalPropulsion.class);
        }
    }
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_TAN =
            registerPiece("operator_tan", PieceOperatorTan.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_ATAN =
            registerPiece("operator_atan", PieceOperatorAtan.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_SINH =
            registerPiece("operator_sinh", PieceOperatorSinh.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_COSH =
            registerPiece("operator_cosh", PieceOperatorCosh.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_TANH =
            registerPiece("operator_tanh", PieceOperatorTanh.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_GREATER_THAN =
            registerPiece("operator_greater_than", PieceOperatorGreaterThan.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_GREATER_THAN_OR_EQUAL =
            registerPiece("operator_greater_than_or_equal", PieceOperatorGreaterThanOrEqual.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_EQUAL =
            registerPiece("operator_equal", PieceOperatorEqual.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> CONSTANT_STRING =
            registerPiece("constant_string", PieceConstantString.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_FORMAT_STRING =
            registerPiece("operator_format_string", PieceOperatorFormatString.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_FROM_STRING =
            registerPiece("operator_from_string", PieceOperatorFromString.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_FROM_STRING_LIST =
            registerPiece("operator_list_from_string_list", PieceOperatorListFromStringList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_NUMBER_LIST_TO_VECTOR =
            registerPiece("operator_number_list_to_vector", PieceOperatorNumberListToVector.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_VECTOR_TO_NUMBER_LIST =
            registerPiece("operator_vector_to_number_list", PieceOperatorVectorToNumberList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_TO_STRING =
            registerPiece("operator_to_string", PieceOperatorToString.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_TO_STRING_LIST =
            registerPiece("operator_list_to_string_list", PieceOperatorListToStringList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_GET_ID =
            registerPiece("operator_get_id", PieceOperatorGetId.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_GET_ID_LIST =
            registerPiece("operator_get_id_list", PieceOperatorGetIdList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_DISPLAY_NAME =
            registerPiece("selector_display_name", PieceSelectorDisplayName.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_DISPLAY_NAME_LIST =
            registerPiece("selector_display_name_list", PieceSelectorDisplayNameList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_EFFECT_LIST =
            registerPiece("selector_effect_list", PieceSelectorEffectList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_BLOCK =
            registerPiece("selector_block", PieceSelectorBlock.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_BLOCK_LIST =
            registerPiece("selector_block_list", PieceSelectorBlockList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_BLOCK_STATE =
            registerPiece("operator_block_state", PieceOperatorBlockState.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_BLOCK_STATE_VALUE =
            registerPiece("operator_block_state_value", PieceOperatorBlockStateValue.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_BLOCK_STATE_ENTRIES =
            registerPiece("operator_block_state_entries", PieceOperatorBlockStateEntries.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_TAG_LIST =
            registerPiece("operator_tag_list", PieceOperatorTagList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_BLOCK_POSITION =
            registerPiece("operator_block_position", PieceOperatorBlockPosition.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_BLOCK_POSITION_LIST =
            registerPiece("operator_block_position_list", PieceOperatorBlockPositionList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_ONLINE_PLAYERS =
            registerPiece("selector_online_players", PieceSelectorOnlinePlayers.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_HELD_ITEM =
            registerPiece("selector_held_item", PieceSelectorHeldItem.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_SELECTED_SLOT_ITEM =
            registerPiece("selector_selected_slot_item", PieceSelectorSelectedSlotItem.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_ENTITY_SLOT_ITEM =
            registerPiece("selector_entity_slot_item", PieceSelectorEntitySlotItem.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_INTERNAL_SLOT_ITEM =
            registerPiece("selector_internal_slot_item", PieceSelectorInternalSlotItem.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_ITEM_COUNT =
            registerPiece("operator_item_count", PieceOperatorItemCount.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_ITEM_SLOT =
            registerPiece("operator_item_slot", PieceOperatorItemSlot.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_ITEM_TOTAL_COUNT =
            registerPiece("operator_item_total_count", PieceOperatorItemTotalCount.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_HELD_ITEMS =
            registerPiece("selector_held_items", PieceSelectorHeldItems.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_INTERNAL_ITEMS =
            registerPiece("selector_internal_items", PieceSelectorInternalItems.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_INDEXED_ELEMENT =
            registerPiece("selector_indexed_element", PieceSelectorIndexedElement.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_NBT =
            registerPiece("selector_nbt", PieceSelectorNbt.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_NBT_KEYS =
            registerPiece("selector_nbt_keys", PieceSelectorNbtKeys.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> SELECTOR_NBT_VALUE =
            registerPiece("selector_nbt_value", PieceSelectorNbtValue.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_PARTIAL_MATCH =
            registerPiece("operator_string_partial_match", PieceOperatorStringPartialMatch.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_STARTS_WITH =
            registerPiece("operator_string_starts_with", PieceOperatorStringStartsWith.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_ENDS_WITH =
            registerPiece("operator_string_ends_with", PieceOperatorStringEndsWith.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_CONCAT =
            registerPiece("operator_string_concat", PieceOperatorStringConcat.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_SPLIT =
            registerPiece("operator_string_split", PieceOperatorStringSplit.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_SLICE =
            registerPiece("operator_string_slice", PieceOperatorStringSlice.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_LENGTH =
            registerPiece("operator_string_length", PieceOperatorStringLength.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_REPLACE =
            registerPiece("operator_string_replace", PieceOperatorStringReplace.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_TRIM =
            registerPiece("operator_string_trim", PieceOperatorStringTrim.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRING_LIST_JOIN =
            registerPiece("operator_string_list_join", PieceOperatorStringListJoin.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_PLAYER_NAME =
            registerPiece("operator_player_name", PieceOperatorPlayerName.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_SEARCH =
            registerPiece("operator_list_search", PieceOperatorListSearch.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_SEARCH_EXCLUDE =
            registerPiece("operator_list_search_exclude", PieceOperatorListSearchExclude.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_RANDOM_ELEMENT =
            registerPiece("operator_random_element", PieceOperatorRandomElement.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_ADD =
            registerPiece("operator_list_add", PieceOperatorListAdd.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_REMOVE =
            registerPiece("operator_list_remove", PieceOperatorListRemove.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_REMOVE_INDICES =
            registerPiece("operator_list_remove_indices", PieceOperatorListRemoveIndices.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_INSERT =
            registerPiece("operator_list_insert", PieceOperatorListInsert.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_SLICE =
            registerPiece("operator_list_slice", PieceOperatorListSlice.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_SIZE =
            registerPiece("operator_list_size", PieceOperatorListSize.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_EXCLUSION =
            registerPiece("operator_list_exclusion", PieceOperatorListExclusion.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_INTERSECTION =
            registerPiece("operator_list_intersection", PieceOperatorListIntersection.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_LIST_CONCATENATION =
            registerPiece("operator_list_concatenation", PieceOperatorListConcatenation.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_ADD =
            registerPiece("operator_matrix_add", PieceOperatorMatrixAdd.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_SUBTRACT =
            registerPiece("operator_matrix_subtract", PieceOperatorMatrixSubtract.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_MULTIPLY =
            registerPiece("operator_matrix_multiply", PieceOperatorMatrixMultiply.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_SCALAR_MULTIPLY =
            registerPiece("operator_matrix_scalar_multiply", PieceOperatorMatrixScalarMultiply.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_TRANSPOSE =
            registerPiece("operator_matrix_transpose", PieceOperatorMatrixTranspose.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_DETERMINANT =
            registerPiece("operator_matrix_determinant", PieceOperatorMatrixDeterminant.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_INVERSE =
            registerPiece("operator_matrix_inverse", PieceOperatorMatrixInverse.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_EXTRACT_ROW =
            registerPiece("operator_matrix_extract_row", PieceOperatorMatrixExtractRow.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_EXTRACT_COLUMN =
            registerPiece("operator_matrix_extract_column", PieceOperatorMatrixExtractColumn.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_ELEMENT =
            registerPiece("operator_matrix_element", PieceOperatorMatrixElement.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_ROW_COUNT =
            registerPiece("operator_matrix_row_count", PieceOperatorMatrixRowCount.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_COLUMN_COUNT =
            registerPiece("operator_matrix_column_count", PieceOperatorMatrixColumnCount.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_MULTIPLY_VECTOR =
            registerPiece("operator_matrix_multiply_vector", PieceOperatorMatrixMultiplyVector.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_COLUMN_FROM_LIST =
            registerPiece("operator_matrix_column_from_list", PieceOperatorMatrixColumnFromList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_FLATTEN =
            registerPiece("operator_matrix_flatten", PieceOperatorMatrixFlatten.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_IDENTITY =
            registerPiece("operator_matrix_identity", PieceOperatorMatrixIdentity.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_ZERO =
            registerPiece("operator_matrix_zero", PieceOperatorMatrixZero.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_DIAGONAL =
            registerPiece("operator_matrix_diagonal", PieceOperatorMatrixDiagonal.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_REPLACE_COLUMN =
            registerPiece("operator_matrix_replace_column", PieceOperatorMatrixReplaceColumn.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_REPLACE_ROW =
            registerPiece("operator_matrix_replace_row", PieceOperatorMatrixReplaceRow.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_TRANSFORM_VECTOR =
            registerPiece("operator_matrix_transform_vector", PieceOperatorMatrixTransformVector.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_LINEAR_PART =
            registerPiece("operator_matrix_linear_part", PieceOperatorMatrixLinearPart.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_REPLACE_ELEMENT =
            registerPiece("operator_matrix_replace_element", PieceOperatorMatrixReplaceElement.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_DELETE_ROW =
            registerPiece("operator_matrix_delete_row", PieceOperatorMatrixDeleteRow.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_DELETE_COLUMN =
            registerPiece("operator_matrix_delete_column", PieceOperatorMatrixDeleteColumn.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_MATRIX_CUBOID_REGION =
            registerPiece("operator_matrix_cuboid_region", PieceOperatorMatrixCuboidRegion.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_REGION_VECTOR_LIST =
            registerPiece("operator_region_vector_list", PieceOperatorRegionVectorList.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_INSIDE_REGION =
            registerPiece("operator_inside_region", PieceOperatorInsideRegion.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_OUTSIDE_REGION =
            registerPiece("operator_outside_region", PieceOperatorOutsideRegion.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_ALIVE =
            registerPiece("operator_alive", PieceOperatorAlive.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_WEAK_RAYCAST =
            registerPiece("operator_weak_raycast", PieceOperatorWeakRaycast.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_WEAK_RAYCAST_AXIS =
            registerPiece("operator_weak_raycast_axis", PieceOperatorWeakRaycastAxis.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRONG_RAYCAST =
            registerPiece("operator_strong_raycast", PieceOperatorStrongRaycast.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> OPERATOR_STRONG_RAYCAST_AXIS =
            registerPiece("operator_strong_raycast_axis", PieceOperatorStrongRaycastAxis.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_RAYCAST =
            registerPiece("macro_caster_raycast", PieceMacroCasterRaycast.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_RAYCAST_AXIS =
            registerPiece("macro_caster_raycast_axis", PieceMacroCasterAxisRaycast.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_STRONG_RAYCAST =
            registerPiece("macro_caster_strong_raycast", PieceMacroCasterStrongRaycast.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_STRONG_RAYCAST_AXIS =
            registerPiece("macro_caster_strong_raycast_axis", PieceMacroCasterStrongAxisRaycast.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_WEAK_RAYCAST =
            registerPiece("macro_caster_weak_raycast", PieceMacroCasterWeakRaycast.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_WEAK_RAYCAST_AXIS =
            registerPiece("macro_caster_weak_raycast_axis", PieceMacroCasterWeakAxisRaycast.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_AXIAL_OFFSET =
            registerPiece("macro_caster_axial_offset", PieceMacroCasterAxialOffset.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_AXIAL_ROTATION =
            registerPiece("macro_caster_axial_rotation", PieceMacroCasterAxialRotation.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_AXIAL_OFFSET_3D =
            registerPiece("macro_caster_axial_offset_3d", PieceMacroCasterAxialOffset3D.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_CASTER_AXIAL_ROTATION_3D =
            registerPiece("macro_caster_axial_rotation_3d", PieceMacroCasterAxialRotation3D.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_FACE_AXIAL_OFFSET =
            registerPiece("macro_face_axial_offset", PieceMacroFaceAxialOffset.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> MACRO_FACE_AXIAL_ROTATION =
            registerPiece("macro_face_axial_rotation", PieceMacroFaceAxialRotation.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_MASS_BLOCK_BREAK =
            registerPiece("trick_mass_block_break", PieceTrickMassBlockBreak.class);
    public static final DeferredHolder<SpellPieceType, SpellPieceType> TRICK_IDEA_STORAGE_VIEW =
            registerPiece("trick_idea_storage_view", PieceTrickIdeaStorageView.class);

    private PsitweaksSpells() {
    }

    private static <T extends SpellPiece> DeferredHolder<SpellPieceType, SpellPieceType> registerPiece(String id, Class<T> pieceClass) {
        return SPELL_PIECES.register(id, () -> SpellPieceType.ofClass(pieceClass));
    }

    public static void register(IEventBus eventBus) {
        SPELL_PIECES.register(eventBus);
    }
}
