package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.compat.CreateCompat;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.MekanismGameplayCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityFreezeZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityInterferenceZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySoakZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneHeatExchange;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickFreezeZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHeatZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickInterferenceZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSoakZone;
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
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;
import java.util.Optional;

/**
 * 加熱・凍結・乱流・領域干渉の領域（作動式: 加熱領域・凍結領域・乱流領域・領域干渉）。ほとんどは 5×5×5（サイズ 5）を、
 * テンプレートの (2, 1, 2) を中心に置く。術者は範囲の中に立たせ、巻き込まれないことも確かめる。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZoneElementGameTests {
    private static final String TEMPLATE = "connector_empty";
    /** 風力発電機（高さ 5）を 3 台並べ、屋根もかける区画（12×6×5、ブロックなし）。 */
    private static final String WIND_TEMPLATE = "zone_wind_generators";
    private static final BlockPos CENTER = new BlockPos(2, 1, 2);
    /** 水・氷を置く場所（石で囲む）。囲いの石が別の場所に重ならないよう 2 つおきにする。 */
    private static final BlockPos OPEN = new BlockPos(1, 1, 1);
    private static final BlockPos COVERED = new BlockPos(3, 1, 1);
    private static final BlockPos THIRD = new BlockPos(1, 1, 3);

    private ZoneElementGameTests() {
    }

    /** 加熱領域: 中の生物は燃え、術者は燃えない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat", timeoutTicks = 100)
    public static void heatZoneBurnsBeings(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_caster", new Vec3(4.5, 1.0, 4.5));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 3.5));
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, heat(helper), 5.0, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(25, () -> {
            boolean huskBurning = husk.getRemainingFireTicks() > 0;
            boolean casterBurning = caster.getRemainingFireTicks() > 0;
            cleanup.run();
            helper.assertTrue(huskBurning, "The husk inside the Heat Zone must be set on fire");
            helper.assertFalse(casterBurning, "The Heat Zone must not burn its own caster");
            helper.succeed();
        });
    }

    /**
     * ランダムなブロックの変化を待つ長さ（tick、既定の存続時間 600 tick より短く）。1 ブロックは 1 tick に
     * {@link SpellGramZone#RANDOM_BLOCK_TICK_CHANCE}（1/100）で選ばれるので、500 tick のうちに一度も選ばれない見込みは
     * 1 ブロックあたり約 0.66%。テストは「いくつかのうち何個以上」で確かめ、どのテストも落ちる見込みが 10 万回に 1 回より
     * ずっと小さくなるよう、数と閾値を選ぶ（毎日のレビューでも GameTest を回すため）。
     */
    private static final int RANDOM_WAIT = 500;

    /** {@code spots} のうち、状態が {@code changed} に当てはまるものの数。 */
    private static int countMatching(GameTestHelper helper, List<BlockPos> spots, java.util.function.Predicate<BlockState> changed) {
        int count = 0;
        for (BlockPos spot : spots) {
            if (changed.test(helper.getBlockState(spot))) {
                count++;
            }
        }
        return count;
    }

    /**
     * 加熱領域: ランダムに選んだブロックで、氷は水に戻り、氷塊はそのまま（水が流れ出さないよう石で囲む）。範囲は (2, 1, 2) を
     * 中心にした一辺 5。氷 4 つのうち 2 つ以上（落ちる見込み約 1.1×10⁻⁶）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_ice", timeoutTicks = RANDOM_WAIT + 100)
    public static void heatZoneMeltsIceRandomly(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_ice", new Vec3(4.5, 1.0, 4.5));
        // 氷は四隅の囲いの中。真ん中は四隅の囲いの石に囲まれるので、そこに氷塊を置く。
        BlockPos packed = CENTER;
        List<BlockPos> ices = List.of(OPEN, COVERED, THIRD, new BlockPos(3, 1, 3));
        ices.forEach(pos -> basin(helper, pos, Blocks.ICE));
        helper.setBlock(packed.below(), Blocks.STONE);
        helper.setBlock(packed, Blocks.PACKED_ICE);
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, heat(helper), 5.0, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(RANDOM_WAIT, () -> {
            int melted = countMatching(helper, ices, state -> state.is(Blocks.WATER));
            boolean packedKept = helper.getBlockState(packed).is(Blocks.PACKED_ICE);
            cleanup.run();
            helper.assertTrue(melted >= 2, "The Heat Zone must melt ice into water, melted " + melted + " of 4");
            helper.assertTrue(packedKept, "Packed ice must stay");
            helper.succeed();
        });
    }

    /**
     * 加熱領域: ランダムに選んだブロックで、積もった雪は層の数によらず消える（近くに光源があるときと同じ、水は残らない）。
     * 雪ブロックと、範囲の外の積もった雪はそのまま。範囲は (2, 1, 2) を中心にした一辺 5（x・z 0〜4）。1 層・8 層それぞれ
     * 4 つのうち 2 つ以上（落ちる見込み約 2.3×10⁻⁶）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_snow", timeoutTicks = RANDOM_WAIT + 100)
    public static void heatZoneMeltsSnowRandomly(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_snow", new Vec3(4.5, 1.0, 4.5));
        List<BlockPos> thin = List.of(new BlockPos(0, 1, 0), new BlockPos(1, 1, 0), new BlockPos(2, 1, 0), new BlockPos(3, 1, 0));
        List<BlockPos> deep = List.of(new BlockPos(0, 1, 4), new BlockPos(1, 1, 4), new BlockPos(2, 1, 4), new BlockPos(3, 1, 4));
        BlockPos block = CENTER;
        BlockPos outside = new BlockPos(6, 1, 2);
        List<BlockPos> spots = new java.util.ArrayList<>(thin);
        spots.addAll(deep);
        spots.add(block);
        spots.add(outside);
        spots.forEach(pos -> helper.setBlock(pos.below(), Blocks.STONE));
        thin.forEach(pos -> helper.setBlock(pos, Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS, 1)));
        deep.forEach(pos -> helper.setBlock(pos, Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS, 8)));
        helper.setBlock(block, Blocks.SNOW_BLOCK);
        helper.setBlock(outside, Blocks.SNOW);
        Runnable cleanup = () -> {
            spots.forEach(pos -> {
                helper.setBlock(pos, Blocks.AIR);
                helper.setBlock(pos.below(), Blocks.AIR);
            });
            cleanup(helper, caster);
        };
        try {
            cast(caster, heat(helper), 5.0, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(RANDOM_WAIT, () -> {
            int thinMelted = countMatching(helper, thin, BlockState::isAir);
            int deepMelted = countMatching(helper, deep, BlockState::isAir);
            boolean blockKept = helper.getBlockState(block).is(Blocks.SNOW_BLOCK);
            boolean outsideKept = helper.getBlockState(outside).is(Blocks.SNOW);
            cleanup.run();
            helper.assertTrue(thinMelted >= 2, "One-layer snow inside the Heat Zone must melt away, melted " + thinMelted + " of 4");
            helper.assertTrue(deepMelted >= 2, "Eight-layer snow inside the Heat Zone must melt away, melted " + deepMelted + " of 4");
            helper.assertTrue(blockKept, "A Snow Block must stay");
            helper.assertTrue(outsideKept, "Snow outside the Heat Zone must stay");
            helper.succeed();
        });
    }

    /** 範囲 (2, 1, 2) を中心にした一辺 3 の、真ん中を除く 8 か所（y 1）。 */
    private static List<BlockPos> ringAroundCenter() {
        List<BlockPos> ring = new java.util.ArrayList<>();
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                if (x != 2 || z != 2) {
                    ring.add(new BlockPos(x, 1, z));
                }
            }
        }
        return ring;
    }

    /** 加熱領域: ランダムに選んだブロックで、濡れたスポンジが乾く（8 つのうち 5 つ以上、落ちる見込み約 1.3×10⁻⁷）。範囲の外の濡れたスポンジはそのまま。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_sponge", timeoutTicks = RANDOM_WAIT + 100)
    public static void heatZoneDriesSpongesRandomly(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_sponge", new Vec3(4.5, 1.0, 4.5));
        List<BlockPos> sponges = ringAroundCenter();
        BlockPos outside = new BlockPos(6, 1, 2);
        sponges.forEach(pos -> helper.setBlock(pos, Blocks.WET_SPONGE));
        helper.setBlock(outside, Blocks.WET_SPONGE);
        Runnable cleanup = () -> {
            sponges.forEach(pos -> helper.setBlock(pos, Blocks.AIR));
            helper.setBlock(outside, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            cast(caster, heat(helper, CENTER), 3.0, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(RANDOM_WAIT, () -> {
            int dried = countMatching(helper, sponges, state -> state.is(Blocks.SPONGE));
            boolean outsideKept = helper.getBlockState(outside).is(Blocks.WET_SPONGE);
            cleanup.run();
            helper.assertTrue(dried >= 5, "Wet Sponges inside the Heat Zone must dry, dried " + dried + " of 8");
            helper.assertTrue(outsideKept, "A Wet Sponge outside the Heat Zone must stay wet");
            helper.succeed();
        });
    }

    /**
     * 荷電領域: ランダムに選んだブロックで、錆びる銅の錆が 1 段ずつ落ちる（雷が落ちたときと同じ）。向きなどの状態は保つ。
     * 錆止めされた銅と、範囲の外の銅はそのまま。1 段でも落ちれば数え（1 度選ばれれば足りる）、7 つのうち 4 つ以上
     * （落ちる見込み約 6.4×10⁻⁸）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_charged_copper", timeoutTicks = RANDOM_WAIT + 100)
    public static void chargedZoneScrapesCopperRandomly(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_charged_copper", new Vec3(4.5, 1.0, 4.5));
        List<BlockPos> ring = ringAroundCenter();
        List<BlockPos> blocks = ring.subList(0, 7);
        BlockPos stairs = ring.get(7);
        BlockPos waxed = CENTER;
        BlockPos outside = new BlockPos(6, 1, 2);
        blocks.forEach(pos -> helper.setBlock(pos, Blocks.OXIDIZED_COPPER));
        helper.setBlock(stairs, Blocks.OXIDIZED_CUT_COPPER_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, net.minecraft.core.Direction.EAST));
        helper.setBlock(waxed, Blocks.WAXED_OXIDIZED_COPPER);
        helper.setBlock(outside, Blocks.OXIDIZED_COPPER);
        Runnable cleanup = () -> {
            ring.forEach(pos -> helper.setBlock(pos, Blocks.AIR));
            helper.setBlock(waxed, Blocks.AIR);
            helper.setBlock(outside, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            cast(caster, charged(helper, CENTER), 3.0, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(RANDOM_WAIT, () -> {
            int scraped = countMatching(helper, blocks, state -> state.is(Blocks.WEATHERED_COPPER) || state.is(Blocks.EXPOSED_COPPER)
                    || state.is(Blocks.COPPER_BLOCK));
            BlockState stairsState = helper.getBlockState(stairs);
            boolean stairsKeptShape = stairsState.getBlock() instanceof net.minecraft.world.level.block.StairBlock
                    && stairsState.getValue(net.minecraft.world.level.block.StairBlock.FACING) == net.minecraft.core.Direction.EAST;
            boolean waxedKept = helper.getBlockState(waxed).is(Blocks.WAXED_OXIDIZED_COPPER);
            boolean outsideKept = helper.getBlockState(outside).is(Blocks.OXIDIZED_COPPER);
            cleanup.run();
            helper.assertTrue(scraped >= 4, "Copper inside the Charged Zone must lose its oxidation, scraped " + scraped + " of 7");
            helper.assertTrue(stairsKeptShape, "Copper stairs must keep their shape and facing, got " + stairsState);
            helper.assertTrue(waxedKept, "Waxed copper must stay as it is");
            helper.assertTrue(outsideKept, "Copper outside the Charged Zone must stay oxidized");
            helper.succeed();
        });
    }

    /**
     * 湿潤領域: ランダムに選んだブロックで、耕地は一番湿った状態（7）になり、スポンジは濡れたスポンジになる。範囲の外の
     * スポンジはそのまま。耕地はバニラのランダムティックでも乾くので、5 以上（始めは 2）を湿ったとみなす。耕地・スポンジ
     * それぞれ 4 つのうち 2 つ以上（落ちる見込み約 2.6×10⁻⁶）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_soak_random_blocks", timeoutTicks = RANDOM_WAIT + 100)
    public static void soakZoneMoistensFarmlandAndWetsSpongesRandomly(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_soak_blocks", new Vec3(4.5, 1.0, 4.5));
        List<BlockPos> ring = ringAroundCenter();
        List<BlockPos> farmland = ring.subList(0, 4);
        List<BlockPos> sponges = ring.subList(4, 8);
        BlockPos outside = new BlockPos(6, 1, 2);
        farmland.forEach(pos -> helper.setBlock(pos, Blocks.FARMLAND.defaultBlockState()
                .setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 2)));
        sponges.forEach(pos -> helper.setBlock(pos, Blocks.SPONGE));
        helper.setBlock(outside, Blocks.SPONGE);
        Runnable cleanup = () -> {
            ring.forEach(pos -> helper.setBlock(pos, Blocks.AIR));
            helper.setBlock(outside, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            cast(caster, soak(helper, CENTER), 3.0, 0.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(RANDOM_WAIT, () -> {
            int moist = countMatching(helper, farmland, state -> state.is(Blocks.FARMLAND)
                    && state.getValue(net.minecraft.world.level.block.FarmBlock.MOISTURE) >= 5);
            int wet = countMatching(helper, sponges, state -> state.is(Blocks.WET_SPONGE));
            boolean outsideKept = helper.getBlockState(outside).is(Blocks.SPONGE);
            cleanup.run();
            helper.assertTrue(moist >= 2, "Farmland inside the Soak Zone must be moistened, moist " + moist + " of 4");
            helper.assertTrue(wet >= 2, "Sponges inside the Soak Zone must get wet, wet " + wet + " of 4");
            helper.assertTrue(outsideKept, "A Sponge outside the Soak Zone must stay dry");
            helper.succeed();
        });
    }

    /**
     * 同じ術者の加熱領域と湿潤領域が重なったところのスポンジは、乾かすのと濡らすのを行き来しないよう、どちらもしない。
     * 湿潤領域は x 1〜3、加熱領域は x 3〜5 で、x 3 が重なり。湿潤だけのところのスポンジは濡れ、加熱だけのところの濡れた
     * スポンジは乾く（それぞれ 3 つのうち 1 つ以上、落ちる見込み約 5.7×10⁻⁷）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_soak_sponge", timeoutTicks = RANDOM_WAIT + 100)
    public static void heatAndSoakOverlapLeaveSpongesAlone(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_soak_sponge", new Vec3(7.5, 1.0, 4.5));
        List<BlockPos> soakOnly = List.of(new BlockPos(1, 1, 1), new BlockPos(1, 1, 2), new BlockPos(1, 1, 3));
        List<BlockPos> heatOnly = List.of(new BlockPos(5, 1, 1), new BlockPos(5, 1, 2), new BlockPos(5, 1, 3));
        List<BlockPos> overlapDry = List.of(new BlockPos(3, 1, 1), new BlockPos(3, 1, 2));
        BlockPos overlapWet = new BlockPos(3, 1, 3);
        soakOnly.forEach(pos -> helper.setBlock(pos, Blocks.SPONGE));
        heatOnly.forEach(pos -> helper.setBlock(pos, Blocks.WET_SPONGE));
        overlapDry.forEach(pos -> helper.setBlock(pos, Blocks.SPONGE));
        helper.setBlock(overlapWet, Blocks.WET_SPONGE);
        Runnable cleanup = () -> {
            soakOnly.forEach(pos -> helper.setBlock(pos, Blocks.AIR));
            heatOnly.forEach(pos -> helper.setBlock(pos, Blocks.AIR));
            overlapDry.forEach(pos -> helper.setBlock(pos, Blocks.AIR));
            helper.setBlock(overlapWet, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            cast(caster, soak(helper, CENTER), 3.0, 0.0);
            cast(caster, heat(helper, new BlockPos(4, 1, 2)), 3.0, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(RANDOM_WAIT, () -> {
            int wetted = countMatching(helper, soakOnly, state -> state.is(Blocks.WET_SPONGE));
            int dried = countMatching(helper, heatOnly, state -> state.is(Blocks.SPONGE));
            int overlapKeptDry = countMatching(helper, overlapDry, state -> state.is(Blocks.SPONGE));
            boolean overlapKeptWet = helper.getBlockState(overlapWet).is(Blocks.WET_SPONGE);
            cleanup.run();
            helper.assertTrue(wetted >= 1, "Sponges under the Soak Zone alone must get wet, wet " + wetted + " of 3");
            helper.assertTrue(dried >= 1, "Wet Sponges under the Heat Zone alone must dry, dried " + dried + " of 3");
            helper.assertTrue(overlapKeptDry == 2 && overlapKeptWet,
                    "Sponges where your Heat and Soak Zones overlap must stay as they are: dry kept " + overlapKeptDry
                            + " of 2, wet kept " + overlapKeptWet);
            helper.succeed();
        });
    }

    /**
     * 加熱領域の中のダメージは、威力によらずバニラの炎上と同じ毎秒 1（威力 1・3・6・8 でも 0 にならず、ほかの威力でも 2 に
     * ならない）。外に出たあとは 威力 × 0.6 秒ほど燃え続ける。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_damage_3", timeoutTicks = 120)
    public static void heatZoneDamageFollowsPower3(GameTestHelper helper) {
        heatZoneDamageFollowsPower(helper, 3.0F);
    }

    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_damage_4", timeoutTicks = 120)
    public static void heatZoneDamageFollowsPower4(GameTestHelper helper) {
        heatZoneDamageFollowsPower(helper, 4.0F);
    }

    private static void heatZoneDamageFollowsPower(GameTestHelper helper, float power) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_damage_" + (int) power, new Vec3(4.5, 1.0, 4.5));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 2.5));
        husk.setNoAi(true);
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, heat(helper, CENTER), 5.0, power);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        float[] before = new float[1];
        helper.runAfterDelay(20, () -> before[0] = husk.getHealth());
        helper.runAfterDelay(80, () -> {
            float lost = before[0] - husk.getHealth();
            int burning = husk.getRemainingFireTicks();
            int burnTicks = SpellGramIndex.all(helper.getLevel(), EntityHeatZone.class).stream().findFirst()
                    .map(EntityHeatZone::burnTicks).orElse(-1);
            cleanup.run();
            // 燃える残り時間を 20 tick 単位で足すので、60 tick の間にちょうど 3 回（バニラの炎上、1 回 1）。
            helper.assertTrue(Math.abs(lost - 3.0F) < 0.05F,
                    "Power " + power + " must burn like vanilla inside, 1 a second (3 in 3 s), lost " + lost);
            // 足し直すのは 威力 × 0.6 秒を切ったときだけなので、残りはそれから 10 tick 未満しか減っていない。
            helper.assertTrue(burnTicks > 0 && burning + 10 > burnTicks,
                    "The husk must keep burning for Power x 0.6 s after leaving: " + burning + " ticks, burn " + burnTicks);
            helper.succeed();
        });
    }

    /**
     * 加熱領域の中で燃え続ける長さは、Fire Protection（属性 burning_time）で短くなる（バニラの火と同じ掛け方）。
     * burning_time 0.4 なら、威力 × 0.6 秒の 0.4 倍を切らないよう 20 tick 単位で足す。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_fire_protection", timeoutTicks = 100)
    public static void heatZoneBurnTimeFollowsFireProtection(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_fire_protection", new Vec3(4.5, 1.0, 4.5));
        Husk plain = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 2.5));
        Husk protectedHusk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 3.5));
        plain.setNoAi(true);
        protectedHusk.setNoAi(true);
        protectedHusk.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.BURNING_TIME).setBaseValue(0.4);
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, heat(helper, CENTER), 5.0, 5.0F);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            int plainTicks = plain.getRemainingFireTicks();
            int protectedTicks = protectedHusk.getRemainingFireTicks();
            int burnTicks = SpellGramIndex.all(helper.getLevel(), EntityHeatZone.class).stream().findFirst()
                    .map(EntityHeatZone::burnTicks).orElse(-1);
            int shortened = (int) Math.ceil(burnTicks * 0.4);
            cleanup.run();
            helper.assertTrue(burnTicks > 30, "The heat zone must burn for more than 30 ticks, got " + burnTicks);
            helper.assertTrue(plainTicks >= burnTicks && plainTicks < burnTicks + 20,
                    "Without Fire Protection the husk must keep " + burnTicks + " ticks of fire, got " + plainTicks);
            helper.assertTrue(protectedTicks >= shortened && protectedTicks < shortened + 20,
                    "Burning time 0.4 must shorten the fire to " + shortened + " ticks, got " + protectedTicks);
            helper.succeed();
        });
    }

    /**
     * Create があるとき、加熱領域は同じ術者の乱流領域が重なったところ（熱乱流）で、落ちているアイテムを一括燻製する
     * （生の牛肉 → ステーキ）。Create がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_create", timeoutTicks = 400)
    public static void heatZoneSmokesItemsWithCreate(GameTestHelper helper) {
        heatSmokeTest(helper, true);
    }

    /** 乱流領域が重なっていない加熱領域は一括燻製しない。他人の乱流領域が重なっていても同じ。Create がなければ飛ばす。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_create_no_turbulence", timeoutTicks = 400)
    public static void heatZoneDoesNotSmokeWithoutOwnTurbulence(GameTestHelper helper) {
        heatSmokeTest(helper, false);
    }

    private static void heatSmokeTest(GameTestHelper helper, boolean ownTurbulence) {
        if (!CreateCompat.isCreateLoaded()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, ownTurbulence ? "zone_heat_create" : "zone_heat_create_alone",
                new Vec3(4.5, 1.0, 4.5));
        ServerPlayer other = ownTurbulence ? null : GameTestPlayers.inLevel(helper, "zone_heat_create_other", new Vec3(0.5, 1.0, 4.5));
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
        Vec3 itemAt = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
        ItemEntity item = new ItemEntity(level, itemAt.x, itemAt.y, itemAt.z, new ItemStack(Items.BEEF));
        item.setNeverPickUp();
        item.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(item);
        AABB around = new AABB(helper.absolutePos(CENTER)).inflate(4.0);
        Runnable cleanup = () -> {
            level.getEntitiesOfClass(ItemEntity.class, around).forEach(ItemEntity::discard);
            if (other != null) {
                cleanup(helper, other);
            }
            cleanup(helper, caster);
        };
        try {
            // 事象干渉力 4 → 速さは上限の 2 倍（Create の処理時間 150 tick を 75 tick ほどで）。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, heat(helper), 5.0, 5.0);
            // 一括燻製は同じ術者の乱流領域が重なったところだけ。燻製しない場合も、他人の乱流領域を同じ範囲に重ねる。
            cast(ownTurbulence ? caster : other, turbulence(helper), 5.0, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(110, () -> {
            boolean smoked = !level.getEntitiesOfClass(ItemEntity.class, around,
                    entity -> entity.getItem().is(Items.COOKED_BEEF)).isEmpty();
            boolean rawKept = !level.getEntitiesOfClass(ItemEntity.class, around,
                    entity -> entity.getItem().is(Items.BEEF)).isEmpty();
            cleanup.run();
            if (ownTurbulence) {
                helper.assertTrue(smoked, "A Heat Zone with your own Turbulence Zone must smoke raw beef into steak with Create");
            } else {
                helper.assertTrue(!smoked && rawKept, "A Heat Zone without your own Turbulence Zone must not smoke items");
            }
            helper.succeed();
        });
    }

    /** Mekanism があるとき、加熱領域は中の抵抗型発熱器を温める。Mekanism がなければ飛ばす。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_mekanism", timeoutTicks = 100)
    public static void heatZoneHeatsMekanismBlocks(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_mekanism", new Vec3(4.5, 1.0, 4.5));
        BlockPos heaterPos = new BlockPos(1, 1, 1);
        helper.setBlock(heaterPos, heater.get());
        BlockPos absolute = helper.absolutePos(heaterPos);
        double before = MekanismGameplayCompat.temperatureAt(level, absolute);
        Runnable cleanup = () -> {
            helper.setBlock(heaterPos, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            helper.assertTrue(!Double.isNaN(before), "The Resistive Heater must hold heat");
            cast(caster, heat(helper), 5.0, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            double after = MekanismGameplayCompat.temperatureAt(level, absolute);
            cleanup.run();
            helper.assertTrue(after > before + 5.0,
                    "The Heat Zone must heat the Resistive Heater, " + before + " K -> " + after + " K");
            helper.succeed();
        });
    }

    /**
     * 加熱領域が Mekanism の機械を温める上限は、周囲の温度によらず 500 + 中の威力 × 50 K（1150 K で頭打ち）。入れた威力 1
     * （中の威力 5）で上限 750 K なので、周囲の温度の抵抗型発熱器を温める。上限の式も確かめる。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_mekanism_low_power", timeoutTicks = 100)
    public static void heatZoneWithPowerOneHeatsMekanismBlocks(GameTestHelper helper) {
        helper.assertTrue(EntityHeatZone.maxTemperatureFor(5.0F) == 750.0 && EntityHeatZone.maxTemperatureFor(10.0F) == 1000.0
                        && EntityHeatZone.maxTemperatureFor(13.0F) == 1150.0 && EntityHeatZone.maxTemperatureFor(14.0F) == 1150.0
                        && EntityHeatZone.maxTemperatureFor(100.0F) == 1150.0,
                "The highest temperature must be 500 + Power x 50 K, stopping at 1150 K");
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_mekanism_low_power", new Vec3(4.5, 1.0, 4.5));
        BlockPos heaterPos = new BlockPos(1, 1, 1);
        helper.setBlock(heaterPos, heater.get());
        BlockPos absolute = helper.absolutePos(heaterPos);
        double before = MekanismGameplayCompat.temperatureAt(level, absolute);
        Runnable cleanup = () -> {
            helper.setBlock(heaterPos, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            helper.assertTrue(!Double.isNaN(before), "The Resistive Heater must hold heat");
            // 威力 1（中の威力 5）・一辺 5（コスト 100）、干渉力 4 → 速さ 2 で約 240 J/t。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, heat(helper), 5.0, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            double after = MekanismGameplayCompat.temperatureAt(level, absolute);
            cleanup.run();
            helper.assertTrue(after > before + 5.0,
                    "A Power 1 Heat Zone must heat the Resistive Heater, " + before + " K -> " + after + " K");
            helper.assertTrue(after <= EntityHeatZone.maxTemperatureFor(com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.resourcePower(1.0)) + 0.5,
                    "It must not heat above 750 K, " + before + " K -> " + after + " K");
            helper.succeed();
        });
    }

    /** 凍結領域: 中の生物は凍え、術者は凍えない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_freeze", timeoutTicks = 100)
    public static void freezeZoneFreezesBeings(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_freeze_caster", new Vec3(4.5, 1.0, 4.5));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 3.5));
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, freeze(helper), 5.0, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(25, () -> {
            int huskFrozen = husk.getTicksFrozen();
            int casterFrozen = caster.getTicksFrozen();
            cleanup.run();
            helper.assertTrue(huskFrozen > 0, "The husk inside the Freeze Zone must freeze, frozen ticks " + huskFrozen);
            helper.assertTrue(casterFrozen == 0, "The Freeze Zone must not freeze its own caster, frozen ticks " + casterFrozen);
            helper.succeed();
        });
    }

    /**
     * 凍結領域: ランダムに選んだブロックで、真上が空気の水源の水は氷になり（四隅の 4 つのうち 2 つ以上、落ちる見込み約
     * 1.1×10⁻⁶）、上が塞がれた水はそのまま。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_freeze_water", timeoutTicks = RANDOM_WAIT + 100)
    public static void freezeZoneFreezesOpenWaterRandomly(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_freeze_water", new Vec3(4.5, 1.0, 4.5));
        // 開いた水は四隅の囲いの中。真ん中は四隅の囲いの石に囲まれるので、上を石で塞いだ水を置く。
        List<BlockPos> open = List.of(OPEN, COVERED, THIRD, new BlockPos(3, 1, 3));
        BlockPos covered = CENTER;
        open.forEach(pos -> basin(helper, pos, Blocks.WATER));
        helper.setBlock(covered.below(), Blocks.STONE);
        helper.setBlock(covered, Blocks.WATER);
        helper.setBlock(covered.above(), Blocks.STONE);
        Runnable cleanup = () -> {
            helper.setBlock(covered.above(), Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            cast(caster, freeze(helper), 5.0, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(RANDOM_WAIT, () -> {
            int frozen = countMatching(helper, open, state -> state.is(Blocks.ICE));
            boolean coveredKept = helper.getBlockState(covered).is(Blocks.WATER);
            cleanup.run();
            helper.assertTrue(frozen >= 2, "Water with air above must turn into ice, frozen " + frozen + " of 4");
            helper.assertTrue(coveredKept, "Water with a block above must stay water");
            helper.succeed();
        });
    }

    /** 凍結領域に湿潤領域を重ねると、濡れた生物は凍えきるまでの時間が半分になる（凍えの値が倍の速さでたまる）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_freeze_soak", timeoutTicks = 100)
    public static void soakedBeingsFreezeTwiceAsFast(GameTestHelper helper) {
        // 凍えの差し引きは 凍結の中の威力 × 4（中の威力 5 で 20、10 で 40）、濡れていれば 2 倍、中の威力 10 の吹雪で 2 倍。
        helper.assertTrue(EntityFreezeZone.freezeAmount(5.0F, false, 0.0F) == 40
                        && EntityFreezeZone.freezeAmount(10.0F, false, 0.0F) == 60
                        && EntityFreezeZone.freezeAmount(5.0F, true, 0.0F) == 60
                        && EntityFreezeZone.freezeAmount(5.0F, true, 10.0F) == 100,
                "The freezing speed must follow the Power, wetness and the Blizzard");
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_freeze_soak_caster", new Vec3(8.5, 1.0, 2.5));
        Husk wet = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 2.5));
        Husk dry = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 2.5));
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            // 凍結領域は x 0〜4（威力 1 = 中の威力 5）、湿潤領域は x 0〜2 だけ（濡れるのは片方の生物）。
            cast(caster, soak(helper, new BlockPos(1, 1, 2)), 3.0, 0.0);
            cast(caster, freeze(helper, CENTER), 5.0, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        // 3 回足した後（凍結領域の 30 tick 目の直後）: 1 回目はそのまま、2 回目からは差し引き ふだん 20・濡れていれば 40 ずつ
        // （ふだん 76 ほど、濡れていれば 136 ほど）。
        helper.runAfterDelay(32, () -> {
            boolean wetSoaked = CombatStatuses.isSoaked(wet);
            int wetFrozen = wet.getTicksFrozen();
            int dryFrozen = dry.getTicksFrozen();
            cleanup.run();
            helper.assertTrue(wetSoaked, "The husk in the Soak Zone must be wet");
            helper.assertTrue(dryFrozen > 0, "The dry husk must freeze too, frozen ticks " + dryFrozen);
            helper.assertTrue(wetFrozen * 2 >= dryFrozen * 3,
                    "The wet husk must freeze about twice as fast: wet " + wetFrozen + ", dry " + dryFrozen);
            helper.succeed();
        });
    }

    /**
     * 乱流領域: 他者の飛び道具は入ってきたときに大きく曲がり、術者の飛び道具は曲がらない。残留ポーションの雲は早く縮む。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_turbulence", timeoutTicks = 100)
    public static void turbulenceZoneTurnsProjectilesAndBlowsCloudsAway(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_turbulence_caster", new Vec3(0.5, 1.0, 0.5));
        Vec3 start = helper.absoluteVec(new Vec3(1.5, 3.2, 2.5));
        Vec3 initial = new Vec3(0.4, 0.0, 0.0);
        Arrow others = new Arrow(level, start.x, start.y, start.z, new ItemStack(Items.ARROW), null);
        others.setNoGravity(true);
        others.setDeltaMovement(initial);
        Arrow own = new Arrow(level, start.x, start.y, start.z - 1.0, new ItemStack(Items.ARROW), null);
        own.setOwner(caster);
        own.setNoGravity(true);
        own.setDeltaMovement(initial);
        Vec3 cloudAt = helper.absoluteVec(new Vec3(2.5, 1.0, 3.5));
        AreaEffectCloud cloud = new AreaEffectCloud(level, cloudAt.x, cloudAt.y, cloudAt.z);
        cloud.setRadius(3.0F);
        cloud.setDuration(600);
        Runnable cleanup = () -> {
            others.discard();
            own.discard();
            cloud.discard();
            cleanup(helper, caster);
        };
        try {
            cast(caster, turbulence(helper), 5.0, 10.0);
            level.addFreshEntity(others);
            level.addFreshEntity(own);
            level.addFreshEntity(cloud);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(4, () -> {
            double othersTurn = Math.toDegrees(Math.acos(Math.min(1.0,
                    others.getDeltaMovement().normalize().dot(initial.normalize()))));
            double ownTurn = Math.toDegrees(Math.acos(Math.min(1.0,
                    own.getDeltaMovement().normalize().dot(initial.normalize()))));
            boolean cloudShrunk = cloud.isRemoved() || cloud.getRadius() < 2.8F;
            cleanup.run();
            helper.assertTrue(othersTurn > 30.0, "An arrow shot by someone else must turn aside, turned " + othersTurn);
            helper.assertTrue(ownTurn < 5.0, "The caster's own arrow must fly straight, turned " + ownTurn);
            helper.assertTrue(cloudShrunk, "The lingering cloud must be blown away");
            helper.succeed();
        });
    }

    /**
     * 自律詠唱機が置いた乱流領域は、持ち主のプレイヤーの飛び道具を曲げない（持ち主は同じ側）。ほかの人の飛び道具は曲げる。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_turbulence_machine_owner", timeoutTicks = 100)
    public static void machineTurbulenceZoneSparesTheOwnersProjectiles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "zone_turbulence_machine_owner", new Vec3(0.5, 1.0, 0.5));
        com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer machine =
                new com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer(level, java.util.UUID.randomUUID(),
                        owner.getUUID(), new AttackTargetGameTests.OwnerHost(owner));
        Vec3 machineAt = helper.absoluteVec(new Vec3(0.5, 1.0, 0.5));
        machine.moveTo(machineAt.x, machineAt.y, machineAt.z);
        Vec3 start = helper.absoluteVec(new Vec3(1.5, 3.2, 2.5));
        Vec3 initial = new Vec3(0.4, 0.0, 0.0);
        Arrow others = new Arrow(level, start.x, start.y, start.z, new ItemStack(Items.ARROW), null);
        others.setNoGravity(true);
        others.setDeltaMovement(initial);
        Arrow owners = new Arrow(level, start.x, start.y, start.z - 1.0, new ItemStack(Items.ARROW), null);
        owners.setOwner(owner);
        owners.setNoGravity(true);
        owners.setDeltaMovement(initial);
        Runnable cleanup = () -> {
            others.discard();
            owners.discard();
            List.copyOf(SpellGramIndex.ofCaster(level, com.moratan251.psitweaks.common.caster.CasterIds.idOf(machine)))
                    .forEach(object -> object.discard());
            machine.detach();
            cleanup(helper, owner);
        };
        try {
            cast(machine, turbulence(helper), 5.0, 10.0);
            level.addFreshEntity(others);
            level.addFreshEntity(owners);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        boolean placed = SpellGramIndex.ofCaster(level, com.moratan251.psitweaks.common.caster.CasterIds.idOf(machine)).stream()
                .anyMatch(object -> object instanceof EntityTurbulenceZone);
        if (!placed) {
            cleanup.run();
            helper.fail("The autonomous caster must place the turbulence zone");
            return;
        }
        helper.runAfterDelay(4, () -> {
            double othersTurn = Math.toDegrees(Math.acos(Math.min(1.0,
                    others.getDeltaMovement().normalize().dot(initial.normalize()))));
            double ownersTurn = Math.toDegrees(Math.acos(Math.min(1.0,
                    owners.getDeltaMovement().normalize().dot(initial.normalize()))));
            cleanup.run();
            helper.assertTrue(othersTurn > 30.0, "An arrow shot by someone else must turn aside, turned " + othersTurn);
            helper.assertTrue(ownersTurn < 5.0, "The machine owner's arrow must fly straight, turned " + ownersTurn);
            helper.succeed();
        });
    }

    /**
     * Mekanism Generators があるとき、乱流領域の中の風力発電機は屋根の下でも乱流の風だけで発電し、空の下の発電機には風が上乗せ
     * になる。風量は置いた時点の事象干渉力で増える。外の風力発電機は屋根の下で止まったまま。Mekanism Generators が
     * なければ飛ばす。
     * 風力発電機は高さが 5 ブロックあり、5×4×5 の区画には収まらないので、専用の区画に 3 台と屋根を収める。空の下に置く 1 台が
     * あるので、区画の天井（バリア）は付けない（skyAccess）。
     */
    @GameTest(template = WIND_TEMPLATE, batch = "psitweaks_zone_turbulence_wind", timeoutTicks = 200, skyAccess = true)
    public static void turbulenceZoneRunsWindGeneratorsIndoors(GameTestHelper helper) {
        Optional<Block> windGenerator = block("mekanismgenerators:wind_generator");
        if (!MekanismCompat.isGeneratorsLoaded() || windGenerator.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_turbulence_wind", new Vec3(0.5, 1.0, 0.5));
        BlockPos inside = new BlockPos(2, 1, 2);
        BlockPos outside = new BlockPos(6, 1, 2);
        BlockPos openSky = new BlockPos(10, 1, 2);
        // 加算方式: 範囲の中の空の下に 1 台（屋根に穴を開ける）。
        BlockPos insideOpen = new BlockPos(0, 1, 4);
        // 風力発電機は 4 ブロック上（y 5、発電機のてっぺん）から空が見えるかを見る。中と外の 2 台の上（x 0〜7）に、すぐ上の y 6 で
        // 屋根をかけ、3 台目は空の下に置く。乱流領域は x 0〜4 なので、外の 1 台（x 6）は入らない。
        for (int x = 0; x <= 7; x++) {
            for (int z = 0; z <= 4; z++) {
                if (x != insideOpen.getX() || z != insideOpen.getZ()) {
                    helper.setBlock(new BlockPos(x, 6, z), Blocks.STONE);
                }
            }
        }
        helper.setBlock(insideOpen, windGenerator.get());
        helper.setBlock(inside, windGenerator.get());
        helper.setBlock(outside, windGenerator.get());
        helper.setBlock(openSky, windGenerator.get());
        Runnable cleanup = () -> {
            helper.setBlock(inside, Blocks.AIR);
            helper.setBlock(outside, Blocks.AIR);
            helper.setBlock(openSky, Blocks.AIR);
            helper.setBlock(insideOpen, Blocks.AIR);
            for (int x = 0; x <= 7; x++) {
                for (int z = 0; z <= 4; z++) {
                    helper.setBlock(new BlockPos(x, 6, z), Blocks.AIR);
                }
            }
            cleanup(helper, caster);
        };
        try {
            // 事象干渉力 4 → 風量は上限の 4 倍。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, turbulence(helper), 5.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        // 風力発電機は 20 tick ごとに発電の倍率を決め直す。
        helper.runAfterDelay(60, () -> {
            double insideMultiplier = MekanismGameplayCompat.windMultiplierOf(level.getBlockEntity(helper.absolutePos(inside)));
            double outsideMultiplier = MekanismGameplayCompat.windMultiplierOf(level.getBlockEntity(helper.absolutePos(outside)));
            double skyMultiplier = MekanismGameplayCompat.windMultiplierOf(level.getBlockEntity(helper.absolutePos(openSky)));
            double insideOpenMultiplier = MekanismGameplayCompat.windMultiplierOf(level.getBlockEntity(helper.absolutePos(insideOpen)));
            cleanup.run();
            helper.assertTrue(outsideMultiplier == 0.0,
                    "A Wind Generator outside the zone must stay still under a roof, multiplier " + outsideMultiplier);
            helper.assertTrue(skyMultiplier > 0.0, "A Wind Generator under the open sky must run, multiplier " + skyMultiplier);
            // 風量 25 × 14（威力 10 の中の威力）÷ 240 × 干渉力 4 WU を、範囲の中の 2 台で均等に分ける。加算方式で 1 WU ごとに
            // 37.5 J/t、高さによらない。
            double units = 25.0 * com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.resourcePower(10.0) / 240.0 * 4.0 / 2.0;
            double openUnits = units;
            double roofed = units * EntityTurbulenceZone.WIND_JOULES_PER_WU / MekanismGameplayCompat.windGenerationMinimum();
            helper.assertTrue(Math.abs(insideMultiplier - roofed) < 1.0E-6,
                    "A roofed Wind Generator in the zone must run on the zone's wind alone, " + insideMultiplier + " vs " + roofed);
            double openAdded = openUnits * EntityTurbulenceZone.WIND_JOULES_PER_WU / MekanismGameplayCompat.windGenerationMinimum();
            helper.assertTrue(Math.abs(insideOpenMultiplier - (skyMultiplier + openAdded)) < 1.0E-6,
                    "An open-sky Wind Generator in the zone must get the wind on top, " + insideOpenMultiplier + " vs " + (skyMultiplier + openAdded));
            helper.succeed();
        });
    }

    /**
     * 重なった 2 つの乱流領域: 1 台の風力発電機が受け取るのは合わせて 10 WU まで。照準に出す使用量も、
     * 2 つの領域を合わせて 10 WU になる（それぞれの分を数えて上限を超えて出さない）。
     */
    @GameTest(template = WIND_TEMPLATE, batch = "psitweaks_zone_turbulence_wind_overlap", timeoutTicks = 200, skyAccess = true)
    public static void overlappingTurbulenceZonesGiveAGeneratorTenWindUnits(GameTestHelper helper) {
        Optional<Block> windGenerator = block("mekanismgenerators:wind_generator");
        if (!MekanismCompat.isGeneratorsLoaded() || windGenerator.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_turbulence_overlap", new Vec3(8.5, 1.0, 0.5));
        helper.setBlock(CENTER, windGenerator.get());
        Runnable cleanup = () -> {
            helper.setBlock(CENTER, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            // どちらも一辺 9・威力 10（中の威力 14）・事象干渉力 2（81 × 14 ÷ 240 × 2 ≈ 9.45 WU）: 発電機は 1 台なので、
            // それぞれから 9.45 WU（合わせて 18.9 WU、1 台は 10 WU まで）。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            cast(caster, turbulence(helper), 9.0, 10.0);
            cast(caster, turbulence(helper), 9.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(60, () -> {
            BlockPos at = helper.absolutePos(CENTER);
            double received = EntityTurbulenceZone.windUnitsAt(level, at);
            double used = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                    .filter(zone -> zone instanceof EntityTurbulenceZone).mapToDouble(zone -> ((EntityTurbulenceZone) zone).getResourceUsed())
                    .sum();
            cleanup.run();
            helper.assertTrue(Math.abs(received - 10.0) < 1.0E-6, "A generator must take 10 WU at most, got " + received);
            helper.assertTrue(Math.abs(used - 10.0) < 1.0E-3, "The two zones must count 10 WU in use together, got " + used);
            helper.succeed();
        });
    }

    /**
     * 4 ブロック上（発電機のてっぺん）が液体の風力発電機は止まっているので、乱流領域は風を分ける先に数えない。中のもう 1 台が
     * 風を全部受け取る。Mekanism Generators がなければ飛ばす。
     */
    @GameTest(template = WIND_TEMPLATE, batch = "psitweaks_zone_turbulence_wind_fluid", timeoutTicks = 200, skyAccess = true)
    public static void turbulenceZoneSkipsWindGeneratorsUnderFluid(GameTestHelper helper) {
        Optional<Block> windGenerator = block("mekanismgenerators:wind_generator");
        if (!MekanismCompat.isGeneratorsLoaded() || windGenerator.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_turbulence_wind_fluid", new Vec3(0.5, 1.0, 0.5));
        BlockPos dry = new BlockPos(1, 1, 2);
        BlockPos wet = new BlockPos(3, 1, 2);
        helper.setBlock(dry, windGenerator.get());
        helper.setBlock(wet, windGenerator.get());
        // Mekanism が見る 4 ブロック上（ふつうは発電機のてっぺんの当たり判定のブロック。GameTest の setBlock では作られない）に
        // 水を置く。流れ出さないよう下と横を石で囲む。
        BlockPos top = wet.above(EntityTurbulenceZone.GENERATOR_FLUID_CHECK_HEIGHT);
        List<BlockPos> walls = List.of(top.below(), top.east(), top.west(), top.north(), top.south());
        walls.forEach(wall -> helper.setBlock(wall, Blocks.STONE));
        helper.setBlock(top, Blocks.WATER);
        BlockPos wetTop = helper.absolutePos(top);
        Runnable cleanup = () -> {
            helper.setBlock(top, Blocks.AIR);
            helper.setBlock(dry, Blocks.AIR);
            helper.setBlock(wet, Blocks.AIR);
            walls.forEach(wall -> helper.setBlock(wall, Blocks.AIR));
            cleanup(helper, caster);
        };
        if (level.getFluidState(wetTop).isEmpty()) {
            cleanup.run();
            helper.fail("The top of the second Wind Generator must hold water for this test: " + level.getBlockState(wetTop));
            return;
        }
        try {
            cast(caster, turbulence(helper), 5.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(60, () -> {
            double dryUnits = EntityTurbulenceZone.windUnitsAt(level, helper.absolutePos(dry));
            double wetUnits = EntityTurbulenceZone.windUnitsAt(level, helper.absolutePos(wet));
            double wetMultiplier = MekanismGameplayCompat.windMultiplierOf(level.getBlockEntity(helper.absolutePos(wet)));
            double amount = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                    .filter(zone -> zone instanceof EntityTurbulenceZone).mapToDouble(zone -> ((EntityTurbulenceZone) zone).windAmount())
                    .findFirst().orElse(-1.0);
            cleanup.run();
            helper.assertTrue(amount > 0.0 && amount < EntityTurbulenceZone.MAX_WU_PER_GENERATOR,
                    "The zone must blow less than one generator can take, got " + amount);
            helper.assertTrue(wetUnits == 0.0 && wetMultiplier == 0.0,
                    "A Wind Generator with fluid on top must get no wind and stay still, got " + wetUnits + " WU, multiplier "
                            + wetMultiplier);
            helper.assertTrue(Math.abs(dryUnits - amount) < 1.0E-6,
                    "The other Wind Generator must take all the wind, " + dryUnits + " vs " + amount);
            helper.succeed();
        });
    }

    /**
     * 乱流領域の風量は置いた時点の事象干渉力に比例して増える: 干渉力 1 で基準、2 で 2 倍、4 以上で 4 倍（上限）。1 WU は
     * 37.5 J/t・床の広さ × 中の威力 ÷ 240 WU、コストは加熱・荷電と同じ 50 + 広さ × 中の威力 × 2 / 5 なので、干渉力 1 でのコスト 1 Psi
     * あたりの発電の上乗せは 25 ÷ 64 J/t（約 94 FE/Psi）。規模とコストが加熱領域と同じことも確かめる。
     * 風量は Mekanism がなくても持つので、Mekanism なしでも試す。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_turbulence_wind_interference", timeoutTicks = 40)
    public static void turbulenceWindGrowsWithInterference(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_turbulence_interference", new Vec3(0.5, 1.0, 0.5));
        double[] interferences = {1.0, 2.0, 4.0, 8.0};
        double[] winds = new double[interferences.length];
        try {
            for (int i = 0; i < interferences.length; i++) {
                caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(interferences[i]);
                List<Object> before = new java.util.ArrayList<>(SpellGramIndex.ofCaster(level, caster.getUUID()));
                // 一辺 6・威力 6（中の威力 10）: 床の広さ 36 × 10 ÷ 240 = 1.5 WU × 事象干渉力。
                cast(caster, turbulence(helper), 6.0, 6.0);
                EntityTurbulenceZone placed = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                        .filter(object -> object instanceof EntityTurbulenceZone && before.stream().noneMatch(old -> old == object))
                        .map(EntityTurbulenceZone.class::cast).findFirst()
                        .orElseThrow(() -> new IllegalStateException("The Turbulence Zone must be placed"));
                winds[i] = placed.windAmount();
            }
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup(helper, caster);
            throw new RuntimeException(e);
        }
        cleanup(helper, caster);
        double[] expected = {1.5, 3.0, 6.0, 6.0};
        for (int i = 0; i < interferences.length; i++) {
            helper.assertTrue(Math.abs(winds[i] - expected[i]) < 1.0E-6, "Interference " + interferences[i]
                    + " must blow " + expected[i] + " WU, got " + winds[i]);
        }
        // 干渉力 1 でのコスト 1 Psi あたりの上乗せ（1 WU の J/t ÷ 割る数 ÷ 広さ × 中の威力 1 あたりのコスト 2 / 5）は 25 ÷ 64。
        double perPsi = EntityTurbulenceZone.WIND_JOULES_PER_WU / EntityTurbulenceZone.WIND_FOOTPRINT_DIVISOR / (2.0 / 5.0);
        helper.assertTrue(Math.abs(perPsi - 25.0 / 64.0) < 1.0E-9, "The wind power per Psi must stay 25/64 J/t, got " + perPsi);
        // コストと規模は加熱領域と同じ式（サイズ 16・威力 10 でコスト 1483、規模 701）。
        int turbulenceCost = com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickTurbulenceZone.costFor(256, 10.0);
        int heatCost = com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHeatZone.costFor(256, 10.0);
        helper.assertTrue(turbulenceCost == 1483 && heatCost == 1483,
                "The Turbulence Zone must cost the same as the Heat Zone: " + turbulenceCost + " vs " + heatCost);
        int potency = com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.poweredPotency(256, 10.0);
        helper.assertTrue(potency == 701, "Size 16, Power 10 must have Potency 701, got " + potency);
        helper.succeed();
    }

    /**
     * 乱流＋加熱＝熱乱流（同じ術者、重なったところだけ）: 火のダメージを足す。他人の乱流領域と重なっても起きない。
     * 範囲はテンプレートの中に収める（外のチャンクではエンティティが動かないことがある）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_storm", timeoutTicks = 100)
    public static void ownTurbulenceTurnsHeatIntoHeatStorm(GameTestHelper helper) {
        stormTest(helper, true);
    }

    /** 乱流＋凍結＝吹雪（同じ術者、重なったところだけ）: 凍結のダメージを与える。他人の乱流領域と重なっても起きない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_blizzard", timeoutTicks = 100)
    public static void ownTurbulenceTurnsFreezeIntoBlizzard(GameTestHelper helper) {
        stormTest(helper, false);
    }

    private static void stormTest(GameTestHelper helper, boolean heat) {
        ServerLevel level = helper.getLevel();
        String name = heat ? "heat_storm" : "blizzard";
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_" + name + "_caster", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "zone_" + name + "_other", new Vec3(4.5, 1.0, 0.5));
        // 加熱（凍結）領域は x 1〜3・z 1〜3。術者の乱流は x 1〜2、他人の乱流は x 3。
        Husk stormed = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 2.5)), 100.0F);
        Husk plain = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 2.5)), 100.0F);
        Runnable cleanup = () -> {
            cleanup(helper, other);
            cleanup(helper, caster);
        };
        try {
            cast(caster, heat ? heat(helper, CENTER) : freeze(helper, CENTER), 3.0, 5.0);
            castTurbulenceOver(caster, helper.absolutePos(new BlockPos(1, 0, 1)), 2, 3, 3);
            // 同じところに弱い乱流（威力 1）も重ねる: 強さは最も強い乱流（威力 10）で決まる。
            castTurbulenceOver(caster, helper.absolutePos(new BlockPos(1, 0, 1)), 2, 3, 3, 1.0);
            castTurbulenceOver(other, helper.absolutePos(new BlockPos(3, 0, 1)), 1, 3, 3);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        float stormedBefore = stormed.getHealth();
        float plainBefore = plain.getHealth();
        helper.runAfterDelay(25, () -> {
            float stormedLoss = stormedBefore - stormed.getHealth();
            float plainLoss = plainBefore - plain.getHealth();
            boolean storm = heat
                    ? SpellGramIndex.all(level, EntityHeatZone.class).stream().anyMatch(EntityHeatZone::isStorm)
                    : SpellGramIndex.all(level, EntityFreezeZone.class).stream().anyMatch(EntityFreezeZone::isStorm);
            cleanup.run();
            helper.assertTrue(storm, "The caster's own turbulence must turn the zone into a " + name);
            if (heat) {
                // 普通の火は 1 秒に 1。熱乱流は 0.5 秒ごとに 1.79（当たった直後の火のダメージは無敵時間で消える）。
                helper.assertTrue(stormedLoss >= 3.0F && stormedLoss > plainLoss,
                        "The Heat Storm must hurt more than plain fire: storm " + stormedLoss + ", plain " + plainLoss);
            } else {
                // 凍結領域だけでは、凍えきるまで（数秒）ダメージを与えない。
                // 吹雪は 0.5 秒ごとに 1.79（弱い乱流が重なっても、最も強い乱流で決まる）。
                helper.assertTrue(stormedLoss >= 3.0F, "The Blizzard must hurt the being inside, lost " + stormedLoss);
                helper.assertTrue(plainLoss == 0.0F, "Someone else's turbulence must not make a Blizzard, lost " + plainLoss);
            }
            helper.succeed();
        });
    }

    /** 組み合わせの攻撃の強さは、乱流領域の 1 秒あたりのコスト ÷ 3（40 まで）を、相方の領域の威力 × 4 で頭打ちにしたもの。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_storm_attack_power")
    public static void stormAttackPowerFollowsTheCostOfTheTurbulence(GameTestHelper helper) {
        helper.assertTrue(Math.abs(EntityTurbulenceZone.attackPower(306.0F, 600) - 3.4F) < 1.0E-4F,
                "A cost of 306 for 30 s must have attack power 3.4");
        helper.assertTrue(Math.abs(EntityTurbulenceZone.attackPower(612.0F, 1200) - 3.4F) < 1.0E-4F,
                "A longer zone of the same size and Power must have the same attack power");
        helper.assertTrue(Math.abs(EntityTurbulenceZone.attackPower(1074.0F, 600) - 1074.0F / 90.0F) < 1.0E-4F,
                "The Power must not limit the attack power of a cost of 1074");
        helper.assertTrue(EntityTurbulenceZone.attackPower(3603.0F, 600) == 40.0F, "The attack power must stop at 40");
        helper.assertTrue(EntityTurbulenceZone.attackPower(306.0F, 0) == 0.0F, "A zone without a lifetime must have no attack power");
        float perPower = EntityTurbulenceZone.PARTNER_ATTACK_POWER_PER_POWER;
        float[][] partners = {{1.0F, 0.6F}, {5.0F, 3.0F}, {10.0F, 6.0F}, {30.0F, 6.0F}};
        for (float[] partner : partners) {
            float hit = SpellGramZone.STORM_DAMAGE_PER_POWER * EntityTurbulenceZone.limitedAttackPower(40.0F, partner[0], perPower);
            helper.assertTrue(Math.abs(hit - partner[1]) < 1.0E-4F, "A partner of Power " + partner[0] + " must allow " + partner[1] + " a hit, got " + hit);
        }
        helper.assertTrue(EntityTurbulenceZone.limitedAttackPower(3.4F, 10.0F, perPower) == 3.4F,
                "A strong partner must not raise the attack power of cheap turbulence");
        helper.succeed();
    }

    /**
     * 相方の領域の入れた威力 × 4 が、強い乱流の攻撃の強さを頭打ちにする: 相方の威力 1・5・10 で 4・20・40、10 を超えた相方は 10 と
     * 同じ。威力 40 の乱流（サイズ 16 でコスト 4555）は強さ 40 に届き、威力 1 の加熱領域が 4 に頭打ちにする。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_partner_attack_limit", timeoutTicks = 60)
    public static void partnerPowerLimitsTheStormAttack(GameTestHelper helper) {
        float perPower = EntityTurbulenceZone.PARTNER_ATTACK_POWER_PER_POWER;
        float[][] partners = {{1.0F, 4.0F}, {5.0F, 20.0F}, {10.0F, 40.0F}, {11.0F, 40.0F}, {100.0F, 40.0F}};
        for (float[] partner : partners) {
            float limited = EntityTurbulenceZone.limitedAttackPower(40.0F,
                    SpellGramZone.enteredPowerOf(PieceTrickZoneBase.resourcePower(partner[0])), perPower);
            helper.assertTrue(limited == partner[1],
                    "A partner of Power " + partner[0] + " must limit the attack power to " + partner[1] + ", got " + limited);
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_partner_limit_caster", new Vec3(4.5, 1.0, 0.5));
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, heat(helper, CENTER), 3.0, 1.0);
            castTurbulenceOver(caster, helper.absolutePos(new BlockPos(1, 0, 1)), 3, 3, 3, 40.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(15, () -> {
            BlockPos inside = helper.absolutePos(new BlockPos(2, 1, 2));
            EntityHeatZone zone = SpellGramIndex.all(level, EntityHeatZone.class).stream()
                    .filter(found -> caster.getUUID().equals(found.getCasterUuid())).findFirst().orElseThrow();
            float storm = zone.stormAttackPowerAt(inside);
            float combo = zone.comboAttackPowerAt(inside, zone.getPower(), EntityTurbulenceZone.PARTNER_ATTACK_POWER_PER_POWER);
            cleanup.run();
            helper.assertTrue(storm == 40.0F, "The turbulence alone must reach attack power 40, was " + storm);
            helper.assertTrue(combo == 4.0F, "A Heat Zone of Power 1 must limit the attack power to 4, was " + combo);
            helper.succeed();
        });
    }

    /** 小さく安い乱流領域を重ねた領域は、そのコストの分の強さでしか攻撃しない（機械を動かす乱流の中の威力は 14 のまま）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_cheap_storm", timeoutTicks = 60)
    public static void cheapTurbulenceAttacksWeakly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_cheap_storm_caster", new Vec3(4.5, 1.0, 0.5));
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, heat(helper, CENTER), 3.0, 5.0);
            castTurbulenceOver(caster, helper.absolutePos(new BlockPos(1, 0, 1)), 3, 3, 3, 10.0, 3.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(15, () -> {
            BlockPos inside = helper.absolutePos(new BlockPos(2, 1, 2));
            EntityHeatZone zone = SpellGramIndex.all(level, EntityHeatZone.class).stream()
                    .filter(found -> caster.getUUID().equals(found.getCasterUuid())).findFirst().orElseThrow();
            float attack = zone.stormAttackPowerAt(inside);
            float power = zone.stormPowerAt(inside);
            cleanup.run();
            // サイズ 3・威力 10（中の威力 14）のコストは 100: 30 秒で毎秒 3.33、強さはその 1/3。
            helper.assertTrue(Math.abs(attack - 100.0F / 30.0F / 3.0F) < 1.0E-3F, "The attack power must follow the cost, was " + attack);
            helper.assertTrue(power == 14.0F, "The Power that drives machines must stay 14, was " + power);
            helper.succeed();
        });
    }

    /**
     * 加熱・凍結・湿潤・乱流を全部重ねる: 加熱と凍結は打ち消し合ってどちらも効かず（火がつかない、凍えない、水も氷も
     * そのまま）、湿潤と乱流を重ねたときと同じになる（濡れる、暴風雨で水に弱いブレイズだけがダメージを受ける）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_all_four", timeoutTicks = 100)
    public static void heatAndFreezeCancelOutLeavingTheRainstorm(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_all_four_caster", new Vec3(4.5, 1.0, 4.5));
        basin(helper, OPEN, Blocks.WATER);
        basin(helper, COVERED, Blocks.ICE);
        Husk husk = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 2.5)), 100.0F);
        var blaze = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.BLAZE, new Vec3(2.5, 1.0, 3.5)), 100.0F);
        // AI があるとブレイズは自分で浮き上がり、領域の上へ出てしまう。
        blaze.setNoAi(true);
        float huskBefore = husk.getHealth();
        float blazeBefore = blaze.getHealth();
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, heat(helper, CENTER), 3.0, 5.0);
            cast(caster, freeze(helper, CENTER), 3.0, 5.0);
            cast(caster, soak(helper, CENTER), 3.0, 0.0);
            castTurbulenceOver(caster, helper.absolutePos(new BlockPos(1, 0, 1)), 3, 3, 3);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(25, () -> {
            boolean huskBurning = husk.getRemainingFireTicks() > 0;
            int huskFrozen = husk.getTicksFrozen();
            boolean huskWet = CombatStatuses.isSoaked(husk);
            float huskLoss = huskBefore - husk.getHealth();
            float blazeLoss = blazeBefore - blaze.getHealth();
            boolean waterKept = helper.getBlockState(OPEN).is(Blocks.WATER);
            boolean iceKept = helper.getBlockState(COVERED).is(Blocks.ICE);
            cleanup.run();
            helper.assertFalse(huskBurning, "Heat must not work where it overlaps a Freeze Zone");
            helper.assertTrue(huskFrozen == 0, "Freeze must not work where it overlaps a Heat Zone, frozen " + huskFrozen);
            helper.assertTrue(waterKept && iceKept, "Neither the water nor the ice may change where heat and cold cancel out");
            helper.assertTrue(huskWet, "The Soak Zone must still soak the husk");
            helper.assertTrue(huskLoss == 0.0F, "Nothing may hurt the husk (no Heat Storm, no Blizzard), lost " + huskLoss);
            helper.assertTrue(blazeLoss >= 2.0F, "The Rainstorm must hurt the Blaze, which water harms, lost " + blazeLoss);
            helper.succeed();
        });
    }

    /** {@code caster} が、ブロック {@code from}（角）から x×y×z ブロックの乱流領域（威力 10）を置く。 */
    static void castTurbulenceOver(ServerPlayer caster, BlockPos from, int xs, int ys, int zs)
            throws SpellRuntimeException {
        castTurbulenceOver(caster, from, xs, ys, zs, 10.0);
    }

    /** {@code caster} が、ブロック {@code from}（角）から x×y×z ブロックの、威力 {@code power} の乱流領域を置く。 */
    static void castTurbulenceOver(ServerPlayer caster, BlockPos from, int xs, int ys, int zs, double power)
            throws SpellRuntimeException {
        castTurbulenceOver(caster, from, xs, ys, zs, power, 32.0);
    }

    /** {@code caster} が、入力のサイズ {@code size}（コストを決める）で、ブロック {@code from}（角）から x×y×z ブロックの乱流領域を置く。 */
    static void castTurbulenceOver(ServerPlayer caster, BlockPos from, int xs, int ys, int zs, double power, double size)
            throws SpellRuntimeException {
        MatrixValue matrix = new MatrixValue(4, 4, new double[]{
                xs, 0, 0, from.getX() - 0.5,
                0, ys, 0, from.getY() - 0.5,
                0, 0, zs, from.getZ() - 0.5,
                0, 0, 0, 1});
        var piece = new PieceTrickTurbulenceZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
                    return matrix;
                }
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return size;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? power : null;
            }
        };
        piece.execute(new SpellContext().setPlayer(caster));
    }

    /**
     * 領域干渉: 置いた時点の自分より事象干渉力の低い他者は、範囲の中を対象にできず（範囲の中の自分自身も）、範囲の中に
     * 領域を置けない。置いた本人と、事象干渉力の高い他者は止められない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_interference", timeoutTicks = 40)
    public static void interferenceZoneStopsWeakerOthers(GameTestHelper helper) {
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "zone_interference_owner", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer weaker = GameTestPlayers.inLevel(helper, "zone_interference_weak", new Vec3(4.5, 1.0, 4.5));
        ServerPlayer stronger = GameTestPlayers.inLevel(helper, "zone_interference_strong", new Vec3(8.5, 1.0, 2.5));
        Runnable cleanup = () -> {
            cleanup(helper, weaker);
            cleanup(helper, stronger);
            cleanup(helper, owner);
        };
        try {
            owner.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(3.0D);
            stronger.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(5.0D);
            cast(owner, interference(helper, CENTER), 5.0, 0.0);
            Vector3 inside = vector(helper.absoluteVec(new Vec3(2.5, 1.5, 2.5)));
            Vector3 outside = vector(helper.absoluteVec(new Vec3(7.5, 1.5, 2.5)));

            SpellContext weak = new SpellContext().setPlayer(weaker);
            helper.assertFalse(weak.isInRadius(inside), "A weaker other must not target a position inside the zone");
            helper.assertTrue(weak.isInRadius(outside), "A weaker other may still target outside the zone");
            helper.assertFalse(weak.isInRadius(weaker), "A weaker other inside the zone must not target even itself");
            helper.assertFalse(weak.isInRadius(owner), "A weaker other must not target an entity inside the zone");

            SpellContext own = new SpellContext().setPlayer(owner);
            helper.assertTrue(own.isInRadius(inside) && own.isInRadius(owner), "The owner must not be stopped");
            SpellContext strong = new SpellContext().setPlayer(stronger);
            helper.assertTrue(strong.isInRadius(inside), "An other with higher interference must not be stopped");

            boolean placed;
            try {
                cast(weaker, soak(helper, CENTER), 3.0, 0.0);
                placed = true;
            } catch (SpellRuntimeException e) {
                placed = !SpellRuntimeException.OUTSIDE_RADIUS.equals(e.getMessage());
            }
            helper.assertFalse(placed, "A weaker other must not place a zone centered inside the Interference Zone");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** 領域干渉: 先に置かれていた、事象干渉力の低い他者の領域は、重なったところだけ効かなくなる。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_interference_overlap", timeoutTicks = 60)
    public static void interferenceZoneStopsWeakerZonesWhereTheyOverlap(GameTestHelper helper) {
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "zone_interference_overlap_owner", new Vec3(0.5, 1.0, 4.5));
        ServerPlayer weaker = GameTestPlayers.inLevel(helper, "zone_interference_overlap_weak", new Vec3(4.5, 1.0, 0.5));
        Husk overlapped = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 2.5));
        Husk free = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 2.5));
        Runnable cleanup = () -> {
            cleanup(helper, weaker);
            cleanup(helper, owner);
        };
        try {
            owner.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(3.0D);
            // 弱い側の湿潤領域（x 0〜4）を先に置き、強い側の領域干渉（x 0〜2）を重ねる。
            cast(weaker, soak(helper, CENTER), 5.0, 0.0);
            cast(owner, interference(helper, new BlockPos(1, 1, 2)), 3.0, 0.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(15, () -> {
            boolean overlappedWet = CombatStatuses.isSoaked(overlapped);
            boolean freeWet = CombatStatuses.isSoaked(free);
            cleanup.run();
            helper.assertFalse(overlappedWet, "The weaker Soak Zone must not work where the Interference Zone overlaps it");
            helper.assertTrue(freeWet, "The weaker Soak Zone must still work outside the Interference Zone");
            helper.succeed();
        });
    }

    /**
     * 熱の受け渡しの条件:加熱領域と凍結領域が面で触れ合っているか。大きさ違いでも、ずれていても、
     * 面で触れていれば触れ合う（同じ大きさで二分していても同じ）。触れ合う面の広さは、運ぶ熱を分ける割合に使う。
     */
    @GameTest(template = TEMPLATE)
    public static void heatAndFreezeZonesTouchByAFace(GameTestHelper helper) throws SpellRuntimeException {
        BlockPos hot = helper.absolutePos(new BlockPos(1, 1, 2));
        BlockPos cold = helper.absolutePos(new BlockPos(4, 1, 2));
        AABB hotBox = ZoneArea.cube(hot, 3).bounds();
        helper.assertTrue(ZoneHeatExchange.contactFace(hotBox, ZoneArea.cube(cold, 3).bounds()) != null,
                "Two halves of one box touch");
        helper.assertTrue(ZoneHeatExchange.contactArea(hotBox, ZoneArea.cube(cold, 3).bounds()) == 9.0,
                "Two size-3 halves touch over 3 x 3 blocks");
        helper.assertTrue(ZoneHeatExchange.contactFace(hotBox, ZoneArea.cube(cold.east(), 5).bounds()) != null,
                "Zones of different sizes side by side touch");
        helper.assertTrue(ZoneHeatExchange.contactArea(hotBox, ZoneArea.cube(cold.east(), 5).bounds()) == 9.0,
                "A size-3 zone against a size-5 zone touches over its own 3 x 3 face");
        helper.assertTrue(ZoneHeatExchange.contactArea(hotBox, ZoneArea.cube(cold.above().south(), 3).bounds()) == 4.0,
                "Zones sliding past each other by one block touch over 2 x 2 blocks");
        helper.assertTrue(ZoneHeatExchange.contactFace(hotBox, ZoneArea.cube(cold.east(2), 3).bounds()) == null,
                "Zones with a gap do not touch");
        helper.assertTrue(ZoneHeatExchange.contactFace(hotBox, ZoneArea.cube(cold.west(), 3).bounds()) == null,
                "Overlapping zones do not merely touch");
        helper.assertTrue(ZoneHeatExchange.contactFace(hotBox, ZoneArea.cube(cold.south(3), 3).bounds()) == null,
                "Zones meeting only along an edge do not touch");
        // つながりの広さ（重なりでも受け渡す）:触れ合いは面の広さ、重なりは重なりの箱のいちばん広い面。
        helper.assertTrue(ZoneHeatExchange.linkArea(hotBox, ZoneArea.cube(cold, 3).bounds()) == 9.0,
                "Touching zones are joined by their touching face");
        helper.assertTrue(ZoneHeatExchange.linkArea(hotBox, ZoneArea.cube(cold.west(), 3).bounds()) == 9.0,
                "Zones overlapping by one block are joined by the 3 x 3 face of the overlap");
        helper.assertTrue(ZoneHeatExchange.linkArea(hotBox, ZoneArea.cube(cold.west().above().south(), 3).bounds()) == 4.0,
                "Zones overlapping by 1 x 2 x 2 blocks are joined by 2 x 2");
        helper.assertTrue(ZoneHeatExchange.linkArea(hotBox, ZoneArea.cube(cold.east(2), 3).bounds()) == 0.0
                        && ZoneHeatExchange.linkArea(hotBox, ZoneArea.cube(cold.south(3), 3).bounds()) == 0.0,
                "Zones apart or meeting only along an edge are not joined");
        helper.succeed();
    }

    /**
     * 乱流の重ねがけ: 熱乱流・吹雪の総量の上乗せは、乱流が範囲を覆う割合（体積の比）を掛け、合計は 1 まで。
     * 小さな乱流をいくつ重ねても、範囲全体を 1 つの乱流で覆ったときより増えない。
     */
    @GameTest(template = TEMPLATE)
    public static void stormCoverageNeverExceedsTheWholeZone(GameTestHelper helper) {
        AABB zone = new AABB(0, 0, 0, 16, 16, 16);
        List<AABB> small = new java.util.ArrayList<>();
        for (int i = 0; i < 31; i++) {
            small.add(new AABB(0, 0, 0, 2, 2, 2));
        }
        double[] smallShares = SpellGramZone.coverageShares(zone, small);
        double smallSum = java.util.Arrays.stream(smallShares).sum();
        helper.assertTrue(Math.abs(smallSum - 31 * 8.0 / 4096.0) < 1.0E-9,
                "31 size-2 Turbulence Zones must cover only 31 x 8 of 4096 blocks' worth, got " + smallSum);
        double[] whole = SpellGramZone.coverageShares(zone, List.of(new AABB(-2, -2, -2, 18, 18, 18)));
        helper.assertTrue(Math.abs(whole[0] - 1.0) < 1.0E-9, "A Turbulence Zone covering the whole zone must count as 1, got " + whole[0]);
        double[] stacked = SpellGramZone.coverageShares(zone, List.of(zone, zone, zone));
        double stackedSum = java.util.Arrays.stream(stacked).sum();
        helper.assertTrue(Math.abs(stackedSum - 1.0) < 1.0E-9, "Stacked Turbulence Zones must add up to at most 1, got " + stackedSum);
        double[] apart = SpellGramZone.coverageShares(zone, List.of(new AABB(20, 0, 0, 22, 2, 2)));
        helper.assertTrue(apart[0] == 0.0, "A Turbulence Zone outside must cover nothing");
        helper.succeed();
    }

    /**
     * 熱の受け渡し: 同じ術者の凍結領域が Mekanism のブロックから奪った熱のうち、周囲の
     * 温度より上にあった分は、面で触れ合っている加熱領域の熱量に入る。大きさの違う組（加熱 3・凍結 5）でも運ぶ。
     * Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange", timeoutTicks = 100)
    public static void freezeZoneCarriesHeatToATouchingHeatZone(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_exchange", new Vec3(9.5, 1.0, 6.5));
        // 加熱領域は x 0〜2（一辺 3）、凍結領域は x 3〜7（一辺 5）。x = 3 の面の 3 × 3 で触れ合う。
        BlockPos coldHeater = new BlockPos(5, 1, 2);
        helper.setBlock(coldHeater, heater.get());
        // テンプレートの外の領域も動くように、そのチャンクを強制読み込みする。
        Runnable unforce = forceChunks(helper, new BlockPos(-1, 0, -1), new BlockPos(10, 0, 6));
        Runnable cleanup = () -> {
            helper.setBlock(coldHeater, Blocks.AIR);
            cleanup(helper, caster);
            unforce.run();
        };
        try {
            // 凍結領域の中の発熱器を 800 K まで温めておく。
            MekanismGameplayCompat.supplyZoneHeat(level, List.of(level.getBlockEntity(helper.absolutePos(coldHeater))),
                    new java.util.IdentityHashMap<>(), 1.0E9, 800.0, at -> true);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(5, 1, 2)), 5.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            EntityHeatZone hot = only(helper, caster, EntityHeatZone.class);
            EntityFreezeZone cold = only(helper, caster, EntityFreezeZone.class);
            double received = hot.movedHeatReceived();
            double carried = cold.heatCarried();
            double own = hot.ownHeatTotalPerTick();
            double extra = hot.heatTotalPerTick() - own;
            float shown = hot.getResourceAmount();
            double total = hot.heatTotalPerTick();
            double coldTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(coldHeater));
            cleanup.run();
            helper.assertTrue(carried > 0.0, "The Freeze Zone must carry the heat above the ambient temperature, carried " + carried);
            helper.assertTrue(Math.abs(received - carried) <= carried * 1.0E-9,
                    "A Heat Zone touching only this Freeze Zone must get all of it: received " + received + ", carried " + carried);
            helper.assertTrue(extra > 0.0, "The carried heat must raise the Heat Zone's heat total, got +" + extra);
            // 照準の表示の熱量にも、運ばれてきた熱が入る（同期は 10 tick ごと）。
            helper.assertTrue(shown > own * 1.0001,
                    "The shown heat must include the carried heat: shown " + shown + ", own " + own + ", total " + total);
            helper.assertTrue(coldTemp < 790.0, "The Freeze Zone must cool the heater, " + coldTemp + " K");
            helper.succeed();
        });
    }

    /**
     * 熱の受け渡しは重なりでも働く:自分の加熱領域と凍結領域の端を 1 列重ねると、重なったところは
     * 打ち消し合うまま、重なりの外の熱源から凍結領域が奪った熱は加熱領域へ渡る。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_overlap", timeoutTicks = 100)
    public static void overlappingFreezeZoneCarriesHeatToTheHeatZone(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_exchange_overlap", new Vec3(9.5, 1.0, 6.5));
        // 加熱領域は x 0〜2、凍結領域は x 2〜4（x = 2 の列が重なり、打ち消し合う）。発熱器は凍結領域だけの (4, 1, 2)、
        // 重なりの (2, 1, 2) にも 1 つ置き、こちらは冷えないことを見る。
        BlockPos coldHeater = new BlockPos(4, 1, 2);
        BlockPos cancelledHeater = new BlockPos(2, 1, 2);
        Runnable cleanup = () -> {
            helper.setBlock(coldHeater, Blocks.AIR);
            helper.setBlock(cancelledHeater, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            hotHeater(helper, heater.get(), coldHeater);
            hotHeater(helper, heater.get(), cancelledHeater);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(3, 1, 2)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            double received = only(helper, caster, EntityHeatZone.class).movedHeatReceived();
            EntityFreezeZone cold = only(helper, caster, EntityFreezeZone.class);
            double carried = cold.heatCarried();
            double discarded = cold.heatDiscarded();
            double coldTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(coldHeater));
            double cancelledTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(cancelledHeater));
            cleanup.run();
            String state = "received " + received + ", carried " + carried + ", discarded " + discarded
                    + "; heater " + coldTemp + " K, heater in the overlap " + cancelledTemp + " K";
            helper.assertTrue(carried > 0.0 && discarded <= carried * 1.0E-9,
                    "An overlapping Freeze Zone must carry its heat to the Heat Zone: " + state);
            helper.assertTrue(Math.abs(received - carried) <= carried * 1.0E-9,
                    "The Heat Zone must receive what the Freeze Zone carried: " + state);
            helper.assertTrue(coldTemp < 790.0, "The heater outside the overlap must be cooled: " + state);
            helper.assertTrue(cancelledTemp > coldTemp + 1.0,
                    "The heater in the overlap must not be cooled by the zone (they cancel out there): " + state);
            helper.succeed();
        });
    }

    /**
     * 熱の受け渡し: 凍結領域を 2 つの加熱領域で挟むと、運んだ熱は触れ合う面の広さに比例して分かれ（3 × 3 と 2 × 2 なら 9 : 4）、
     * 受け取った合計は運んだ熱と同じ（増えも減りもしない）。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_shared", timeoutTicks = 100)
    public static void heatZonesSandwichingAFreezeZoneShareTheCarriedHeat(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_exchange_shared", new Vec3(10.5, 1.0, 7.5));
        // 凍結領域は x 3〜5・y 0〜2・z 1〜3。西の加熱領域 x 0〜2 とは 3 × 3、東の加熱領域 x 6〜8・y 1〜3・z 2〜4 とは 2 × 2 で
        // 触れ合う。
        BlockPos coldHeater = new BlockPos(4, 1, 2);
        helper.setBlock(coldHeater, heater.get());
        // テンプレートの外の領域も動くように、そのチャンクを強制読み込みする。
        Runnable unforce = forceChunks(helper, new BlockPos(-1, 0, -1), new BlockPos(10, 0, 6));
        Runnable cleanup = () -> {
            helper.setBlock(coldHeater, Blocks.AIR);
            cleanup(helper, caster);
            unforce.run();
        };
        try {
            MekanismGameplayCompat.supplyZoneHeat(level, List.of(level.getBlockEntity(helper.absolutePos(coldHeater))),
                    new java.util.IdentityHashMap<>(), 1.0E9, 800.0, at -> true);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, heat(helper, new BlockPos(7, 2, 3)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            int westX = helper.absolutePos(new BlockPos(1, 1, 2)).getX();
            double west = 0.0;
            double east = 0.0;
            for (var object : SpellGramIndex.ofCaster(level, caster.getUUID())) {
                if (object instanceof EntityHeatZone zone) {
                    if (zone.getArea().center().getX() == westX) {
                        west += zone.movedHeatReceived();
                    } else {
                        east += zone.movedHeatReceived();
                    }
                }
            }
            double carried = only(helper, caster, EntityFreezeZone.class).heatCarried();
            cleanup.run();
            String amounts = "west " + west + ", east " + east + ", carried " + carried;
            helper.assertTrue(carried > 0.0 && east > 0.0, "Both Heat Zones must receive heat: " + amounts);
            helper.assertTrue(Math.abs(west + east - carried) <= carried * 1.0E-9,
                    "The Heat Zones must receive exactly the carried heat in total: " + amounts);
            helper.assertTrue(Math.abs(west / east - 9.0 / 4.0) <= 1.0E-6,
                    "The carried heat must be split by the touching faces, 9 : 4: " + amounts);
            helper.succeed();
        });
    }

    /**
     * 熱の受け渡し: 運べるのは周囲の温度より上にあった熱だけ。周囲の温度の抵抗型発熱器は、凍結領域が周囲より下まで冷やしても、
     * 触れ合っている加熱領域へ熱を運ばない（空気から熱は集まらない）。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_ambient", timeoutTicks = 120)
    public static void freezeZoneCarriesNoHeatFromAnAmbientHeater(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_exchange_ambient", new Vec3(9.5, 1.0, 6.5));
        BlockPos cooled = new BlockPos(4, 1, 2);
        BlockPos reference = new BlockPos(8, 1, 5);
        helper.setBlock(cooled, heater.get());
        helper.setBlock(reference, heater.get());
        Runnable cleanup = () -> {
            helper.setBlock(cooled, Blocks.AIR);
            helper.setBlock(reference, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            // 凍結領域は x 3〜5（干渉力 4 → 速さ 2、威力 10）。加熱領域は x 0〜2 で、x = 3 の面で触れ合う。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(60, () -> {
            double cooledTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(cooled));
            double ambient = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(reference));
            double carried = only(helper, caster, EntityFreezeZone.class).heatCarried();
            double received = only(helper, caster, EntityHeatZone.class).movedHeatReceived();
            cleanup.run();
            String state = "cooled " + cooledTemp + " K, ambient " + ambient + " K, carried " + carried + " J, received " + received + " J";
            helper.assertTrue(cooledTemp < ambient - 5.0, "The Freeze Zone must cool the heater below the ambient temperature: " + state);
            helper.assertTrue(carried < 1.0 && received < 1.0,
                    "Heat cooled away below the ambient temperature must not be carried to the Heat Zone: " + state);
            helper.succeed();
        });
    }

    /** 凍結領域 1 つの数値（GameTest 用）: 運んだ熱と、渡し先がなくて捨てた熱。 */
    private static double[] carriedAndDiscarded(List<EntityFreezeZone> zones) {
        double carried = 0.0;
        double discarded = 0.0;
        for (EntityFreezeZone zone : zones) {
            carried += zone.heatCarried();
            discarded += zone.heatDiscarded();
        }
        return new double[]{carried, discarded};
    }

    /** 術者 {@code caster} の凍結領域を、中心の x・z の順に並べたもの。 */
    private static List<EntityFreezeZone> freezeZonesOf(GameTestHelper helper, ServerPlayer caster) {
        return SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).stream()
                .filter(object -> object instanceof EntityFreezeZone).map(object -> (EntityFreezeZone) object)
                .sorted(java.util.Comparator.<EntityFreezeZone>comparingInt(zone -> zone.getArea().center().getX())
                        .thenComparingInt(zone -> zone.getArea().center().getZ()))
                .toList();
    }

    /** 抵抗型発熱器を {@code pos} に置き、800 K まで温める。 */
    private static void hotHeater(GameTestHelper helper, Block heater, BlockPos pos) {
        helper.setBlock(pos, heater);
        ServerLevel level = helper.getLevel();
        MekanismGameplayCompat.supplyZoneHeat(level, List.of(level.getBlockEntity(helper.absolutePos(pos))),
                new java.util.IdentityHashMap<>(), 1.0E9, 800.0, at -> true);
    }

    /**
     * 熱源ごとにまとめる:1 つの抵抗型発熱器を自分の凍結領域 3 つで冷やし、加熱領域に触れて
     * いるのが 1 つだけでも、3 つが奪った熱（周囲より上の分）は全部加熱領域へ渡る。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_one_source", timeoutTicks = 100)
    public static void freezeZonesCoolingOneSourcePassAllOfItsHeat(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_exchange_one_source", new Vec3(10.5, 1.0, 7.5));
        // 発熱器は (4, 1, 2)。加熱領域は x 0〜2。凍結領域は x 3〜5（加熱領域と触れ合う）、x 4〜6、x 4〜6・z 2〜4（どちらも触れない）。
        BlockPos source = new BlockPos(4, 1, 2);
        // テンプレートの外の領域も動くように、そのチャンクを強制読み込みする。
        Runnable unforce = forceChunks(helper, new BlockPos(-1, 0, -1), new BlockPos(10, 0, 6));
        Runnable cleanup = () -> {
            helper.setBlock(source, Blocks.AIR);
            cleanup(helper, caster);
            unforce.run();
        };
        try {
            hotHeater(helper, heater.get(), source);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(5, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(5, 1, 3)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            List<EntityFreezeZone> zones = freezeZonesOf(helper, caster);
            double received = only(helper, caster, EntityHeatZone.class).movedHeatReceived();
            double[] total = carriedAndDiscarded(zones);
            double untouched = zones.size() < 3 ? -1.0 : Math.min(zones.get(1).heatCarried(), zones.get(2).heatCarried());
            cleanup.run();
            String amounts = "received " + received + ", carried " + total[0] + ", discarded " + total[1] + ", zones " + zones.size();
            helper.assertTrue(zones.size() == 3, "Three Freeze Zones must be placed: " + amounts);
            helper.assertTrue(untouched > 0.0,
                    "Freeze Zones not touching the Heat Zone must still pass on the heat of a source a touching one cools: "
                            + amounts + ", least " + untouched);
            helper.assertTrue(Math.abs(received - total[0]) <= total[0] * 1.0E-9,
                    "The Heat Zone must receive what the three Freeze Zones carried: " + amounts);
            helper.assertTrue(total[1] <= total[0] * 0.05, "Almost nothing may be thrown away: " + amounts);
            helper.succeed();
        });
    }

    /**
     * 熱源ごとにまとめる: 熱源が 2 つあり、片方にしか加熱領域に触れている凍結領域がないとき、渡るのはその熱源の分だけ。触れていない
     * 凍結領域も、触れている凍結領域と同じ熱源から奪った分は渡し、もう片方の熱源から奪った分は捨てる。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_two_sources", timeoutTicks = 100)
    public static void onlyTheSourceATouchingFreezeZoneCoolsGivesHeat(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_exchange_two_sources", new Vec3(10.5, 1.0, 7.5));
        // 発熱器は (4, 1, 2) と (7, 1, 2)。凍結領域は x 3〜5（加熱領域 x 0〜2 と触れ合い、(4, 1, 2) だけを冷やす）と、
        // x 4〜8（一辺 5、触れない。2 つとも冷やす）。
        BlockPos shared = new BlockPos(4, 1, 2);
        BlockPos apart = new BlockPos(7, 1, 2);
        // テンプレートの外の領域も動くように、そのチャンクを強制読み込みする。
        Runnable unforce = forceChunks(helper, new BlockPos(-1, 0, -1), new BlockPos(10, 0, 6));
        Runnable cleanup = () -> {
            helper.setBlock(shared, Blocks.AIR);
            helper.setBlock(apart, Blocks.AIR);
            cleanup(helper, caster);
            unforce.run();
        };
        try {
            hotHeater(helper, heater.get(), shared);
            hotHeater(helper, heater.get(), apart);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(6, 1, 2)), 5.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            List<EntityFreezeZone> zones = freezeZonesOf(helper, caster);
            double received = only(helper, caster, EntityHeatZone.class).movedHeatReceived();
            EntityFreezeZone touching = zones.isEmpty() ? null : zones.get(0);
            EntityFreezeZone wide = zones.size() < 2 ? null : zones.get(1);
            double touchingCarried = touching == null ? -1.0 : touching.heatCarried();
            double touchingDiscarded = touching == null ? -1.0 : touching.heatDiscarded();
            double wideCarried = wide == null ? -1.0 : wide.heatCarried();
            double wideDiscarded = wide == null ? -1.0 : wide.heatDiscarded();
            cleanup.run();
            String amounts = "received " + received + "; touching carried " + touchingCarried + ", discarded " + touchingDiscarded
                    + "; wide carried " + wideCarried + ", discarded " + wideDiscarded;
            helper.assertTrue(touchingCarried > 0.0 && touchingDiscarded <= touchingCarried * 1.0E-9,
                    "The touching Freeze Zone must pass on all of its heat: " + amounts);
            helper.assertTrue(wideCarried > 0.0, "The wide Freeze Zone must pass on the heat of the shared heater: " + amounts);
            helper.assertTrue(wideDiscarded > 0.0, "The heat of the heater no touching zone cools must be thrown away: " + amounts);
            helper.assertTrue(Math.abs(received - touchingCarried - wideCarried) <= (touchingCarried + wideCarried) * 1.0E-9,
                    "The Heat Zone must receive exactly what was passed on: " + amounts);
            helper.succeed();
        });
    }

    /**
     * 熱源ごとにまとめる: 他人の凍結領域が奪った熱は運ばない。他人の凍結領域（事象干渉力が高く、重なったところで勝つ）が自分の
     * 加熱領域に触れ、自分の凍結領域と同じ発熱器を冷やしても、自分の加熱領域には熱が入らない。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_other", timeoutTicks = 100)
    public static void anotherCastersFreezeZoneGivesNoHeat(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_exchange_mine", new Vec3(10.5, 1.0, 7.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "zone_heat_exchange_other", new Vec3(11.5, 1.0, 8.5));
        BlockPos source = new BlockPos(4, 1, 2);
        Runnable cleanup = () -> {
            helper.setBlock(source, Blocks.AIR);
            cleanup(helper, caster);
            cleanup(helper, other);
        };
        try {
            hotHeater(helper, heater.get(), source);
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            other.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            // 自分の加熱領域 x 0〜2 と凍結領域 x 3〜5。他人の凍結領域も x 3〜5 で、自分の加熱領域に触れる。
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
            cast(other, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            double received = only(helper, caster, EntityHeatZone.class).movedHeatReceived();
            double[] mine = carriedAndDiscarded(freezeZonesOf(helper, caster));
            double[] theirs = carriedAndDiscarded(freezeZonesOf(helper, other));
            double temperature = MekanismGameplayCompat.temperatureAt(helper.getLevel(), helper.absolutePos(source));
            cleanup.run();
            String amounts = "received " + received + "; mine carried " + mine[0] + ", discarded " + mine[1]
                    + "; theirs carried " + theirs[0] + ", discarded " + theirs[1] + "; heater " + temperature + " K";
            helper.assertTrue(temperature < 790.0, "The other caster's Freeze Zone must cool the heater: " + amounts);
            helper.assertTrue(theirs[0] == 0.0 && theirs[1] > 0.0,
                    "The other caster's Freeze Zone must not pass its heat to my Heat Zone: " + amounts);
            helper.assertTrue(received == 0.0, "My Heat Zone must get no heat taken by another caster's zone: " + amounts);
            helper.succeed();
        });
    }

    /** 熱の受け渡しの数値の記録（GameTest の結果をログに残す）。 */
    private static final org.slf4j.Logger HEAT_REPORT = com.mojang.logging.LogUtils.getLogger();

    /** 術者 {@code caster} の、中心がテンプレートの {@code relativeCenter} にある {@code type} の領域。 */
    private static <T extends SpellGramZone> T zoneAt(GameTestHelper helper, ServerPlayer caster, Class<T> type,
                                                        BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        return SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).stream()
                .filter(type::isInstance).map(type::cast).filter(zone -> zone.getArea().center().equals(center))
                .findFirst().orElseThrow(() -> new IllegalStateException("No " + type.getSimpleName() + " at " + relativeCenter));
    }

    /**
     * テンプレートの外まで広がる配置のために、テンプレートの {@code from} から {@code to} までのチャンクを強制読み込みし、戻す処理を
     * 返す。GameTest が読み込むのはテンプレートのチャンクだけで、その隣のチャンクではブロックエンティティは動くがエンティティ
     * （領域）は動かない。もともと強制読み込みのチャンクは戻さない。
     */
    private static Runnable forceChunks(GameTestHelper helper, BlockPos from, BlockPos to) {
        ServerLevel level = helper.getLevel();
        BlockPos a = helper.absolutePos(from);
        BlockPos b = helper.absolutePos(to);
        List<net.minecraft.world.level.ChunkPos> added = new java.util.ArrayList<>();
        for (int cx = Math.min(a.getX(), b.getX()) >> 4; cx <= Math.max(a.getX(), b.getX()) >> 4; cx++) {
            for (int cz = Math.min(a.getZ(), b.getZ()) >> 4; cz <= Math.max(a.getZ(), b.getZ()) >> 4; cz++) {
                if (!level.getForcedChunks().contains(net.minecraft.world.level.ChunkPos.asLong(cx, cz))) {
                    level.setChunkForced(cx, cz, true);
                    added.add(new net.minecraft.world.level.ChunkPos(cx, cz));
                }
            }
        }
        return () -> added.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, false));
    }

    /** 凍結領域の運んだ熱・捨てた熱と、加熱領域の受け取った熱を、決まった順に並べたもの（差を取るため）。 */
    private static double[] heatCounters(List<EntityFreezeZone> freezes, List<EntityHeatZone> heats) {
        double[] values = new double[freezes.size() * 2 + heats.size()];
        int i = 0;
        for (EntityFreezeZone zone : freezes) {
            values[i++] = zone.heatCarried();
            values[i++] = zone.heatDiscarded();
        }
        for (EntityHeatZone zone : heats) {
            values[i++] = zone.movedHeatReceived();
        }
        return values;
    }

    /**
     * 熱の受け渡しの複雑な配置: 熱の機械・凍結領域・加熱領域を組み合わせたときの熱の移動を確かめる。
     * 800 K の抵抗型発熱器 4 つ、自分の加熱領域 2 つ（片方は熱乱流）・凍結領域 3 つ、他人の凍結領域 1 つ。
     * <ul>
     * <li>H1 加熱 x 0〜2。F1 凍結 x 3〜5 が面で触れる（9）。F1 は S1 だけを冷やす。</li>
     * <li>H2 加熱 x 8〜10（乱流 x 9〜11 で熱乱流）。F2 凍結 x 4〜8（一辺 5）と x = 8 の列で重なる（9）。F2 は S1 と S2 を冷やす。</li>
     * <li>F3 凍結 x 3〜5・z 6〜8 は S4 だけを冷やし、どの加熱領域にもつながらない。</li>
     * <li>他人の凍結領域 x 0〜2・z 4〜6 は S3 を冷やし、H1 に面で触れる。</li>
     * </ul>
     * 置いて 20 tick（渡し先がそろうまで）から 40 tick までの差で確かめる: S1 の熱（F1 の分と F2 の半分）は H1 と H2 に 9 : 9、
     * S2 の熱（F2 の残り半分）は H2 だけ、S4・S3 の熱は捨てる。受け取った合計は運んだ合計と同じ。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_layout", timeoutTicks = 140)
    public static void complexHeatExchangeLayoutRoutesEverySource(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_layout_mine", new Vec3(12.5, 1.0, 9.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "zone_heat_layout_other", new Vec3(13.5, 1.0, 10.5));
        BlockPos s1 = new BlockPos(4, 1, 2);
        BlockPos s2 = new BlockPos(7, 1, 2);
        BlockPos s3 = new BlockPos(1, 1, 5);
        BlockPos s4 = new BlockPos(4, 1, 7);
        List<BlockPos> sources = List.of(s1, s2, s3, s4);
        Runnable unforce = forceChunks(helper, new BlockPos(-1, 0, -1), new BlockPos(12, 0, 9));
        Runnable cleanup = () -> {
            sources.forEach(pos -> helper.setBlock(pos, Blocks.AIR));
            cleanup(helper, caster);
            cleanup(helper, other);
            unforce.run();
        };
        try {
            sources.forEach(pos -> hotHeater(helper, heater.get(), pos));
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            other.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, heat(helper, new BlockPos(9, 1, 2)), 3.0, 10.0);
            cast(caster, turbulence(helper, new BlockPos(10, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(6, 1, 2)), 5.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 7)), 3.0, 10.0);
            cast(other, freeze(helper, new BlockPos(1, 1, 5)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(20, () -> {
            List<EntityFreezeZone> freezes = List.of(
                    zoneAt(helper, caster, EntityFreezeZone.class, new BlockPos(4, 1, 2)),
                    zoneAt(helper, caster, EntityFreezeZone.class, new BlockPos(6, 1, 2)),
                    zoneAt(helper, caster, EntityFreezeZone.class, new BlockPos(4, 1, 7)),
                    zoneAt(helper, other, EntityFreezeZone.class, new BlockPos(1, 1, 5)));
            List<EntityHeatZone> heats = List.of(
                    zoneAt(helper, caster, EntityHeatZone.class, new BlockPos(1, 1, 2)),
                    zoneAt(helper, caster, EntityHeatZone.class, new BlockPos(9, 1, 2)));
            double[] before = heatCounters(freezes, heats);
            helper.runAfterDelay(20, () -> {
                double[] after = heatCounters(freezes, heats);
                double[] d = new double[after.length];
                for (int i = 0; i < d.length; i++) {
                    d[i] = after[i] - before[i];
                }
                // d: F1 運んだ・捨てた、F2 運んだ・捨てた、F3 運んだ・捨てた、他人 運んだ・捨てた、H1 受け取った、H2 受け取った。
                double f1 = d[0];
                double f2 = d[2];
                double expectedH1 = 0.5 * (f1 + f2 / 2.0);
                double expectedH2 = 0.5 * f1 + 0.75 * f2;
                boolean storm = heats.get(1).isStorm();
                double h1Total = heats.get(0).heatTotalPerTick();
                double h1Own = heats.get(0).ownHeatTotalPerTick();
                double h2Total = heats.get(1).heatTotalPerTick();
                double h2Own = heats.get(1).ownHeatTotalPerTick();
                StringBuilder temps = new StringBuilder();
                for (BlockPos pos : sources) {
                    temps.append(String.format(java.util.Locale.ROOT, " %.1f", MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(pos))));
                }
                cleanup.run();
                String report = String.format(java.util.Locale.ROOT,
                        "[heat-exchange layout] 20 ticks: F1 carried %.2f discarded %.2f | F2 carried %.2f discarded %.2f"
                                + " | F3 carried %.2f discarded %.2f | other carried %.2f discarded %.2f"
                                + " | H1 received %.2f (expected %.2f) | H2 received %.2f (expected %.2f)"
                                + " | H1 heat %.2f J/t (own %.2f) | H2 heat %.2f J/t (own %.2f, storm %s) | S1-S4 K%s",
                        d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], d[8], expectedH1, d[9], expectedH2,
                        h1Total, h1Own, h2Total, h2Own, storm, temps);
                HEAT_REPORT.info(report);
                double carried = d[0] + d[2] + d[4];
                helper.assertTrue(f1 > 0.0 && f2 > 0.0, "F1 and F2 must carry heat: " + report);
                helper.assertTrue(d[1] <= f1 * 1.0E-9 && d[3] <= f2 * 1.0E-9,
                        "F1 and F2 cool only sources joined to a Heat Zone, so nothing may be thrown away: " + report);
                helper.assertTrue(d[4] == 0.0 && d[5] > 0.0, "F3 is joined to nothing, so its heat must be thrown away: " + report);
                helper.assertTrue(d[6] == 0.0 && d[7] > 0.0,
                        "The other caster's Freeze Zone must not give its heat to my Heat Zone: " + report);
                helper.assertTrue(Math.abs(d[8] + d[9] - carried) <= carried * 1.0E-9,
                        "The Heat Zones must receive exactly what was carried in total: " + report);
                helper.assertTrue(Math.abs(d[8] - expectedH1) <= expectedH1 * 1.0E-6 && Math.abs(d[9] - expectedH2) <= expectedH2 * 1.0E-6,
                        "S1's heat must be split 9 : 9 and S2's must go to H2 only: " + report);
                helper.assertTrue(storm, "H2 must be a Heat Storm: " + report);
                helper.succeed();
            });
        });
    }

    /**
     * 熱の受け渡しで、組み上がったマルチブロックは 1 つの熱源:4 × 18 × 4 の加温蒸発濃縮プラントを、西のバルブから自分の
     * 凍結領域 F_w（自分の加熱領域に触れる）、東のバルブから自分の凍結領域 F_e（どこにも触れない）、南のバルブから他人の凍結領域で
     * 冷やす。F_e が奪った熱も全部渡り、他人の凍結領域が奪った熱は渡らない。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_multiblock", timeoutTicks = 220)
    public static void multiblockCooledFromSeveralSidesIsOneHeatSource(GameTestHelper helper) {
        Optional<Block> valve = block("mekanism:thermal_evaporation_valve");
        if (!MekanismCompat.isMekanismLoaded() || valve.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_multiblock_mine", new Vec3(12.5, 1.0, 9.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "zone_heat_multiblock_other", new Vec3(13.5, 1.0, 10.5));
        // 加温蒸発濃縮プラントは x 4〜7・y 1〜18・z 0〜3（Mekanism の /mek build と同じ組み立て、コントローラーは北 (5, 2, 0)）。
        // バルブは西 (4, 2, 1)・東 (7, 2, 2)・南 (5, 2, 3)。
        BlockPos west = new BlockPos(4, 2, 1);
        BlockPos east = new BlockPos(7, 2, 2);
        BlockPos south = new BlockPos(5, 2, 3);
        Runnable unforce = forceChunks(helper, new BlockPos(-1, 0, -1), new BlockPos(10, 0, 6));
        Runnable cleanup = () -> {
            for (int x = 4; x <= 7; x++) {
                for (int y = 1; y <= 18; y++) {
                    for (int z = 0; z <= 3; z++) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                    }
                }
            }
            cleanup(helper, caster);
            cleanup(helper, other);
            unforce.run();
        };
        MekanismGameplayCompat.buildEvaporationPlant(level, helper.absolutePos(new BlockPos(4, 1, 0)));
        helper.setBlock(west, valve.get());
        helper.setBlock(east, valve.get());
        helper.setBlock(south, valve.get());
        helper.runAfterDelay(20, () -> {
            if (!MekanismGameplayCompat.isFormedMultiblockAt(level, helper.absolutePos(west))) {
                cleanup.run();
                helper.fail("The Thermal Evaporation Plant did not form");
                return;
            }
            try {
                MekanismGameplayCompat.supplyZoneHeat(level, List.of(level.getBlockEntity(helper.absolutePos(west))),
                        new java.util.IdentityHashMap<>(), 1.0E12, 800.0, at -> true);
                caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
                other.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
                // 自分の加熱領域 x 0〜2、F_w x 3〜5（x = 3 で触れる）、F_e x 7〜9、他人 x 4〜6・z 3〜5。
                cast(caster, heat(helper, new BlockPos(1, 2, 1)), 3.0, 10.0);
                cast(caster, freeze(helper, new BlockPos(4, 2, 1)), 3.0, 10.0);
                cast(caster, freeze(helper, new BlockPos(8, 2, 2)), 3.0, 10.0);
                cast(other, freeze(helper, new BlockPos(5, 2, 4)), 3.0, 10.0);
            } catch (RuntimeException | SpellRuntimeException e) {
                cleanup.run();
                throw new RuntimeException(e);
            }
            double startTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(west));
            helper.runAfterDelay(20, () -> {
                List<EntityFreezeZone> freezes = List.of(
                        zoneAt(helper, caster, EntityFreezeZone.class, new BlockPos(4, 2, 1)),
                        zoneAt(helper, caster, EntityFreezeZone.class, new BlockPos(8, 2, 2)),
                        zoneAt(helper, other, EntityFreezeZone.class, new BlockPos(5, 2, 4)));
                List<EntityHeatZone> heats = List.of(zoneAt(helper, caster, EntityHeatZone.class, new BlockPos(1, 2, 1)));
                double[] before = heatCounters(freezes, heats);
                helper.runAfterDelay(20, () -> {
                    double[] after = heatCounters(freezes, heats);
                    double[] d = new double[after.length];
                    for (int i = 0; i < d.length; i++) {
                        d[i] = after[i] - before[i];
                    }
                    double endTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(west));
                    cleanup.run();
                    String report = String.format(java.util.Locale.ROOT,
                            "[heat-exchange multiblock] 20 ticks: F_w carried %.2f discarded %.2f | F_e carried %.2f discarded %.2f"
                                    + " | other carried %.2f discarded %.2f | H received %.2f | plant %.1f K -> %.1f K",
                            d[0], d[1], d[2], d[3], d[4], d[5], d[6], startTemp, endTemp);
                    HEAT_REPORT.info(report);
                    helper.assertTrue(d[0] > 0.0 && d[2] > 0.0, "Both of my Freeze Zones must carry the plant's heat: " + report);
                    helper.assertTrue(d[1] <= d[0] * 1.0E-9 && d[3] <= d[2] * 1.0E-9,
                            "The plant is one heat source joined to my Heat Zone, so nothing may be thrown away: " + report);
                    helper.assertTrue(d[4] == 0.0 && d[5] > 0.0,
                            "The other caster's Freeze Zone must not give the plant's heat to my Heat Zone: " + report);
                    helper.assertTrue(Math.abs(d[6] - d[0] - d[2]) <= (d[0] + d[2]) * 1.0E-9,
                            "My Heat Zone must receive exactly what my Freeze Zones carried: " + report);
                    helper.succeed();
                });
            });
        });
    }

    /**
     * 受け渡しの見え方:つながっていても、熱源が周囲の温度なら運ぶ熱はないので、加熱領域の受け取りは
     * 0 で「運んでいる」演出にならず、照準の表示の運搬・受け取りも 0（行を出さない）。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_quiet", timeoutTicks = 120)
    public static void connectedZonesWithoutHotSourcesShowNoCarrying(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_exchange_quiet", new Vec3(9.5, 1.0, 6.5));
        BlockPos cooled = new BlockPos(4, 1, 2);
        helper.setBlock(cooled, heater.get());
        Runnable cleanup = () -> {
            helper.setBlock(cooled, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            // 加熱領域 x 0〜2 と凍結領域 x 3〜5 が x = 3 で触れ合う。発熱器は周囲の温度のまま。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(60, () -> {
            EntityHeatZone hot = only(helper, caster, EntityHeatZone.class);
            EntityFreezeZone cold = only(helper, caster, EntityFreezeZone.class);
            double received = hot.receivedHeatPerTick();
            boolean carrying = hot.showsCarryingHint();
            float shownReceived = hot.getResourceMoved();
            float shownCarried = cold.getResourceMoved();
            double link = ZoneHeatExchange.linkArea(hot.getArea().bounds(), cold.getArea().bounds());
            cleanup.run();
            String state = "received " + received + " J/t, carrying hint " + carrying + ", shown received " + shownReceived
                    + ", shown carried " + shownCarried + ", link " + link;
            helper.assertTrue(link > 0.0, "The zones must be joined: " + state);
            helper.assertTrue(received == 0.0 && !carrying, "Without heat to carry, the zones must not show carrying: " + state);
            helper.assertTrue(shownReceived == 0.0F && shownCarried == 0.0F,
                    "The aim display must show no carried or received heat: " + state);
            helper.succeed();
        });
    }

    /**
     * 受け渡しの見え方: 運んでいるとき、照準の表示の、凍結領域の運搬の合計と加熱領域の受け取りが同じ。触れていない凍結領域も、
     * 同じ熱源を冷やす触れている凍結領域を通して渡った分を運搬に数える。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_exchange_shown", timeoutTicks = 100)
    public static void shownCarriedHeatMatchesShownReceivedHeat(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_exchange_shown", new Vec3(10.5, 1.0, 7.5));
        // 発熱器 (4, 1, 2) を、加熱領域 x 0〜2 と触れ合う凍結領域 x 3〜5 と、触れない凍結領域 x 4〜6 で冷やす。
        BlockPos source = new BlockPos(4, 1, 2);
        Runnable unforce = forceChunks(helper, new BlockPos(-1, 0, -1), new BlockPos(10, 0, 6));
        Runnable cleanup = () -> {
            helper.setBlock(source, Blocks.AIR);
            cleanup(helper, caster);
            unforce.run();
        };
        try {
            hotHeater(helper, heater.get(), source);
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(5, 1, 2)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            EntityHeatZone hot = only(helper, caster, EntityHeatZone.class);
            List<EntityFreezeZone> colds = freezeZonesOf(helper, caster);
            float shownReceived = hot.getResourceMoved();
            double shownCarried = colds.stream().mapToDouble(EntityFreezeZone::getResourceMoved).sum();
            double leastCarried = colds.stream().mapToDouble(EntityFreezeZone::getResourceMoved).min().orElse(-1.0);
            boolean carrying = hot.showsCarryingHint();
            cleanup.run();
            String state = "shown received " + shownReceived + " J/t, shown carried " + shownCarried + " J/t (least " + leastCarried
                    + "), carrying hint " + carrying + ", zones " + colds.size();
            helper.assertTrue(colds.size() == 2 && leastCarried > 0.0,
                    "Both Freeze Zones must show carried heat, the untouched one too: " + state);
            helper.assertTrue(carrying, "The Heat Zone must show carrying: " + state);
            helper.assertTrue(Math.abs(shownReceived - shownCarried) <= shownCarried * 1.0E-4,
                    "The shown carried heat must add up to the shown received heat: " + state);
            helper.succeed();
        });
    }

    /**
     * 電気炉: 自分の荷電領域と乱流領域が重なった加熱領域は、一括燻製の代わりに一括製錬をする（鉄の原石 → 鉄インゴット）。
     * レシピのない土は燃えない。Create の一括製錬が燃やして消す生の牛肉も、落ちているものとデポの上のものの両方が、消えずに
     * ステーキになる。Create がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_arc_furnace", timeoutTicks = 400)
    public static void chargedHeatZoneBlastsItemsWithCreate(GameTestHelper helper) {
        arcFurnaceTest(helper, true);
    }

    /**
     * 乱流領域が重なっていなければ、加熱領域と荷電領域が重なっていても一括製錬しない。他人の乱流領域が重なっていても同じ。
     * Create がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_arc_furnace_no_turbulence", timeoutTicks = 400)
    public static void chargedHeatZoneDoesNotBlastWithoutOwnTurbulence(GameTestHelper helper) {
        arcFurnaceTest(helper, false);
    }

    private static void arcFurnaceTest(GameTestHelper helper, boolean ownTurbulence) {
        if (!CreateCompat.isCreateLoaded()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, ownTurbulence ? "zone_arc_furnace" : "zone_arc_furnace_alone",
                new Vec3(8.5, 1.0, 2.5));
        ServerPlayer other = ownTurbulence ? null : GameTestPlayers.inLevel(helper, "zone_arc_furnace_other", new Vec3(8.5, 1.0, 4.5));
        helper.setBlock(new BlockPos(1, 0, 2), Blocks.STONE);
        Vec3 itemAt = helper.absoluteVec(new Vec3(1.5, 1.0, 2.5));
        ItemEntity iron = new ItemEntity(level, itemAt.x, itemAt.y, itemAt.z, new ItemStack(Items.RAW_IRON));
        iron.setNeverPickUp();
        iron.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(iron);
        ItemEntity dirt = new ItemEntity(level, itemAt.x, itemAt.y, itemAt.z, new ItemStack(Items.DIRT));
        dirt.setNeverPickUp();
        dirt.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(dirt);
        ItemEntity beef = new ItemEntity(level, itemAt.x, itemAt.y, itemAt.z, new ItemStack(Items.BEEF));
        beef.setNeverPickUp();
        beef.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(beef);
        // デポの上の生の牛肉（デポ・メカニカルベルトの側の加工）。デポは領域を置く前に置く（領域が覚える
        // ブロックエンティティの一覧に入るように）。
        BlockPos depotAt = new BlockPos(1, 1, 3);
        helper.setBlock(depotAt, block("create:depot").orElseThrow());
        AABB around = new AABB(helper.absolutePos(CENTER)).inflate(5.0);
        Runnable cleanup = () -> {
            helper.setBlock(depotAt, Blocks.AIR);
            level.getEntitiesOfClass(ItemEntity.class, around).forEach(ItemEntity::discard);
            if (other != null) {
                cleanup(helper, other);
            }
            cleanup(helper, caster);
        };
        try {
            IItemHandler depot = level.getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(depotAt), null);
            helper.assertTrue(depot != null && depot.insertItem(0, new ItemStack(Items.BEEF), false).isEmpty(),
                    "Raw beef must go onto the Depot");
            // 事象干渉力 4 → 速さは上限の 2 倍（Create の処理時間 150 tick を 75 tick ほどで）。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, charged(helper, new BlockPos(1, 1, 2)), 3.0, 5.0);
            // 一括製錬は同じ術者の乱流領域も重なったところだけ。製錬しない場合も、他人の乱流領域を同じ範囲に重ねる。
            cast(ownTurbulence ? caster : other, turbulence(helper, new BlockPos(1, 1, 2)), 3.0, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(120, () -> {
            boolean blasted = !level.getEntitiesOfClass(ItemEntity.class, around,
                    entity -> entity.getItem().is(Items.IRON_INGOT)).isEmpty();
            boolean rawKept = !level.getEntitiesOfClass(ItemEntity.class, around,
                    entity -> entity.getItem().is(Items.RAW_IRON)).isEmpty();
            boolean dirtKept = !level.getEntitiesOfClass(ItemEntity.class, around,
                    entity -> entity.getItem().is(Items.DIRT)).isEmpty();
            boolean beefCooked = !level.getEntitiesOfClass(ItemEntity.class, around,
                    entity -> entity.getItem().is(Items.COOKED_BEEF)).isEmpty();
            boolean beefRaw = !level.getEntitiesOfClass(ItemEntity.class, around,
                    entity -> entity.getItem().is(Items.BEEF)).isEmpty();
            boolean depotCooked = depotHolds(level, helper.absolutePos(depotAt), Items.COOKED_BEEF);
            boolean depotRaw = depotHolds(level, helper.absolutePos(depotAt), Items.BEEF);
            cleanup.run();
            String state = "dropped beef: cooked " + beefCooked + ", raw " + beefRaw + "; Depot beef: cooked " + depotCooked
                    + ", raw " + depotRaw;
            if (ownTurbulence) {
                helper.assertTrue(blasted,
                        "A Heat Zone overlapped by your own Charged and Turbulence Zones must blast raw iron into an iron ingot");
                helper.assertTrue(beefCooked && !beefRaw && depotCooked && !depotRaw,
                        "The electric furnace must cook raw beef into steak instead of burning it away, dropped and on a Depot: "
                                + state);
            } else {
                helper.assertTrue(!blasted && rawKept, "Without your own Turbulence Zone, the Heat and Charged Zones must not blast items");
                helper.assertTrue(!beefCooked && beefRaw && !depotCooked && depotRaw,
                        "Without your own Turbulence Zone, raw beef must stay as it is: " + state);
            }
            helper.assertTrue(dirtKept, "It must not burn dirt, which has no smelting recipe");
            helper.succeed();
        });
    }

    /** {@code pos} のデポ（持っているアイテムと、加工の出力の置き場）に {@code item} があるか。 */
    private static boolean depotHolds(ServerLevel level, BlockPos pos, net.minecraft.world.item.Item item) {
        IItemHandler depot = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (depot == null) {
            return false;
        }
        for (int slot = 0; slot < depot.getSlots(); slot++) {
            if (depot.getStackInSlot(slot).is(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 凍結領域は中の Mekanism の熱を持つ設備を下限（275 − 威力 × 5 K）に向けて冷やす（外の設備より速く冷える、下限より
     * 下には冷えない）。加熱領域と重なったブロックの設備は冷えない（打ち消し合う）。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_freeze_mekanism", timeoutTicks = 120)
    public static void freezeZoneCoolsMekanismBlocks(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        helper.assertTrue(Math.abs(EntityFreezeZone.coolFractionPerTick(5.0F, 1.0F) - 0.01) < 1.0E-9
                        && EntityFreezeZone.coolFractionPerTick(1000.0F, 1.0F) == EntityFreezeZone.MAX_COOL_FRACTION,
                "The cooling fraction must be Power x 0.002 x machine speed, at most 0.5");
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_freeze_mekanism", new Vec3(4.5, 1.0, 0.5));
        BlockPos cooled = new BlockPos(1, 1, 1);
        BlockPos cancelled = new BlockPos(1, 1, 3);
        BlockPos outside = new BlockPos(4, 1, 4);
        List<BlockPos> heaters = List.of(cooled, cancelled, outside);
        heaters.forEach(pos -> helper.setBlock(pos, heater.get()));
        Runnable cleanup = () -> {
            heaters.forEach(pos -> helper.setBlock(pos, Blocks.AIR));
            cleanup(helper, caster);
        };
        try {
            // 3 台とも 800 K まで温める。
            for (BlockPos pos : heaters) {
                MekanismGameplayCompat.supplyZoneHeat(level, List.of(level.getBlockEntity(helper.absolutePos(pos))),
                        new java.util.IdentityHashMap<>(), 1.0E9, 800.0, at -> true);
            }
            // 凍結領域は x 0〜2・z 1〜3（干渉力 4 → 速さ 2、1 台あたり毎 tick 5.6% まで）。冷却量は熱量と同じ 720 J/Psi
            // なので、威力 10（中の威力 14、コスト 100 → 約 240 J/t）にする。cancelled の 1 ブロックだけ加熱領域も重ねる。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, freeze(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, heat(helper, cancelled), 1.0, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(42, () -> {
            double cooledTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(cooled));
            double cancelledTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(cancelled));
            double outsideTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(outside));
            cleanup.run();
            String temps = "cooled " + cooledTemp + " K, cancelled " + cancelledTemp + " K, outside " + outsideTemp + " K";
            helper.assertTrue(cooledTemp < outsideTemp - 50.0, "The Freeze Zone must cool the heater inside: " + temps);
            // 冷やせる下限は 275 − 中の威力 × 5 K（入れた威力 10 で 205 K）。
            helper.assertTrue(cooledTemp > EntityFreezeZone.minTemperatureFor(com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.resourcePower(10.0)) - 0.5,
                    "It must not cool below the lowest temperature, 205 K: " + temps);
            helper.assertTrue(cancelledTemp > cooledTemp + 50.0,
                    "A heater where the Heat Zone cancels the Freeze Zone must not be cooled: " + temps);
            helper.succeed();
        });
    }

    /**
     * 凍結領域は Mekanism の機械を周囲の温度より下まで冷やせる: 下限は周囲の温度によらず 275 − 中の威力 × 5 K（入れた威力 10 で
     * 205 K）。周囲の温度の発熱器を冷やすと、周囲より 50 K 以上下がり（平地の周囲 300 K から下限 205 K へ）、下限より下には
     * ならない。下限の式も確かめる。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_freeze_below_ambient", timeoutTicks = 160)
    public static void freezeZoneCoolsMekanismBlocksBelowAmbient(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        helper.assertTrue(EntityFreezeZone.minTemperatureFor(com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.resourcePower(1.0)) == 250.0
                        && EntityFreezeZone.minTemperatureFor(com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.resourcePower(5.0)) == 230.0
                        && EntityFreezeZone.minTemperatureFor(com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.resourcePower(10.0)) == 205.0
                        && EntityFreezeZone.minTemperatureFor(100.0F) == 205.0,
                "The lowest temperature must be 275 - Power x 5 K, stopping at the Power of 14");
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_freeze_below_ambient", new Vec3(4.5, 1.0, 0.5));
        BlockPos cooled = new BlockPos(1, 1, 1);
        BlockPos reference = new BlockPos(4, 1, 4);
        helper.setBlock(cooled, heater.get());
        helper.setBlock(reference, heater.get());
        Runnable cleanup = () -> {
            helper.setBlock(cooled, Blocks.AIR);
            helper.setBlock(reference, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, freeze(helper, new BlockPos(1, 1, 1)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        // 干渉力 4 → 速さ 2、威力 10（中の威力 14）で、1 tick に下限との温度差の 5.6% ずつ縮む（300 K から 100 tick で下限の
        // 1 K 以内まで下がる）。
        helper.runAfterDelay(110, () -> {
            double cooledTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(cooled));
            double ambient = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(reference));
            cleanup.run();
            String temps = "cooled " + cooledTemp + " K, ambient " + ambient + " K";
            helper.assertTrue(cooledTemp < ambient - 55.0,
                    "The Freeze Zone must cool the heater well below the ambient temperature, towards 205 K: " + temps);
            helper.assertTrue(cooledTemp > EntityFreezeZone.minTemperatureFor(com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.resourcePower(10.0)) - 0.5,
                    "It must not cool below the lowest temperature, 205 K: " + temps);
            helper.succeed();
        });
    }

    /** 加熱領域の中のブレイズバーナーは燃料なしで「加熱」になり、外のものは変わらない。Create がなければ飛ばす。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner", timeoutTicks = 100)
    public static void heatZoneKindlesBlazeBurners(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.KINDLE);
    }

    /** 加熱領域が消えると、ブレイズバーナーは数 tick で元の段に戻る。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_revert", timeoutTicks = 100)
    public static void heatZoneBurnerRevertsWhenTheZoneEnds(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.REVERT);
    }

    /**
     * 領域からは極度の加熱にしない:同じ大きさの凍結領域と直方体を二分していても、
     * 加熱領域の中のブレイズバーナーは「加熱」まで。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_split", timeoutTicks = 100)
    public static void heatZoneSplittingABoxWithAFreezeZoneOnlyKindlesBurners(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.SPLIT);
    }

    /**
     * 熱の総量: ブレイズバーナーは 1 台 200 J/t を熱の総量から払い、払える台数だけ加熱になる。1 台ぶんしか
     * ない領域では、中心に近い 1 台だけが加熱になる。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_budget", timeoutTicks = 100)
    public static void heatZoneKindlesOnlyTheBurnersItCanPayFor(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.BUDGET);
    }

    /**
     * 威力は 10 より上も入れられる: 一辺 3 でも威力 100（中の威力 104、コスト 424 → 干渉力 1 で約 254 J/t）ならバーナー 1 台を
     * 加熱にできる。1 か所あたりの強さ（上限の温度）は入れた威力 10（中の威力 14）で頭打ち。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_small", timeoutTicks = 100)
    public static void smallHeatZoneWithHighPowerKindlesABurner(GameTestHelper helper) {
        int cost = com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHeatZone.costFor(9, 100.0);
        helper.assertTrue(cost == 424, "Size 3, Power 100 must cost 424, got " + cost);
        // 規模は入れた威力 10 で頭打ち: サイズ 3 なら威力によらず 10 + ⌊9 × 10 × 0.27⌋ = 34。
        int potency = com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase.poweredPotency(9, 100.0);
        helper.assertTrue(potency == 34, "Potency must stop growing at Power 10: size 3, Power 100 must be 34, got " + potency);
        burnerTest(helper, BurnerCase.SMALL);
    }

    /**
     * 重なった自分の加熱領域: 熱量は足し算だが、同じバーナーを二度払いしない（どちらか 1 つだけが
     * 払う）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_stacked", timeoutTicks = 100)
    public static void overlappingHeatZonesPayForABurnerOnce(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.STACKED);
    }

    /** 凍結領域と重なって打ち消されたところのブレイズバーナーは変わらない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_cancel", timeoutTicks = 100)
    public static void cancelledHeatZoneLeavesBlazeBurnersAlone(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.CANCEL);
    }

    private enum BurnerCase { KINDLE, REVERT, SPLIT, CANCEL, BUDGET, SMALL, STACKED }

    /**
     * ブレイズバーナーの段は、Create のクラスを使わずブロックの状態（{@code blaze} = smouldering・kindled・seething）で見る
     * （Create がないときにこのテストのクラスが読めなくならないように）。
     */
    private static void burnerTest(GameTestHelper helper, BurnerCase mode) {
        Optional<Block> burner = block("create:blaze_burner");
        if (!CreateCompat.isCreateLoaded() || burner.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_burner_" + mode.name().toLowerCase(java.util.Locale.ROOT),
                new Vec3(4.5, 1.0, 4.5));
        // 加熱領域は (1, 1, 2) を中心にした一辺 9（x -3〜5）。熱の総量でバーナー 1 台（200 J/t）だけ払えるよう、干渉力 1（速さ 0.5）・
        // 威力 10（中の威力 14、コスト 503 × 720 J ÷ 600 tick × 0.5 → 約 302 J/t）にする。
        BlockPos inside = new BlockPos(1, 1, 1);
        BlockPos outside = new BlockPos(8, 1, 2);
        // BUDGET: 1 台ぶん（200 J/t）しか払えない領域に、中心から遠いもう 1 台を置く。
        BlockPos farther = new BlockPos(4, 1, 5);
        helper.setBlock(inside, withProperty(burner.get().defaultBlockState(), "blaze", "smouldering"));
        helper.setBlock(outside, withProperty(burner.get().defaultBlockState(), "blaze", "smouldering"));
        if (mode == BurnerCase.BUDGET) {
            helper.setBlock(farther, withProperty(burner.get().defaultBlockState(), "blaze", "smouldering"));
        }
        Runnable cleanup = () -> {
            helper.setBlock(inside, Blocks.AIR);
            helper.setBlock(outside, Blocks.AIR);
            helper.setBlock(farther, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(1.0D);
            switch (mode) {
                case KINDLE, REVERT, BUDGET -> cast(caster, heat(helper, new BlockPos(1, 1, 2)), 9.0, 10.0);
                // 一辺 3（x 0〜2）・威力 100。外のバーナー (8, 1, 2) は外。
                case SMALL -> cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 100.0);
                // 同じ加熱領域を 2 つ重ねる（どちらもバーナー 1 台ぶん払える）。
                case STACKED -> {
                    cast(caster, heat(helper, new BlockPos(1, 1, 2)), 9.0, 10.0);
                    cast(caster, heat(helper, new BlockPos(1, 1, 2)), 9.0, 10.0);
                }
                // 加熱 x -3〜5、凍結 x 6〜14（同じ大きさで二分）。
                case SPLIT -> {
                    cast(caster, heat(helper, new BlockPos(1, 1, 2)), 9.0, 10.0);
                    cast(caster, freeze(helper, new BlockPos(10, 1, 2)), 9.0, 10.0);
                }
                case CANCEL -> {
                    cast(caster, heat(helper, new BlockPos(1, 1, 2)), 9.0, 10.0);
                    cast(caster, freeze(helper, new BlockPos(1, 1, 2)), 9.0, 10.0);
                }
            }
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(mode == BurnerCase.SPLIT ? 25 : 14, () -> {
            String insideLevel = propertyValue(helper.getBlockState(inside), "blaze");
            if (mode == BurnerCase.BUDGET) {
                String fartherLevel = propertyValue(helper.getBlockState(farther), "blaze");
                int powered = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                        .filter(zone -> zone instanceof EntityHeatZone).mapToInt(zone -> ((EntityHeatZone) zone).poweredBurnerCount())
                        .sum();
                cleanup.run();
                helper.assertTrue(powered == 1, "A zone with heat for one burner must kindle exactly one, kindled " + powered);
                helper.assertTrue("kindled".equals(insideLevel) && "smouldering".equals(fartherLevel),
                        "The burner nearer the centre must be kindled first: near " + insideLevel + ", far " + fartherLevel);
                helper.succeed();
                return;
            }
            String outsideLevel = propertyValue(helper.getBlockState(outside), "blaze");
            if (mode == BurnerCase.STACKED) {
                int powered = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                        .filter(zone -> zone instanceof EntityHeatZone).mapToInt(zone -> ((EntityHeatZone) zone).poweredBurnerCount())
                        .sum();
                cleanup.run();
                helper.assertTrue("kindled".equals(insideLevel), "The Blaze Burner must be kindled, was " + insideLevel);
                helper.assertTrue(powered == 1, "Two overlapping Heat Zones must pay for the burner once, paid " + powered);
                helper.succeed();
                return;
            }
            if (mode == BurnerCase.SMALL) {
                EntityHeatZone zone = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                        .filter(object -> object instanceof EntityHeatZone).map(object -> (EntityHeatZone) object).findFirst().orElse(null);
                float zonePower = zone == null ? -1.0F : zone.getPower();
                double maxTemperature = zone == null ? -1.0 : zone.maxTemperature();
                cleanup.run();
                helper.assertTrue("kindled".equals(insideLevel), "A size-3, Power 100 Heat Zone must kindle the burner, was " + insideLevel);
                helper.assertTrue(zonePower == 14.0F && maxTemperature == 1150.0,
                        "Effects must stop growing at Power 10: power " + zonePower + ", max temperature " + maxTemperature);
                helper.succeed();
                return;
            }
            if (mode == BurnerCase.REVERT) {
                helper.assertTrue("kindled".equals(insideLevel), "The Blaze Burner must be kindled first, was " + insideLevel);
                SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(zone -> zone.discard());
                helper.runAfterDelay(8, () -> {
                    String after = propertyValue(helper.getBlockState(inside), "blaze");
                    cleanup.run();
                    helper.assertTrue("smouldering".equals(after),
                            "The Blaze Burner must go back once the zone ends, was " + after);
                    helper.succeed();
                });
                return;
            }
            cleanup.run();
            String expected = switch (mode) {
                case KINDLE, SPLIT -> "kindled";
                default -> "smouldering";
            };
            helper.assertTrue(expected.equals(insideLevel),
                    "The Blaze Burner inside must be " + expected + " (" + mode + "), was " + insideLevel);
            helper.assertTrue("smouldering".equals(outsideLevel),
                    "The Blaze Burner outside must stay smouldering, was " + outsideLevel);
            helper.succeed();
        });
    }

    private static net.minecraft.world.level.block.state.BlockState withProperty(
            net.minecraft.world.level.block.state.BlockState state, String name, String value) {
        var property = state.getBlock().getStateDefinition().getProperty(name);
        return property == null ? state : withValue(state, property, value);
    }

    private static <T extends Comparable<T>> net.minecraft.world.level.block.state.BlockState withValue(
            net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.level.block.state.properties.Property<T> property, String value) {
        return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
    }

    private static String propertyValue(net.minecraft.world.level.block.state.BlockState state, String name) {
        var property = state.getBlock().getStateDefinition().getProperty(name);
        return property == null ? "(none)" : valueName(state, property);
    }

    private static <T extends Comparable<T>> String valueName(net.minecraft.world.level.block.state.BlockState state,
                                                              net.minecraft.world.level.block.state.properties.Property<T> property) {
        return property.getName(state.getValue(property));
    }

    /**
     * Sable があるとき、加重領域が機体を押すためのフックが今の Sable の版で揃い、機体のない場所では何も押さない。
     * Sable がなければ飛ばす（機体の動きそのものはゲーム内で確かめる）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_gravity_sable", timeoutTicks = 40)
    public static void gravityZoneSableLinkResolves(GameTestHelper helper) {
        if (!com.moratan251.psitweaks.common.compat.SablePhysicsCompat.isLoaded()) {
            helper.succeed();
            return;
        }
        AABB box = new AABB(helper.absolutePos(new BlockPos(0, 0, 0))).inflate(2.0);
        int pushed = com.moratan251.psitweaks.common.compat.SablePhysicsCompat.accelerateSubLevels(helper.getLevel(), box,
                new Vec3(0.0, 1.0, 0.0), 1.0, pos -> true);
        helper.assertTrue(com.moratan251.psitweaks.common.compat.SablePhysicsCompat.zoneLinkAvailable(),
                "The Gravity Zone's Sable hooks must resolve on this Sable version");
        helper.assertTrue(pushed == 0, "Nothing may be pushed where there is no contraption, pushed " + pushed);
        helper.succeed();
    }

    /**
     * 他人どうしの奪い合い: 種類を問わず、重なったところでは事象干渉力の高い側の領域だけが効く。
     * 他人の加熱と凍結は打ち消し合わず、奪い合いで決まる。重ならないところはそれぞれ効く。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contest", timeoutTicks = 40)
    public static void othersZonesContestByInterference(GameTestHelper helper) {
        ServerPlayer low = GameTestPlayers.inLevel(helper, "zone_contest_low", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer high = GameTestPlayers.inLevel(helper, "zone_contest_high", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            cleanup(helper, low);
            cleanup(helper, high);
        };
        try {
            low.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            high.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(3.0D);
            // 低い側の湿潤（x 0〜4）を先に置き、高い側の加熱（x 4〜6）を重ねる。重なりは x 4。
            cast(low, soak(helper, CENTER), 5.0, 0.0);
            cast(high, heat(helper, new BlockPos(5, 1, 2)), 3.0, 5.0);
            SpellGramZone soak = only(helper, low, EntitySoakZone.class);
            SpellGramZone heat = only(helper, high, EntityHeatZone.class);
            BlockPos overlap = helper.absolutePos(new BlockPos(4, 1, 2));
            helper.assertTrue(heat.isInsideAndActive(overlap) && !soak.isInsideAndActive(overlap),
                    "Where others' zones overlap, only the higher interference side may work, whatever the kinds");
            helper.assertTrue(soak.isInsideAndActive(helper.absolutePos(new BlockPos(1, 1, 2)))
                            && heat.isInsideAndActive(helper.absolutePos(new BlockPos(6, 1, 2))),
                    "Outside the overlap, both zones still work");

            // 低い側の凍結を高い側の加熱に重ねても打ち消し合わず、高い側の加熱が効く。
            cast(low, freeze(helper, new BlockPos(5, 1, 2)), 3.0, 5.0);
            SpellGramZone freeze = only(helper, low, EntityFreezeZone.class);
            BlockPos inHeat = helper.absolutePos(new BlockPos(5, 1, 2));
            helper.assertTrue(heat.isInsideAndActive(inHeat) && !freeze.isInsideAndActive(inHeat),
                    "Others' heat and freeze must not cancel out; the higher interference must win");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** 他人どうしで事象干渉力が同じなら、先に置いた側の領域が重なったところで効く。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contest_equal", timeoutTicks = 40)
    public static void equalInterferenceKeepsTheEarlierZone(GameTestHelper helper) {
        ServerPlayer first = GameTestPlayers.inLevel(helper, "zone_contest_first", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer second = GameTestPlayers.inLevel(helper, "zone_contest_second", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            cleanup(helper, first);
            cleanup(helper, second);
        };
        try {
            first.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            second.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            cast(first, soak(helper, CENTER), 5.0, 0.0);
            cast(second, heat(helper, new BlockPos(5, 1, 2)), 3.0, 10.0);
            SpellGramZone earlier = only(helper, first, EntitySoakZone.class);
            SpellGramZone later = only(helper, second, EntityHeatZone.class);
            BlockPos overlap = helper.absolutePos(new BlockPos(4, 1, 2));
            helper.assertTrue(earlier.isInsideAndActive(overlap) && !later.isInsideAndActive(overlap),
                    "With equal interference, the zone placed first must keep the overlap, however strong the later one is");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** 領域干渉の範囲では、術者以外のエンダー系のテレポートが、中から外へも外から中へも止まる。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_interference_teleport", timeoutTicks = 40)
    public static void interferenceZoneStopsEnderTeleportsOfOthers(GameTestHelper helper) {
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "zone_teleport_owner", new Vec3(2.5, 1.0, 2.5));
        ServerPlayer stranger = GameTestPlayers.inLevel(helper, "zone_teleport_stranger", new Vec3(2.5, 1.0, 1.5));
        Runnable cleanup = () -> {
            cleanup(helper, owner);
            cleanup(helper, stranger);
        };
        try {
            cast(owner, interference(helper, CENTER), 5.0, 0.0);
            var level = helper.getLevel();
            Vec3 inside = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
            Vec3 outside = helper.absoluteVec(new Vec3(12.5, 1.0, 2.5));
            Vec3 farther = helper.absoluteVec(new Vec3(14.5, 1.0, 2.5));
            var enderman = helper.spawnWithNoFreeWill(EntityType.ENDERMAN, new Vec3(2.5, 1.0, 3.5));
            helper.assertTrue(EntityInterferenceZone.blocksTeleport(level, enderman, inside, outside),
                    "An Enderman must not teleport out of the zone");
            helper.assertTrue(EntityInterferenceZone.blocksTeleport(level, enderman, outside, inside),
                    "An Enderman must not teleport into the zone");
            helper.assertFalse(EntityInterferenceZone.blocksTeleport(level, enderman, outside, farther),
                    "Teleports that never touch the zone must work");
            helper.assertTrue(EntityInterferenceZone.blocksTeleport(level, stranger, inside, outside),
                    "Another player's Ender Pearl must not leave the zone");
            helper.assertFalse(EntityInterferenceZone.blocksTeleport(level, owner, inside, outside),
                    "The caster's own teleports must work");

            var event = new net.neoforged.neoforge.event.entity.EntityTeleportEvent.EnderEntity(enderman, outside.x, outside.y, outside.z);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
            helper.assertTrue(event.isCanceled(), "The Enderman's teleport event must be cancelled");
            enderman.discard();
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /**
     * 領域干渉の規模とコストは遮音領域と同じ: 規模 = 10 + 広さ、コスト = 40 + 広さ（広さ = サイズ²）。威力の入力はない。
     * サイズ 5（広さ 25）で 35・65。
     */
    @GameTest(template = TEMPLATE)
    public static void interferenceZoneCostMatchesSilenceZone(GameTestHelper helper) throws Exception {
        PieceTrickZoneBase<?> interferencePiece = new PieceTrickInterferenceZone(new Spell()) {
            @Override
            public <T> T getParamEvaluation(SpellParam<?> param) {
                return sizeFive(param);
            }
        };
        PieceTrickZoneBase<?> silencePiece = new com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSilenceZone(new Spell()) {
            @Override
            public <T> T getParamEvaluation(SpellParam<?> param) {
                return sizeFive(param);
            }
        };
        helper.assertFalse(interferencePiece.params.containsKey(SpellParam.GENERIC_NAME_POWER),
                "The Interference Zone must not take a Power");
        int[] interference = zoneStats(interferencePiece);
        int[] silence = zoneStats(silencePiece);
        helper.assertTrue(interference[0] == 35 && interference[1] == 65,
                "At Size 5: Potency 35, Cost 65, got " + interference[0] + ", " + interference[1]);
        helper.assertTrue(interference[0] == silence[0] && interference[1] == silence[1],
                "The Interference Zone must cost the same as the Silence Zone, got " + silence[0] + ", " + silence[1]);
        helper.succeed();
    }

    /**
     * 総量を持つ領域（加熱・凍結・乱流・荷電）の威力の尺度: 入れた威力は 1 以上（0.5 は 1）で、コスト・強さは中の威力
     * （入れた威力 + 4）、規模は入れた威力で決まる。サイズ 16・30 秒で、威力 1・5・10 のコストは 562・971・1483、規模は 79・355・701。
     * 10 を超えた分はコストと総量だけを増やし、強さと規模は 10 と同じ（威力 11 は 1586）。乱流の組み合わせの強さはサイズ 16 なら
     * 威力 31（コスト 3634）で上限 40 に届く。
     * 加熱の上限の温度は 750 K から 1 段 50 K ずつ上がり、威力 9 から 1150 K で止まる。乱流のそらす角度は 45 度から 1 段 5 度ずつ
     * 上がり、威力 10 で 90 度。湿潤領域は 40 + 広さ × 2。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_power_scale", timeoutTicks = 40)
    public static void resourceZonePowerScale(GameTestHelper helper) throws Exception {
        java.util.Map<String, java.util.function.ToIntBiFunction<Integer, Double>> costs = new java.util.LinkedHashMap<>();
        costs.put("Heat", PieceTrickHeatZone::costFor);
        costs.put("Freeze", PieceTrickFreezeZone::costFor);
        costs.put("Turbulence", PieceTrickTurbulenceZone::costFor);
        costs.put("Charged", com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickChargedZone::costFor);
        for (var entry : costs.entrySet()) {
            var costFor = entry.getValue();
            int[] size16 = {costFor.applyAsInt(256, 1.0), costFor.applyAsInt(256, 5.0), costFor.applyAsInt(256, 10.0)};
            helper.assertTrue(size16[0] == 562 && size16[1] == 971 && size16[2] == 1483,
                    entry.getKey() + " at Size 16 must cost 562 / 971 / 1483 at Power 1 / 5 / 10, got " + java.util.Arrays.toString(size16));
            helper.assertTrue(costFor.applyAsInt(16, 1.0) == 82 && costFor.applyAsInt(64, 1.0) == 178,
                    entry.getKey() + " at Power 1 must cost 82 at Size 4 and 178 at Size 8");
        }

        int[] one = resourceStats(16, 1.0);
        int[] five = resourceStats(16, 5.0);
        int[] ten = resourceStats(16, 10.0);
        helper.assertTrue(one[0] == 79 && five[0] == 355 && ten[0] == 701,
                "Potency must follow the Power itself: " + one[0] + " / " + five[0] + " / " + ten[0]);
        helper.assertTrue(one[1] == 562 && five[1] == 971 && ten[1] == 1483,
                "The compiled Cost must follow the Power plus 4: " + one[1] + " / " + five[1] + " / " + ten[1]);
        helper.assertTrue(java.util.Arrays.equals(resourceStats(16, 0.5), one), "A Power below 1 must count as 1");
        int[] eleven = resourceStats(16, 11.0);
        helper.assertTrue(eleven[0] == 701 && eleven[1] == 1586,
                "Above 10 only the Cost must grow: Potency 701, Cost 1586 at Power 11, got " + eleven[0] + ", " + eleven[1]);
        float strongest = EntityTurbulenceZone.attackPower(PieceTrickTurbulenceZone.costFor(256, 31.0), 600);
        float below = EntityTurbulenceZone.attackPower(PieceTrickTurbulenceZone.costFor(256, 30.0), 600);
        helper.assertTrue(PieceTrickTurbulenceZone.costFor(256, 31.0) == 3634 && strongest == EntityTurbulenceZone.MAX_ATTACK_POWER
                        && below < EntityTurbulenceZone.MAX_ATTACK_POWER,
                "A Size 16 Turbulence Zone must reach attack power 40 at Power 31, got " + below + " at 30 and " + strongest + " at 31");

        double[] temperatures = {
                EntityHeatZone.maxTemperatureFor(PieceTrickZoneBase.resourcePower(1.0)),
                EntityHeatZone.maxTemperatureFor(PieceTrickZoneBase.resourcePower(8.0)),
                EntityHeatZone.maxTemperatureFor(PieceTrickZoneBase.resourcePower(9.0)),
                EntityHeatZone.maxTemperatureFor(PieceTrickZoneBase.resourcePower(10.0))};
        helper.assertTrue(temperatures[0] == 750.0 && temperatures[1] == 1100.0 && temperatures[2] == 1150.0 && temperatures[3] == 1150.0,
                "The Heat Zone must heat up to 750 / 1100 / 1150 / 1150 K at Power 1 / 8 / 9 / 10, got "
                        + java.util.Arrays.toString(temperatures));
        double[] angles = {
                EntityTurbulenceZone.deflectDegreesFor(PieceTrickZoneBase.resourcePower(1.0)),
                EntityTurbulenceZone.deflectDegreesFor(PieceTrickZoneBase.resourcePower(5.0)),
                EntityTurbulenceZone.deflectDegreesFor(PieceTrickZoneBase.resourcePower(10.0))};
        helper.assertTrue(angles[0] == 45.0 && angles[1] == 65.0 && angles[2] == 90.0,
                "The Turbulence Zone must turn projectiles up to 45 / 65 / 90 degrees at Power 1 / 5 / 10, got "
                        + java.util.Arrays.toString(angles));
        double coldest = EntityFreezeZone.minTemperatureFor(PieceTrickZoneBase.resourcePower(10.0));
        helper.assertTrue(coldest == 205.0, "The Freeze Zone at Power 10 must cool down to 205 K, got " + coldest);

        PieceTrickZoneBase<?> soakPiece = new PieceTrickSoakZone(new Spell()) {
            @Override
            public <T> T getParamEvaluation(SpellParam<?> param) {
                return sizeFive(param);
            }
        };
        int[] soak = zoneStats(soakPiece);
        helper.assertTrue(soak[0] == 35 && soak[1] == 90, "The Soak Zone at Size 5: Potency 35, Cost 90, got " + soak[0] + ", " + soak[1]);

        String label = new PieceTrickHeatZone(new Spell()).getStatLabels().get(vazkii.psi.api.spell.EnumSpellStat.COST).toString();
        helper.assertTrue(label.contains("+ 4"), "The shown Cost must add 4 to the Power, got " + label);

        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_power_scale_caster", new Vec3(0.5, 1.0, 0.5));
        try {
            cast(caster, heat(helper, CENTER), 4.0, 1.0);
            EntityHeatZone low = only(helper, caster, EntityHeatZone.class);
            helper.assertTrue(low.getPlacementCost() == 82.0F && low.getPower() == 5.0F && low.burnSeconds() == 3.0F,
                    "A Power 1, Size 4 Heat Zone must be placed for 82 with the strength of 5, got " + low.getPlacementCost()
                            + ", " + low.getPower() + ", " + low.burnSeconds() + " s");
            low.discard();
            cast(caster, heat(helper, CENTER), 4.0, 11.0);
            EntityHeatZone high = only(helper, caster, EntityHeatZone.class);
            helper.assertTrue(high.getPower() == 14.0F && Math.abs(high.burnSeconds() - 8.4F) < 1.0E-4F && high.maxTemperature() == 1150.0,
                    "A Power 11 Heat Zone must be as strong as Power 10: " + high.getPower() + ", " + high.burnSeconds() + " s, "
                            + high.maxTemperature() + " K");
            helper.assertTrue(high.getPlacementCost() == 146.0F,
                    "A Power 11, Size 4 Heat Zone must still cost more than Power 10, got " + high.getPlacementCost());
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup(helper, caster);
            throw new RuntimeException(e);
        }
        cleanup(helper, caster);
        helper.succeed();
    }

    /** サイズ {@code size}・威力 {@code power}（時間は省く）の加熱領域の {規模, コスト}。 */
    private static int[] resourceStats(int size, double power) throws Exception {
        PieceTrickZoneBase<?> piece = new PieceTrickHeatZone(new Spell()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getParamEvaluation(SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return (T) Double.valueOf(size);
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? (T) Double.valueOf(power) : null;
            }
        };
        return zoneStats(piece);
    }

    /**
     * 「威力」の入力に定数をつないだ向きが NBT に残っている領域干渉の術式も、読み込めて組める。Psi は今の入力の名前だけを
     * NBT から読むので、なくなった入力の向きは読み飛ばされ、つないでいた定数はどこにも使われない。
     */
    @GameTest(template = TEMPLATE)
    public static void interferenceZoneSpellsWithAPowerSideStillCompile(GameTestHelper helper) {
        Spell spell = new Spell();
        spell.name = "interference_zone_power_side";
        SpellPiece zone = vazkii.psi.api.PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location("trick_interference_zone")).create(spell);
        zone.paramSides.put(zone.params.get(PsitweaksSpellParams.SIZE), SpellParam.Side.RIGHT);
        placeOnGrid(spell, zone, 4, 4);
        placeOnGrid(spell, constantNumber(spell, "3"), 5, 4);
        placeOnGrid(spell, constantNumber(spell, "10"), 4, 3);

        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        spell.writeToNBT(tag);
        // Psi の SpellPiece#writeToNBT と同じ書き方の入力名で、上の定数を指す「威力」の向きを書き足す。
        String sizeKey = PsitweaksSpellParams.SIZE.replaceAll("^psi.spellparam.", "_");
        String powerKey = SpellParam.GENERIC_NAME_POWER.replaceAll("^psi.spellparam.", "_");
        int added = addParamSide(tag, sizeKey, powerKey, SpellParam.Side.TOP.asInt());
        helper.assertTrue(added == 1, "Expected one Interference Zone in the saved spell, found " + added);

        Spell loaded = Spell.createFromNBT(tag);
        helper.assertTrue(loaded != null && loaded.grid.gridData[4][4] instanceof PieceTrickInterferenceZone,
                "The saved spell must load with its Interference Zone");
        SpellPiece loadedZone = loaded.grid.gridData[4][4];
        helper.assertTrue(loadedZone.paramSides.get(loadedZone.params.get(PsitweaksSpellParams.SIZE)) == SpellParam.Side.RIGHT,
                "The Size side must survive the reload");
        var compiled = new vazkii.psi.common.spell.SpellCompiler().compile(loaded, helper.getLevel().registryAccess(), null);
        helper.assertTrue(compiled.left().isPresent(),
                "A spell with a Power side left over must still compile: " + compiled.right().map(Throwable::getMessage).orElse(""));
        var meta = compiled.left().orElseThrow().metadata;
        int potency = meta.getStat(vazkii.psi.api.spell.EnumSpellStat.POTENCY);
        int cost = meta.getStat(vazkii.psi.api.spell.EnumSpellStat.COST);
        helper.assertTrue(potency == 19 && cost == 49, "At Size 3: Potency 19, Cost 49, got " + potency + ", " + cost);
        helper.succeed();
    }

    private static SpellPiece constantNumber(Spell spell, String value) {
        var piece = (vazkii.psi.common.spell.constant.PieceConstantNumber) vazkii.psi.api.PsiAPI.SPELL_PIECE_REGISTRY
                .get(vazkii.psi.api.PsiAPI.location("constant_number")).create(spell);
        piece.valueStr = value;
        return piece;
    }

    private static void placeOnGrid(Spell spell, SpellPiece piece, int x, int y) {
        piece.x = x;
        piece.y = y;
        piece.isInGrid = true;
        spell.grid.gridData[x][y] = piece;
    }

    /** {@code tag} の中で、入力 {@code anchorKey} の向きを持つピースに、入力 {@code key} の向き {@code side} を足す。足した数を返す。 */
    private static int addParamSide(net.minecraft.nbt.Tag tag, String anchorKey, String key, int side) {
        int added = 0;
        if (tag instanceof net.minecraft.nbt.CompoundTag compound) {
            if (compound.getCompound("params").contains(anchorKey)) {
                compound.getCompound("params").putInt(key, side);
                added++;
            }
            for (String child : compound.getAllKeys()) {
                added += addParamSide(compound.get(child), anchorKey, key, side);
            }
        } else if (tag instanceof net.minecraft.nbt.ListTag list) {
            for (net.minecraft.nbt.Tag element : list) {
                added += addParamSide(element, anchorKey, key, side);
            }
        }
        return added;
    }

    /** 入力の評価: サイズは 5、ほか（時間など）は省いたもの。 */
    @SuppressWarnings("unchecked")
    private static <T> T sizeFive(SpellParam<?> param) {
        return param.name.equals(PsitweaksSpellParams.SIZE) ? (T) Double.valueOf(5.0) : null;
    }

    /** 領域 {@code piece} の {規模, コスト}。 */
    private static int[] zoneStats(PieceTrickZoneBase<?> piece) throws Exception {
        var meta = new vazkii.psi.api.spell.SpellMetadata();
        piece.addToMetadata(meta);
        return new int[]{meta.getStat(vazkii.psi.api.spell.EnumSpellStat.POTENCY), meta.getStat(vazkii.psi.api.spell.EnumSpellStat.COST)};
    }

    /** {@code caster} が置いた {@code type} の領域（1 つだけのはず）。 */
    private static <T extends SpellGramZone> T only(GameTestHelper helper, ServerPlayer caster, Class<T> type) {
        return SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).stream()
                .filter(type::isInstance).map(type::cast).findFirst().orElseThrow();
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

    private static PieceTrickZoneBase<?> charged(GameTestHelper helper, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        return new com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickChargedZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }

    private static PieceTrickZoneBase<?> freeze(GameTestHelper helper, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        return new PieceTrickFreezeZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }

    private static PieceTrickZoneBase<?> interference(GameTestHelper helper, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        return new PieceTrickInterferenceZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }

    private static PieceTrickZoneBase<?> soak(GameTestHelper helper, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        return new PieceTrickSoakZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }

    private static Vector3 vector(Vec3 at) {
        return new Vector3(at.x, at.y, at.z);
    }

    private static PieceTrickZoneBase<?> heat(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(CENTER);
        return new PieceTrickHeatZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }

    private static PieceTrickZoneBase<?> freeze(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(CENTER);
        return new PieceTrickFreezeZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }

    private static PieceTrickZoneBase<?> turbulence(GameTestHelper helper, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        return new PieceTrickTurbulenceZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }

    private static PieceTrickZoneBase<?> turbulence(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(CENTER);
        return new PieceTrickTurbulenceZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }

    /** 置く前に {@link #cast} が決める入力: 行列（中心のまわりの 5×5×5）、サイズ、威力。 */
    private static final ThreadLocal<double[]> SIZE_AND_POWER = ThreadLocal.withInitial(() -> new double[]{5.0, 0.0});

    private static Object paramValue(SpellParam<?> param, BlockPos center) {
        double[] sizeAndPower = SIZE_AND_POWER.get();
        if (param.name.equals(PsitweaksSpellParams.MATRIX)) {
            double edge = sizeAndPower[0];
            double low = -Math.floor((edge - 1) / 2.0) - 0.5;
            return new MatrixValue(4, 4, new double[]{
                    edge, 0, 0, center.getX() + low,
                    0, edge, 0, center.getY() + low,
                    0, 0, edge, center.getZ() + low,
                    0, 0, 0, 1});
        }
        if (param.name.equals(PsitweaksSpellParams.SIZE)) {
            return sizeAndPower[0];
        }
        return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? sizeAndPower[1] : null;
    }

    private static void cast(ServerPlayer caster, PieceTrickZoneBase<?> piece, double size, double power)
            throws SpellRuntimeException {
        SIZE_AND_POWER.set(new double[]{size, power});
        try {
            piece.execute(new SpellContext().setPlayer(caster));
        } finally {
            SIZE_AND_POWER.remove();
        }
    }

    /** {@code pos} に {@code block} を置き、下と横を石で囲む（水が流れ出さないように）。 */
    private static void basin(GameTestHelper helper, BlockPos pos, Block block) {
        helper.setBlock(pos.below(), Blocks.STONE);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            helper.setBlock(pos.relative(side), Blocks.STONE);
        }
        helper.setBlock(pos, block);
    }

    private static void cleanup(GameTestHelper helper, ServerPlayer caster) {
        SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).forEach(zone -> zone.discard());
        GameTestPlayers.remove(helper, caster);
    }

    private static Optional<Block> block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id));
    }
}
