package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.EntityTunnelerArrow;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageTransferContents;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TunnelerAndBucketGameTests {
    @GameTest(template = "psi110_empty")
    public static void tunnelerPiercesPoweredWither(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "tunneler-test"));
        var wither = EntityType.WITHER.create(level);
        var tunneler = new EntityTunnelerArrow(PsitweaksEntities.TUNNELER_ARROW.get(), level);
        var source = level.damageSources().source(PsitweaksDamageTypes.TUNNELER, tunneler, owner);
        for (float health : new float[] {wither.getMaxHealth(), wither.getMaxHealth() / 2, 50.0F}) {
            wither.setHealth(health);
            helper.assertTrue(wither.hurt(source, 10.0F), "Tunneler rejected at health " + health);
            helper.assertTrue(wither.getHealth() < health, "Tunneler did not damage wither");
        }
        wither.setInvulnerableTicks(100);
        helper.assertTrue(!wither.hurt(source, 10.0F), "Tunneler bypassed spawn invulnerability");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void ordinaryArrowStillBlocked(GameTestHelper helper) {
        var level = helper.getLevel();
        var wither = EntityType.WITHER.create(level);
        wither.setHealth(wither.getMaxHealth() / 2);
        var arrow = EntityType.ARROW.create(level);
        helper.assertTrue(!wither.hurt(level.damageSources().arrow(arrow, null), 10.0F),
                "Powered wither accepted an ordinary arrow");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void emptyBucketsDepositOneAtATime(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "bucket-test"));
        var storage = IdeaStorageService.get(level.getServer(), player.getUUID());
        var menu = new IdeaStorageMenu(1, player.getInventory(), player.getUUID(), 4);
        var key = ItemResourceKey.of(new ItemStack(Items.BUCKET)).orElseThrow();
        menu.setCarried(new ItemStack(Items.BUCKET, 3));
        for (int i = 1; i <= 3; i++) {
            menu.handleTransferContents(player, MessageIdeaStorageTransferContents.TARGET_NONE,
                    FluidStack.EMPTY, null, i == 2);
            helper.assertTrue(storage.simulateExtract(key, 16) == i, "Right click must deposit exactly one bucket");
            helper.assertTrue(menu.getCarried().getCount() == 3 - i, "Cursor bucket count mismatch");
        }
        storage.extract(key, 3);
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void emptyBucketStillFillsFromFluidEntry(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "bucket-fill-test"));
        var storage = IdeaStorageService.get(level.getServer(), player.getUUID());
        var water = new FluidStack(Fluids.WATER, 1000);
        storage.insertFluid(water, 1000);
        var menu = new IdeaStorageMenu(1, player.getInventory(), player.getUUID(), 4);
        menu.setCarried(new ItemStack(Items.BUCKET));
        menu.handleTransferContents(player, MessageIdeaStorageTransferContents.TARGET_FLUID, water, null, false);
        helper.assertTrue(menu.getCarried().is(Items.WATER_BUCKET), "Fluid entry did not fill bucket");
        helper.assertTrue(storage.simulateExtractFluid(FluidResourceKey.of(water).orElseThrow(), 1000) == 0,
                "Bucket filling did not consume water");
        helper.succeed();
    }
}
