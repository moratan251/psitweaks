package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import vazkii.psi.api.cad.CADComponentDefinition;
import vazkii.psi.api.cad.CADComponentLookup;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.common.item.base.ModItems;

/**
 * Writes Psi CAD component definitions under Psi's own ids so they replace the ones in Psi's jar.
 * Psitweaks' data pack sorts after Psi's (mods.toml ordering AFTER), so these files win.
 * A definition replaces the whole entry, so each one repeats Psi's other stats and model unchanged.
 */
public class PsiCadComponentOverrideProvider implements DataProvider {
    public static final int EBONY_ASSEMBLY_POTENCY = 450;
    public static final int IVORY_ASSEMBLY_POTENCY = 400;

    private final PackOutput.PathProvider pathProvider;

    public PsiCadComponentOverrideProvider(PackOutput output) {
        this.pathProvider = output.createRegistryElementsPathProvider(CADComponentLookup.REGISTRY);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, CADComponentDefinition> definitions = new LinkedHashMap<>();
        addAssembly(definitions, ModItems.cadAssemblyEbony.get(), 90, EBONY_ASSEMBLY_POTENCY, "item/cad_ebony_psimetal");
        addAssembly(definitions, ModItems.cadAssemblyIvory.get(), 95, IVORY_ASSEMBLY_POTENCY, "item/cad_ivory_psimetal");

        List<CompletableFuture<?>> futures = new ArrayList<>();
        definitions.forEach((id, definition) -> {
            JsonElement json = CADComponentDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition).getOrThrow();
            futures.add(DataProvider.saveStable(output, json, pathProvider.json(id)));
        });
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks Psi CAD component overrides";
    }

    private static void addAssembly(Map<ResourceLocation, CADComponentDefinition> definitions, Item assembly,
            int efficiency, int potency, String psiModelPath) {
        Map<EnumCADStat, Integer> stats = new LinkedHashMap<>();
        stats.put(EnumCADStat.EFFICIENCY, efficiency);
        stats.put(EnumCADStat.POTENCY, potency);
        definitions.put(BuiltInRegistries.ITEM.getKey(assembly), CADComponentDefinition.assembly(
                stats, Optional.of(ResourceLocation.fromNamespaceAndPath("psi", psiModelPath))));
    }
}
