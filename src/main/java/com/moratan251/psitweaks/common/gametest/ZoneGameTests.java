package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.CreateCompat;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityIceCircle;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramLimits;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.entities.SpellGram.marker.EntityMeteorLineNight;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityGravityZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySoakZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGravityZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSoakZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;

/**
 * 領域型の魔法式オブジェクト（試作: 湿潤領域・加重領域）。範囲は領域行列を中心 ±大きさで切り詰めたブロックの集まりで、
 * 中の生物にだけ効く。
 * <p>
 * プレイヤーをワールドに入れるので、他のテストと同時に動かないようバッチを分ける。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZoneGameTests {
    private static final String TEMPLATE = "connector_empty";

    private ZoneGameTests() {
    }

    /**
     * 範囲は行列の中のブロックのうち、行列の中心のブロックを中心にした一辺サイズの立方体に入るもの（演算子: 直方体領域と
     * 同じ数え方で、偶数は正の向きに 1 つ多い）。行列が小さければ行列の範囲。
     */
    @GameTest(template = TEMPLATE)
    public static void zoneAreaIsTheMatrixClippedAroundItsCenter(GameTestHelper helper) throws SpellRuntimeException {
        // ブロック 0〜8 を含む 9×9×9 の行列（作動式: 領域表示のブロックのモードと同じく、整数の座標で判定する）。
        MatrixValue big = cube(new Vector3(-0.5, -0.5, -0.5), 9);
        ZoneArea clipped = ZoneArea.of(big, 5);
        helper.assertTrue(clipped.center().equals(new BlockPos(4, 4, 4)), "The center block should be 4,4,4: " + clipped.center());
        helper.assertTrue(clipped.contains(new BlockPos(2, 4, 6)) && clipped.contains(new BlockPos(6, 2, 2)),
                "Blocks of the 5-block cube around the center must be inside");
        helper.assertFalse(clipped.contains(new BlockPos(1, 4, 4)) || clipped.contains(new BlockPos(4, 7, 4)),
                "Blocks farther than the Size must be outside");
        helper.assertTrue(clipped.bounds().getXsize() == 5 && clipped.bounds().getYsize() == 5,
                "The outline must wrap 5 blocks per axis: " + clipped.bounds());

        ZoneArea even = ZoneArea.of(big, 4);
        helper.assertTrue(even.contains(new BlockPos(3, 4, 4)) && even.contains(new BlockPos(6, 4, 4))
                        && !even.contains(new BlockPos(2, 4, 4)) && !even.contains(new BlockPos(7, 4, 4)),
                "An even Size must reach one block farther on the positive side, like Operator: Cuboid Region");

        ZoneArea whole = ZoneArea.of(big, PieceTrickZoneBase.MAX_SIZE);
        helper.assertTrue(whole.contains(new BlockPos(0, 0, 0)) && whole.contains(new BlockPos(8, 8, 8))
                        && !whole.contains(new BlockPos(9, 4, 4)) && whole.bounds().getXsize() == 9,
                "A matrix smaller than the Size must be the zone as it is");
        helper.assertTrue(PieceTrickZoneBase.areaOf(5) == 25 && PieceTrickZoneBase.areaOf(17) == 289,
                "The area is the Size squared");
        helper.succeed();
    }

    /**
     * 効く範囲は輪郭の線のとおり（2026-11-12 ユーザー）: 回した行列は、行列の中だけだと輪郭の箱の角が欠けるので、{@link ZoneArea#boxed}
     * で輪郭の箱にする。中心のブロックは変わらず、軸にそった行列はそのまま。
     */
    @GameTest(template = TEMPLATE)
    public static void tiltedZoneAreaFollowsItsOutline(GameTestHelper helper) throws SpellRuntimeException {
        // 原点を中心にした 9×9×9 の立方体を Z 軸のまわりに 45° 回した行列。
        MatrixValue tilted = turnedCube(9);
        ZoneArea raw = ZoneArea.of(tilted, 9);
        helper.assertTrue(raw.contains(BlockPos.ZERO) && !raw.contains(new BlockPos(4, 4, 0)),
                "Inside the turned matrix alone, the outline's corners are cut off");
        ZoneArea boxed = raw.boxed();
        helper.assertTrue(boxed.center().equals(BlockPos.ZERO), "The center block must stay: " + boxed.center());
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    helper.assertTrue(boxed.contains(new BlockPos(x, y, z)), "Every block of the outline must be inside: " + x + "," + y + "," + z);
                }
            }
        }
        helper.assertFalse(boxed.contains(new BlockPos(5, 0, 0)) || boxed.contains(new BlockPos(0, -5, 0)),
                "Blocks outside the outline must stay outside");
        helper.assertTrue(boxed.bounds().equals(raw.bounds()) && boxed.bounds().getXsize() == 9,
                "The outline must not change: " + boxed.bounds());

        // 小さい立方体を回すと、輪郭はそれを囲む箱（サイズまで）で、範囲もその箱になる。
        ZoneArea small = ZoneArea.of(turnedCube(3), 9).boxed();
        helper.assertTrue(small.bounds().getXsize() == 5 && small.bounds().getYsize() == 5 && small.bounds().getZsize() == 3
                        && small.contains(new BlockPos(2, 2, 1)) && small.contains(new BlockPos(-2, 2, -1)),
                "A small turned cube must become the box around it: " + small.bounds());

        ZoneArea straight = ZoneArea.of(cube(new Vector3(-0.5, -0.5, -0.5), 9), 5);
        helper.assertTrue(straight.boxed() == straight, "An axis-aligned matrix must be kept as it is");
        helper.succeed();
    }

    /** 原点を中心にした一辺 {@code edge} の立方体を、Z 軸のまわりに 45° 回した領域行列。 */
    private static MatrixValue turnedCube(double edge) {
        double c = Math.cos(Math.PI / 4) * edge;
        double s = Math.sin(Math.PI / 4) * edge;
        // 辺 a = (c, s, 0)、b = (-s, c, 0)、c = (0, 0, edge)。始点 = 中心 − (a + b + c) / 2。
        return new MatrixValue(4, 4, new double[]{
                c, -s, 0, -(c - s) * 0.5,
                s, c, 0, -(s + c) * 0.5,
                0, 0, edge, -edge * 0.5,
                0, 0, 0, 1});
    }

    /** 湿潤領域の中の生物は濡れ、外の生物は濡れない。同じ種類の領域を重ねて置ける（2026-11-11 から）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_soak", timeoutTicks = 100)
    public static void soakZoneSoaksOnlyTheBeingsInside(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_soak_caster", new Vec3(0.5, 1.0, 0.5));
        Husk inside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 2.5));
        Husk outside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(6.5, 1.0, 6.5));
        inside.setNoAi(true);
        outside.setNoAi(true);
        BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            GameTestPlayers.remove(helper, caster);
        };
        try {
            // 中心のブロックを中心にした 3×3×3（サイズ 3）。行列は 7×7×7 なのでサイズで切り詰められる。
            castSoak(caster, cube(vector(center).add(-3.5, -3.5, -3.5), 7), 3.0);
            castSoak(caster, cube(vector(center).add(-3.5, -3.5, -3.5), 7), 3.0);
            List<EntitySoakZone> zones = SpellGramIndex.near(level, Vec3.atCenterOf(center), 0.5, EntitySoakZone.class);
            helper.assertTrue(zones.size() == 2, "Two Soak Zones of one caster must both stay, found " + zones.size());
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(15, () -> {
            boolean insideWet = CombatStatuses.isSoaked(inside);
            boolean outsideWet = CombatStatuses.isSoaked(outside);
            cleanup.run();
            helper.assertTrue(insideWet, "The husk inside the Soak Zone must be wet");
            helper.assertFalse(outsideWet, "The husk outside the Soak Zone must stay dry");
            helper.succeed();
        });
    }

    /**
     * 1 人が置ける魔法式オブジェクトは、サークルと領域を合わせて上限（既定 32）まで。超えて置くといちばん古いものが解ける。
     * 流星群の夜は数えない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_limit", timeoutTicks = 40)
    public static void placingBeyondTheLimitEndsTheOldest(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_limit_caster", new Vec3(2.5, 1.0, 2.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            GameTestPlayers.remove(helper, caster);
        };
        try {
            int max = SpellGramLimits.maxFor(caster);
            helper.assertTrue(max == SpellGramLimits.maxPerPlayer() && max == SpellGramLimits.DEFAULT_MAX_PER_CASTER
                            && SpellGramLimits.maxPerAutonomousCaster() == SpellGramLimits.DEFAULT_MAX_PER_CASTER,
                    "Players and Autonomous Casters must each default to 32, player limit was " + max);
            helper.assertTrue(SpellGramLimits.counts(new EntityIceCircle(level)) && SpellGramLimits.counts(new EntitySoakZone(level))
                            && !SpellGramLimits.counts(new EntityMeteorLineNight(level, Vec3.ZERO, 1.0)),
                    "Circles and zones count toward the limit, the Meteor Line night does not");
            castSoak(caster, null, 1.0);
            List<SpellGramObject> first = SpellGramLimits.placedBy(level, caster.getUUID());
            helper.assertTrue(first.size() == 1, "The first zone must be placed, found " + first.size());
            SpellGramObject oldest = first.get(0);
            for (int i = 1; i <= max; i++) {
                castSoak(caster, null, 1.0);
            }
            List<SpellGramObject> placed = SpellGramLimits.placedBy(level, caster.getUUID());
            helper.assertTrue(placed.size() == max, "Only " + max + " objects may stay, found " + placed.size());
            helper.assertTrue(oldest.isEnded() && !placed.contains(oldest), "The oldest zone must end first");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** サイズの上限は 16（2026-11-12、32 から変更）。それより大きいサイズで置くと、一辺 16 の範囲に切り詰める。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_size", timeoutTicks = 40)
    public static void zoneSizeIsClampedToTheMaximum(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_size_caster", new Vec3(0.5, 1.0, 0.5));
        BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
        try {
            helper.assertTrue(PieceTrickZoneBase.MAX_SIZE == 16, "The largest Size of a zone must be 16");
            // 行列は 49×49×49、サイズは 40。どちらも上限より大きいので、一辺 16 になる。
            castSoak(caster, cube(vector(center).add(-24.5, -24.5, -24.5), 49), 40.0);
            List<EntitySoakZone> zones = SpellGramIndex.near(level, Vec3.atCenterOf(center), 0.5, EntitySoakZone.class);
            helper.assertTrue(zones.size() == 1, "The Soak Zone must be placed, found " + zones.size());
            var outline = zones.get(0).getOutline();
            helper.assertTrue(outline.getXsize() == 16 && outline.getYsize() == 16 && outline.getZsize() == 16,
                    "A Size above the limit must give a 16-block cube: " + outline);
        } catch (RuntimeException | SpellRuntimeException e) {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            GameTestPlayers.remove(helper, caster);
            throw new RuntimeException(e);
        }
        SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
        GameTestPlayers.remove(helper, caster);
        helper.succeed();
    }

    /** 行列を省くと、術者のいるブロックを中心にした一辺サイズの立方体になる（偶数は正の向きに 1 つ多い）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_no_matrix", timeoutTicks = 40)
    public static void zoneWithoutMatrixIsTheCubeAroundTheCaster(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_no_matrix_caster", new Vec3(2.5, 1.0, 2.5));
        BlockPos at = helper.absolutePos(new BlockPos(2, 1, 2));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            GameTestPlayers.remove(helper, caster);
        };
        try {
            helper.assertTrue(caster.blockPosition().equals(at), "The caster must stand in " + at + ": " + caster.blockPosition());
            castSoak(caster, null, 3.0);
            List<EntitySoakZone> zones = SpellGramIndex.near(level, Vec3.atCenterOf(at), 0.5, EntitySoakZone.class);
            helper.assertTrue(zones.size() == 1, "A Soak Zone without a matrix must be placed around the caster, found " + zones.size());
            ZoneArea area = zones.get(0).getArea();
            helper.assertTrue(area.center().equals(at), "The center must be the caster's block: " + area.center());
            helper.assertTrue(area.contains(at.offset(-1, -1, -1)) && area.contains(at.offset(1, 1, 1)),
                    "The 3-block cube around the caster must be inside");
            helper.assertFalse(area.contains(at.offset(2, 0, 0)) || area.contains(at.offset(0, -2, 0)),
                    "Blocks farther than the Size must be outside");
            var outline = zones.get(0).getOutline();
            helper.assertTrue(outline.getXsize() == 3 && outline.getYsize() == 3 && outline.getZsize() == 3,
                    "The outline must wrap 3 blocks per axis: " + outline);

            ZoneArea even = ZoneArea.cube(at, 4);
            helper.assertTrue(even.contains(at.offset(-1, 0, 0)) && even.contains(at.offset(2, 0, 0))
                            && !even.contains(at.offset(-2, 0, 0)) && !even.contains(at.offset(3, 0, 0)),
                    "An even Size must reach one block farther on the positive side, like Operator: Cuboid Region");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** 加重領域は中のエンティティ（生物も落ちているアイテムも）を指定の向きへ押し続ける。外の生物は持ち上げない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_gravity", timeoutTicks = 100)
    public static void gravityZonePushesTheBeingsInside(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_gravity_caster", new Vec3(0.5, 1.0, 0.5));
        Husk inside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 2.5));
        Husk outside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(6.5, 1.0, 6.5));
        // 落ちているアイテムも押す（エンティティ全般）。
        Vec3 itemAt = helper.absoluteVec(new Vec3(3.5, 1.0, 2.5));
        ItemEntity item = new ItemEntity(level, itemAt.x, itemAt.y, itemAt.z, new ItemStack(Items.STICK));
        item.setNeverPickUp();
        item.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(item);
        BlockPos center = helper.absolutePos(new BlockPos(2, 2, 2));
        double insideY = inside.getY();
        double outsideY = outside.getY();
        double itemY = item.getY();
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            item.discard();
            GameTestPlayers.remove(helper, caster);
        };
        try {
            // 5×5×5（サイズ 5）。威力 10 で上向きに押す。
            var piece = new PieceTrickGravityZone(new Spell()) {
                @Override
                public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                        return cube(vector(center).add(-2.5, -2.5, -2.5), 5);
                    }
                    if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                        return 5.0;
                    }
                    return param.name.equals(SpellParam.GENERIC_NAME_DIRECTION) ? new Vector3(0, 3, 0) : 10.0;
                }
            };
            piece.execute(new SpellContext().setPlayer(caster));
            List<EntityGravityZone> zones = SpellGramIndex.near(level, Vec3.atCenterOf(center), 0.5, EntityGravityZone.class);
            helper.assertTrue(zones.size() == 1 && zones.get(0).getPushDirection().y > 0.99,
                    "The Gravity Zone must be placed with an upward direction");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(12, () -> {
            double insideRise = inside.getY() - insideY;
            double outsideRise = outside.getY() - outsideY;
            double itemRise = item.getY() - itemY;
            cleanup.run();
            helper.assertTrue(insideRise > 0.5, "The husk inside must be lifted, rose " + insideRise);
            helper.assertTrue(itemRise > 0.5, "The dropped item inside must be lifted too, rose " + itemRise);
            helper.assertTrue(outsideRise < 0.1, "The husk outside must not be lifted, rose " + outsideRise);
            helper.succeed();
        });
    }

    /** 湿潤領域は中のブロックに水をかけたように効く: 炎・焚き火・ろうそくを消し、コンクリートパウダーを固める。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_soak_blocks", timeoutTicks = 100)
    public static void soakZoneDousesFireAndSetsConcrete(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_soak_blocks", new Vec3(0.5, 1.0, 0.5));
        BlockPos fire = new BlockPos(2, 1, 2);
        BlockPos campfire = new BlockPos(3, 1, 2);
        BlockPos candle = new BlockPos(1, 1, 2);
        BlockPos powder = new BlockPos(2, 1, 3);
        BlockPos outside = new BlockPos(6, 1, 6);
        for (BlockPos below : List.of(fire, campfire, candle, powder, outside)) {
            helper.setBlock(below.below(), Blocks.STONE);
        }
        helper.setBlock(fire, Blocks.FIRE);
        helper.setBlock(campfire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        helper.setBlock(candle, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true));
        helper.setBlock(powder, Blocks.WHITE_CONCRETE_POWDER);
        helper.setBlock(outside, Blocks.WHITE_CONCRETE_POWDER);
        BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            GameTestPlayers.remove(helper, caster);
        };
        try {
            // 5×5×5（サイズ 5）。外のコンクリートパウダーは範囲の外。
            castSoak(caster, cube(vector(center).add(-2.5, -2.5, -2.5), 5), 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(10, () -> {
            boolean fireGone = !helper.getBlockState(fire).is(Blocks.FIRE);
            boolean campfireOut = !helper.getBlockState(campfire).getValue(CampfireBlock.LIT);
            boolean candleOut = !helper.getBlockState(candle).getValue(CandleBlock.LIT);
            boolean concrete = helper.getBlockState(powder).is(Blocks.WHITE_CONCRETE);
            boolean outsideKept = helper.getBlockState(outside).is(Blocks.WHITE_CONCRETE_POWDER);
            cleanup.run();
            helper.assertTrue(fireGone, "The Soak Zone must put out the fire inside");
            helper.assertTrue(campfireOut, "The Soak Zone must douse the campfire inside");
            helper.assertTrue(candleOut, "The Soak Zone must put out the candle inside");
            helper.assertTrue(concrete, "The Soak Zone must turn the concrete powder inside into concrete");
            helper.assertTrue(outsideKept, "Concrete powder outside the zone must stay powder");
            helper.succeed();
        });
    }

    /** Create があるとき、湿潤領域は中の落ちているアイテムを一括洗浄する（マグマブロック → 黒曜石）。Create がなければ飛ばす。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_soak_create", timeoutTicks = 400)
    public static void soakZoneWashesItemsWithCreate(GameTestHelper helper) {
        if (!CreateCompat.isCreateLoaded()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_soak_create", new Vec3(0.5, 1.0, 0.5));
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
        Vec3 itemAt = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
        ItemEntity item = new ItemEntity(level, itemAt.x, itemAt.y, itemAt.z, new ItemStack(Items.MAGMA_BLOCK));
        item.setNeverPickUp();
        item.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(item);
        BlockPos center = helper.absolutePos(new BlockPos(2, 1, 2));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
            level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(center).inflate(4.0)).forEach(ItemEntity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            // 事象干渉力 4 → 洗う速さは上限の 2 倍。洗う速さは置いた時点の干渉力で決まる。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            castSoak(caster, cube(vector(center).add(-2.5, -2.5, -2.5), 5), 5.0);
            List<EntitySoakZone> zones = SpellGramIndex.near(level, Vec3.atCenterOf(center), 0.5, EntitySoakZone.class);
            helper.assertTrue(zones.size() == 1 && zones.get(0).getMachineSpeed() == SpellGramZone.MAX_MACHINE_SPEED,
                    "The washing speed must come from the caster's interference, capped at twice a fan");
            helper.assertTrue(SpellGramZone.machineSpeedFor(1.0) == 0.5F && SpellGramZone.machineSpeedFor(2.0) == 1.0F,
                    "Interference 1 washes at half a fan, 2 at a fan");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        // Create の処理時間（既定 150 tick）を 2 倍の速さで進めるので、75 tick ほどで洗い上がる。
        helper.runAfterDelay(110, () -> {
            boolean washed = !level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(center).inflate(4.0),
                    entity -> entity.getItem().is(Items.OBSIDIAN)).isEmpty();
            cleanup.run();
            helper.assertTrue(washed, "The Soak Zone must wash the magma block into obsidian with Create");
            helper.succeed();
        });
    }

    private static void castSoak(ServerPlayer caster, @Nullable MatrixValue matrix, double size) throws SpellRuntimeException {
        var piece = new PieceTrickSoakZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    return matrix;
                }
                return param.name.equals(PsitweaksSpellParams.SIZE) ? size : null;
            }
        };
        piece.execute(new SpellContext().setPlayer(caster));
    }

    private static Vector3 vector(BlockPos pos) {
        return new Vector3(pos.getX(), pos.getY(), pos.getZ());
    }

    private static MatrixValue cube(Vector3 start, double edge) {
        return new MatrixValue(4, 4, new double[]{
                edge, 0, 0, start.x,
                0, edge, 0, start.y,
                0, 0, edge, start.z,
                0, 0, 0, 1});
    }
}
