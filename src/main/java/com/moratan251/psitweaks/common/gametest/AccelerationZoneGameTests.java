package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityAccelerationZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickAccelerationZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHeatZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;

import static com.moratan251.psitweaks.common.gametest.ZoneCasting.cast;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.castOrClean;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.cleanup;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.paramValue;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.zonesOf;

/**
 * 加速領域（作動式: 加速領域）。GameTest のサーバーは randomTickSpeed を 0 にしているので、育つ様子を見るテストは専用の
 * まとまり（batch）の前に randomTickSpeed を上げ、終わったら戻す。まとまりは順に走るので、ほかのテストには響かない。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AccelerationZoneGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String GROWTH_BATCH = "psitweaks_zone_acceleration_growth";
    private static final String STILL_BATCH = "psitweaks_zone_acceleration_still";
    /** 育つ様子を見るまとまりの randomTickSpeed。 */
    private static final int GROWTH_RANDOM_TICK_SPEED = 100;
    private static final BlockPos CENTER = new BlockPos(2, 2, 2);
    private static int savedGrowthSpeed;
    private static int savedStillSpeed;

    private AccelerationZoneGameTests() {
    }

    private static GameRules.IntegerValue randomTickSpeed(ServerLevel level) {
        return level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING);
    }

    @BeforeBatch(batch = GROWTH_BATCH)
    public static void raiseRandomTicks(ServerLevel level) {
        savedGrowthSpeed = randomTickSpeed(level).get();
        randomTickSpeed(level).set(GROWTH_RANDOM_TICK_SPEED, level.getServer());
    }

    @AfterBatch(batch = GROWTH_BATCH)
    public static void restoreRandomTicksAfterGrowth(ServerLevel level) {
        randomTickSpeed(level).set(savedGrowthSpeed, level.getServer());
    }

    @BeforeBatch(batch = STILL_BATCH)
    public static void stopRandomTicks(ServerLevel level) {
        savedStillSpeed = randomTickSpeed(level).get();
        randomTickSpeed(level).set(0, level.getServer());
    }

    @AfterBatch(batch = STILL_BATCH)
    public static void restoreRandomTicksAfterStill(ServerLevel level) {
        randomTickSpeed(level).set(savedStillSpeed, level.getServer());
    }

    /**
     * 選ぶ数は 体積 × randomTickSpeed ÷ 4096 × 中の威力。威力は 1〜10（中の威力 5〜14、倍率 6〜15）で、10 より上は 10 と同じ
     * （コストも増えない）。照準の表示は倍率。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_acceleration_power")
    public static void accelerationPowerSetsPicksAndMultiplier(GameTestHelper helper) {
        assertClose(helper, EntityAccelerationZone.picksPerTick(4096, 3, 14.0F), 42.0, "picks at randomTickSpeed 3, Power 10");
        assertClose(helper, EntityAccelerationZone.picksPerTick(4096, 3, 5.0F), 15.0, "picks at randomTickSpeed 3, Power 1");
        assertClose(helper, EntityAccelerationZone.picksPerTick(125, 100, 14.0F), 125.0 * 100.0 / 4096.0 * 14.0,
                "picks of a Size 5 zone");
        assertClose(helper, EntityAccelerationZone.picksPerTick(4096, 0, 14.0F), 0.0, "picks at randomTickSpeed 0");
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_accel_power", new Vec3(2.5, 1.0, 2.5));
        try {
            EntityAccelerationZone low = castAndTake(helper, caster, 5.0, 1.0);
            EntityAccelerationZone high = castAndTake(helper, caster, 5.0, 10.0);
            EntityAccelerationZone over = castAndTake(helper, caster, 5.0, 12.0);
            helper.assertTrue(low.multiplier() == 6.0F, "Power 1 must speed up 6 times, got " + low.multiplier());
            helper.assertTrue(high.multiplier() == 15.0F, "Power 10 must speed up 15 times, got " + high.multiplier());
            helper.assertTrue(over.multiplier() == 15.0F, "Power above 10 must count as 10, got " + over.multiplier());
            int cost = PieceTrickZoneBase.resourceZoneCost(25, 10.0);
            helper.assertTrue(cost == 190, "Size 5, Power 10 must cost 190, got " + cost);
            helper.assertTrue(high.getPlacementCost() == cost && over.getPlacementCost() == cost,
                    "Power above 10 must not cost more: " + high.getPlacementCost() + " / " + over.getPlacementCost());
            Object shown = ((TranslatableContents) high.hudLines().get(0).getContents()).getArgs()[0];
            helper.assertTrue("15".equals(shown), "The HUD must show the multiplier 15, got " + shown);
        } catch (SpellRuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            cleanup(helper, caster);
        }
        helper.succeed();
    }

    /** randomTickSpeed が 0 なら、加速領域の中でも何も育たない。 */
    @GameTest(template = TEMPLATE, batch = STILL_BATCH, timeoutTicks = 200)
    public static void accelerationZoneDoesNothingWithoutRandomTicks(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_accel_still", new Vec3(2.5, 2.0, 3.5));
        buildCanesAndCacti(helper);
        Runnable cleanup = () -> {
            clearBox(helper);
            cleanup(helper, caster);
        };
        castOrClean(caster, acceleration(helper), 5.0, 10.0, cleanup);
        helper.runAfterDelay(100, () -> {
            int canes = sumGrowth(helper, CANES, Blocks.SUGAR_CANE, SugarCaneBlock.AGE);
            cleanup.run();
            helper.assertTrue(canes == 0, "Nothing may grow while randomTickSpeed is 0, sugar cane grew " + canes);
            helper.succeed();
        });
    }

    /**
     * タグに入っているサトウキビは速く育ち、タグにないサボテンはバニラの速さのまま（randomTickSpeed 100、威力 10 で 100 tick）。
     * サトウキビ 1 本の一番上には 1 tick に約 0.37 回（バニラの 15 倍）のランダムティックが来て、6 本の伸びの合計の見込みは
     * 150 を超える（3 段で頭打ち）。サボテン 3 本はバニラの分だけで、合計の見込みは約 7。どちらも閾値（60 以上・30 以下）を
     * 外れる見込みは 10 億回に 1 回より小さい。
     */
    @GameTest(template = TEMPLATE, batch = GROWTH_BATCH, timeoutTicks = 200)
    public static void accelerationZoneGrowsOnlyTaggedPlants(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_accel_growth", new Vec3(2.5, 2.0, 3.5));
        buildCanesAndCacti(helper);
        Runnable cleanup = () -> {
            clearBox(helper);
            cleanup(helper, caster);
        };
        castOrClean(caster, acceleration(helper), 5.0, 10.0, cleanup);
        helper.runAfterDelay(100, () -> {
            int canes = sumGrowth(helper, CANES, Blocks.SUGAR_CANE, SugarCaneBlock.AGE);
            int cacti = sumGrowth(helper, CACTI, Blocks.CACTUS, CactusBlock.AGE);
            cleanup.run();
            helper.assertTrue(canes >= 60, "Sugar cane (in the tag) must grow faster, grew " + canes + " in all");
            helper.assertTrue(cacti <= 30, "Cacti (not in the tag) must grow at the usual speed, grew " + cacti + " in all");
            helper.succeed();
        });
    }

    /**
     * 芽生えたアメジスト（#c:budding_blocks）も速く育つ（randomTickSpeed 100、威力 10 で 300 tick）。4 つで芽の段の合計の見込みは
     * 約 62（ばらつき約 5）、バニラだけなら約 6。25 以上なら速くなっている（外れる見込みは 10 億回に 1 回より小さい）。
     */
    @GameTest(template = TEMPLATE, batch = GROWTH_BATCH, timeoutTicks = 400)
    public static void accelerationZoneGrowsBuddingAmethyst(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_accel_amethyst", new Vec3(2.5, 1.0, 2.5));
        List<BlockPos> budding = List.of(new BlockPos(1, 2, 1), new BlockPos(3, 2, 1), new BlockPos(1, 2, 3), new BlockPos(3, 2, 3));
        budding.forEach(pos -> helper.setBlock(pos, Blocks.BUDDING_AMETHYST));
        Runnable cleanup = () -> {
            clearBox(helper);
            cleanup(helper, caster);
        };
        castOrClean(caster, acceleration(helper), 5.0, 10.0, cleanup);
        helper.runAfterDelay(300, () -> {
            int stages = 0;
            for (int x = 0; x <= 4; x++) {
                for (int y = 0; y <= 4; y++) {
                    for (int z = 0; z <= 4; z++) {
                        stages += budStage(helper.getBlockState(new BlockPos(x, y, z)));
                    }
                }
            }
            cleanup.run();
            helper.assertTrue(stages >= 25, "Budding Amethyst must grow faster, grew " + stages + " bud stages in all");
            helper.succeed();
        });
    }

    /**
     * 同じ術者の加速領域（威力 10、15 倍）が重なった加熱領域は、積もった雪を速く溶かす（randomTickSpeed によらない）。
     * 加熱だけなら 1 か所が 40 tick で溶ける見込みは約 33%、15 倍なら約 99.8%。25 か所のうち 20 か所以上
     * （外れる見込みは、速まっていれば 1 兆回に 1 回より小さく、速まっていなければ通る見込みが約 50 万分の 1）。ほかの術者の
     * 加速領域は速めない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_acceleration_heat", timeoutTicks = 150)
    public static void accelerationSpeedsUpOwnZonesOnly(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_accel_heat", new Vec3(2.5, 2.0, 2.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "zone_accel_heat_other", new Vec3(2.5, 2.0, 2.5));
        BlockPos snowCenter = new BlockPos(2, 1, 2);
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, 1, z), Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
            }
        }
        Runnable cleanup = () -> {
            clearBox(helper);
            cleanup(helper, caster);
            cleanup(helper, other);
        };
        try {
            cast(caster, heat(helper, snowCenter), 5.0, 1.0);
            cast(caster, acceleration(helper, snowCenter), 5.0, 10.0);
            cast(other, acceleration(helper, snowCenter), 5.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        EntityHeatZone heatZone = zonesOf(helper, caster, EntityHeatZone.class).get(0);
        List<EntityAccelerationZone> boosters = EntityAccelerationZone.boostersOf(heatZone);
        double inside = EntityAccelerationZone.multiplierAt(boosters, helper.absolutePos(snowCenter));
        double outside = EntityAccelerationZone.multiplierAt(boosters, helper.absolutePos(new BlockPos(2, 1, 9)));
        if (boosters.size() != 1 || boosters.get(0).getCasterUuid() == null
                || !boosters.get(0).getCasterUuid().equals(caster.getUUID())) {
            cleanup.run();
            helper.fail("Only the caster's own Acceleration Zone may speed up the Heat Zone, found " + boosters.size());
            return;
        }
        if (inside != 15.0 || outside != 1.0) {
            cleanup.run();
            helper.fail("The multiplier must be 15 inside and 1 outside, got " + inside + " and " + outside);
            return;
        }
        helper.runAfterDelay(40, () -> {
            int melted = 0;
            for (int x = 0; x <= 4; x++) {
                for (int z = 0; z <= 4; z++) {
                    if (helper.getBlockState(new BlockPos(x, 1, z)).isAir()) {
                        melted++;
                    }
                }
            }
            int count = melted;
            cleanup.run();
            helper.assertTrue(count >= 20, "Snow under the caster's own Acceleration Zone must melt faster, melted " + count + " of 25");
            helper.succeed();
        });
    }

    /** サトウキビ（砂の上、水路の両側に 3 本ずつ、y 2）とサボテン（砂の上、y 2）を置く。床は石（y 0）。 */
    private static void buildCanesAndCacti(GameTestHelper helper) {
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 1, 1), Blocks.STONE);
        for (int x = 1; x <= 3; x++) {
            helper.setBlock(new BlockPos(x, 1, 0), Blocks.SAND);
            helper.setBlock(new BlockPos(x, 1, 2), Blocks.SAND);
            helper.setBlock(new BlockPos(x, 1, 1), Blocks.WATER);
        }
        for (BlockPos cane : CANES) {
            helper.setBlock(cane, Blocks.SUGAR_CANE);
        }
        for (BlockPos cactus : CACTI) {
            helper.setBlock(cactus.below(), Blocks.SAND);
            helper.setBlock(cactus, Blocks.CACTUS);
        }
    }

    private static final List<BlockPos> CANES = List.of(new BlockPos(1, 2, 0), new BlockPos(2, 2, 0), new BlockPos(3, 2, 0),
            new BlockPos(1, 2, 2), new BlockPos(2, 2, 2), new BlockPos(3, 2, 2));
    private static final List<BlockPos> CACTI = List.of(new BlockPos(0, 2, 4), new BlockPos(2, 2, 4), new BlockPos(4, 2, 4));

    /**
     * 植物 {@code bases}（根元の位置）の伸びの合計: 1 本ごとに 16 ×（段の数 − 1）+ 一番上の年齢（年齢が 15 を超えると 1 段伸び、
     * 年齢は 0 に戻る）。
     */
    private static int sumGrowth(GameTestHelper helper, List<BlockPos> bases, net.minecraft.world.level.block.Block plant,
                                 IntegerProperty age) {
        int total = 0;
        for (BlockPos base : bases) {
            BlockPos top = base;
            while (helper.getBlockState(top.above()).is(plant)) {
                top = top.above();
            }
            BlockState state = helper.getBlockState(top);
            if (!state.is(plant)) {
                continue;
            }
            total += 16 * (top.getY() - base.getY()) + state.getValue(age);
        }
        return total;
    }

    /** アメジストの芽の段（小 1・中 2・大 3・塊 4、ほかは 0）。 */
    private static int budStage(BlockState state) {
        if (!(state.getBlock() instanceof AmethystClusterBlock)) {
            return 0;
        }
        if (state.is(Blocks.SMALL_AMETHYST_BUD)) {
            return 1;
        }
        if (state.is(Blocks.MEDIUM_AMETHYST_BUD)) {
            return 2;
        }
        if (state.is(Blocks.LARGE_AMETHYST_BUD)) {
            return 3;
        }
        return state.is(Blocks.AMETHYST_CLUSTER) ? 4 : 0;
    }

    /** テンプレートの 5×5×5（y 0〜4）を上から空気にする（植物を壊したときの落とし物を出さないように）。 */
    private static void clearBox(GameTestHelper helper) {
        for (int y = 4; y >= 0; y--) {
            for (int x = 0; x <= 4; x++) {
                for (int z = 0; z <= 4; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    private static void assertClose(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-6, what + ": expected " + expected + ", got " + actual);
    }

    /** 加速領域を置き、置いたもの（一番新しいもの）を返して取り除く。 */
    private static EntityAccelerationZone castAndTake(GameTestHelper helper, ServerPlayer caster, double size, double power)
            throws SpellRuntimeException {
        cast(caster, acceleration(helper), size, power);
        List<EntityAccelerationZone> zones = zonesOf(helper, caster, EntityAccelerationZone.class);
        EntityAccelerationZone zone = zones.get(zones.size() - 1);
        zone.discard();
        return zone;
    }

    private static PieceTrickZoneBase<?> acceleration(GameTestHelper helper) {
        return acceleration(helper, CENTER);
    }

    /** 入力を {@link ZoneCasting#cast} の値で返す加速領域のピース（術式の盤面なしで置くため）。 */
    private static PieceTrickZoneBase<?> acceleration(GameTestHelper helper, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        return new PieceTrickAccelerationZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }

    private static PieceTrickZoneBase<?> heat(GameTestHelper helper, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        return new PieceTrickHeatZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }
}
