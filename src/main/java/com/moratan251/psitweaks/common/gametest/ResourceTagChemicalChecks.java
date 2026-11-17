package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.common.compat.BlockResourceMekanism;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;

/** Chemical tag patterns of the Ideaspace and block resource filters. Loaded only when Mekanism is present. */
final class ResourceTagChemicalChecks {
    private ResourceTagChemicalChecks() {
    }

    static void run(GameTestHelper helper) {
        var hydrogen = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
        var lithium = ResourceLocation.fromNamespaceAndPath("mekanism", "lithium");
        var gaseous = BlockResourceMekanism.chemicalFilter("#mekanism:gaseous");
        helper.assertTrue(gaseous.test(hydrogen) && !gaseous.test(lithium), "#mekanism:gaseous must match hydrogen only");
        var anyNamespace = BlockResourceMekanism.chemicalFilter("#gaseous");
        helper.assertTrue(anyNamespace.test(hydrogen), "#gaseous must match the tag path in any namespace");
        helper.assertTrue(BlockResourceMekanism.chemicalFilter("hydrogen").test(hydrogen),
                "An ID pattern must still match the chemical ID");
    }
}
