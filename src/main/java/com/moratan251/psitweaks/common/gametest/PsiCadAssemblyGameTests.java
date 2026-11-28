package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.cad.CADComponentLookup;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.base.ModItems;

/** Psi 本体の CAD 素体の性能を Psitweaks のデータで上書きしたもの。 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PsiCadAssemblyGameTests {
    private PsiCadAssemblyGameTests() {
    }

    /** エボニー・アイボリーの規模だけを上げ、効率と CAD の見た目は Psi のまま。 */
    @GameTest(template = "psi110_empty")
    public static void ebonyAndIvoryAssembliesUseRaisedPotency(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        checkAssembly(helper, registries, ModItems.cadAssemblyEbony.get(), 450, 90, "item/cad_ebony_psimetal");
        checkAssembly(helper, registries, ModItems.cadAssemblyIvory.get(), 400, 95, "item/cad_ivory_psimetal");

        int psimetal = CADComponentLookup.statValue(registries,
                new ItemStack(ModItems.cadAssemblyPsimetal.get()), EnumCADStat.POTENCY);
        helper.assertTrue(psimetal == 250, "The psimetal assembly must keep Psi's potency 250, got " + psimetal);
        helper.succeed();
    }

    private static void checkAssembly(GameTestHelper helper, RegistryAccess registries, Item item,
            int potency, int efficiency, String modelPath) {
        ItemStack assembly = new ItemStack(item);
        int definedPotency = CADComponentLookup.statValue(registries, assembly, EnumCADStat.POTENCY);
        int definedEfficiency = CADComponentLookup.statValue(registries, assembly, EnumCADStat.EFFICIENCY);
        helper.assertTrue(definedPotency == potency && definedEfficiency == efficiency,
                item + " must have potency " + potency + " / efficiency " + efficiency
                        + ", got " + definedPotency + " / " + definedEfficiency);
        ResourceLocation model = CADComponentLookup.definition(registries, assembly).orElseThrow().model().orElse(null);
        helper.assertTrue(ResourceLocation.fromNamespaceAndPath("psi", modelPath).equals(model),
                item + " must keep Psi's CAD model, got " + model);

        ItemStack cad = ItemCAD.makeCAD(registries, assembly.copy());
        int cadPotency = ((ICAD) cad.getItem()).getStatValue(cad, EnumCADStat.POTENCY);
        helper.assertTrue(cadPotency == potency, "A CAD built on " + item + " must have potency " + potency
                + ", got " + cadPotency);
    }
}
