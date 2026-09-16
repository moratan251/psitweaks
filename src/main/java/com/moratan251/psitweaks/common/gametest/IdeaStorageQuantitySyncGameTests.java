package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync.QuantityUpdate;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStorageQuantitySyncGameTests {
    @GameTest(template = "connector_empty")
    public static void quantityChangesOmitTemplatesAndUpdateRecipeInventory(GameTestHelper helper) {
        var player = player(helper);
        var storage = IdeaStorageService.get(helper.getLevel().getServer(), player.getUUID());
        var largeItem = new ItemStack(Items.APPLE);
        var data = new CompoundTag();
        data.putByteArray("LargeTemplate", new byte[240_000]);
        largeItem.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        var ordinaryItem = new ItemStack(Items.APPLE);
        var water = new FluidStack(Fluids.WATER, 1);
        var hydrogen = ResourceLocation.parse("mekanism:hydrogen");
        storage.insert(largeItem, 7);
        storage.insert(ordinaryItem, 4);
        storage.insertFluid(water, 1000);
        storage.insertChemical(hydrogen, 1000);
        var menu = new IdeaStorageMenu(7, player.getInventory(), player.getUUID(), 4);
        var full = menu.pollStorageSync(0);
        helper.assertTrue(encodedSize(helper, full) > 240_000, "Test template is not large enough");
        menu.acceptClientSync(roundTrip(helper, full));
        var itemTemplate = menu.clientSnapshot().entries().getFirst().template();
        var fluidTemplate = menu.clientSnapshot().fluidEntries().getFirst().template();
        long templates = menu.clientTemplateVersion();
        for (int tick = 1; tick <= 4; tick++) {
            storage.insert(largeItem, 1);
            storage.insertFluid(water, 100);
            storage.insertChemical(hydrogen, 10);
            storage.insertEnergy(1, false);
            helper.assertTrue(menu.pollStorageSync(tick) == null, "Quantity change bypassed five-tick throttle");
        }
        var delta = roundTrip(helper, menu.pollStorageSync(5));
        helper.assertTrue(!delta.full() && delta.entries().isEmpty() && delta.fluidEntries().isEmpty()
                && delta.chemicalEntries().isEmpty() && encodedSize(helper, delta) < 100,
                "Quantity update retransmitted templates");
        helper.assertTrue(delta.itemAmounts().equals(List.of(new QuantityUpdate(0, 11)))
                && delta.fluidAmounts().equals(List.of(new QuantityUpdate(0, 1400)))
                && delta.chemicalAmounts().equals(List.of(new QuantityUpdate(0, 1040))), "Wrong quantity changes");
        // A recipe screen can cover the storage screen. Menu state must still receive all amounts.
        menu.acceptClientSync(delta);
        helper.assertTrue(menu.clientSnapshot().entries().getFirst().template() == itemTemplate
                && menu.clientSnapshot().fluidEntries().getFirst().template() == fluidTemplate
                && menu.clientTemplateVersion() == templates && menu.clientQuantityVersion() == 1,
                "Amount update rebuilt templates");
        helper.assertTrue(menu.clientStorageEntries().getFirst().count() == 11
                && menu.clientStorageEntries().get(1).count() == 4 && menu.clientSnapshot().energy() == 4,
                "Recipe ingredients, component variants or FE became stale");

        storage.extract(ItemResourceKey.of(largeItem).orElseThrow(), 3);
        storage.extractFluid(FluidResourceKey.of(water).orElseThrow(), 400);
        storage.extractChemical(hydrogen, 40);
        var decrease = roundTrip(helper, menu.pollStorageSync(10));
        menu.acceptClientSync(decrease);
        helper.assertTrue(!decrease.full() && menu.clientStorageEntries().getFirst().count() == 8
                && menu.clientSnapshot().fluidEntries().getFirst().amount() == 1000
                && menu.clientSnapshot().chemicalEntries().getFirst().amount() == 1000, "Decrease did not synchronize");
        helper.assertTrue(menu.pollStorageSync(15) == null, "Unchanged inventory was resent");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void changedResourceKeysAndZeroAmountsReplaceSnapshot(GameTestHelper helper) {
        var player = player(helper);
        var storage = IdeaStorageService.get(helper.getLevel().getServer(), player.getUUID());
        var apple = new ItemStack(Items.APPLE);
        var water = new FluidStack(Fluids.WATER, 1);
        var hydrogen = ResourceLocation.parse("mekanism:hydrogen");
        storage.insert(apple, 10);
        storage.insertFluid(water, 1000);
        storage.insertChemical(hydrogen, 1000);
        var menu = new IdeaStorageMenu(7, player.getInventory(), player.getUUID(), 4);
        menu.acceptClientSync(roundTrip(helper, menu.pollStorageSync(0)));
        // Replace at the same index with the same item/fluid IDs but different components.
        storage.extract(ItemResourceKey.of(apple).orElseThrow(), Long.MAX_VALUE);
        apple.set(DataComponents.CUSTOM_NAME, Component.literal("Different apple"));
        storage.insert(apple, 10);
        storage.extractFluid(FluidResourceKey.of(water).orElseThrow(), Long.MAX_VALUE);
        water.set(DataComponents.CUSTOM_NAME, Component.literal("Different water"));
        storage.insertFluid(water, 1000);
        var replacement = roundTrip(helper, menu.pollStorageSync(5));
        helper.assertTrue(replacement.full(), "Same-sized component replacement used stale indexes");
        menu.acceptClientSync(replacement);
        helper.assertTrue(ItemStack.isSameItemSameComponents(menu.clientStorageEntries().getFirst().template(), apple)
                && FluidStack.isSameFluidSameComponents(menu.clientSnapshot().fluidEntries().getFirst().template(), water),
                "Replacement components were lost");
        storage.extract(ItemResourceKey.of(apple).orElseThrow(), Long.MAX_VALUE);
        storage.extractFluid(FluidResourceKey.of(water).orElseThrow(), Long.MAX_VALUE);
        storage.extractChemical(hydrogen, Long.MAX_VALUE);
        var empty = roundTrip(helper, menu.pollStorageSync(10));
        menu.acceptClientSync(empty);
        helper.assertTrue(empty.full() && menu.clientStorageEntries().isEmpty()
                && menu.clientSnapshot().fluidEntries().isEmpty() && menu.clientSnapshot().chemicalEntries().isEmpty(),
                "Zero-count resources did not disappear");
        storage.insert(apple, 3);
        var reappeared = roundTrip(helper, menu.pollStorageSync(15));
        menu.acceptClientSync(reappeared);
        helper.assertTrue(reappeared.full() && menu.clientStorageEntries().getFirst().count() == 3,
                "Reappearing resource did not restore its template");
        var reopened = new IdeaStorageMenu(7, player.getInventory(), player.getUUID(), 4).pollStorageSync(20);
        helper.assertTrue(reopened.full() && reopened.revision() == 1 && !reopened.session().equals(reappeared.session()),
                "New menu retained old synchronization state");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void staleOrInvalidDeltasCannotChangeClientState(GameTestHelper helper) {
        var player = player(helper);
        var menu = new IdeaStorageMenu(7, player.getInventory(), player.getUUID(), 4);
        UUID session = UUID.randomUUID();
        var full = new MessageIdeaStorageSync(7, session, 1, true,
                List.of(new MessageIdeaStorageSync.Entry(new ItemStack(Items.APPLE), 7)), List.of(), List.of(),
                256, 64, 64, 0, Long.MAX_VALUE, false);
        menu.acceptClientSync(full);
        var large = List.of(new QuantityUpdate(0, Long.MAX_VALUE));
        var invalid = List.of(
                MessageIdeaStorageSync.quantityUpdate(7, UUID.randomUUID(), 2, 5, 10, large, List.of(), List.of()),
                MessageIdeaStorageSync.quantityUpdate(8, session, 2, 5, 10, large, List.of(), List.of()),
                MessageIdeaStorageSync.quantityUpdate(7, session, 1, 5, 10, large, List.of(), List.of()),
                MessageIdeaStorageSync.quantityUpdate(7, session, 3, 5, 10, large, List.of(), List.of()),
                MessageIdeaStorageSync.quantityUpdate(7, session, 2, 5, 10, List.of(new QuantityUpdate(-1, 10)), List.of(), List.of()),
                MessageIdeaStorageSync.quantityUpdate(7, session, 2, 5, 10, List.of(new QuantityUpdate(1, 10)), List.of(), List.of()),
                MessageIdeaStorageSync.quantityUpdate(7, session, 2, 5, 10, List.of(new QuantityUpdate(0, 0)), List.of(), List.of()),
                MessageIdeaStorageSync.quantityUpdate(7, session, 2, 5, 10,
                        List.of(new QuantityUpdate(0, 8), new QuantityUpdate(0, 9)), List.of(), List.of()),
                MessageIdeaStorageSync.quantityUpdate(7, session, 2, 5, 10, large, List.of(new QuantityUpdate(0, 10)), List.of()));
        for (var message : invalid) {
            menu.acceptClientSync(roundTrip(helper, message));
            helper.assertTrue(menu.clientSnapshot() == full && menu.clientSnapshotVersion() == 1,
                    "Invalid update partially changed the snapshot or FE");
        }
        var valid = roundTrip(helper, MessageIdeaStorageSync.quantityUpdate(7, session, 2, 5, Long.MAX_VALUE,
                large, List.of(), List.of()));
        menu.acceptClientSync(valid);
        helper.assertTrue(menu.clientStorageEntries().getFirst().count() == Long.MAX_VALUE, "Long quantity was truncated");
        menu.acceptClientSync(full);
        menu.acceptClientSync(valid);
        helper.assertTrue(menu.clientSnapshotVersion() == 2 && menu.clientStorageEntries().getFirst().count() == Long.MAX_VALUE,
                "Stale full snapshot or repeated delta overwrote the latest state");
        helper.succeed();
    }

    private static FakePlayer player(GameTestHelper helper) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "storage-quantity-sync"));
    }

    private static MessageIdeaStorageSync roundTrip(GameTestHelper helper, MessageIdeaStorageSync message) {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            MessageIdeaStorageSync.STREAM_CODEC.encode(buf, message);
            var decoded = MessageIdeaStorageSync.STREAM_CODEC.decode(buf);
            helper.assertTrue(!buf.isReadable(), "Payload left unread bytes");
            return decoded;
        } finally {
            buf.release();
        }
    }

    private static int encodedSize(GameTestHelper helper, MessageIdeaStorageSync message) {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            MessageIdeaStorageSync.STREAM_CODEC.encode(buf, message);
            return buf.readableBytes();
        } finally {
            buf.release();
        }
    }
}
