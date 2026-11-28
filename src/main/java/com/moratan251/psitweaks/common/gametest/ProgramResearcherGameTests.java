package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Program Research Table tells clients which program it researches, for the hologram over its keyboard. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ProgramResearcherGameTests {
    @GameTest(template = "psi110_empty")
    public static void researcherSendsTheProgramItResearches(GameTestHelper helper) {
        if (MekanismCompat.isMekanismLoaded()) {
            ProgramResearcherChecks.checkSendsResearchingProgram(helper);
        } else {
            helper.succeed();
        }
    }
}
