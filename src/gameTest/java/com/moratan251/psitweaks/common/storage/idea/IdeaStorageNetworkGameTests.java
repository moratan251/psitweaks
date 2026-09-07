package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.network.*;
import io.netty.buffer.Unpooled;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.*;

@GameTestHolder("psitweaks")
@PrefixGameTestTemplate(false)
public class IdeaStorageNetworkGameTests {
    private static ItemStack book(int name) {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);
        ListTag pages = new ListTag();
        for (int i = 0; i < 100; i++) pages.add(StringTag.valueOf("a".repeat(400)));
        stack.getOrCreateTag().put("pages", pages);
        stack.setHoverName(Component.literal("book " + name));
        return stack;
    }

    private static List<MessageIdeaStorageSyncPart> send(IdeaStorageSyncSession session, PlayerIdeaStorage storage,
            IdeaStorageMenuToken token) {
        List<MessageIdeaStorageSyncPart> parts = new ArrayList<>();
        session.send(storage, token, parts::add);
        return parts;
    }

    @GameTest(template = "empty")
    public static void largeSnapshotsAndDelta(GameTestHelper h) {
        var storage = new PlayerIdeaStorage();
        for (int i = 0; i < 27; i++) h.assertTrue(storage.insert(book(i), 1) == 1, "book accepted");
        ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
        ListTag contents = new ListTag();
        for (int i = 0; i < 18; i++) {
            CompoundTag item = book(i).save(new CompoundTag());
            item.putByte("Slot", (byte) i);
            contents.add(item);
        }
        shulker.getOrCreateTagElement("BlockEntityTag").put("Items", contents);
        h.assertTrue(storage.insert(shulker, 1) == 1, "single resource exceeding 512 KiB accepted");
        var token = new IdeaStorageMenuToken(1, UUID.randomUUID());
        var session = new IdeaStorageSyncSession();
        var receiver = new IdeaStorageSyncAccumulator();
        var parts = send(session, storage, token);
        h.assertTrue(parts.size() >= 4, "large inventory split");
        MessageIdeaStorageSync snapshot = null;
        int totalBytes = 0;
        for (int i = 0; i < parts.size(); i++) {
            FriendlyByteBuf wire = new FriendlyByteBuf(Unpooled.buffer());
            try {
                parts.get(i).write(wire);
                totalBytes += wire.readableBytes();
                h.assertTrue(wire.readableBytes() < 512 * 1024, "frame below 512 KiB including headers");
                new ClientboundCustomPayloadPacket(new ResourceLocation("psitweaks", "main"), wire);
                var result = receiver.accept(token, MessageIdeaStorageSyncPart.read(wire));
                h.assertTrue(result.isPresent() == (i == parts.size() - 1), "only final frame publishes");
                if (result.isPresent()) snapshot = result.get();
            } finally { wire.release(); }
        }
        h.assertTrue(snapshot != null && snapshot.entries().size() == 28, "complete snapshot");
        ItemResourceKey shulkerKey = ItemResourceKey.of(shulker).orElseThrow();
        h.assertTrue(snapshot.entries().stream().anyMatch(e -> ItemResourceKey.of(e.template()).filter(shulkerKey::equals).isPresent()), "fragmented item NBT intact");
        long firstId = snapshot.entries().get(0).entryId();
        storage.insert(book(0), 1);
        var delta = send(session, storage, token);
        h.assertTrue(delta.size() == 1 && delta.get(0).data().length < 64 && !delta.get(0).reset(), "quantity-only delta contains no NBT");
        snapshot = receiver.accept(token, delta.get(0)).orElseThrow();
        h.assertTrue(snapshot.entries().get(0).entryId() == firstId && snapshot.entries().get(0).count() == 2, "stable ID and delta quantity");
        storage.extract(ItemResourceKey.of(book(0)).orElseThrow(), 2);
        snapshot = receiver.accept(token, send(session, storage, token).get(0)).orElseThrow();
        h.assertTrue(snapshot.entries().size() == 27 && session.resolve(firstId) == null, "tombstone removes client entry and server reference");
        System.out.println("IDEA STORAGE: large snapshot bytes=" + totalBytes + ", frames=" + parts.size() + ", delta bytes=" + delta.get(0).data().length);
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void rejectStaleAndOutOfOrder(GameTestHelper h) {
        var storage = new PlayerIdeaStorage();
        for (int i = 0; i < 27; i++) storage.insert(book(i), 1);
        var token = new IdeaStorageMenuToken(1, UUID.randomUUID());
        var session = new IdeaStorageSyncSession();
        var parts = send(session, storage, token);
        var receiver = new IdeaStorageSyncAccumulator();
        h.assertTrue(receiver.accept(new IdeaStorageMenuToken(1, UUID.randomUUID()), parts.get(0)).isEmpty(), "other session ignored");
        for (var part : parts) receiver.accept(token, part);
        h.assertTrue(receiver.accept(token, parts.get(0)).isEmpty(), "old revision ignored");
        storage.insert(book(0), 1);
        h.assertTrue(receiver.accept(token, send(session, storage, token).get(0)).isPresent(), "stale packet does not poison receiver");
        var outOfOrder = new IdeaStorageSyncAccumulator();
        h.assertTrue(outOfOrder.accept(token, parts.get(1)).isEmpty(), "missing start rejected");
        for (var part : parts) h.assertTrue(outOfOrder.accept(token, part).isEmpty(), "broken update never published");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void compactRequestsAndServerIdentity(GameTestHelper h) {
        var player = IdeaStorageGameTests.player(h);
        var menu = new IdeaStorageMenu(1, player.getInventory(), player.getUUID(), 4);
        player.containerMenu = menu;
        var storage = IdeaStorageService.get(h.getLevel().getServer(), player.getUUID());
        storage.insert(book(1), 2);
        menu.broadcastChanges();
        h.assertTrue(menu.resolveEntry(1) instanceof ItemResourceKey, "server index populated");
        new IdeaStorageSyncSession().send(storage, menu.token(), menu::receiveSyncPart);
        h.assertTrue(menu.clientSnapshot() != null && menu.clientStorageEntries().size() == 1,
                "menu owns completed snapshot independently of visible screen");
        var request = new MessageIdeaStorageExtract(menu.token(), 1, MessageIdeaStorageExtract.MODE_CURSOR_STACK);
        FriendlyByteBuf wire = new FriendlyByteBuf(Unpooled.buffer());
        try {
            wire.writeResourceLocation(new ResourceLocation("psitweaks", "main"));
            request.write(wire);
            h.assertTrue(wire.readableBytes() < 64, "request independent of item NBT");
            var packet = new ServerboundCustomPayloadPacket(wire);
            var decoded = MessageIdeaStorageExtract.read(packet.getData());
            h.assertTrue(decoded.token().resolve(player) == menu, "current session accepted");
            var key = (ItemResourceKey) menu.resolveEntry(decoded.entryId());
            menu.handleExtract(player, key.template(), decoded.mode());
            h.assertTrue(ItemResourceKey.of(menu.getCarried()).orElseThrow().equals(key), "server template retains book NBT");
            packet.getData().release();
        } finally { wire.release(); }
        h.assertTrue(new IdeaStorageMenuToken(1, UUID.randomUUID()).resolve(player) == null, "reused window id rejected");
        h.assertTrue(menu.resolveEntry(Long.MAX_VALUE) == null, "forged ID rejected");
        menu.setCraftOpen(true);
        var refs = new ArrayList<MessageIdeaStorageFillCrafting.Reference>();
        refs.add(new MessageIdeaStorageFillCrafting.Reference(1, ItemResourceKey.of(book(1)).orElseThrow().hashCode()));
        for (int i = 1; i < 9; i++) refs.add(new MessageIdeaStorageFillCrafting.Reference(0, 0));
        var fill = new MessageIdeaStorageFillCrafting(menu.token(), refs);
        h.assertTrue(fill.resolve(menu).size() == 9, "craft templates resolved on server");
        wire = new FriendlyByteBuf(Unpooled.buffer());
        try { fill.write(wire); h.assertTrue(wire.readableBytes() < 256, "crafting references bounded"); }
        finally { wire.release(); }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void singleDepositAndHiddenCrafting(GameTestHelper h) {
        var player = IdeaStorageGameTests.player(h);
        var menu = new IdeaStorageMenu(1, player.getInventory(), player.getUUID(), 4);
        player.containerMenu = menu;
        var storage = IdeaStorageService.get(h.getLevel().getServer(), player.getUUID());
        menu.setCarried(new ItemStack(Items.DIAMOND, 12));
        menu.handleTransferContents(player, 0, FluidStack.EMPTY, null, false);
        h.assertTrue(menu.getCarried().getCount() == 11 && storage.simulateExtract(ItemResourceKey.of(new ItemStack(Items.DIAMOND)).orElseThrow(), 99) == 1, "right click deposits exactly one");
        menu.depositCarried(false);
        h.assertTrue(menu.getCarried().isEmpty(), "left click deposits remaining stack");
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));
        menu.handleTransferContents(player, 0, FluidStack.EMPTY, null, false);
        h.assertTrue(menu.getCarried().is(Items.BUCKET), "container contents transferred");
        menu.handleTransferContents(player, 0, FluidStack.EMPTY, null, false);
        h.assertTrue(menu.getCarried().is(Items.BUCKET), "empty container not deposited");
        storage.insert(new ItemStack(Items.DIAMOND), Long.MAX_VALUE);
        menu.setCarried(new ItemStack(Items.DIAMOND, 2));
        menu.handleTransferContents(player, 0, FluidStack.EMPTY, null, false);
        h.assertTrue(menu.getCarried().getCount() == 2, "full storage preserves cursor items");
        menu.setCraftOpen(true);
        menu.getSlot(36).set(new ItemStack(Items.OAK_LOG));
        menu.setCraftOpen(false);
        menu.setCarried(new ItemStack(Items.OAK_PLANKS));
        menu.clicked(0, 0, ClickType.PICKUP_ALL, player);
        h.assertTrue(menu.getCarried().getCount() == 1 && menu.getSlot(36).getItem().is(Items.OAK_LOG), "hidden result never crafted");
        menu.setCarried(new ItemStack(Items.OAK_LOG));
        menu.clicked(0, 0, ClickType.PICKUP_ALL, player);
        h.assertTrue(menu.getCarried().getCount() == 1, "hidden materials excluded");
        menu.setCraftOpen(true);
        menu.setCarried(new ItemStack(Items.OAK_PLANKS));
        menu.clicked(0, 0, ClickType.PICKUP_ALL, player);
        h.assertTrue(menu.getCarried().getCount() == 1, "visible result also excluded, like vanilla crafting table");
        h.succeed();
    }
}
