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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
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
        tunneler.setOwner(owner);
        var arrowSource = tunneler.arrowDamageSource();
        helper.assertTrue(arrowSource.is(PsitweaksDamageTypes.TUNNELER) && arrowSource.getDirectEntity() == tunneler
                && arrowSource.getEntity() == owner, "The arrow's damage source lost its type, arrow or shooter");
        for (float health : new float[] {wither.getMaxHealth(), wither.getMaxHealth() / 2, 50.0F}) {
            wither.setHealth(health);
            var source = tunneler.hurtSourceFor(wither, arrowSource);
            helper.assertTrue((source.getDirectEntity() == null) == wither.isPowered(),
                    "Only a powered wither should get a source without the arrow, at health " + health);
            helper.assertTrue(source.is(PsitweaksDamageTypes.TUNNELER) && source.getEntity() == owner,
                    "The hurt source lost its type or shooter at health " + health);
            helper.assertTrue(wither.hurt(source, 10.0F), "Tunneler rejected at health " + health);
            helper.assertTrue(wither.getHealth() < health, "Tunneler did not damage wither");
        }
        wither.setInvulnerableTicks(100);
        helper.assertTrue(!wither.hurt(tunneler.hurtSourceFor(wither, arrowSource), 10.0F),
                "Tunneler bypassed spawn invulnerability");
        var zombie = EntityType.ZOMBIE.create(level);
        helper.assertTrue(tunneler.hurtSourceFor(zombie, arrowSource) == arrowSource,
                "Targets other than a powered wither should get the arrow's own damage source");
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

    /**
     * テンプレート（1×1×1）のブロックの中に、止まった矢を作る（ワールドには入れない）。{@code x} はブロックの中の位置（0〜1）。
     * テンプレートの外に置くと、強制読み込みされていない隣のチャンクに入ってエンティティが tick しないことがある。
     */
    private static EntityTunnelerArrow restingArrow(GameTestHelper helper, double x) {
        var arrow = new EntityTunnelerArrow(PsitweaksEntities.TUNNELER_ARROW.get(), helper.getLevel());
        Vec3 at = helper.absoluteVec(new Vec3(x, 1.5, 0.5));
        arrow.setPos(at.x, at.y, at.z);
        arrow.setDeltaMovement(Vec3.ZERO);
        return arrow;
    }

    /** 経過tick数は保存され、読み込んだ矢は続きから数える（tickCount のように 0 に戻らない）。 */
    @GameTest(template = "psi110_empty")
    public static void tunnelerAgeSurvivesSaveAndLoad(GameTestHelper helper) {
        var arrow = restingArrow(helper, 0.5);
        arrow.setAge(150);
        CompoundTag tag = arrow.saveWithoutId(new CompoundTag());
        var loaded = new EntityTunnelerArrow(PsitweaksEntities.TUNNELER_ARROW.get(), helper.getLevel());
        loaded.load(tag);
        helper.assertTrue(loaded.getAge() == 150, "Loaded tunneler age should be 150, got " + loaded.getAge());
        helper.succeed();
    }

    /**
     * 経過tick数を保存する前に保存された矢（"life" はあるが経過tick数がない）は最初の tick で消え、/summon で出した矢
     * （どちらもない）は消えない。
     */
    @GameTest(template = "psi110_empty")
    public static void tunnelerSavedWithoutAgeIsDiscarded(GameTestHelper helper) {
        var level = helper.getLevel();
        CompoundTag saved = restingArrow(helper, 0.2).saveWithoutId(new CompoundTag());
        saved.remove("TunnelerAge");
        saved.remove("UUID");
        var legacy = new EntityTunnelerArrow(PsitweaksEntities.TUNNELER_ARROW.get(), level);
        legacy.load(saved);
        var summoned = new EntityTunnelerArrow(PsitweaksEntities.TUNNELER_ARROW.get(), level);
        summoned.load(new CompoundTag());
        Vec3 at = helper.absoluteVec(new Vec3(0.8, 1.5, 0.5));
        summoned.setPos(at.x, at.y, at.z);
        summoned.setDeltaMovement(Vec3.ZERO);
        summoned.setNoGravity(true);
        level.addFreshEntity(legacy);
        level.addFreshEntity(summoned);
        helper.succeedWhen(() -> {
            helper.assertTrue(legacy.isRemoved(), "A tunneler saved without its age should be discarded");
            boolean summonedRemoved = summoned.isRemoved();
            summoned.discard();
            helper.assertTrue(!summonedRemoved, "A summoned tunneler should not be discarded");
        });
    }

    /** 射手のそばに残る通常モードの矢も、{@link EntityTunnelerArrow#MAX_LIFETIME_TICKS} で消える。 */
    @GameTest(template = "psi110_empty")
    public static void tunnelerNearOwnerDespawnsAtMaxLifetime(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "tunneler-lifetime"));
        owner.setPos(helper.absoluteVec(new Vec3(0.5, 1.0, 0.5)));
        var old = restingArrow(helper, 0.2);
        old.setOwner(owner);
        old.setAge(EntityTunnelerArrow.MAX_LIFETIME_TICKS - 1);
        var young = restingArrow(helper, 0.8);
        young.setOwner(owner);
        young.setAge(300);
        level.addFreshEntity(old);
        level.addFreshEntity(young);
        helper.succeedWhen(() -> {
            helper.assertTrue(old.isRemoved(), "A tunneler near its owner should despawn at the max lifetime");
            boolean youngRemoved = young.isRemoved();
            young.discard();
            helper.assertTrue(!youngRemoved, "A tunneler near its owner should stay before the max lifetime");
        });
    }

    /**
     * エンティティが tick しないチャンクにいる矢は、その tick のうちに消える（シミュレーション距離の外で止まって保存されない）。
     * tick するテストの場所の矢は消えない（対照）。どちらもワールドに入れずに 1 tick だけ回す。
     */
    @GameTest(template = "psi110_empty")
    public static void tunnelerInNonTickingChunkIsDiscarded(GameTestHelper helper) {
        var level = helper.getLevel();
        helper.succeedWhen(() -> {
            var inside = restingArrow(helper, 0.5);
            helper.assertTrue(level.isPositionEntityTicking(inside.blockPosition()), "The test area does not tick entities yet");
            var outside = restingArrow(helper, 0.5);
            outside.setPos(outside.getX() + 65536.0, outside.getY(), outside.getZ());
            helper.assertTrue(!level.isPositionEntityTicking(outside.blockPosition()), "65536 blocks away should not tick entities");
            inside.tick();
            outside.tick();
            helper.assertTrue(!inside.isRemoved(), "A tunneler in a ticking chunk should stay");
            helper.assertTrue(outside.isRemoved(), "A tunneler in a chunk that does not tick entities should be discarded");
        });
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
