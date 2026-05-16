package com.moratan251.psitweaks.client.spells;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.SablePhysicsCompat;
import net.minecraft.client.resources.model.Material;
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
    public static final DeferredHolder<Material, Material> CONSTANT_STRING =
            registerPieceMaterial("constant_string");
    public static final DeferredHolder<Material, Material> OPERATOR_STRING_TO_NUMBER =
            registerPieceMaterial("operator_string_to_number");
    public static final DeferredHolder<Material, Material> OPERATOR_NUMBER_TO_STRING =
            registerPieceMaterial("operator_number_to_string");
    public static final DeferredHolder<Material, Material> SELECTOR_ENTITY_TYPE_ID =
            registerPieceMaterial("selector_entity_type_id");
    public static final DeferredHolder<Material, Material> SELECTOR_BLOCK_ID =
            registerPieceMaterial("selector_block_id");
    public static final DeferredHolder<Material, Material> OPERATOR_STRING_EQUALS =
            registerPieceMaterial("operator_string_equals");

    private PsitweaksClientSpells() {
    }

    public static void register(IEventBus eventBus) {
        SPELL_PIECE_MATERIALS.register(eventBus);
    }

    private static DeferredHolder<Material, Material> registerPieceMaterial(String id) {
        return SPELL_PIECE_MATERIALS.register(id,
                () -> new Material(InventoryMenu.BLOCK_ATLAS, Psitweaks.location("spell/" + id)));
    }
}
