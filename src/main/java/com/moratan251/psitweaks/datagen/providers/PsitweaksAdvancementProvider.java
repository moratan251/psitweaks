package com.moratan251.psitweaks.datagen.providers;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.handler.PsitweaksAdvancements;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Material and CAD Assembly advancements, shown on Psi's own tab (psi:main) after Psigem.
 * Their names live in {@link PsitweaksLanguageProvider} (addAdvancementTranslations).
 */
public class PsitweaksAdvancementProvider extends AdvancementProvider {
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public PsitweaksAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
            ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(PsitweaksAdvancementProvider::generate));
        this.pathProvider = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
        this.registries = registries;
    }

    /** The vanilla provider cannot write load conditions, so the advancements that need another mod are saved here. */
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        CompletableFuture<?> modOnly = registries.thenCompose(lookup -> {
            List<CompletableFuture<?>> saved = new ArrayList<>();
            AdvancementHolder programResearcher = generateMekanismOnly(requiringMod(output, lookup, saved, "mekanism"));
            generateMekanismGeneratorsOnly(requiringMod(output, lookup, saved, "mekanismgenerators"), programResearcher);
            return CompletableFuture.allOf(saved.toArray(CompletableFuture[]::new));
        });
        return CompletableFuture.allOf(super.run(output), modOnly);
    }

    /** A saver that writes each advancement with a condition to load only when {@code modId} is loaded. */
    private Consumer<AdvancementHolder> requiringMod(CachedOutput output, HolderLookup.Provider lookup,
            List<CompletableFuture<?>> saved, String modId) {
        return holder -> saved.add(DataProvider.saveStable(output, lookup, Advancement.CONDITIONAL_CODEC,
                Optional.of(new WithConditions<>(holder.value(), new ModLoadedCondition(modId))),
                pathProvider.json(holder.id())));
    }

    /**
     * Advancements for the Mekanism machines. They load only with Mekanism, so only advancements that also need
     * Mekanism may hang from them. Returns the Program Researcher one, the parent of the Generators advancements.
     */
    private static AdvancementHolder generateMekanismOnly(Consumer<AdvancementHolder> saver) {
        // The parent is saved by generate(), so only its id is at hand here.
        return add(saver, "program_researcher_pickup", byId(own("psionic_control_circuit_pickup")),
                modItem(Psitweaks.location("program_researcher")));
    }

    /** Advancements about Mekanism Generators (which needs Mekanism). */
    private static void generateMekanismGeneratorsOnly(Consumer<AdvancementHolder> saver, AdvancementHolder programResearcher) {
        // Hidden; awarded from code when Trick: Phonon Maser lights a Fusion Reactor (MekanismGeneratorsGameplayIntegration).
        ResourceLocation id = PsitweaksAdvancements.PHONON_MASER_FUSION_IGNITION;
        String key = "advancement.psitweaks:" + id.getPath().substring("main/".length());
        new Advancement.Builder()
                .parent(programResearcher)
                .display(modItem(ResourceLocation.fromNamespaceAndPath("mekanismgenerators", "fusion_reactor_controller")),
                        Component.translatable(key), Component.translatable(key + ".desc"),
                        null, AdvancementType.CHALLENGE, true, true, true)
                .addCriterion(PsitweaksAdvancements.CODE_TRIGGERED, CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                .save(saver, id.toString());
    }

    /** Looked up by id so this class does not load other mods' classes; datagen runs with Mekanism and its Generators. */
    private static Item modItem(ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id)
                .orElseThrow(() -> new IllegalStateException(id + " is not registered; run datagen with Mekanism and Mekanism Generators"));
    }

    private static void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
            ExistingFileHelper existingFileHelper) {
        // The data run cannot see Psi's advancements, so Psigem is named by id.
        AdvancementHolder alloyPsion = add(saver, "alloy_psion_pickup",
                byId(ResourceLocation.fromNamespaceAndPath("psi", "main/psigem_pickup")), PsitweaksItems.ALLOY_PSION);
        add(saver, "alloy_psion_assembly_pickup", alloyPsion, PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION);
        add(saver, "psionic_control_circuit_pickup", alloyPsion, PsitweaksItems.PSIONIC_CONTROL_CIRCUIT);
        AdvancementHolder chaoticPsimetal = add(saver, "chaotic_psimetal_pickup", alloyPsion, PsitweaksItems.CHAOTIC_PSIMETAL);
        add(saver, "chaotic_psimetal_assembly_pickup", chaoticPsimetal, PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL);
        AdvancementHolder flashmetal = add(saver, "flashmetal_pickup", chaoticPsimetal, PsitweaksItems.FLASHMETAL);
        add(saver, "flashmetal_assembly_pickup", flashmetal, PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL);
        AdvancementHolder psionicEcho = add(saver, "psionic_echo_pickup", flashmetal, PsitweaksItems.PSIONIC_ECHO);
        AdvancementHolder alloyPsionicEcho = add(saver, "alloy_psionic_echo_pickup", psionicEcho, PsitweaksItems.ALLOY_PSIONIC_ECHO);
        add(saver, "echo_control_circuit_pickup", alloyPsionicEcho, PsitweaksItems.ECHO_CONTROL_CIRCUIT);
        AdvancementHolder heavyPsimetal = add(saver, "heavy_psimetal_pickup", psionicEcho, PsitweaksItems.HEAVY_PSIMETAL);
        add(saver, "heavy_psimetal_assembly_pickup", heavyPsimetal,
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA, PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA);
        AdvancementHolder antinite = add(saver, "antinite_pickup", heavyPsimetal, PsitweaksItems.RAW_ANTINITE, PsitweaksItems.ANTINITE_INGOT);
        AdvancementHolder hypostasisGem = add(saver, "hypostasis_gem_pickup", antinite, PsitweaksItems.HYPOSTASIS_GEM);
        AdvancementHolder alloyHypostasis = add(saver, "alloy_hypostasis_pickup", hypostasisGem, PsitweaksItems.ALLOY_HYPOSTASIS);
        add(saver, "hypostasis_control_circuit_pickup", alloyHypostasis, PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT);
        AdvancementHolder psycheonicMetal = add(saver, "psycheonic_metal_pickup", hypostasisGem, PsitweaksItems.PSYCHEONIC_METAL_INGOT);
        add(saver, "psycheonic_metal_assembly_pickup", psycheonicMetal, PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL);
    }

    /** Saves {@code psitweaks:main/<id>}, completed by holding any one of {@code items}; the first is the icon. */
    private static AdvancementHolder add(Consumer<AdvancementHolder> saver, String id, AdvancementHolder parent, ItemLike... items) {
        String key = "advancement.psitweaks:" + id;
        // Not Builder.advancement(), which turns on the telemetry event vanilla sends for its own advancements.
        return new Advancement.Builder()
                .parent(parent)
                .display(items[0], Component.translatable(key), Component.translatable(key + ".desc"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items)))
                .save(saver, own(id).toString());
    }

    /** A parent known only by its id, which is all {@link Advancement.Builder#parent(AdvancementHolder)} reads. */
    private static AdvancementHolder byId(ResourceLocation id) {
        return new AdvancementHolder(id, null);
    }

    private static ResourceLocation own(String id) {
        return Psitweaks.location("main/" + id);
    }
}
