package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Psi-Link Generator pushes its FE out of the energy port on its back. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PsionicGeneratorGameTests {
    @GameTest(template = "psi110_empty")
    public static void generatorPlacedFromItemEjectsOutOfItsBack(GameTestHelper helper) {
        if (MekanismCompat.isMekanismLoaded()) {
            PsionicGeneratorChecks.checkEjectsOutOfItsBack(helper, true);
        } else {
            helper.succeed();
        }
    }

    @GameTest(template = "psi110_empty")
    public static void generatorPlacedWithoutItemEjectsOutOfItsBack(GameTestHelper helper) {
        if (MekanismCompat.isMekanismLoaded()) {
            PsionicGeneratorChecks.checkEjectsOutOfItsBack(helper, false);
        } else {
            helper.succeed();
        }
    }
}
