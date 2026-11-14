package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityChargedZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickChargedZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickTurbulenceZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;

/**
 * 作動式: 荷電領域（2026-11-12）。範囲の中の機械・持ち物・落ちているアイテムに FE を送り、AE2 のチャージ加工をし、乱流領域と
 * 重なると雷雲になって中の生物を雷撃で打つ。Mekanism・AE2 を使うテストは、入っていないときは中身を飛ばして通す。
 * <p>
 * プレイヤーをワールドに入れるので、他のテストと同時に動かないようバッチを分ける。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ChargedZoneGameTests {
    private static final String TEMPLATE = "connector_empty";

    private ChargedZoneGameTests() {
    }

    /**
     * 充電能力（2026-11-14 ユーザー）: 置いたときのコスト × 50 FE ÷ 存続時間 × 速さ。コストも存続時間も時間に比例するので、
     * 1 tick あたりの量は時間によらない。サイズ 16・威力 10（コスト 1074）の速さ 1 で約 358 FE/t（2026-11-15 に 200 FE/Psi）。
     * 雷撃は乱流の威力 1 あたり 1。
     */
    @GameTest(template = TEMPLATE)
    public static void chargedZoneCapacityFollowsTheCost(GameTestHelper helper) {
        int cost = PieceTrickChargedZone.costFor(256, 10.0);
        helper.assertTrue(cost == 1074, "Size 16, Power 10 must cost 1074 for 30 s, got " + cost);
        double perTick = EntityChargedZone.capacityPerTick(cost, 600, 1.0F);
        helper.assertTrue(Math.abs(perTick - 1074 * EntityChargedZone.FE_PER_PSI / 600.0) < 1.0E-6,
                "1074 Psi must give " + 1074 * EntityChargedZone.FE_PER_PSI / 600.0 + " FE/t, got " + perTick);
        helper.assertTrue(Math.abs(EntityChargedZone.capacityPerTick(cost * 4, 2400, 1.0F) - perTick) < 1.0E-6,
                "A 120 s zone must charge as fast per tick as a 30 s one");
        helper.assertTrue(Math.abs(EntityChargedZone.capacityPerTick(cost, 600, 0.5F) - perTick / 2.0) < 1.0E-6,
                "Speed 0.5 must halve the capacity");
        helper.assertTrue(EntityChargedZone.strikeDamage(10.0F) == 10.0F && EntityChargedZone.strikeDamage(0.0F) == 0.0F,
                "A strike must deal 1 per Power of the turbulence");
        helper.succeed();
    }

    /**
     * 充電能力は受け手で分け合う（2026-11-14 ユーザー）: 落ちているエネルギータブレット（Mekanism）が 1 つでも 4 つでも、同じ時間に
     * 入る FE の合計は同じ（充電能力 × 時間）。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_shared", timeoutTicks = 120)
    public static void chargedZoneSharesItsCapacity(GameTestHelper helper) {
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_shared_caster", new Vec3(4.5, 1.0, 4.5));
        List<ItemEntity> one = List.of(dropTablet(helper, tablet, new Vec3(0.5, 1.2, 0.5)));
        List<ItemEntity> four = List.of(dropTablet(helper, tablet, new Vec3(0.5, 1.2, 4.5)), dropTablet(helper, tablet, new Vec3(1.5, 1.2, 4.5)),
                dropTablet(helper, tablet, new Vec3(0.5, 1.2, 3.5)), dropTablet(helper, tablet, new Vec3(1.5, 1.2, 3.5)));
        Runnable cleanup = () -> {
            one.forEach(Entity::discard);
            four.forEach(Entity::discard);
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        EntityChargedZone[] zones = new EntityChargedZone[2];
        try {
            // 1 つのところと 4 つのところに、同じ大きさ・威力の領域を別々に置く。
            zones[0] = placeCharged(caster, helper.absolutePos(new BlockPos(0, 1, 0)), 1, 5.0);
            zones[1] = placeCharged(caster, helper.absolutePos(new BlockPos(1, 1, 4)), 3, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(41, () -> {
            int singleTotal = one.stream().mapToInt(item -> stored(item.getItem())).sum();
            int sharedTotal = four.stream().mapToInt(item -> stored(item.getItem())).sum();
            double singleCapacity = zones[0].capacityPerTick();
            double sharedCapacity = zones[1].capacityPerTick();
            cleanup.run();
            // 8 回（40 tick）ぶん。FE の端数は受け手ごとに 2 FE 未満まで次の回へ繰り越すので、足りないのは受け手 1 つにつき 2 FE まで。
            helper.assertTrue(singleTotal <= singleCapacity * 40 + 1 && singleTotal >= singleCapacity * 40 - 8,
                    "One tablet must take the whole capacity (" + singleCapacity * 40 + " FE in 40 ticks), got " + singleTotal);
            helper.assertTrue(sharedTotal <= sharedCapacity * 40 + 1 && sharedTotal >= sharedCapacity * 40 - 32,
                    "Four tablets must share the capacity (" + sharedCapacity * 40 + " FE in 40 ticks), got " + sharedTotal);
            int each = four.stream().mapToInt(item -> stored(item.getItem())).min().orElse(0);
            helper.assertTrue(each > 0, "Every tablet must get a share");
            helper.succeed();
        });
    }

    private static ItemEntity dropTablet(GameTestHelper helper, Item tablet, Vec3 relative) {
        Vec3 at = helper.absoluteVec(relative);
        ItemEntity dropped = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(tablet));
        dropped.setNoGravity(true);
        dropped.setDeltaMovement(Vec3.ZERO);
        dropped.setPickUpDelay(32767);
        helper.getLevel().addFreshEntity(dropped);
        return dropped;
    }

    /**
     * 生き物の手のエネルギータブレットと落ちているエネルギータブレット（Mekanism）に FE が入る。ブロック（AE2 のチャージャー）には
     * 入らない（2026-11-12 ユーザー: ブロックへの給電は外した）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_energy", timeoutTicks = 80)
    public static void chargedZoneFillsCarriedItemsButNotBlocks(GameTestHelper helper) {
        Block chargerBlock = ModList.get().isLoaded("ae2") ? block("ae2:charger") : null;
        Item tablet = ModList.get().isLoaded("mekanism") ? item("mekanism:energy_tablet") : null;
        if (chargerBlock == null && tablet == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos charger = new BlockPos(0, 1, 0);
        if (chargerBlock != null) {
            helper.setBlock(charger, chargerBlock);
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_energy_caster", new Vec3(4.5, 1.0, 4.5));
        Husk holder = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 0.5));
        holder.setNoAi(true);
        Vec3 droppedAt = helper.absoluteVec(new Vec3(0.5, 1.2, 2.5));
        ItemEntity dropped = new ItemEntity(level, droppedAt.x, droppedAt.y, droppedAt.z,
                tablet != null ? new ItemStack(tablet) : new ItemStack(Items.STONE));
        if (tablet != null) {
            holder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tablet));
            dropped.setNoGravity(true);
            dropped.setDeltaMovement(Vec3.ZERO);
            dropped.setPickUpDelay(32767);
            level.addFreshEntity(dropped);
        }
        Runnable cleanup = () -> {
            holder.discard();
            dropped.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            placeCharged(caster, helper.absolutePos(new BlockPos(2, 1, 2)), 5, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(30, () -> {
            int inCharger = chargerBlock != null ? stored(level, helper.absolutePos(charger)) : 0;
            int inHand = tablet != null ? stored(holder.getMainHandItem()) : 1;
            int onGround = tablet == null ? 1 : dropped.isAlive() ? stored(dropped.getItem()) : -1;
            cleanup.run();
            helper.assertTrue(inCharger == 0, "Blocks inside the zone must not get FE, the Charger has " + inCharger);
            helper.assertTrue(inHand > 0, "The Energy Tablet held inside the zone must get FE, has " + inHand);
            helper.assertTrue(onGround > 0, "The dropped Energy Tablet inside the zone must get FE, has " + onGround);
            helper.succeed();
        });
    }

    /** AE2 があれば、落ちているケルタスクォーツの水晶がチャージ済みケルタスクォーツの水晶になる。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_ae2", timeoutTicks = 140)
    public static void chargedZoneChargesCertusQuartz(GameTestHelper helper) {
        Item certus = item("ae2:certus_quartz_crystal");
        Item charged = item("ae2:charged_certus_quartz_crystal");
        if (!ModList.get().isLoaded("ae2") || certus == null || charged == null) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        helper.assertTrue(EntityChargedZone.ae2ChargeResult(level, new ItemStack(certus)).is(charged),
                "The AE2 Charger recipe must turn Certus Quartz into Charged Certus Quartz");
        helper.assertTrue(EntityChargedZone.ae2ChargeResult(level, new ItemStack(Items.STONE)).isEmpty(),
                "Stone has no Charger recipe");
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_ae2_caster", new Vec3(4.5, 1.0, 4.5));
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1.2, 1.5));
        ItemEntity crystal = new ItemEntity(level, at.x, at.y, at.z, new ItemStack(certus));
        crystal.setDeltaMovement(Vec3.ZERO);
        crystal.setPickUpDelay(32767);
        level.addFreshEntity(crystal);
        AABB box = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(6.0);
        Runnable cleanup = () -> {
            level.getEntitiesOfClass(ItemEntity.class, box).forEach(Entity::discard);
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            // 事象干渉力 4（速さ 2）、サイズ 9・威力 10（コスト 374）: 充電能力 約 62 FE/t、1 個 3200 FE で約 52 tick。
            caster.getAttribute(com.moratan251.psitweaks.common.attributes.PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            placeCharged(caster, helper.absolutePos(new BlockPos(2, 1, 2)), 9, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(100, () -> {
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive);
            boolean hasCharged = items.stream().anyMatch(item -> item.getItem().is(charged));
            boolean hasCertus = items.stream().anyMatch(item -> item.getItem().is(certus));
            cleanup.run();
            helper.assertTrue(hasCharged && !hasCertus, "The dropped Certus Quartz must be charged, charged " + hasCharged
                    + ", certus left " + hasCertus);
            helper.succeed();
        });
    }

    /** 乱流領域と重ねると雷雲になり、中の生物を雷撃が打つ（術者は打たない）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_charged_thundercloud", timeoutTicks = 120)
    public static void thundercloudStrikesBeingsInside(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "charged_storm_caster", new Vec3(4.5, 1.0, 4.5));
        Husk target = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 1.5)), 200.0F);
        target.setNoAi(true);
        target.setHealth(200.0F);
        Runnable cleanup = () -> {
            target.discard();
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        EntityChargedZone[] zone = new EntityChargedZone[1];
        try {
            BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
            placeCharged(caster, center, 5, 5.0);
            placeTurbulence(caster, center, 5, 10.0);
            zone[0] = SpellGramIndex.all(level, EntityChargedZone.class).stream()
                    .filter(found -> caster.getUUID().equals(found.getCasterUuid())).findFirst().orElseThrow();
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        // 乱流と重なったと気づくまで最大 10 tick、最初の雷撃までさらに最大 50 tick。
        helper.runAfterDelay(90, () -> {
            boolean storm = zone[0].isStorm();
            float health = target.getHealth();
            cleanup.run();
            helper.assertTrue(storm, "A Charged Zone overlapping the caster's Turbulence Zone must become a Thundercloud");
            helper.assertTrue(health < 200.0F, "A strike of the Thundercloud must hit the husk inside, health " + health);
            helper.succeed();
        });
    }

    private static int stored(ServerLevel level, BlockPos pos) {
        int best = 0;
        IEnergyStorage any = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (any != null) {
            best = any.getEnergyStored();
        }
        for (Direction side : Direction.values()) {
            IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
            if (storage != null) {
                best = Math.max(best, storage.getEnergyStored());
            }
        }
        return best;
    }

    private static int stored(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return storage == null ? -1 : storage.getEnergyStored();
    }

    @Nullable
    private static Block block(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        return BuiltInRegistries.BLOCK.containsKey(key) ? BuiltInRegistries.BLOCK.get(key) : null;
    }

    @Nullable
    private static Item item(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        return BuiltInRegistries.ITEM.containsKey(key) ? BuiltInRegistries.ITEM.get(key) : null;
    }

    private static MatrixValue cube(BlockPos center, int size) {
        double low = -(size / 2) - 0.5;
        return new MatrixValue(4, 4, new double[]{
                size, 0, 0, center.getX() + low,
                0, size, 0, center.getY() + low,
                0, 0, size, center.getZ() + low,
                0, 0, 0, 1});
    }

    /** {@code center} のブロックを中心にした一辺 {@code size}（奇数）、威力 {@code power} の荷電領域を置き、それを返す。 */
    private static EntityChargedZone placeCharged(ServerPlayer caster, BlockPos center, int size, double power)
            throws SpellRuntimeException {
        MatrixValue matrix = cube(center, size);
        var piece = new PieceTrickChargedZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    return matrix;
                }
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return (double) size;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? power : null;
            }
        };
        List<EntityChargedZone> before = SpellGramIndex.all(caster.level(), EntityChargedZone.class);
        execute(piece, caster);
        return SpellGramIndex.all(caster.level(), EntityChargedZone.class).stream().filter(zone -> !before.contains(zone))
                .findFirst().orElseThrow();
    }

    /** {@code center} のブロックを中心にした一辺 {@code size}（奇数）、威力 {@code power} の乱流領域を置く。 */
    private static void placeTurbulence(ServerPlayer caster, BlockPos center, int size, double power) throws SpellRuntimeException {
        MatrixValue matrix = cube(center, size);
        var piece = new PieceTrickTurbulenceZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    return matrix;
                }
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return (double) size;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? power : null;
            }
        };
        execute(piece, caster);
    }

    private static void execute(PieceTrickZoneBase<? extends SpellGramZone> piece, ServerPlayer caster) throws SpellRuntimeException {
        piece.execute(new SpellContext().setPlayer(caster));
    }
}
