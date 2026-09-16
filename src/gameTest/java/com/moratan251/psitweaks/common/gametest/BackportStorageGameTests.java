package com.moratan251.psitweaks.common.gametest;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.storage.idea.*;
import com.moratan251.psitweaks.common.storage.connector.*;
import com.moratan251.psitweaks.common.network.*;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import io.netty.buffer.Unpooled;
import java.util.*;
import java.nio.file.Files;
@GameTestHolder("psitweaks")
@PrefixGameTestTemplate(false)
public class BackportStorageGameTests {
    @GameTest(template = "psi110_empty")
    public static void partialTransfersConserveEnergy(GameTestHelper helper) {
        var storage = new PlayerIdeaStorage();
        var source = new EnergyStorage(5000, 5000, 5000, 1500);
        helper.assertTrue(IdeaStorageEnergyTransfer.absorb(storage, source, 4000) == 1500, "Source shortage");
        helper.assertTrue(source.getEnergyStored() == 0 && storage.energy() == 1500, "Absorb conservation");
        var target = new EnergyStorage(1000, 1000, 1000, 900);
        helper.assertTrue(IdeaStorageEnergyTransfer.supply(storage, target, 4000) == 100, "Target space");
        helper.assertTrue(target.getEnergyStored() == 1000 && storage.energy() == 1400, "Supply conservation");
        helper.assertTrue(IdeaStorageEnergyTransfer.supply(storage, target, 4000) == 0, "Full target");
        var empty = new EnergyStorage(5000);
        helper.assertTrue(IdeaStorageEnergyTransfer.supply(storage, empty, 4000) == 1400, "Storage shortage");
        helper.assertTrue(storage.energy() == 0 && empty.getEnergyStored() == 1400, "Drain conservation");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void capacityAndSimulationAreSafe(GameTestHelper helper) {
        long old = PsitweaksConfig.COMMON.ideaStorageMaxEnergy.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(Long.MAX_VALUE);
            var storage = new PlayerIdeaStorage();
            int[] dirty = {0};
            storage.setDirtyCallback(() -> dirty[0]++);
            storage.insertEnergy(Long.MAX_VALUE - 100, false);
            helper.assertTrue(storage.insertEnergy(Long.MAX_VALUE, true) == 100 && dirty[0] == 1,
                    "Simulation overflow or mutation");
            var source = new EnergyStorage(1000, 1000, 1000, 1000);
            helper.assertTrue(IdeaStorageEnergyTransfer.absorb(storage, source, 4000) == 100, "Storage room limit");
            helper.assertTrue(storage.energy() == Long.MAX_VALUE && source.getEnergyStored() == 900,
                    "Near-long-limit conservation");
            helper.assertTrue(IdeaStorageEnergyTransfer.absorb(storage, source, 4000) == 0, "Full storage");
            PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(1000L);
            helper.assertTrue(storage.energy() == Long.MAX_VALUE && storage.insertEnergy(1, false) == 0,
                    "Capacity reduction destroyed balance");
            helper.assertTrue(storage.extractEnergy(4000, false) == 4000, "Over-capacity withdrawal");
            storage.markLoadFailed();
            helper.assertTrue(storage.extractEnergy(1, false) == 0 && storage.insertEnergy(1, false) == 0,
                    "Failed storage must remain untouched");
        } finally {
            PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(old);
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void actualTransferAndDirectionRestrictions(GameTestHelper helper) {
        var storage = new PlayerIdeaStorage();
        var source = new EnergyStorage(5000, 0, 5000, 5000) {
            @Override
            public int extractEnergy(int amount, boolean simulate) {
                return super.extractEnergy(simulate ? amount : Math.min(123, amount), simulate);
            }
        };
        helper.assertTrue(IdeaStorageEnergyTransfer.absorb(storage, source, 4000) == 123,
                "Absorb must use actual extraction");
        var target = new EnergyStorage(5000, 5000, 0) {
            @Override
            public int receiveEnergy(int amount, boolean simulate) {
                return super.receiveEnergy(simulate ? amount : Math.min(45, amount), simulate);
            }
        };
        helper.assertTrue(IdeaStorageEnergyTransfer.supply(storage, target, 4000) == 45,
                "Supply must use actual insertion");
        helper.assertTrue(storage.energy() == 78 && target.getEnergyStored() == 45 && source.getEnergyStored() == 4877,
                "Simulation/execution mismatch lost energy");
        helper.assertTrue(IdeaStorageEnergyTransfer.supply(storage, source, 4000) == 0, "Extract-only handler");
        helper.assertTrue(IdeaStorageEnergyTransfer.absorb(storage, target, 4000) == 0, "Receive-only handler");
        helper.assertTrue(IdeaStorageEnergyTransfer.amountForPower(1.5) == 6000, "Fractional power");
        helper.assertTrue(IdeaStorageEnergyTransfer.amountForPower(Double.MAX_VALUE) == Integer.MAX_VALUE,
                "FE API integer overflow");
        for (double value : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            helper.assertTrue(IdeaStorageEnergyTransfer.amountForPower(value) == 0, "Invalid power " + value);
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void reentrantSupplyCannotWithdrawTwice(GameTestHelper helper) {
        for (boolean connectorPath : new boolean[] {false, true}) {
            var storage = new PlayerIdeaStorage();
            storage.insertEnergy(100, false);
            var other = new EnergyStorage(100);
            var target = new EnergyStorage(100) {
                @Override public int receiveEnergy(int amount, boolean simulate) {
                    helper.assertTrue(IdeaStorageEnergyTransfer.supply(storage, other, 80) == 0,
                            "Nested supply escaped warehouse guard");
                    helper.assertTrue(storage.extractEnergy(10, false) == 0 && storage.insertEnergy(10, false) == 0,
                            "Direct FE access escaped warehouse guard");
                    return super.receiveEnergy(Math.min(amount, 80), simulate);
                }
            };
            int moved = connectorPath ? ConnectorTransfers.pushEnergy(storage, target, 100)
                    : IdeaStorageEnergyTransfer.supply(storage, target, 100);
            helper.assertTrue(moved == 80 && storage.energy() == 20 && target.getEnergyStored() == 80
                    && other.getEnergyStored() == 0, "Reentrant supply changed total FE");
            helper.assertTrue(storage.extractEnergy(1, false) == 1, "Supply left warehouse locked");
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void reentrantAbsorbCannotConsumeReservedCapacity(GameTestHelper helper) {
        long previous = PsitweaksConfig.COMMON.ideaStorageMaxEnergy.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(100L);
            var storage = new PlayerIdeaStorage();
            var other = new EnergyStorage(100, 0, 100, 100);
            var source = new EnergyStorage(100, 0, 100, 100) {
                @Override public int extractEnergy(int amount, boolean simulate) {
                    helper.assertTrue(IdeaStorageEnergyTransfer.absorb(storage, other, 100) == 0,
                            "Nested absorption consumed reserved capacity");
                    helper.assertTrue(storage.insertEnergy(100, false) == 0, "Direct insertion consumed reserved capacity");
                    if (!simulate) PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(1L);
                    return super.extractEnergy(amount, simulate);
                }
            };
            helper.assertTrue(IdeaStorageEnergyTransfer.absorb(storage, source, 100) == 100
                    && storage.energy() == 100 && source.getEnergyStored() == 0 && other.getEnergyStored() == 100,
                    "Absorption or mid-call capacity reduction lost FE");
            helper.assertTrue(storage.extractEnergy(1, false) == 1, "Absorption left warehouse locked");
        } finally { PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(previous); }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void energyTransferReleasesGuardAndRefundsOnFailure(GameTestHelper helper) {
        var storage = new PlayerIdeaStorage();
        storage.insertEnergy(100, false);
        var broken = new EnergyStorage(100) {
            @Override public int receiveEnergy(int amount, boolean simulate) {
                if (!simulate) throw new IllegalStateException("Test failure before acceptance");
                return super.receiveEnergy(amount, true);
            }
        };
        boolean thrown = false;
        try { ConnectorTransfers.pushEnergy(storage, broken, 100); }
        catch (IllegalStateException expected) { thrown = true; }
        helper.assertTrue(thrown && storage.energy() == 100, "Failed supply did not refund");
        var working = new EnergyStorage(100);
        helper.assertTrue(IdeaStorageEnergyTransfer.supply(storage, working, 20) == 20
                && storage.energy() == 80 && working.getEnergyStored() == 20, "Exception left FE guard locked");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void unreadSaveAndEnergyRoundTrip(GameTestHelper h) throws Exception {
        UUID owner = UUID.randomUUID();
        var original = new IdeaStorageSavedData(owner);
        original.storage().insert(new ItemStack(Items.APPLE), 7);
        original.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 900);
        original.storage().insertEnergy(5_000_000_123L, false);
        CompoundTag valid = original.save(new CompoundTag());
        var invalid = new ArrayList<CompoundTag>();
        var item = valid.copy(); item.getList("Items", 10).getCompound(0).getCompound("item").putString("id", "missing:item"); invalid.add(item);
        var fluid = valid.copy(); fluid.getList("Fluids", 10).getCompound(0).getCompound("fluid").putString("FluidName", "missing:fluid"); invalid.add(fluid);
        var count = valid.copy(); count.getList("Items", 10).getCompound(0).putLong("count", -1); invalid.add(count);
        var overflow = valid.copy(); var list = overflow.getList("Items", 10); list.getCompound(0).putLong("count", Long.MAX_VALUE); list.add(list.getCompound(0).copy()); invalid.add(overflow);
        var future = valid.copy(); future.putInt("DataVersion", 999); future.putString("future", "retain"); invalid.add(future);
        var file = Files.createTempFile("backport109-storage", ".nbt");
        try {
            for (var tag : invalid) {
                var loaded = IdeaStorageSavedData.load(owner, tag);
                h.assertTrue(loaded.storage().isLoadFailed() && loaded.storage().insertEnergy(1, false) == 0
                        && loaded.storage().extractEnergy(1, false) == 0, "Unread storage was mutable");
                loaded.setDirty(); NbtIo.writeCompressed(loaded.save(new CompoundTag()), file.toFile());
                h.assertTrue(NbtIo.readCompressed(file.toFile()).equals(tag), "Unread original NBT overwritten");
            }
            NbtIo.writeCompressed(valid, file.toFile());
            var restored = IdeaStorageSavedData.load(owner, NbtIo.readCompressed(file.toFile()));
            h.assertTrue(restored.storage().energy() == 5_000_000_123L && restored.storage().itemTypeCount() == 1
                    && restored.storage().fluidTypeCount() == 1, "Saved resources lost");
            valid.remove("Energy"); valid.putInt("DataVersion", 2);
            h.assertTrue(IdeaStorageSavedData.load(owner, valid).storage().energy() == 0, "Old storage migration failed");
        } finally { Files.deleteIfExists(file); }
        h.succeed();
    }
    @GameTest(template = "psi110_empty")
    public static void energyAndQuantityUpdatesDoNotResendTemplates(GameTestHelper h) {
        var storage = new PlayerIdeaStorage();
        var item = new ItemStack(Items.APPLE); item.getOrCreateTag().putByteArray("large", new byte[200_000]); storage.insert(item, 7);
        storage.insertFluid(new FluidStack(Fluids.WATER, 1), 900);
        var session = new IdeaStorageSyncSession(); var token = new IdeaStorageMenuToken(1, UUID.randomUUID());
        var receiver = new IdeaStorageSyncAccumulator(); var parts = new ArrayList<MessageIdeaStorageSyncPart>();
        session.send(storage, token, parts::add); var first = receiver.accept(token, parts.get(0)).orElseThrow();
        storage.insertEnergy(5_000_000_123L, false); parts.clear(); session.send(storage, token, parts::add);
        h.assertTrue(parts.size() == 1 && parts.get(0).data().length == 0, "FE resent templates");
        var next = receiver.accept(token, parts.get(0)).orElseThrow();
        h.assertTrue(next.entries() == first.entries() && next.energy() == 5_000_000_123L, "FE copied inventory or truncated long");
        storage.insert(item, 1); parts.clear(); session.send(storage, token, parts::add);
        h.assertTrue(parts.get(0).data().length < 100, "Quantity resent template");
        next = receiver.accept(token, parts.get(0)).orElseThrow();
        h.assertTrue(next.entries().get(0).count() == 8 && next.entries().get(0).entryId() == first.entries().get(0).entryId(), "Quantity identity changed");
        h.assertTrue(next.entries().get(0).template() == first.entries().get(0).template()
                && next.fluidEntries().get(0).template() == first.fluidEntries().get(0).template(), "Quantity update copied templates");
        h.succeed();
    }
    @GameTest(template = "psi110_empty")
    public static void largeMenuPayloadsRoundTripBelowForgeLimit(GameTestHelper h) {
        CompoundTag tag = new CompoundTag(); tag.putByteArray("large", new byte[1_800_000]);
        var frames = new ArrayList<MessageMenuFragment>(); UUID session = UUID.randomUUID();
        MessageMenuFragment.send(2, session, MessageMenuFragment.CONNECTOR_STATE, b -> b.writeNbt(tag), frames::add);
        var receiver = new MenuPayloadBuffer(); byte[] result = null;
        for (var frame : frames) {
            var wire = new FriendlyByteBuf(Unpooled.buffer());
            try {
                frame.write(wire); h.assertTrue(wire.readableBytes() < 32767, "Forge payload limit exceeded");
                result = receiver.accept(MessageMenuFragment.read(wire));
                h.assertTrue((result != null) == frame.last(), "Partial frame published");
            } finally { wire.release(); }
        }
        var wire = new FriendlyByteBuf(Unpooled.wrappedBuffer(result));
        try { h.assertTrue(tag.equals(wire.readNbt(new NbtAccounter(32L * 1024 * 1024))), "Large NBT changed"); }
        finally { wire.release(); }
        h.assertTrue(new MenuPayloadBuffer().accept(frames.get(1)) == null, "Out-of-order frame accepted");
        h.succeed();
    }
}
