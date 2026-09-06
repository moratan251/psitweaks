package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import java.util.List;
import java.util.UUID;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("psitweaks")
@PrefixGameTestTemplate(false)
public final class IdeaStorageGameTests {
    @GameTest(template = "empty")
    public static void unlockAndRecipes(GameTestHelper helper) {
        var player = player(helper);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(com.moratan251.psitweaks.common.items.PsitweaksItems.PROGRAM_IDEA_STORAGE.get()));
        com.moratan251.psitweaks.common.handler.SpellUnlockHandler.onRightClickUnlockItem(
                new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem(
                        player, net.minecraft.world.InteractionHand.MAIN_HAND));
        helper.assertTrue(player.getTags().contains("psitweaks.unlock.idea_storage"), "program unlock registered after resource reload");
        var recipes = helper.getLevel().getRecipeManager();
        boolean mekanism = com.moratan251.psitweaks.common.compat.MekanismCompat.isMekanismLoaded();
        helper.assertTrue(recipes.byKey(ResourceLocation.fromNamespaceAndPath("psitweaks", "program_research/program_idea_storage")).isPresent() == mekanism,
                "research recipe conditional on Mekanism");
        helper.assertTrue(recipes.byKey(ResourceLocation.fromNamespaceAndPath("psitweaks", "fallback/program_idea_storage")).isPresent() != mekanism,
                "fallback recipe conditional on missing Mekanism");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void roundTripAndDirty(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        IdeaStorageSavedData data = new IdeaStorageSavedData(owner);
        ItemStack item = new ItemStack(Items.DIAMOND);
        item.getOrCreateTag().putString("test", "original");
        ItemResourceKey key = ItemResourceKey.of(item).orElseThrow();
        helper.assertTrue(data.storage().insert(item, 128) == 128, "insert");
        item.getOrCreateTag().putString("test", "changed");
        helper.assertTrue(data.isDirty(), "mutation must dirty SavedData");
        FluidStack fluid = new FluidStack(Fluids.WATER, 1);
        fluid.getOrCreateTag().putString("test", "water");
        data.storage().insertFluid(fluid, 3_000_000_000L);
        ResourceLocation gas = ResourceLocation.fromNamespaceAndPath("test", "gas/same");
        ResourceLocation slurry = ResourceLocation.fromNamespaceAndPath("test", "slurry/same");
        data.storage().insertChemical(gas, 1000);
        data.storage().insertChemical(slurry, 2000);
        data.storage().setGridRows(8);
        IdeaStorageSavedData loaded = IdeaStorageSavedData.load(owner, data.save(new CompoundTag()));
        helper.assertTrue(!loaded.isDirty(), "reading must not dirty data");
        helper.assertTrue(loaded.storage().simulateExtract(key, 999) == 128, "NBT keys copied and restored");
        helper.assertTrue(loaded.storage().simulateExtractFluid(FluidResourceKey.of(fluid).orElseThrow(), Long.MAX_VALUE)
                == 1_048_576_000L, "fluid limit and long roundtrip");
        helper.assertTrue(loaded.storage().chemicalTypeCount() == 2, "chemical categories must stay separate");
        helper.assertTrue(loaded.storage().getGridRows() == 8, "rows restored");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void futureDataAndOwnerIsolation(GameTestHelper helper) {
        CompoundTag future = new CompoundTag();
        future.putInt("DataVersion", IdeaStorageSavedData.CURRENT_DATA_VERSION + 1);
        IdeaStorageSavedData data = IdeaStorageSavedData.load(UUID.randomUUID(), future);
        helper.assertTrue(data.storage().isLoadFailed(), "future version blocked");
        helper.assertTrue(data.storage().insert(new ItemStack(Items.DIRT), 1) == 0, "no writes after load failure");
        data.storage().setGridRows(7);
        helper.assertTrue(!data.isDirty(), "future data must not be overwritten");
        var server = helper.getLevel().getServer();
        UUID owner = UUID.randomUUID();
        var first = IdeaStorageService.get(server, owner);
        first.insert(new ItemStack(Items.DIRT), 7);
        helper.assertTrue(first == IdeaStorageService.get(server, owner), "server owns cached data");
        helper.assertTrue(IdeaStorageService.get(server, UUID.randomUUID()).itemTypeCount() == 0, "owner isolation");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void craftAndClose(GameTestHelper helper) {
        var player = player(helper);
        var storage = IdeaStorageService.get(helper.getLevel().getServer(), player.getUUID());
        for (var entry : storage.itemEntries()) storage.extract(entry.getKey(), entry.getValue());
        storage.insert(new ItemStack(Items.OAK_LOG), 3);
        var menu = new IdeaStorageMenu(1, player.getInventory(), player.getUUID(), 4);
        player.containerMenu = menu;
        menu.setCraftOpen(true);
        helper.assertTrue(menu.handleFillCrafting(player, List.of(new ItemStack(Items.OAK_LOG), ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY)), "fill from storage");
        helper.assertTrue(menu.getSlot(45).getItem().is(Items.OAK_PLANKS), "real crafting recipe");
        menu.quickMoveStack(player, 45);
        helper.assertTrue(player.getInventory().countItem(Items.OAK_PLANKS) == 12, "craft/refill conserves quantity");
        storage.insert(new ItemStack(Items.COBBLESTONE), 4);
        menu.getSlot(36).set(new ItemStack(Items.COBBLESTONE, 2));
        menu.removed(player);
        helper.assertTrue(storage.simulateExtract(ItemResourceKey.of(new ItemStack(Items.COBBLESTONE)).orElseThrow(), 99) == 6,
                "close returns matrix contents");
        helper.assertTrue(!new IdeaStorageMenu(2, player.getInventory(), UUID.randomUUID(), 4).stillValid(player), "foreign owner rejected");
        helper.succeed();
    }

    static net.minecraft.server.level.ServerPlayer player(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var player = new net.minecraft.server.level.ServerPlayer(server, helper.getLevel(),
                new com.mojang.authlib.GameProfile(UUID.randomUUID(), "storage-test"));
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server, connection, player);
        return player;
    }

    @GameTest(template = "empty")
    public static void fluidContainers(GameTestHelper helper) {
        var player = player(helper);
        var storage = IdeaStorageService.get(helper.getLevel().getServer(), player.getUUID());
        var menu = new IdeaStorageMenu(1, player.getInventory(), player.getUUID(), 4);
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));
        menu.handleTransferContents(player, 0, FluidStack.EMPTY, null, false);
        var water = FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow();
        helper.assertTrue(menu.getCarried().is(Items.BUCKET), "bucket emptied");
        helper.assertTrue(storage.simulateExtractFluid(water, 9999) == 1000, "water deposited");
        menu.handleTransferContents(player, 1, new FluidStack(Fluids.WATER, 1), null, false);
        helper.assertTrue(menu.getCarried().is(Items.WATER_BUCKET), "bucket filled");
        helper.assertTrue(storage.fluidTypeCount() == 0, "filled quantity debited");
        storage.insertFluid(new FluidStack(Fluids.WATER, 1), Long.MAX_VALUE);
        menu.handleTransferContents(player, 0, FluidStack.EMPTY, null, false);
        helper.assertTrue(menu.getCarried().is(Items.WATER_BUCKET), "full storage preserves container");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chemicalContainers(GameTestHelper helper) {
        if (com.moratan251.psitweaks.common.compat.MekanismCompat.isMekanismLoaded()) {
            IdeaStorageChemicalChecks.run(helper);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void syncRoundTrip(GameTestHelper helper) {
        var message = new MessageIdeaStorageSync(List.of(new MessageIdeaStorageSync.Entry(new ItemStack(Items.DIAMOND), 9_000_000_000L)),
                List.of(new MessageIdeaStorageSync.FluidEntry(new FluidStack(Fluids.WATER, 1), 9000)),
                List.of(new MessageIdeaStorageSync.ChemicalEntry(ResourceLocation.fromNamespaceAndPath("test", "gas/example"), 9_000_000_000L)),
                256, 64, 64, false);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            message.write(buf);
            var copy = MessageIdeaStorageSync.read(buf);
            helper.assertTrue(copy.entries().get(0).count() == 9_000_000_000L, "long item count");
            helper.assertTrue(copy.chemicalEntries().get(0).amount() == 9_000_000_000L, "long chemical count");
            helper.assertTrue(copy.fluidEntries().get(0).template().getFluid() == Fluids.WATER, "fluid identity");
            helper.assertTrue(buf.readableBytes() == 0, "wire format consumed");
        } finally { buf.release(); }
        helper.succeed();
    }
}
