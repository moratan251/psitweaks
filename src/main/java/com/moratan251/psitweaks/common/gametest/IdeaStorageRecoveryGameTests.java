package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageResourceBase;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import com.moratan251.psitweaks.common.storage.idea.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.*;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStorageRecoveryGameTests {
    @GameTest(template = "connector_empty")
    public static void returnedItemsAreRecoveredBeforeSpellError(GameTestHelper helper) throws Exception {
        for (int scenario = 0; scenario < 3; scenario++) {
            boolean excess = scenario == 2;
            var actual = new ItemStack(scenario == 0 ? Items.CARROT : Items.APPLE, 10);
            if (scenario == 1) actual.set(DataComponents.CUSTOM_NAME, Component.literal("Preserve components"));
            var key = ItemResourceKey.of(actual).orElseThrow();
            var data = IdeaStorageSavedData.factory(UUID.randomUUID()).constructor().get();
            var source = new ItemStackHandler(1) {
                @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (simulate) return new ItemStack(Items.APPLE, Math.min(4, amount));
                    return super.extractItem(slot, amount + (excess ? 1 : 0), false);
                }
            };
            source.setStackInSlot(0, actual.copy());
            runAsSpell(helper, Kind.ITEM, () -> IdeaStorageResourceTransfers.depositItems(data.storage(), source, stack -> true, 4));
            int taken = excess ? 5 : 4;
            helper.assertTrue(source.getStackInSlot(0).getCount() == 10 - taken
                    && data.storage().simulateExtract(key, 100) == taken, "Item extraction was lost or changed identity/components");
            helper.assertTrue(data.storage().itemTypeCount() == 1, "Preview resource was also credited");
            assertSaved(helper, data);
        }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void returnedFluidsAreRecoveredBeforeSpellError(GameTestHelper helper) throws Exception {
        for (int scenario = 0; scenario < 3; scenario++) {
            boolean excess = scenario == 2;
            var actual = new FluidStack(scenario == 0 ? Fluids.LAVA : Fluids.WATER, 10);
            if (scenario == 1) actual.set(DataComponents.CUSTOM_NAME, Component.literal("Preserve fluid components"));
            var key = FluidResourceKey.of(actual).orElseThrow();
            var data = IdeaStorageSavedData.factory(UUID.randomUUID()).constructor().get();
            var source = new FluidTank(100) {
                @Override public FluidStack drain(FluidStack request, FluidAction action) {
                    if (action.simulate()) return new FluidStack(Fluids.WATER, Math.min(4, request.getAmount()));
                    return super.drain(request.getAmount() + (excess ? 1 : 0), action);
                }
            };
            source.setFluid(actual.copy());
            runAsSpell(helper, Kind.FLUID, () -> IdeaStorageResourceTransfers.depositFluids(data.storage(), source, stack -> true, 4));
            int taken = excess ? 5 : 4;
            helper.assertTrue(source.getFluidAmount() == 10 - taken && data.storage().simulateExtractFluid(key, 100) == taken,
                    "Fluid extraction was lost or changed identity/components");
            helper.assertTrue(data.storage().fluidTypeCount() == 1, "Preview fluid was also credited");
            assertSaved(helper, data);
        }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void returnedChemicalsAreRecoveredBeforeSpellError(GameTestHelper helper) throws Exception {
        if (MekanismCompat.isMekanismLoaded()) IdeaStorageRecoveryChemicalChecks.run(helper);
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void recoveryBypassesLimitsButCannotReuseReceipt(GameTestHelper helper) throws Exception {
        int oldTypes = PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.get();
        int oldStacks = PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.get();
        var data = IdeaStorageSavedData.factory(UUID.randomUUID()).constructor().get();
        var apple = new ItemStack(Items.APPLE);
        var carrot = ItemResourceKey.of(new ItemStack(Items.CARROT)).orElseThrow();
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.set(1);
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(1);
            data.storage().insert(apple, 63);
            try (var receipt = data.storage().reserveItemInsertion(apple, 1)) {
                helper.assertTrue(data.storage().insert(apple, 1) == 0, "Lease allowed reentrant insertion");
                helper.assertTrue(!receipt.commit(carrot, 100), "Broken extraction must report recovery");
                boolean rejected = false;
                try { receipt.commit(carrot, 100); } catch (IllegalArgumentException expected) { rejected = true; }
                helper.assertTrue(rejected, "A second commit duplicated recovered resources");
            }
            helper.assertTrue(data.storage().simulateExtract(carrot, 200) == 100 && data.storage().itemTypeCount() == 2,
                    "Type/quantity limits discarded an unexpected extraction");
            var receipt = data.storage().reserveItemInsertion(apple, 1);
            receipt.close();
            boolean rejected = false;
            try { receipt.commit(carrot, 1); } catch (IllegalArgumentException expected) { rejected = true; }
            helper.assertTrue(rejected && data.storage().insert(apple, 1) == 1, "Closed lease remained usable or locked storage");
            assertSaved(helper, data);
        } finally {
            PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.set(oldTypes);
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(oldStacks);
        }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void arithmeticOverflowKeepsBothBatchesOnDisk(GameTestHelper helper) throws Exception {
        var registries = helper.getLevel().registryAccess();
        for (Kind kind : new Kind[] {Kind.ITEM, Kind.FLUID, Kind.CHEMICAL}) {
            var factory = IdeaStorageSavedData.factory(UUID.randomUUID());
            var data = factory.constructor().get();
            var apple = ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow();
            var water = FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow();
            var hydrogen = ResourceLocation.parse("mekanism:hydrogen");
            switch (kind) {
                case ITEM -> data.storage().insert(apple.template(), 1);
                case FLUID -> data.storage().insertFluid(water.template(), 1);
                case CHEMICAL -> data.storage().insertChemical(hydrogen, 1);
                default -> throw new AssertionError(kind);
            }
            String list = switch (kind) { case ITEM -> "Items"; case FLUID -> "Fluids"; default -> "Chemicals"; };
            var seed = data.save(new CompoundTag(), registries);
            seed.getList(list, 10).getCompound(0).putLong("count", Long.MAX_VALUE - 2);
            data = factory.deserializer().apply(seed, registries);
            switch (kind) {
                case ITEM -> {
                    try (var receipt = data.storage().reserveItemInsertion(new ItemStack(Items.CARROT), 4)) {
                        helper.assertTrue(!receipt.commit(apple, 5), "Item overflow was not reported");
                    }
                }
                case FLUID -> {
                    try (var receipt = data.storage().reserveFluidInsertion(new FluidStack(Fluids.LAVA, 1), 4)) {
                        helper.assertTrue(!receipt.commit(water, 5), "Fluid overflow was not reported");
                    }
                }
                case CHEMICAL -> {
                    try (var receipt = data.storage().reserveChemicalInsertion(ResourceLocation.parse("mekanism:oxygen"), 4)) {
                        helper.assertTrue(!receipt.commit(hydrogen, 5), "Chemical overflow was not reported");
                    }
                }
                default -> throw new AssertionError(kind);
            }
            helper.assertTrue(data.storage().isLoadFailed() && data.storage().reserveItemInsertion(new ItemStack(Items.CARROT), 1) == null,
                    "Unrepresentable recovery must stop further input");
            var saved = data.save(new CompoundTag(), registries).getList(list, 10);
            helper.assertTrue(saved.size() == 2 && saved.getCompound(0).getLong("count") == Long.MAX_VALUE - 2
                    && saved.getCompound(1).getLong("count") == 5, "Overflow truncated the original or recovered batch");
            assertSaved(helper, data);
        }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void externalExceptionsBecomeSpellErrors(GameTestHelper helper) throws Exception {
        for (Kind kind : new Kind[] {Kind.ITEM, Kind.FLUID, Kind.CHEMICAL})
            runAsSpell(helper, kind, () -> { throw new IllegalStateException("Synthetic external handler failure"); });
        helper.succeed();
    }

    static void runAsSpell(GameTestHelper helper, Kind resourceKind, Runnable operation) throws Exception {
        var messages = new ArrayList<Component>();
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "recovery-test")) {
            @Override public void sendSystemMessage(Component message) { messages.add(message); }
        };
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        player.setPos(Vec3.atCenterOf(pos));
        var spell = new Spell();
        boolean[] executed = {false};
        var piece = new PieceTrickIdeaStorageResourceBase(spell) {
            @Override protected Kind kind() { return resourceKind; }
            @Override protected boolean deposit() { return true; }
            @Override public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, pos, 4, null);
            }
            @Override protected void transfer(SpellContext context, ServerPlayer caster, BlockPos at, Direction face,
                                              PlayerIdeaStorage storage, long maximum) {
                executed[0] = true;
                operation.run();
            }
        };
        var compiled = new CompiledSpell(spell);
        // SpellPieceType.create normally supplies this field. The local endpoint fixture bypasses
        // that factory, but Psi's action bookkeeping/hashCode still needs the registered piece ID.
        var registryKey = SpellPiece.class.getDeclaredField("registryKey");
        registryKey.setAccessible(true);
        registryKey.set(piece, Psitweaks.location("trick_idea_storage_deposit_" + resourceKind.name().toLowerCase(java.util.Locale.ROOT)));
        compiled.actions.push(compiled.new Action(piece));
        var context = new SpellContext().setPlayer(player);
        context.cspell = compiled;
        try {
            compiled.safeExecute(context);
        } finally { PsiAPI.internalHandler.setCrashData(null, null); }
        helper.assertTrue(executed[0] && messages.stream().anyMatch(message -> message.getContents() instanceof TranslatableContents contents
                && contents.getKey().equals(PieceTrickIdeaStorageResourceBase.ERROR_TRANSFER)),
                "Expected recoverable spell error through actual CompiledSpell.safeExecute for " + resourceKind);
    }

    static void assertSaved(GameTestHelper helper, IdeaStorageSavedData data) throws Exception {
        helper.assertTrue(data.isDirty() && data.storage().getInventoryVersion() > 0, "Recovery did not notify saving/synchronization");
        var registries = helper.getLevel().registryAccess();
        var original = data.save(new CompoundTag(), registries);
        var path = Files.createTempFile("psitweaks-recovery-", ".dat");
        try {
            NbtIo.writeCompressed(original, path);
            var disk = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            var restored = IdeaStorageSavedData.factory(data.owner()).deserializer().apply(disk, registries);
            helper.assertTrue(restored.save(new CompoundTag(), registries).equals(original), "Recovery changed after disk reload");
            helper.assertTrue(restored.storage().isLoadFailed() == data.storage().isLoadFailed(), "Reload changed recovery availability");
        } finally { Files.deleteIfExists(path); }
    }
}
