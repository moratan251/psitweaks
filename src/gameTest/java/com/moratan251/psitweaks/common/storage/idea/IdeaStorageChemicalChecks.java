package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.compat.IdeaStorageMekanismIntegration;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Separate class so the same test suite can load without Mekanism. */
final class IdeaStorageChemicalChecks {
    static void run(GameTestHelper helper) {
        check(helper, "gas", MekanismAPI.gasRegistry().getValues().stream().filter(c -> !c.isEmptyType() && c.getAttributes().isEmpty()).findFirst().orElseThrow());
        check(helper, "infuse", MekanismAPI.infuseTypeRegistry().getValues().stream().filter(c -> !c.isEmptyType()).findFirst().orElseThrow());
        check(helper, "pigment", MekanismAPI.pigmentRegistry().getValues().stream().filter(c -> !c.isEmptyType()).findFirst().orElseThrow());
        check(helper, "slurry", MekanismAPI.slurryRegistry().getValues().stream().filter(c -> !c.isEmptyType()).findFirst().orElseThrow());
    }

    private static void check(GameTestHelper helper, String kind, Chemical<?> chemical) {
        var player = IdeaStorageGameTests.player(helper);
        var storage = IdeaStorageService.get(helper.getLevel().getServer(), player.getUUID());
        var id = IdeaStorageMekanismIntegration.key(kind, chemical.getRegistryName());
        storage.insertChemical(id, 1000);
        var menu = new IdeaStorageMenu(1, player.getInventory(), player.getUUID(), 4);
        menu.setCarried(new ItemStack(MekanismBlocks.BASIC_CHEMICAL_TANK.asItem()));
        menu.handleTransferContents(player, 2, FluidStack.EMPTY, id, false);
        helper.assertTrue(storage.simulateExtractChemical(id, 9999) == 0, kind + " filled into tank");
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            com.moratan251.psitweaks.common.network.IdeaStorageNetwork.writeItem(buffer, menu.getCarried());
            var decoded = com.moratan251.psitweaks.common.network.IdeaStorageNetwork.readItem(buffer);
            helper.assertTrue(ItemResourceKey.of(menu.getCarried()).equals(ItemResourceKey.of(decoded)),
                    kind + " tank capability NBT survives network");
            helper.assertTrue(!ItemResourceKey.of(decoded).equals(ItemResourceKey.of(new ItemStack(MekanismBlocks.BASIC_CHEMICAL_TANK.asItem()))),
                    kind + " filled and empty tanks must differ");
        } finally { buffer.release(); }
        menu.handleTransferContents(player, 0, FluidStack.EMPTY, null, false);
        helper.assertTrue(storage.simulateExtractChemical(id, 9999) == 1000, kind + " restored from tank");
    }
}
