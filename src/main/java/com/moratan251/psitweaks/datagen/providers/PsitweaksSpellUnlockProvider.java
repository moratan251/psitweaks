package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class PsitweaksSpellUnlockProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public PsitweaksSpellUnlockProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "spell_unlocks");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> entries = new LinkedHashMap<>();

        spellUnlock(entries, "cocytus", "trick_cocytus", "program_cocytus");
        spellUnlock(entries, "time_accelerate", "trick_time_accelerate", "program_time_accelerate");
        spellUnlock(entries, "flight", "trick_flight", "program_flight");
        spellUnlock(entries, "phonon_maser", "trick_phonon_maser", "program_phonon_maser");
        spellUnlock(entries, "meteor_line", "trick_meteor_line", "program_meteor_line");
        spellUnlock(entries, "supreme_infusion", "trick_supreme_infusion", "program_supreme_infusion");
        spellUnlock(entries, "molecular_divider", "trick_molecular_divider", "program_molecular_divider");
        spellUnlock(entries, "radiation_injection", "trick_radiation_injection", "program_radiation_injection");
        spellUnlock(entries, "radiation_filter", "trick_radiation_filter", "program_radiation_filter");
        spellUnlock(entries, "cure_radiation", "trick_cure_radiation", "program_cure_radiation");
        spellUnlock(entries, "guillotine", "trick_guillotine", "program_guillotine");
        spellUnlock(entries, "active_air_mine", "trick_active_air_mine", "program_active_air_mine");
        spellUnlock(entries, "die_flex", "trick_die_flex", "program_die_flex");
        spellUnlock(entries, "material_mutation", "trick_material_mutation", "program_material_mutation");

        CompletableFuture<?>[] futures = entries.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "PsiTweaks spell unlocks";
    }

    private static void spellUnlock(Map<ResourceLocation, JsonObject> entries, String commandId, String piecePath, String itemPath) {
        JsonObject root = new JsonObject();

        root.addProperty("command_id", commandId);
        root.addProperty("piece", Psitweaks.location(piecePath).toString());
        root.addProperty("unlock_item", Psitweaks.location(itemPath).toString());
        root.addProperty("unlock_tag", Psitweaks.MOD_ID + ".unlock." + piecePath);
        entries.put(Psitweaks.location(commandId), root);
    }
}
