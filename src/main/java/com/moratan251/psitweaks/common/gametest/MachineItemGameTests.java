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
    /**
     * 5x4x5, so every machine the checks place (up to (3, 1, 4)) stands inside the test area. A test only starts once
     * the chunks of its area tick entities; a block placed outside it can fall into a neighbouring chunk whose entities
     * are not visible yet, and the dropped machine item then cannot be found.
     */
    private static final String TEMPLATE = "connector_empty";

    @GameTest(template = TEMPLATE)
    public static void machineItemsDescribeTheirSlots(GameTestHelper helper) {
        if (MekanismCompat.isMekanismLoaded()) {
            MachineItemSlotChecks.checkMachineItemsDescribeTheirSlots(helper);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void brokenMachinesKeepTheirContents(GameTestHelper helper) {
        if (MekanismCompat.isMekanismLoaded()) {
            MachineItemSlotChecks.checkBrokenMachinesKeepTheirContents(helper);
        }
        helper.succeed();
    }
}
