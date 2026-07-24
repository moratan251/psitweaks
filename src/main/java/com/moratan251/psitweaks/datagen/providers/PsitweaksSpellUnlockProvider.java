package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class PsitweaksSpellUnlockProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public PsitweaksSpellUnlockProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "spell_unlocks");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> entries = new LinkedHashMap<>();

        spellUnlock(entries, "cocytus", "trick_cocytus", PsitweaksItems.PROGRAM_COCYTUS);
        spellUnlock(entries, "time_accelerate", "trick_time_accelerate", PsitweaksItems.PROGRAM_TIME_ACCELERATE);
        spellUnlock(entries, "flight", "trick_flight", PsitweaksItems.PROGRAM_FLIGHT);
        spellUnlock(entries, "phonon_maser", "trick_phonon_maser", PsitweaksItems.PROGRAM_PHONON_MASER);
        spellUnlock(entries, "meteor_line", "trick_meteor_line", PsitweaksItems.PROGRAM_METEOR_LINE);
        spellUnlock(entries, "supreme_infusion", "trick_supreme_infusion", PsitweaksItems.PROGRAM_SUPREME_INFUSION);
        spellUnlock(entries, "molecular_divider", "trick_molecular_divider", PsitweaksItems.PROGRAM_MOLECULAR_DIVIDER);
        spellUnlock(entries, "radiation_injection", "trick_radiation_injection", PsitweaksItems.PROGRAM_RADIATION_INJECTION);
        spellUnlock(entries, "radiation_filter", "trick_radiation_filter", PsitweaksItems.PROGRAM_RADIATION_FILTER);
        spellUnlock(entries, "cure_radiation", "trick_cure_radiation", PsitweaksItems.PROGRAM_CURE_RADIATION);
        spellUnlock(entries, "guillotine", "trick_guillotine", PsitweaksItems.PROGRAM_GUILLOTINE);
        spellUnlock(entries, "active_air_mine", "trick_active_air_mine", PsitweaksItems.PROGRAM_ACTIVE_AIR_MINE);
        spellUnlock(entries, "die_flex", "trick_die_flex", PsitweaksItems.PROGRAM_DIE_FLEX);
        spellUnlock(entries, "jump_flex", "trick_jump_flex", PsitweaksItems.PROGRAM_JUMP_FLEX);
        spellUnlock(entries, "switch_flex", "trick_switch_flex", PsitweaksItems.PROGRAM_SWITCH_FLEX);
        spellUnlock(entries, "material_mutation", "trick_material_mutation", PsitweaksItems.PROGRAM_MATERIAL_MUTATION);
        spellUnlock(entries, "mass_block_break", "trick_mass_block_break", PsitweaksItems.PROGRAM_MASS_BLOCK_BREAK);

        CompletableFuture<?>[] futures = entries.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "PsiTweaks spell unlocks";
    }

    private static void spellUnlock(Map<ResourceLocation, JsonObject> entries, String commandId, String piecePath,
                                    ItemLike unlockItem) {
        JsonObject root = new JsonObject();

        root.addProperty("command_id", commandId);
        root.addProperty("piece", Psitweaks.location(piecePath).toString());
        root.addProperty("unlock_item", BuiltInRegistries.ITEM.getKey(unlockItem.asItem()).toString());
        root.addProperty("unlock_tag", Psitweaks.MOD_ID + ".unlock." + piecePath);
        entries.put(Psitweaks.location(commandId), root);
    }
}
