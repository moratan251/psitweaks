package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.AutonomousCasterSettings;
import com.moratan251.psitweaks.common.caster.CasterLog;
import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import com.moratan251.psitweaks.common.compat.ConnectorMekanism;
import com.moratan251.psitweaks.common.items.PsitweaksDataComponents;
import com.moratan251.psitweaks.common.network.MessageAutonomousCasterAction.Action;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorAutonomousCasterPosition;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageOwnerResolver;
import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import mekanism.api.AutomationType;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.common.attachments.containers.item.AttachedItems;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.capability.PsiCapabilities;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.core.handler.PlayerData;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.base.ModCADComponents;

/** 自律詠唱機ブロックの確認。Mekanism がある時だけ呼ばれ、GameTest holder としては走査されない。 */
final class AutonomousCasterChecks {
    private static final BlockPos POS = new BlockPos(2, 1, 2);
    private static final String ERROR_PREFIX = "psitweaks.autonomous_caster.error.";

    private AutonomousCasterChecks() {
    }

    private static AutonomousCasterBlockEntity place(GameTestHelper helper) {
        helper.setBlock(POS, PsitweaksMekanismBlocks.AUTONOMOUS_CASTER.defaultState());
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(POS));
        if (!(blockEntity instanceof AutonomousCasterBlockEntity caster)) {
            throw new GameTestAssertException("The autonomous caster has no block entity");
        }
        return caster;
    }

    private static FakePlayer player(GameTestHelper helper) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "[psitweaks_caster_test]"));
    }

    private static void assertLatest(GameTestHelper helper, AutonomousCasterBlockEntity caster, CasterLog.Source source, String text) {
        Optional<CasterLog.Entry> latest = caster.getLog().latest();
        helper.assertTrue(latest.isPresent() && latest.get().source() == source && latest.get().message().getString().contains(text),
                "Expected a " + source + " log entry containing '" + text + "' but found " + latest.map(e -> e.message().getString()));
    }

    private static void assertLatestError(GameTestHelper helper, AutonomousCasterBlockEntity caster, String key) {
        Optional<CasterLog.Entry> latest = caster.getLog().latest();
        boolean matches = latest.isPresent() && latest.get().source() == CasterLog.Source.MACHINE_ERROR
                && latest.get().message().getContents() instanceof TranslatableContents contents
                && (ERROR_PREFIX + key).equals(contents.getKey());
        helper.assertTrue(matches, "Expected machine error " + key + " but found " + latest.map(e -> e.message().getContents()));
        helper.assertTrue(caster.getLastOutcome() == AutonomousCasterBlockEntity.Outcome.FAILURE
                        && caster.getLastFailureSequence() == latest.get().sequence(),
                "The status line does not point at the latest failure");
    }

    static void castsOnRedstonePulse(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        caster.setPsi(1000);
        caster.getBulletSlots().get(0).setStack(AutonomousCasterTestSpells.debugBullet(42));
        BlockPos power = POS.east();
        helper.runAfterDelay(2, () -> {
            AutonomousCasterPlayer fake = caster.getCaster();
            helper.assertTrue(fake != null && fake.isAttached(), "The machine did not prepare its caster");
            helper.assertTrue(caster.getLog().isEmpty() && caster.getLoopCounter() == 0, "The machine cast without a signal");
            helper.setBlock(power, Blocks.REDSTONE_BLOCK);
            helper.runAfterDelay(2, () -> {
                assertLatest(helper, caster, CasterLog.Source.CASTER_MESSAGE, "42");
                helper.assertTrue(caster.getLastOutcome() == AutonomousCasterBlockEntity.Outcome.SUCCESS && caster.getLoopCounter() == 1,
                        "A redstone pulse did not cast the selected bullet once");
                helper.assertTrue(caster.getPsi() == 1000, "A free spell spent Psi");
                helper.runAfterDelay(caster.getCastInterval() + 4, () -> {
                    helper.assertTrue(caster.getLoopCounter() == 1, "Pulse mode cast again while the signal stayed on");
                    helper.setBlock(power, Blocks.AIR);
                    helper.runAfterDelay(2, () -> {
                        helper.setBlock(power, Blocks.REDSTONE_BLOCK);
                        helper.runAfterDelay(2, () -> {
                            List<CasterLog.Entry> entries = caster.getLog().entries();
                            helper.assertTrue(caster.getLoopCounter() == 2, "A second pulse did not cast again");
                            helper.assertTrue(entries.size() == 1 && entries.get(0).repeatCount() == 2,
                                    "Repeated debug output was not merged into one entry");
                            helper.succeed();
                        });
                    });
                });
            });
        });
    }

    static void levelModeRepeatsAtTheInterval(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        FakePlayer player = player(helper);
        caster.setPsi(1000);
        caster.getBulletSlots().get(2).setStack(AutonomousCasterTestSpells.debugBullet(7));
        caster.applySetting(Action.SELECT_BULLET, 2, player);
        caster.applySetting(Action.SET_TRIGGER_MODE, AutonomousCasterBlockEntity.TriggerMode.LEVEL.ordinal(), player);
        caster.applySetting(Action.SET_INTERVAL, 1, player);
        helper.assertTrue(caster.getCastInterval() == AutonomousCasterSettings.minCastInterval(),
                "The cast interval was not clamped to the minimum");
        helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(21, () -> {
            int casts = caster.getLoopCounter();
            helper.assertTrue(casts >= 4 && casts <= 6, "Level mode cast " + casts + " times in 20 ticks at a 4 tick interval");
            helper.setBlock(POS.east(), Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                int stopped = caster.getLoopCounter();
                helper.runAfterDelay(10, () -> {
                    helper.assertTrue(caster.getLoopCounter() == stopped, "Level mode kept casting without a signal");
                    caster.getBulletSlots().get(2).setStack(AutonomousCasterTestSpells.debugBullet(8));
                    helper.runAfterDelay(2, () -> {
                        helper.assertTrue(caster.getLoopCounter() == 0, "Changing the bullet did not reset its loop counter");
                        helper.succeed();
                    });
                });
            });
        });
    }

    static void logsWhyACastFailed(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        FakePlayer player = player(helper);
        caster.applySetting(Action.SET_INTERVAL, 4, player);
        caster.applySetting(Action.CAST_NOW, 0, player);
        helper.runAfterDelay(2, () -> {
            assertLatestError(helper, caster, "no_spell");
            caster.getBulletSlots().get(0).setStack(AutonomousCasterTestSpells.blinkBullet(1));
            caster.setPsi(0);
            caster.applySetting(Action.CAST_NOW, 0, player);
            helper.runAfterDelay(6, () -> {
                assertLatestError(helper, caster, "no_psi");
                caster.setPsi(10);
                caster.applySetting(Action.CAST_NOW, 0, player);
                helper.runAfterDelay(6, () -> {
                    assertLatestError(helper, caster, "not_enough_psi");
                    helper.assertTrue(caster.getPsi() == 10, "A refused cast spent Psi");
                    caster.setPsi(1000);
                    caster.applySetting(Action.CAST_NOW, 0, player);
                    helper.runAfterDelay(6, () -> {
                        helper.assertTrue(caster.getLastOutcome() == AutonomousCasterBlockEntity.Outcome.SUCCESS,
                                "An affordable spell did not succeed: " + caster.getLog().latest().map(e -> e.message().getString()));
                        helper.assertTrue(caster.getPsi() < 1000 && caster.getPsi() > 0, "The spell cost was not taken from internal Psi");
                        helper.succeed();
                    });
                });
            });
        });
    }

    static void ignoresSignalPresentWhenPlaced(GameTestHelper helper) {
        helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
        AutonomousCasterBlockEntity caster = place(helper);
        caster.setPsi(1000);
        caster.getBulletSlots().get(0).setStack(AutonomousCasterTestSpells.debugBullet(3));
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(caster.hasSignal() && caster.getLoopCounter() == 0 && caster.getLog().isEmpty(),
                    "A signal that was already on when the machine was placed caused a cast");
            helper.setBlock(POS.east(), Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
                helper.runAfterDelay(2, () -> {
                    helper.assertTrue(caster.getLoopCounter() == 1, "A new pulse after placement did not cast");
                    helper.succeed();
                });
            });
        });
    }

    static void mirrorsItsInventory(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        helper.assertTrue(caster.getItemSlots().size() == Inventory.INVENTORY_SIZE, "The machine does not have 36 item slots");
        caster.getItemSlots().get(2).setStack(new ItemStack(Items.STONE, 5));
        caster.getItemSlots().get(30).setStack(new ItemStack(Items.BRICK, 4));
        helper.runAfterDelay(2, () -> {
            AutonomousCasterPlayer fake = caster.getCaster();
            helper.assertTrue(fake != null, "The machine did not prepare its caster");
            var inventory = fake.getInventory();
            helper.assertTrue(inventory.getItem(2).is(Items.STONE) && inventory.getItem(2).getCount() == 5,
                    "Machine items were not mirrored to the caster's hotbar");
            helper.assertTrue(inventory.getItem(30).is(Items.BRICK) && inventory.getItem(30).getCount() == 4,
                    "Machine items were not mirrored to the caster's inventory outside the hotbar");
            helper.assertTrue(inventory.getItem(AutonomousCasterPlayer.CAD_SLOT).getItem() instanceof ICAD
                            && PsiAPI.getPlayerCADSlot(fake) == AutonomousCasterPlayer.CAD_SLOT,
                    "Psi does not find the virtual CAD in the caster's inventory");
            // 術式がアイテムを使い、ホットバー外とオフハンドに別のアイテムを受け取り、CAD 枠を空けた場合
            inventory.getItem(2).shrink(2);
            inventory.setItem(20, new ItemStack(Items.APPLE, 3));
            inventory.offhand.set(0, new ItemStack(Items.BREAD, 2));
            inventory.setItem(AutonomousCasterPlayer.CAD_SLOT, ItemStack.EMPTY);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(caster.getItemSlots().get(2).getStack().getCount() == 3,
                        "Items used by the caster were not taken from the machine");
                helper.assertTrue(inventory.getItem(20).is(Items.APPLE) && caster.getItemSlots().get(20).getStack().is(Items.APPLE)
                                && count(caster.getItemSlots(), Items.APPLE) == 3,
                        "Items given to the caster outside the hotbar did not stay in the same machine slot");
                helper.assertTrue(inventory.offhand.get(0).isEmpty() && count(caster.getItemSlots(), Items.BREAD) == 2,
                        "Items given to the caster's off hand were not stored in the machine");
                helper.assertTrue(inventory.getItem(AutonomousCasterPlayer.CAD_SLOT) == caster.getVirtualCad(),
                        "The virtual CAD was not put back");
                helper.assertTrue(!caster.getItemSlots().get(0).isItemValid(caster.getVirtualCad().copy()),
                        "A CAD was accepted as a held item");
                caster.getItemSlots().get(4).setStack(new ItemStack(Items.DIRT, 7));
                helper.runAfterDelay(2, () -> {
                    helper.assertTrue(inventory.getItem(4).is(Items.DIRT) && inventory.getItem(4).getCount() == 7,
                            "Machine slot changes did not reach the caster");
                    helper.succeed();
                });
            });
        });
    }

    private static int count(List<IInventorySlot> slots, Item item) {
        int total = 0;
        for (IInventorySlot slot : slots) {
            if (slot.getStack().is(item)) {
                total += slot.getStack().getCount();
            }
        }
        return total;
    }

    static void restoresPsiFromEcho(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        helper.assertTrue(!caster.supportsUpgrades(), "The machine accepts upgrades");
        var hydrogen = ConnectorMekanism.stack(ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen"), 100);
        helper.assertTrue(hydrogen.isEmpty() || !caster.getEchoTank().isValid(hydrogen), "The echo tank accepted another gas");
        caster.getEchoTank().insert(PsitweaksChemicals.GAS_PSIONIC_ECHO.asStack(1000), mekanism.api.Action.EXECUTE,
                AutomationType.INTERNAL);
        helper.runAfterDelay(10, () -> {
            int expected = 1000 / AutonomousCasterSettings.echoPerOperation() * AutonomousCasterSettings.psiPerOperation();
            helper.assertTrue(caster.getPsi() == expected, "1000 mB of echo restored " + caster.getPsi() + " Psi instead of " + expected);
            helper.assertTrue(caster.getEchoTank().getStored() == 1000 % AutonomousCasterSettings.echoPerOperation(),
                    "Echo was not consumed at the configured rate");
            helper.assertTrue(caster.getRedstoneLevel() > 0, "The comparator output ignored internal Psi");
            caster.setPsi(0);
            helper.assertTrue(caster.getRedstoneLevel() == 0, "The comparator output stayed on with no Psi");
            helper.succeed();
        });
    }

    /** 休眠の後の返却など、詠唱の外で Caster の Psi が増えた分も機械に残る。返却の上限（最大 Psi）は機械の容量。 */
    static void keepsPsiReturnedAfterTheCast(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        int capacity = AutonomousCasterSettings.psiCapacity();
        int start = capacity * 4 / 5;
        int returned = capacity / 10;
        caster.setPsi(start);
        helper.runAfterDelay(2, () -> {
            AutonomousCasterPlayer fake = caster.getCaster();
            helper.assertTrue(fake != null, "The machine has no caster");
            PlayerData data = PlayerDataHandler.get(fake);
            helper.assertTrue(data.getTotalPsi() == capacity,
                    "The caster's max Psi is " + data.getTotalPsi() + " instead of the machine's " + capacity);
            helper.assertTrue(data.getAvailablePsi() == start, "The caster's Psi does not follow the machine");
            data.deductPsi(-returned, 0, false, true);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(caster.getPsi() == start + returned,
                        "Psi returned outside a cast was lost: " + caster.getPsi() + " instead of " + (start + returned));
                helper.assertTrue(data.getAvailablePsi() == caster.getPsi(), "The caster's Psi does not follow the machine");
                helper.succeed();
            });
        });
    }

    /** 視線の表示に使う目の位置と向きが詠唱主体と一致し、向きは GUI を閉じた後の表示のためにブロックの更新でも送られる。 */
    static void showsWhereTheCasterLooks(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        FakePlayer player = player(helper);
        // Caster は最初の tick に作られ、そのときの向きを向く。
        caster.applySetting(Action.SET_YAW, 30, player);
        caster.applySetting(Action.SET_PITCH, -20, player);
        helper.runAfterDelay(2, () -> {
            AutonomousCasterPlayer fake = caster.getCaster();
            helper.assertTrue(fake != null, "The machine has no caster");
            helper.assertTrue(fake.getEyePosition().distanceTo(caster.eyePosition()) < 1.0E-6,
                    "The shown eye position " + caster.eyePosition() + " is not the caster's " + fake.getEyePosition());
            helper.assertTrue(fake.getLookAngle().distanceTo(caster.lookVector()) < 1.0E-6,
                    "The shown line of sight " + caster.lookVector() + " is not the caster's " + fake.getLookAngle());
            CompoundTag update = caster.getReducedUpdateTag(helper.getLevel().registryAccess());
            helper.assertTrue(update.getInt("YawOffset") == 30 && update.getInt("Pitch") == -20,
                    "The block update does not carry the look angles: " + update);
            helper.assertTrue(!caster.isClientRendering(), "The line of sight is shown before the tab is used");
            caster.toggleClientRendering();
            helper.assertTrue(caster.isClientRendering(), "The tab did not turn the line of sight on");
            helper.succeed();
        });
    }

    static void keepsContentsWhenBroken(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        ItemStack bullet = AutonomousCasterTestSpells.debugBullet(5);
        ItemStack dye = new ItemStack(ModCADComponents.cadColorizerRed.get());
        caster.getBulletSlots().get(1).setStack(bullet.copy());
        caster.getDyeSlot().setStack(dye.copy());
        caster.getItemSlots().get(3).setStack(new ItemStack(Items.STONE, 9));
        caster.getItemSlots().get(35).setStack(new ItemStack(Items.BRICK, 6));
        caster.setPsi(AutonomousCasterSettings.psiCapacity());
        caster.getEchoTank().insert(PsitweaksChemicals.GAS_PSIONIC_ECHO.asStack(2000), mekanism.api.Action.EXECUTE,
                AutomationType.INTERNAL);
        caster.getLog().add(CasterLog.Source.CASTER_MESSAGE, net.minecraft.network.chat.Component.literal("before"), 0);
        helper.runAfterDelay(2, () -> {
            BlockPos absolute = helper.absolutePos(POS);
            helper.getLevel().destroyBlock(absolute, true);
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(absolute).inflate(2));
            ItemStack machine = drops.stream().map(ItemEntity::getItem)
                    .filter(stack -> stack.is(PsitweaksMekanismBlocks.AUTONOMOUS_CASTER.asItem()))
                    .findFirst().orElse(ItemStack.EMPTY);
            helper.assertTrue(!machine.isEmpty(), "Breaking the machine did not drop it");
            helper.assertTrue(drops.size() == 1, "Breaking the machine scattered its contents: " + drops.size() + " drops");
            Integer storedPsi = machine.get(PsitweaksDataComponents.AUTONOMOUS_CASTER_PSI.get());
            helper.assertTrue(storedPsi != null && storedPsi == AutonomousCasterSettings.psiCapacity(),
                    "The internal Psi was not stored on the machine item: " + storedPsi);
            AttachedItems items = machine.get(MekanismDataComponents.ATTACHED_ITEMS);
            helper.assertTrue(items != null && items.containers().stream().anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, bullet)),
                    "The bullet was not stored on the machine item");
            drops.forEach(Entity::discard);

            helper.setBlock(POS, PsitweaksMekanismBlocks.AUTONOMOUS_CASTER.defaultState());
            if (!(helper.getLevel().getBlockEntity(absolute) instanceof AutonomousCasterBlockEntity restored)) {
                throw new GameTestAssertException("The machine could not be placed again");
            }
            restored.applyComponentsFromItemStack(machine);
            helper.assertTrue(ItemStack.isSameItemSameComponents(restored.getBulletSlots().get(1).getStack(), bullet),
                    "The bullet did not come back");
            helper.assertTrue(ItemStack.isSameItem(restored.getDyeSlot().getStack(), dye),
                    "The colorizer did not come back");
            helper.assertTrue(restored.getItemSlots().get(3).getStack().getCount() == 9
                    && restored.getItemSlots().get(35).getStack().is(Items.BRICK)
                    && restored.getItemSlots().get(35).getStack().getCount() == 6, "The held items did not come back");
            // 9スロットだった頃の機械と保存の番号を合わせるため、エコー補充スロットはホットバーの直後のまま。
            helper.assertTrue(restored.getInventorySlots(null).get(AutonomousCasterBlockEntity.BULLET_SLOTS + 1
                            + AutonomousCasterBlockEntity.HOTBAR_SLOTS) instanceof ChemicalInventorySlot,
                    "The echo fill slot moved, so machines saved before would load their items into the wrong slots");
            helper.assertTrue(restored.getEchoTank().getStored() == 2000, "The echo gas did not come back");
            helper.assertTrue(restored.getPsi() == AutonomousCasterSettings.psiCapacity(), "The internal Psi did not come back");
            helper.assertTrue(restored.getLog().isEmpty(), "The log was carried over");
            // 空の機械は Psi を付けずに回収し、新品の機械と重ねられるようにする。
            restored.setPsi(0);
            helper.assertTrue(!restored.collectComponents().has(PsitweaksDataComponents.AUTONOMOUS_CASTER_PSI.get()),
                    "A machine without Psi kept the Psi component");
            helper.succeed();
        });
    }

    /** 術式弾スロットが8個だった頃に保存・回収した機械を読んでも、中身が元のスロットに戻る。 */
    static void readsMachinesSavedWithEightBullets(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        ItemStack bullet = AutonomousCasterTestSpells.debugBullet(5);
        ItemStack dye = new ItemStack(ModCADComponents.cadColorizerRed.get());
        caster.getBulletSlots().get(7).setStack(bullet.copy());
        caster.getDyeSlot().setStack(dye.copy());
        caster.getItemSlots().get(0).setStack(new ItemStack(Items.STONE, 3));
        caster.getItemSlots().get(35).setStack(new ItemStack(Items.BRICK, 6));
        helper.runAfterDelay(2, () -> {
            ISocketable sockets = PsiCapabilities.socketable(caster.getVirtualCad());
            helper.assertTrue(AutonomousCasterBlockEntity.BULLET_SLOTS == ISocketable.MAX_ASSEMBLER_SLOTS
                            && sockets != null && sockets.getLastSlot() == AutonomousCasterBlockEntity.BULLET_SLOTS - 1,
                    "The internal CAD does not have one socket per bullet slot");
            readsOldSaves(helper, caster, bullet, dye);
            helper.succeed();
        });
    }

    private static void readsOldSaves(GameTestHelper helper, AutonomousCasterBlockEntity caster, ItemStack bullet, ItemStack dye) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        List<IInventorySlot> slots = caster.getInventorySlots(null);
        int oldBullets = 8;
        int added = AutonomousCasterBlockEntity.BULLET_SLOTS - oldBullets;

        // 今の保存データはそのまま読む。
        CompoundTag current = caster.saveWithoutMetadata(registries);
        slots.forEach(IInventorySlot::setEmpty);
        caster.loadWithComponents(current, registries);
        assertRestored(helper, caster, bullet, dye, "a machine saved now");

        // 8個だった頃の保存データ: 術式弾より後ろの番号が4つ小さく、術式弾の数を記録していない。
        CompoundTag old = current.copy();
        old.remove("BulletSlots");
        for (Tag entry : old.getList(SerializationConstants.ITEMS, Tag.TAG_COMPOUND)) {
            CompoundTag item = (CompoundTag) entry;
            int slot = item.getByte(SerializationConstants.SLOT);
            if (slot >= oldBullets) {
                item.putByte(SerializationConstants.SLOT, (byte) (slot - added));
            }
        }
        slots.forEach(IInventorySlot::setEmpty);
        caster.loadWithComponents(old, registries);
        assertRestored(helper, caster, bullet, dye, "a machine saved with 8 bullet slots");

        // 8個だった頃に回収したブロックアイテム: 増えた術式弾スロットの分だけ短い。
        List<ItemStack> oldStacks = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            if (i < oldBullets || i >= AutonomousCasterBlockEntity.BULLET_SLOTS) {
                oldStacks.add(slots.get(i).getStack().copy());
            }
        }
        ItemStack machine = new ItemStack(PsitweaksMekanismBlocks.AUTONOMOUS_CASTER.asItem());
        machine.set(MekanismDataComponents.ATTACHED_ITEMS, new AttachedItems(oldStacks));
        slots.forEach(IInventorySlot::setEmpty);
        caster.applyComponentsFromItemStack(machine);
        assertRestored(helper, caster, bullet, dye, "a machine item picked up with 8 bullet slots");
    }

    private static void assertRestored(GameTestHelper helper, AutonomousCasterBlockEntity caster, ItemStack bullet, ItemStack dye,
            String source) {
        List<IInventorySlot> bullets = caster.getBulletSlots();
        helper.assertTrue(ItemStack.isSameItemSameComponents(bullets.get(7).getStack(), bullet)
                        && bullets.subList(8, bullets.size()).stream().allMatch(IInventorySlot::isEmpty),
                "The bullets of " + source + " came back in the wrong slots");
        helper.assertTrue(ItemStack.isSameItem(caster.getDyeSlot().getStack(), dye),
                "The colorizer of " + source + " did not come back");
        helper.assertTrue(caster.getItemSlots().get(0).getStack().is(Items.STONE)
                        && caster.getItemSlots().get(35).getStack().is(Items.BRICK)
                        && caster.getItemSlots().get(35).getStack().getCount() == 6,
                "The items of " + source + " came back in the wrong slots");
    }

    /** 「取得子: 詠唱機位置」は機械のブロックの中心を返し、機械以外からの詠唱ではエラーになる。 */
    static void selectorGivesTheMachinePosition(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        helper.runAfterDelay(2, () -> {
            AutonomousCasterPlayer fake = caster.getCaster();
            helper.assertTrue(fake != null, "The machine did not prepare its caster");
            PieceSelectorAutonomousCasterPosition selector = new PieceSelectorAutonomousCasterPosition(new Spell());
            Vec3 center = Vec3.atCenterOf(helper.absolutePos(POS));
            try {
                Object value = selector.execute(new SpellContext().setPlayer(fake));
                helper.assertTrue(value instanceof Vector3 vector && vector.x == center.x && vector.y == center.y && vector.z == center.z,
                        "The selector did not give the center of the machine: " + value);
            } catch (SpellRuntimeException e) {
                throw new GameTestAssertException("The selector failed in the machine: " + e.getMessage());
            }
            try {
                selector.execute(new SpellContext().setPlayer(player(helper)));
                throw new GameTestAssertException("The selector worked for a player's CAD");
            } catch (SpellRuntimeException e) {
                helper.assertTrue("psitweaks.spellerror.not_autonomous_caster".equals(e.getMessage()),
                        "A player's CAD got another error: " + e.getMessage());
            }
            helper.succeed();
        });
    }

    static void ignoresActionsFromOtherMenus(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        FakePlayer player = player(helper);
        caster.getLog().add(CasterLog.Source.CASTER_MESSAGE, net.minecraft.network.chat.Component.literal("keep"), 0);
        int interval = caster.getCastInterval();
        AutonomousCasterBlockEntity.handleAction(player, player.containerMenu.containerId, Action.SET_INTERVAL, interval + 20);
        AutonomousCasterBlockEntity.handleAction(player, player.containerMenu.containerId, Action.CLEAR_LOG, 0);
        helper.assertTrue(caster.getCastInterval() == interval && !caster.getLog().isEmpty(),
                "A player without the machine's menu changed its settings");

        int containerId = 77;
        var menu = new MekanismTileContainer<>(PsitweaksMekanismContainerTypes.AUTONOMOUS_CASTER, containerId,
                player.getInventory(), caster);
        player.containerMenu = menu;
        try {
            AutonomousCasterBlockEntity.handleAction(player, containerId + 1, Action.SET_INTERVAL, interval + 20);
            helper.assertTrue(caster.getCastInterval() == interval, "An action for another window was applied");
            AutonomousCasterBlockEntity.handleAction(player, containerId, Action.SET_INTERVAL, interval + 20);
            helper.assertTrue(caster.getCastInterval() == interval + 20, "An action from the machine's own menu was ignored");
            AutonomousCasterBlockEntity.handleAction(player, containerId, Action.SET_YAW, 999);
            AutonomousCasterBlockEntity.handleAction(player, containerId, Action.SET_PITCH, -999);
            AutonomousCasterBlockEntity.handleAction(player, containerId, Action.SELECT_BULLET, 99);
            AutonomousCasterBlockEntity.handleAction(player, containerId, Action.SELECT_ITEM_SLOT, -5);
            helper.assertTrue(caster.getYawOffset() == AutonomousCasterBlockEntity.MAX_YAW
                            && caster.getPitch() == AutonomousCasterBlockEntity.MIN_PITCH
                            && caster.getSelectedBullet() == AutonomousCasterBlockEntity.BULLET_SLOTS - 1
                            && caster.getSelectedItemSlot() == 0,
                    "Out-of-range settings were not clamped");
            AutonomousCasterBlockEntity.handleAction(player, containerId, Action.SELECT_ITEM_SLOT, 20);
            helper.assertTrue(caster.getSelectedItemSlot() == AutonomousCasterBlockEntity.HOTBAR_SLOTS - 1,
                    "A slot outside the hotbar was selected as the held item");
            AutonomousCasterBlockEntity.handleAction(player, containerId, Action.CLEAR_LOG, 0);
            helper.assertTrue(caster.getLog().isEmpty(), "Clearing the log from the menu did not work");
        } finally {
            player.containerMenu = player.inventoryMenu;
            menu.removed(player);
        }
        helper.succeed();
    }

    /** 所有者のいない機械の Caster は内部キーを名乗り、所有者が決まると同じキーのまま所有者の UUID を名乗り直す。 */
    static void lendsItsOwnersIdeaStorage(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        UUID owner = UUID.randomUUID();
        AtomicReference<AutonomousCasterPlayer> ownerless = new AtomicReference<>();
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    AutonomousCasterPlayer fake = caster.getCaster();
                    helper.assertTrue(fake != null && fake.getUUID().equals(fake.casterKey()),
                            "The caster of an ownerless machine did not carry the machine key");
                    ownerless.set(fake);
                    // GameTestServer にはプロフィールキャッシュが無く、setOwnerUUID は所有者名の解決で落ちるため、保存データとして読み込ませる
                    CompoundTag security = new CompoundTag();
                    security.putUUID("owner", owner);
                    caster.getSecurity().deserialize(security, helper.getLevel().registryAccess());
                    helper.assertTrue(owner.equals(caster.getOwnerUUID()), "The test could not set the machine owner");
                })
                .thenExecuteAfter(2, () -> {
                    AutonomousCasterPlayer fake = caster.getCaster();
                    helper.assertTrue(fake != null && fake.getUUID().equals(owner), "The caster did not carry its owner's UUID");
                    helper.assertTrue(fake.casterKey().equals(ownerless.get().casterKey()) && !ownerless.get().isAttached(),
                            "The caster was not remade under the same machine key");
                    helper.assertTrue(IdeaStorageOwnerResolver.resolve(fake).equals(Optional.of(owner)),
                            "The caster did not use the owner's Ideaspace Storage");
                })
                .thenSucceed();
    }

    /**
     * Trick: Spin Chamber は機械の選択中の術式弾スロットを1つ動かす（最後の次は最初、最初の前は最後）。
     * 術式による切り替えなので、ループ回数は引き継ぐ。
     */
    static void spinChamberMovesTheSelectedBulletSlot(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        FakePlayer player = player(helper);
        int last = AutonomousCasterBlockEntity.BULLET_SLOTS - 1;
        caster.setPsi(10_000);
        caster.getBulletSlots().get(last).setStack(AutonomousCasterTestSpells.spinChamberBullet(1));
        caster.getBulletSlots().get(0).setStack(AutonomousCasterTestSpells.spinChamberBullet(-1));
        caster.applySetting(Action.SELECT_BULLET, last, player);
        helper.startSequence()
                .thenExecuteAfter(2, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(caster.getLastOutcome() == AutonomousCasterBlockEntity.Outcome.SUCCESS
                            && caster.getLoopCounter() == 1, "The machine did not cast the Spin Chamber bullet");
                    helper.assertTrue(caster.getSelectedBullet() == 0,
                            "Trick: Spin Chamber did not move the last slot on to the first, got " + caster.getSelectedBullet());
                })
                .thenExecuteAfter(caster.getCastInterval() + 1, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(caster.getLoopCounter() == 2, "The loop count was not carried over to the new slot");
                    helper.assertTrue(caster.getSelectedBullet() == last,
                            "Trick: Spin Chamber did not move the first slot back to the last, got " + caster.getSelectedBullet());
                })
                .thenSucceed();
    }

    /** Trick: Russian Roulette は機械の選択中の術式弾スロットを8つの中からランダムに選び直す。 */
    static void russianRouletteReselectsTheBulletSlot(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        FakePlayer player = player(helper);
        caster.setPsi(10_000);
        for (int i = 0; i < AutonomousCasterBlockEntity.BULLET_SLOTS; i++) {
            caster.getBulletSlots().get(i).setStack(AutonomousCasterTestSpells.russianRouletteBullet());
        }
        caster.applySetting(Action.SET_TRIGGER_MODE, AutonomousCasterBlockEntity.TriggerMode.LEVEL.ordinal(), player);
        caster.applySetting(Action.SET_INTERVAL, 1, player);
        Set<Integer> seen = new HashSet<>();
        helper.onEachTick(() -> seen.add(caster.getSelectedBullet()));
        helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
        // 最短間隔で 20 回ほど詠唱する。同じスロットのままである確率は (1/8)^19 未満。
        helper.runAfterDelay(80, () -> {
            int casts = caster.getLoopCounter();
            helper.assertTrue(casts >= 10, "The machine cast the Russian Roulette bullets only " + casts + " times");
            helper.assertTrue(seen.size() > 1 && seen.stream().allMatch(slot -> slot >= 0 && slot < AutonomousCasterBlockEntity.BULLET_SLOTS),
                    "Trick: Russian Roulette did not reselect the machine's bullet slot: " + seen);
            helper.succeed();
        });
    }

    /**
     * ループ回数は機械に1つで、成立した詠唱ごとに増える。手で別のスロットを選ぶ・選択中の弾を替える・0 ボタンで 0 に戻り、
     * 同じスロットの選び直しやほかのスロットの差し替えでは戻らない。
     */
    static void sharesOneLoopCountAndResetsItByHand(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        FakePlayer player = player(helper);
        caster.setPsi(10_000);
        caster.getBulletSlots().get(0).setStack(AutonomousCasterTestSpells.loopIndexBullet());
        caster.getBulletSlots().get(1).setStack(AutonomousCasterTestSpells.loopIndexBullet());
        int wait = caster.getCastInterval() + 1;
        helper.startSequence()
                .thenExecuteAfter(2, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    assertLatest(helper, caster, CasterLog.Source.CASTER_MESSAGE, "0.0");
                    helper.assertTrue(caster.getLoopCounter() == 1, "A successful cast did not add 1 to the loop count");
                })
                .thenExecuteAfter(wait, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    assertLatest(helper, caster, CasterLog.Source.CASTER_MESSAGE, "1.0");
                    helper.assertTrue(caster.getLoopCounter() == 2, "The second cast did not read and add to the loop count");
                    caster.getBulletSlots().get(3).setStack(AutonomousCasterTestSpells.debugBullet(5));
                    caster.applySetting(Action.SELECT_BULLET, 0, player);
                })
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(caster.getLoopCounter() == 2,
                            "Reselecting the same slot or changing another slot reset the loop count");
                    caster.applySetting(Action.SELECT_BULLET, 1, player);
                    helper.assertTrue(caster.getLoopCounter() == 0, "Selecting another slot by hand did not reset the loop count");
                })
                .thenExecuteAfter(wait, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    assertLatest(helper, caster, CasterLog.Source.CASTER_MESSAGE, "0.0");
                    helper.assertTrue(caster.getLoopCounter() == 1, "The cast after selecting a slot did not start from 0");
                    caster.getBulletSlots().get(1).setStack(AutonomousCasterTestSpells.loopIndexBullet());
                })
                .thenExecuteAfter(2, () -> helper.assertTrue(caster.getLoopCounter() == 0,
                        "Changing the selected bullet did not reset the loop count"))
                .thenExecuteAfter(wait, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(caster.getLoopCounter() == 1, "The cast after changing the bullet did not count");
                    caster.applySetting(Action.RESET_LOOP_COUNTER, 0, player);
                    helper.assertTrue(caster.getLoopCounter() == 0, "The 0 button did not reset the loop count");
                })
                .thenSucceed();
    }

    /**
     * 機械が実行した Trick: Break Loop はループ回数を 0 に戻し、その詠唱の分も数えない。
     * その前の Trick: Spin Chamber の切り替えでは、移った先のスロットがループ回数を引き継ぐ。
     */
    static void breakLoopResetsTheLoopCount(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        FakePlayer player = player(helper);
        caster.setPsi(10_000);
        caster.getBulletSlots().get(0).setStack(AutonomousCasterTestSpells.loopIndexSpinChamberBullet(1));
        caster.getBulletSlots().get(1).setStack(AutonomousCasterTestSpells.loopIndexBreakLoopBullet());
        int wait = caster.getCastInterval() + 1;
        helper.startSequence()
                .thenExecuteAfter(2, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    assertLatest(helper, caster, CasterLog.Source.CASTER_MESSAGE, "0.0");
                    helper.assertTrue(caster.getSelectedBullet() == 1 && caster.getLoopCounter() == 1,
                            "Trick: Spin Chamber did not move on with the loop count, got slot " + caster.getSelectedBullet()
                                    + " and count " + caster.getLoopCounter());
                })
                .thenExecuteAfter(wait, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    assertLatest(helper, caster, CasterLog.Source.CASTER_MESSAGE, "1.0");
                    helper.assertTrue(caster.getLastOutcome() == AutonomousCasterBlockEntity.Outcome.SUCCESS
                            && caster.getLoopCounter() == 0, "Trick: Break Loop did not reset the loop count, got " + caster.getLoopCounter());
                })
                .thenExecuteAfter(wait, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    assertLatest(helper, caster, CasterLog.Source.CASTER_MESSAGE, "0.0");
                    helper.assertTrue(caster.getLoopCounter() == 0, "The cast after Trick: Break Loop did not start from 0");
                })
                .thenSucceed();
    }

    /** ループ型術式弾は弾スロットに入らない。以前から入っていた弾は撃たずに、エラーを残す。 */
    static void refusesLoopcastBullets(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        FakePlayer player = player(helper);
        caster.setPsi(1000);
        ItemStack loopcast = AutonomousCasterTestSpells.loopcastBullet();
        IInventorySlot slot = caster.getBulletSlots().get(0);
        helper.assertTrue(!AutonomousCasterBlockEntity.isBulletCandidate(loopcast) && !slot.isItemValid(loopcast),
                "A Loopcast Spell Bullet fits in a bullet slot");
        ((BasicInventorySlot) slot).setStackUnchecked(loopcast);
        helper.startSequence()
                .thenExecuteAfter(2, () -> caster.applySetting(Action.CAST_NOW, 0, player))
                .thenExecuteAfter(2, () -> {
                    assertLatestError(helper, caster, "loopcast_bullet");
                    helper.assertTrue(caster.getLoopCounter() == 0, "The refused Loopcast Spell Bullet counted as a cast");
                })
                .thenSucceed();
    }

    /**
     * 側面設定を搬入(2)にした面からは、術式弾が空いている術式弾スロットに入る。
     * ループ型術式弾とほかのアイテムは入らず、アイテム欄にも回らない。
     */
    static void feedsBulletsFromAnInput2Side(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        ConfigInfo items = caster.getConfig().getConfig(TransmissionType.ITEM);
        helper.assertTrue(items != null && items.supports(DataType.INPUT_2), "The item side config has no Input (2) for bullets");
        items.setDataType(DataType.INPUT_2, RelativeSide.TOP);
        caster.getConfig().sideChanged(TransmissionType.ITEM, RelativeSide.TOP);
        IItemHandler top = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(POS), Direction.UP);
        helper.assertTrue(top != null, "The Input (2) side has no item handler");
        ItemStack first = ItemHandlerHelper.insertItem(top, AutonomousCasterTestSpells.debugBullet(1), false);
        ItemStack second = ItemHandlerHelper.insertItem(top, AutonomousCasterTestSpells.debugBullet(2), false);
        helper.assertTrue(first.isEmpty() && second.isEmpty()
                        && !caster.getBulletSlots().get(0).isEmpty() && !caster.getBulletSlots().get(1).isEmpty(),
                "Spell Bullets from the Input (2) side did not fill the empty bullet slots");
        ItemStack loopcast = ItemHandlerHelper.insertItem(top, AutonomousCasterTestSpells.loopcastBullet(), false);
        ItemStack log = ItemHandlerHelper.insertItem(top, new ItemStack(Items.OAK_LOG), false);
        helper.assertTrue(!loopcast.isEmpty() && !log.isEmpty(),
                "The Input (2) side took a Loopcast Spell Bullet or a non-bullet item");
        helper.assertTrue(caster.getBulletSlots().get(2).isEmpty() && caster.getItemSlots().stream().allMatch(IInventorySlot::isEmpty),
                "Items from the Input (2) side went somewhere other than the bullet slots");
        helper.succeed();
    }
}
