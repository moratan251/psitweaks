package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The items of psitweaks' Mekanism machines and the energy cube. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MachineItemGameTests {
    @GameTest(template = "psi110_empty")
    public static void machineItemsDescribeTheirSlots(GameTestHelper helper) {
        if (MekanismCompat.isMekanismLoaded()) {
            MachineItemSlotChecks.checkMachineItemsDescribeTheirSlots(helper);
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void brokenMachinesKeepTheirContents(GameTestHelper helper) {
        if (MekanismCompat.isMekanismLoaded()) {
            MachineItemSlotChecks.checkBrokenMachinesKeepTheirContents(helper);
        }
        helper.succeed();
    }
}
