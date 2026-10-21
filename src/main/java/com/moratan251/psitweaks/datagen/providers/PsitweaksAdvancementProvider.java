package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Material and CAD Assembly advancements, shown on Psi's own tab (psi:main) after Psigem.
 * Their names live in {@link PsitweaksLanguageProvider} (addAdvancementTranslations).
 */
public class PsitweaksAdvancementProvider extends AdvancementProvider {
    public PsitweaksAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
            ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(PsitweaksAdvancementProvider::generate));
    }

    private static void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
            ExistingFileHelper existingFileHelper) {
        add(saver, "alloy_psion_pickup", psi("psigem_pickup"), PsitweaksItems.ALLOY_PSION);
        add(saver, "alloy_psion_assembly_pickup", own("alloy_psion_pickup"), PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION);
        add(saver, "psionic_control_circuit_pickup", own("alloy_psion_pickup"), PsitweaksItems.PSIONIC_CONTROL_CIRCUIT);
        add(saver, "chaotic_psimetal_pickup", own("alloy_psion_pickup"), PsitweaksItems.CHAOTIC_PSIMETAL);
        add(saver, "chaotic_psimetal_assembly_pickup", own("chaotic_psimetal_pickup"), PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL);
        add(saver, "flashmetal_pickup", own("chaotic_psimetal_pickup"), PsitweaksItems.FLASHMETAL);
        add(saver, "flashmetal_assembly_pickup", own("flashmetal_pickup"), PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL);
        add(saver, "psionic_echo_pickup", own("flashmetal_pickup"), PsitweaksItems.PSIONIC_ECHO);
        add(saver, "alloy_psionic_echo_pickup", own("psionic_echo_pickup"), PsitweaksItems.ALLOY_PSIONIC_ECHO);
        add(saver, "echo_control_circuit_pickup", own("alloy_psionic_echo_pickup"), PsitweaksItems.ECHO_CONTROL_CIRCUIT);
        add(saver, "heavy_psimetal_pickup", own("psionic_echo_pickup"), PsitweaksItems.HEAVY_PSIMETAL);
        add(saver, "heavy_psimetal_assembly_pickup", own("heavy_psimetal_pickup"),
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA, PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA);
        add(saver, "antinite_pickup", own("heavy_psimetal_pickup"), PsitweaksItems.RAW_ANTINITE, PsitweaksItems.ANTINITE_INGOT);
        add(saver, "hypostasis_gem_pickup", own("antinite_pickup"), PsitweaksItems.HYPOSTASIS_GEM);
        add(saver, "alloy_hypostasis_pickup", own("hypostasis_gem_pickup"), PsitweaksItems.ALLOY_HYPOSTASIS);
        add(saver, "hypostasis_control_circuit_pickup", own("alloy_hypostasis_pickup"), PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT);
        add(saver, "psycheonic_metal_pickup", own("hypostasis_gem_pickup"), PsitweaksItems.PSYCHEONIC_METAL_INGOT);
        add(saver, "psycheonic_metal_assembly_pickup", own("psycheonic_metal_pickup"), PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL);
    }

    /**
     * Saves {@code psitweaks:main/<id>}, completed by holding any one of {@code items}; the first is the icon.
     * The parent is set by id because the data run cannot see Psi's advancements to check it.
     */
    private static void add(Consumer<AdvancementHolder> saver, String id, ResourceLocation parent, ItemLike... items) {
        String key = "advancement.psitweaks:" + id;
        // Not Builder.advancement(), which turns on the telemetry event vanilla sends for its own advancements.
        new Advancement.Builder()
                .parent(parent)
                .display(items[0], Component.translatable(key), Component.translatable(key + ".desc"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items)))
                .save(saver, own(id).toString());
    }

    private static ResourceLocation psi(String id) {
        return ResourceLocation.fromNamespaceAndPath("psi", "main/" + id);
    }

    private static ResourceLocation own(String id) {
        return Psitweaks.location("main/" + id);
    }
}
