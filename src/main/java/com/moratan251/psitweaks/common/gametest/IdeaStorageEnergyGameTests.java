package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageEnergyTransfer;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageSavedData;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStorageEnergyGameTests {
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
    public static void energyPersistsToDiskAndLoadsOldData(GameTestHelper helper) throws Exception {
        var factory = IdeaStorageSavedData.factory(UUID.randomUUID());
        var data = factory.constructor().get();
        long amount = Math.min(5_000_000_123L, data.storage().maxEnergy());
        data.storage().insertEnergy(amount, false);
        helper.assertTrue(data.isDirty(), "Energy did not mark saved data dirty");
        var file = Files.createTempFile("psitweaks-energy-test-", ".dat");
        try {
            NbtIo.writeCompressed(data.save(new CompoundTag(), helper.getLevel().registryAccess()), file);
            var tag = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            var restored = factory.deserializer().apply(tag, helper.getLevel().registryAccess());
            helper.assertTrue(restored.storage().energy() == amount, "Disk reload lost FE");
            tag.remove("Energy");
            tag.putInt("DataVersion", 2);
            helper.assertTrue(factory.deserializer().apply(tag, helper.getLevel().registryAccess()).storage().energy() == 0,
                    "Old saves must start at zero FE");
            tag.putLong("Energy", -1);
            helper.assertTrue(factory.deserializer().apply(tag, helper.getLevel().registryAccess()).storage().energy() == 0,
                    "Negative saved energy");
        } finally {
            Files.deleteIfExists(file);
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void energySyncPreservesLongValues(GameTestHelper helper) {
        var message = new MessageIdeaStorageSync(List.of(), List.of(), List.of(), 256, 64, 64,
                5_000_000_123L, Long.MAX_VALUE, false);
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            MessageIdeaStorageSync.STREAM_CODEC.encode(buf, message);
            var result = MessageIdeaStorageSync.STREAM_CODEC.decode(buf);
            helper.assertTrue(result.energy() == message.energy() && result.maxEnergy() == Long.MAX_VALUE,
                    "Sync truncated FE balance or limit");
            helper.assertTrue(buf.readableBytes() == 0, "Unread sync fields");
        } finally {
            buf.release();
        }
        helper.succeed();
    }
}
