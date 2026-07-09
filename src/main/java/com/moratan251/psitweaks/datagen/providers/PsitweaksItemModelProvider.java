package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class PsitweaksItemModelProvider extends ItemModelProvider {
    private static final String[] GENERATED_ITEMS = {
            "advanced_spell_bullet_active",
            "advanced_spell_bullet_charge_active",
            "advanced_spell_bullet_circle_active",
            "advanced_spell_bullet_grenade_active",
            "advanced_spell_bullet_loop_active",
            "advanced_spell_bullet_mine_active",
            "advanced_spell_bullet_projectile_active",
            "alloy_hypostasis",
            "alloy_psion",
            "alloy_psionic_echo",
            "antinite_dust",
            "antinite_ingot",
            "auto_caster_custom_tick",
            "auto_caster_second",
            "auto_caster_tick",
            "awakened_spell_bullet_active",
            "awakened_spell_bullet_charge_active",
            "awakened_spell_bullet_circle_active",
            "awakened_spell_bullet_grenade_active",
            "awakened_spell_bullet_loop_active",
            "awakened_spell_bullet_mine_active",
            "awakened_spell_bullet_projectile_active",
            "cad_assembly_alloy_psion",
            "cad_assembly_chaotic_psimetal",
            "cad_assembly_flashmetal",
            "cad_assembly_heavy_psimetal_alpha",
            "cad_assembly_heavy_psimetal_beta",
            "cad_assembly_psycheonic_metal",
            "chaotic_factor",
            "chaotic_psimetal",
            "clump_antinite",
            "crystal_antinite",
            "curios_controller",
            "dirty_dust_antinite",
            "echo_control_circuit",
            "echo_pellet",
            "echo_sheet",
            "enriched_ebony",
            "enriched_echo",
            "enriched_hypostasis",
            "enriched_ivory",
            "enriched_psigem",
            "flash_charm",
            "flash_ring_active",
            "flashmetal",
            "heavy_psimetal",
            "heavy_psimetal_scrap",
            "hypostasis_control_circuit",
            "hypostasis_gem",
            "incomplete_heavy_psimetal_assembly",
            "jade",
            "magatama",
            "magicians_brain",
            "module_phenomenon_interference_enhancement_unit",
            "module_psyon_capacity_unit",
            "module_psyon_supplying_unit",
            "moval_suit_boots",
            "moval_suit_chestplate",
            "moval_suit_helmet",
            "moval_suit_leggings",
            "moval_suit_leggings_ivory",
            "pellet_americium",
            "pellet_neptunium",
            "philosophers_stone",
            "portable_cad_assembler",
            "program_active_air_mine",
            "program_blank",
            "program_cocytus",
            "program_cure_radiation",
            "program_die_flex",
            "program_flight",
            "program_guillotine",
            "program_material_mutation",
            "program_mass_block_break",
            "program_meteor_line",
            "program_molecular_divider",
            "program_phonon_maser",
            "program_radiation_filter",
            "program_radiation_injection",
            "program_supreme_infusion",
            "program_time_accelerate",
            "psionic_control_circuit",
            "psionic_echo",
            "psionic_factor",
            "psionic_factor_ebony",
            "psionic_factor_ivory",
            "psycheonic_metal_ingot",
            "psycheonic_metal_nugget",
            "raw_antinite",
            "resonant_spell_bullet_active",
            "resonant_spell_bullet_charge_active",
            "resonant_spell_bullet_circle_active",
            "resonant_spell_bullet_grenade_active",
            "resonant_spell_bullet_loop_active",
            "resonant_spell_bullet_mine_active",
            "resonant_spell_bullet_projectile_active",
            "shard_antinite",
            "sorcery_booster",
            "spell_magazine",
            "sublimated_spell_bullet_active",
            "sublimated_spell_bullet_charge_active",
            "sublimated_spell_bullet_circle_active",
            "sublimated_spell_bullet_grenade_active",
            "sublimated_spell_bullet_loop_active",
            "sublimated_spell_bullet_mine_active",
            "sublimated_spell_bullet_projectile_active",
            "third_eye_device",
            "transcendent_spell_bullet_active",
            "transcendent_spell_bullet_charge_active",
            "transcendent_spell_bullet_circle_active",
            "transcendent_spell_bullet_grenade_active",
            "transcendent_spell_bullet_loop_active",
            "transcendent_spell_bullet_mine_active",
            "transcendent_spell_bullet_projectile_active",
            "unrefined_flashmetal"
    };

    private static final String[] ACTIVE_ITEMS = {
            "advanced_spell_bullet",
            "advanced_spell_bullet_charge",
            "advanced_spell_bullet_circle",
            "advanced_spell_bullet_grenade",
            "advanced_spell_bullet_loop",
            "advanced_spell_bullet_mine",
            "advanced_spell_bullet_projectile",
            "awakened_spell_bullet",
            "awakened_spell_bullet_charge",
            "awakened_spell_bullet_circle",
            "awakened_spell_bullet_grenade",
            "awakened_spell_bullet_loop",
            "awakened_spell_bullet_mine",
            "awakened_spell_bullet_projectile",
            "flash_ring",
            "resonant_spell_bullet",
            "resonant_spell_bullet_charge",
            "resonant_spell_bullet_circle",
            "resonant_spell_bullet_grenade",
            "resonant_spell_bullet_loop",
            "resonant_spell_bullet_mine",
            "resonant_spell_bullet_projectile",
            "sublimated_spell_bullet",
            "sublimated_spell_bullet_charge",
            "sublimated_spell_bullet_circle",
            "sublimated_spell_bullet_grenade",
            "sublimated_spell_bullet_loop",
            "sublimated_spell_bullet_mine",
            "sublimated_spell_bullet_projectile",
            "transcendent_spell_bullet",
            "transcendent_spell_bullet_charge",
            "transcendent_spell_bullet_circle",
            "transcendent_spell_bullet_grenade",
            "transcendent_spell_bullet_loop",
            "transcendent_spell_bullet_mine",
            "transcendent_spell_bullet_projectile"
    };

    public PsitweaksItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Psitweaks.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (String name : GENERATED_ITEMS) {
            generated(name);
        }
        for (String name : ACTIVE_ITEMS) {
            activeGenerated(name);
        }

        bow("psimetal_bow");
        bowPulling("psimetal_bow_pulling_0");
        bowPulling("psimetal_bow_pulling_1");
        bowPulling("psimetal_bow_pulling_2");

        withExistingParent("inline_caster", ResourceLocation.fromNamespaceAndPath("psi", "item/cad_inline_1"));
        withExistingParent("secondary_caster", ResourceLocation.fromNamespaceAndPath("psi", "item/cad_inline_2"));
        withExistingParent("parallel_caster", ResourceLocation.fromNamespaceAndPath("psi", "item/cad_inline_3"));
    }

    private void generated(String name) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", itemTexture(name));
    }

    private void activeGenerated(String name) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", itemTexture(name))
                .override()
                .predicate(modLoc("active"), 1.0F)
                .model(unchecked("item/" + name + "_active"))
                .end();
    }

    private void bow(String name) {
        withExistingParent(name, mcLoc("item/bow"))
                .texture("layer0", modLoc("item/" + name))
                .override()
                .predicate(mcLoc("pulling"), 1.0F)
                .model(unchecked("item/psimetal_bow_pulling_0"))
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1.0F)
                .predicate(mcLoc("pull"), 0.65F)
                .model(unchecked("item/psimetal_bow_pulling_1"))
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1.0F)
                .predicate(mcLoc("pull"), 0.9F)
                .model(unchecked("item/psimetal_bow_pulling_2"))
                .end();
    }

    private void bowPulling(String name) {
        withExistingParent(name, mcLoc("item/bow"))
                .texture("layer0", modLoc("item/" + name));
    }

    private ResourceLocation itemTexture(String name) {
        return switch (name) {
            case "antinite_dust" -> modLoc("item/dust_antinite");
            case "cad_assembly_psycheonic_metal" -> modLoc("item/cad_assembly_psycheonicmetal");
            case "program_active_air_mine" -> modLoc("item/program_trick_active_air_mine");
            case "program_cocytus" -> modLoc("item/program_trick_cocytus");
            case "program_cure_radiation" -> modLoc("item/program_trick_cure_radiation");
            case "program_die_flex" -> modLoc("item/program_trick_die_flex");
            case "program_flight" -> modLoc("item/program__trick_flight");
            case "program_guillotine" -> modLoc("item/program_trick_guillotine");
            case "program_material_mutation" -> modLoc("item/program_trick_material_mutation");
            case "program_mass_block_break" -> modLoc("item/program_trick_mass_block_break");
            case "program_meteor_line" -> modLoc("item/program_trick_meteor_line");
            case "program_molecular_divider" -> modLoc("item/program_trick_molecular_divider");
            case "program_phonon_maser" -> modLoc("item/program_trick_phonon_maser");
            case "program_radiation_filter" -> modLoc("item/program_trick_radiation_filter");
            case "program_radiation_injection" -> modLoc("item/program_trick_radiation_injection");
            case "program_supreme_infusion" -> modLoc("item/program_trick_supreme_infusion");
            case "program_time_accelerate" -> modLoc("item/program_trick_time_accelerate");
            case "psycheonic_metal_ingot" -> modLoc("item/psycheonic_metal");
            case "psycheonic_metal_nugget" -> modLoc("item/psycheonic_nugget");
            case "spell_magazine" -> modLoc("item/spell_magazine_huge");
            default -> modLoc("item/" + name);
        };
    }

    private ModelFile unchecked(String path) {
        return new ModelFile.UncheckedModelFile(modLoc(path));
    }
}
