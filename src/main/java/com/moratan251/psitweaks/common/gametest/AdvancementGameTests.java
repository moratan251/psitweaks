package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.Map;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The 1.0.0 advancements load onto Psi's tab with the parents from the design. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdvancementGameTests {
    private static final ResourceLocation PSI_ROOT = ResourceLocation.fromNamespaceAndPath("psi", "main/root");
    /** Advancement id → parent id; a parent without a namespace is another psitweaks:main/ advancement. */
    private static final Map<String, String> PARENTS = Map.ofEntries(
            Map.entry("alloy_psion_pickup", "psi:main/psigem_pickup"),
            Map.entry("alloy_psion_assembly_pickup", "alloy_psion_pickup"),
            Map.entry("psionic_control_circuit_pickup", "alloy_psion_pickup"),
            Map.entry("chaotic_psimetal_pickup", "alloy_psion_pickup"),
            Map.entry("chaotic_psimetal_assembly_pickup", "chaotic_psimetal_pickup"),
            Map.entry("flashmetal_pickup", "chaotic_psimetal_pickup"),
            Map.entry("flashmetal_assembly_pickup", "flashmetal_pickup"),
            Map.entry("psionic_echo_pickup", "flashmetal_pickup"),
            Map.entry("alloy_psionic_echo_pickup", "psionic_echo_pickup"),
            Map.entry("echo_control_circuit_pickup", "alloy_psionic_echo_pickup"),
            Map.entry("heavy_psimetal_pickup", "psionic_echo_pickup"),
            Map.entry("heavy_psimetal_assembly_pickup", "heavy_psimetal_pickup"),
            Map.entry("antinite_pickup", "heavy_psimetal_pickup"),
            Map.entry("hypostasis_gem_pickup", "antinite_pickup"),
            Map.entry("alloy_hypostasis_pickup", "hypostasis_gem_pickup"),
            Map.entry("hypostasis_control_circuit_pickup", "alloy_hypostasis_pickup"),
            Map.entry("psycheonic_metal_pickup", "hypostasis_gem_pickup"),
            Map.entry("psycheonic_metal_assembly_pickup", "psycheonic_metal_pickup"));

    @GameTest(template = "psi110_empty")
    public static void advancementsHangFromTheirParentsOnPsiTab(GameTestHelper helper) {
        var advancements = helper.getLevel().getServer().getAdvancements();
        long loaded = advancements.getAllAdvancements().stream()
                .filter(holder -> holder.id().getNamespace().equals(Psitweaks.MOD_ID) && holder.id().getPath().startsWith("main/"))
                .count();
        helper.assertTrue(loaded == PARENTS.size(), "Expected " + PARENTS.size() + " psitweaks:main advancements, got " + loaded);
        PARENTS.forEach((id, parent) -> {
            AdvancementNode node = advancements.tree().get(Psitweaks.location("main/" + id));
            helper.assertTrue(node != null, "Advancement " + id + " is not in the tree (missing, or its parent did not load)");
            ResourceLocation expected = parent.contains(":") ? ResourceLocation.parse(parent) : Psitweaks.location("main/" + parent);
            helper.assertTrue(node.parent() != null && node.parent().holder().id().equals(expected),
                    id + " must hang from " + expected + ", got " + (node.parent() == null ? null : node.parent().holder().id()));
            helper.assertTrue(node.root().holder().id().equals(PSI_ROOT), id + " is not on Psi's tab: " + node.root().holder().id());
            helper.assertTrue(node.advancement().display().isPresent(), id + " has no display");
        });
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void eitherItemCompletesTwoItemAdvancements(GameTestHelper helper) {
        assertEitherItem(helper, "heavy_psimetal_assembly_pickup",
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA, PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA);
        assertEitherItem(helper, "antinite_pickup", PsitweaksItems.RAW_ANTINITE, PsitweaksItems.ANTINITE_INGOT);
        helper.succeed();
    }

    /** One criterion with one item predicate that either item satisfies on its own. */
    private static void assertEitherItem(GameTestHelper helper, String id, ItemLike first, ItemLike second) {
        var holder = helper.getLevel().getServer().getAdvancements().get(Psitweaks.location("main/" + id));
        helper.assertTrue(holder != null, "Advancement " + id + " is missing");
        var criteria = holder.value().criteria();
        helper.assertTrue(criteria.size() == 1, id + " must have one criterion, got " + criteria.keySet());
        var trigger = criteria.values().iterator().next().triggerInstance();
        helper.assertTrue(trigger instanceof InventoryChangeTrigger.TriggerInstance, id + " must use inventory_changed");
        var items = ((InventoryChangeTrigger.TriggerInstance) trigger).items();
        helper.assertTrue(items.size() == 1, id + " must have one item predicate (all of them are required), got " + items.size());
        helper.assertTrue(items.get(0).test(new ItemStack(first)) && items.get(0).test(new ItemStack(second)),
                id + " must accept either item");
    }
}
