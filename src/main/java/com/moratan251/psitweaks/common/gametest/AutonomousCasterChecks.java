package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.AutonomousCasterSettings;
import com.moratan251.psitweaks.common.caster.CasterLog;
import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import com.moratan251.psitweaks.common.compat.ConnectorMekanism;
import com.moratan251.psitweaks.common.network.MessageAutonomousCasterAction.Action;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageOwnerResolver;
import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.AutomationType;
import mekanism.common.attachments.containers.item.AttachedItems;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import vazkii.psi.api.cad.ICAD;
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
            helper.assertTrue(caster.getLog().isEmpty() && caster.getLoopCounter(0) == 0, "The machine cast without a signal");
            helper.setBlock(power, Blocks.REDSTONE_BLOCK);
            helper.runAfterDelay(2, () -> {
                assertLatest(helper, caster, CasterLog.Source.CASTER_MESSAGE, "42");
                helper.assertTrue(caster.getLastOutcome() == AutonomousCasterBlockEntity.Outcome.SUCCESS && caster.getLoopCounter(0) == 1,
                        "A redstone pulse did not cast the selected bullet once");
                helper.assertTrue(caster.getPsi() == 1000, "A free spell spent Psi");
                helper.runAfterDelay(caster.getCastInterval() + 4, () -> {
                    helper.assertTrue(caster.getLoopCounter(0) == 1, "Pulse mode cast again while the signal stayed on");
                    helper.setBlock(power, Blocks.AIR);
                    helper.runAfterDelay(2, () -> {
                        helper.setBlock(power, Blocks.REDSTONE_BLOCK);
                        helper.runAfterDelay(2, () -> {
                            List<CasterLog.Entry> entries = caster.getLog().entries();
                            helper.assertTrue(caster.getLoopCounter(0) == 2, "A second pulse did not cast again");
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
            int casts = caster.getLoopCounter(2);
            helper.assertTrue(casts >= 4 && casts <= 6, "Level mode cast " + casts + " times in 20 ticks at a 4 tick interval");
            helper.setBlock(POS.east(), Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                int stopped = caster.getLoopCounter(2);
                helper.runAfterDelay(10, () -> {
                    helper.assertTrue(caster.getLoopCounter(2) == stopped, "Level mode kept casting without a signal");
                    caster.getBulletSlots().get(2).setStack(AutonomousCasterTestSpells.debugBullet(8));
                    helper.runAfterDelay(2, () -> {
                        helper.assertTrue(caster.getLoopCounter(2) == 0, "Changing the bullet did not reset its loop counter");
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
            helper.assertTrue(caster.hasSignal() && caster.getLoopCounter(0) == 0 && caster.getLog().isEmpty(),
                    "A signal that was already on when the machine was placed caused a cast");
            helper.setBlock(POS.east(), Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
                helper.runAfterDelay(2, () -> {
                    helper.assertTrue(caster.getLoopCounter(0) == 1, "A new pulse after placement did not cast");
                    helper.succeed();
                });
            });
        });
    }

    static void mirrorsItsHotbar(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        caster.getItemSlots().get(2).setStack(new ItemStack(Items.STONE, 5));
        helper.runAfterDelay(2, () -> {
            AutonomousCasterPlayer fake = caster.getCaster();
            helper.assertTrue(fake != null, "The machine did not prepare its caster");
            var inventory = fake.getInventory();
            helper.assertTrue(inventory.getItem(2).is(Items.STONE) && inventory.getItem(2).getCount() == 5,
                    "Machine items were not mirrored to the caster's hotbar");
            helper.assertTrue(inventory.getItem(AutonomousCasterPlayer.CAD_SLOT).getItem() instanceof ICAD,
                    "The virtual CAD is not in the caster's inventory");
            // 術式がアイテムを使い、ホットバー外に別のアイテムを受け取り、CAD 枠を空けた場合
            inventory.getItem(2).shrink(2);
            inventory.setItem(20, new ItemStack(Items.APPLE, 3));
            inventory.setItem(AutonomousCasterPlayer.CAD_SLOT, ItemStack.EMPTY);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(caster.getItemSlots().get(2).getStack().getCount() == 3,
                        "Items used by the caster were not taken from the machine");
                helper.assertTrue(inventory.getItem(20).isEmpty() && count(caster.getItemSlots(), Items.APPLE) == 3,
                        "Items given to the caster were not stored in the machine");
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

    static void keepsContentsWhenBroken(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        ItemStack bullet = AutonomousCasterTestSpells.debugBullet(5);
        ItemStack assembly = new ItemStack(ModCADComponents.cadAssemblyPsimetal.get());
        caster.getBulletSlots().get(1).setStack(bullet.copy());
        caster.getComponentSlots().get(0).setStack(assembly.copy());
        caster.getItemSlots().get(3).setStack(new ItemStack(Items.STONE, 9));
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
            helper.assertTrue(ItemStack.isSameItem(restored.getComponentSlots().get(0).getStack(), assembly),
                    "The CAD component did not come back");
            helper.assertTrue(restored.getItemSlots().get(3).getStack().getCount() == 9, "The held items did not come back");
            helper.assertTrue(restored.getEchoTank().getStored() == 2000, "The echo gas did not come back");
            helper.assertTrue(restored.getPsi() == 0 && restored.getLog().isEmpty(), "Psi or the log was carried over");
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
            AutonomousCasterBlockEntity.handleAction(player, containerId, Action.CLEAR_LOG, 0);
            helper.assertTrue(caster.getLog().isEmpty(), "Clearing the log from the menu did not work");
        } finally {
            player.containerMenu = player.inventoryMenu;
            menu.removed(player);
        }
        helper.succeed();
    }

    static void lendsItsOwnersIdeaStorage(GameTestHelper helper) {
        AutonomousCasterBlockEntity caster = place(helper);
        UUID owner = UUID.randomUUID();
        // GameTestServer にはプロフィールキャッシュが無く、setOwnerUUID は所有者名の解決で落ちるため、保存データとして読み込ませる
        CompoundTag security = new CompoundTag();
        security.putUUID("owner", owner);
        caster.getSecurity().deserialize(security, helper.getLevel().registryAccess());
        helper.assertTrue(owner.equals(caster.getOwnerUUID()), "The test could not set the machine owner");
        helper.runAfterDelay(2, () -> {
            AutonomousCasterPlayer fake = caster.getCaster();
            helper.assertTrue(fake != null && !fake.getUUID().equals(owner), "The caster impersonated the owner");
            helper.assertTrue(IdeaStorageOwnerResolver.resolve(fake).equals(Optional.of(owner)),
                    "The caster did not use the owner's Ideaspace Storage");
            helper.succeed();
        });
    }
}
