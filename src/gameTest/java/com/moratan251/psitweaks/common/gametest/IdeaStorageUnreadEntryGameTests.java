package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageNbtLimits;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageSavedData;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Backport of {@code IdeaStorageReviewGameTests#unreadableEntriesAreKeptWithoutLockingWarehouse} (1.21.1 commit 5c192eb):
 * individual unreadable entries are kept verbatim and written back once while the rest of the warehouse stays usable.
 * Whole-file failures (newer DataVersion, unreadable list, long overflow) are covered by
 * {@code BackportStorageGameTests#unreadSaveAndEnergyRoundTrip}.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStorageUnreadEntryGameTests {
    private record UnreadCase(String name, CompoundTag tag, String category, CompoundTag unreadEntry, int items, int fluids, int chemicals) { }

    /**
     * 1.20.1 has no item components, so the 1.21.1 "bad components" case becomes an item tag that
     * {@link ItemResourceKey#parse} rejects: it exceeds {@link IdeaStorageNbtLimits#MAX_ITEM_NBT_BYTES} under Forge's
     * reader accounting (4 + 9 bytes per ByteTag list entry) while holding only references to the cached ByteTag
     * and staying small on disk. A deeper or longer-string tag could not be written to or read from a save file.
     */
    private static ListTag oversizedItemPayload() {
        var list = new ListTag();
        var zero = ByteTag.valueOf((byte) 0);
        for (int i = 0; i < 6_000_000; i++) list.add(zero);
        return list;
    }

    @GameTest(template = "connector_empty")
    public static void unreadableEntriesAreKeptWithoutLockingWarehouse(GameTestHelper helper) throws Exception {
        UUID owner = UUID.randomUUID();
        var data = new IdeaStorageSavedData(owner);
        data.storage().insert(new ItemStack(Items.APPLE), 7);
        data.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 900);
        data.storage().insertChemical(ResourceLocation.fromNamespaceAndPath("psitweaks_test", "gas"), 500);
        data.storage().insertEnergy(100, false);
        CompoundTag valid = data.save(new CompoundTag());
        var cases = new ArrayList<UnreadCase>();
        var unknownItem = valid.copy();
        unknownItem.getList("Items", 10).getCompound(0).getCompound("item").putString("id", "absent_mod:lost_item");
        cases.add(new UnreadCase("unknown item", unknownItem, "Items", unknownItem.getList("Items", 10).getCompound(0), 0, 1, 1));
        var unknownFluid = valid.copy();
        unknownFluid.getList("Fluids", 10).getCompound(0).getCompound("fluid").putString("FluidName", "absent_mod:lost_fluid");
        cases.add(new UnreadCase("unknown fluid", unknownFluid, "Fluids", unknownFluid.getList("Fluids", 10).getCompound(0), 1, 0, 1));
        var invalidFluidId = valid.copy();
        invalidFluidId.getList("Fluids", 10).getCompound(0).getCompound("fluid").putString("FluidName", "Not A Valid ID!");
        cases.add(new UnreadCase("invalid fluid ID", invalidFluidId, "Fluids", invalidFluidId.getList("Fluids", 10).getCompound(0), 1, 0, 1));
        var oversizedItem = valid.copy();
        CompoundTag oversizedTag = new CompoundTag();
        oversizedTag.put("payload", oversizedItemPayload());
        CompoundTag oversizedItemData = oversizedItem.getList("Items", 10).getCompound(0).getCompound("item");
        oversizedItemData.put("tag", oversizedTag);
        helper.assertTrue(!IdeaStorageNbtLimits.fitsItem(oversizedItemData), "Test item tag must exceed the storage NBT budget");
        cases.add(new UnreadCase("oversized item NBT", oversizedItem, "Items", oversizedItem.getList("Items", 10).getCompound(0), 0, 1, 1));
        var missingItem = valid.copy();
        missingItem.getList("Items", 10).getCompound(0).remove("item");
        cases.add(new UnreadCase("missing item", missingItem, "Items", missingItem.getList("Items", 10).getCompound(0), 0, 1, 1));
        var badCount = valid.copy();
        badCount.getList("Items", 10).getCompound(0).putLong("count", -1);
        cases.add(new UnreadCase("non-positive count", badCount, "Items", badCount.getList("Items", 10).getCompound(0), 0, 1, 1));
        var badChemical = valid.copy();
        badChemical.getList("Chemicals", 10).getCompound(0).putString("chemical", "Not A Valid ID!");
        cases.add(new UnreadCase("invalid chemical ID", badChemical, "Chemicals", badChemical.getList("Chemicals", 10).getCompound(0), 1, 1, 0));
        var file = Files.createTempFile("psitweaks-partial-warehouse-", ".dat");
        try {
            for (UnreadCase unread : cases) {
                var loaded = IdeaStorageSavedData.load(owner, unread.tag());
                var storage = loaded.storage();
                helper.assertTrue(!storage.isLoadFailed() && loaded.unreadEntryCount() == 1
                        && storage.itemTypeCount() == unread.items() && storage.fluidTypeCount() == unread.fluids()
                        && storage.chemicalTypeCount() == unread.chemicals() && storage.energy() == 100,
                        "Readable entries were not restored alongside " + unread.name());
                helper.assertTrue(storage.insert(new ItemStack(Items.DIAMOND), 3) == 3 && storage.insertEnergy(1, false) == 1,
                        "Warehouse with " + unread.name() + " stayed locked");
                NbtIo.writeCompressed(loaded.save(new CompoundTag()), file.toFile());
                CompoundTag disk = NbtIo.readCompressed(file.toFile());
                ListTag saved = disk.getList(unread.category(), 10);
                int retained = 0;
                for (int i = 0; i < saved.size(); i++) if (saved.getCompound(i).equals(unread.unreadEntry())) retained++;
                helper.assertTrue(retained == 1, "Unread " + unread.name() + " entry was not saved unchanged exactly once");
                var reloaded = IdeaStorageSavedData.load(owner, disk);
                helper.assertTrue(!reloaded.storage().isLoadFailed() && reloaded.unreadEntryCount() == 1
                        && reloaded.storage().simulateExtract(ItemResourceKey.of(new ItemStack(Items.DIAMOND)).orElseThrow(), 10) == 3
                        && reloaded.storage().energy() == 101, "Partial warehouse did not round-trip: " + unread.name());
                helper.assertTrue(reloaded.save(new CompoundTag()).equals(disk),
                        "Repeated saves changed or duplicated unread " + unread.name());
            }
        } finally { Files.deleteIfExists(file); }
        helper.succeed();
    }
}
