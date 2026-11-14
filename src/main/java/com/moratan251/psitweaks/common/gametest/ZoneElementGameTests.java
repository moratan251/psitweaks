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
import com.moratan251.psitweaks.common.entities.SpellGram.zone.InfernoZones;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import com.moratan251.psitweaks.common.handler.PsitweaksAdvancements;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
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

    /** 加熱領域: 中の生物は燃え、術者は燃えない。氷と薄氷は水に戻り、氷塊はそのまま。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat", timeoutTicks = 100)
    public static void heatZoneBurnsBeingsAndMeltsIce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_caster", new Vec3(4.5, 1.0, 4.5));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 3.5));
        basin(helper, OPEN, Blocks.ICE);
        basin(helper, COVERED, Blocks.FROSTED_ICE);
        basin(helper, THIRD, Blocks.PACKED_ICE);
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
            boolean iceMelted = helper.getBlockState(OPEN).is(Blocks.WATER);
            boolean frostedMelted = helper.getBlockState(COVERED).is(Blocks.WATER);
            boolean packedKept = helper.getBlockState(THIRD).is(Blocks.PACKED_ICE);
            cleanup.run();
            helper.assertTrue(huskBurning, "The husk inside the Heat Zone must be set on fire");
            helper.assertFalse(casterBurning, "The Heat Zone must not burn its own caster");
            helper.assertTrue(iceMelted, "The Heat Zone must melt ice into water");
            helper.assertTrue(frostedMelted, "The Heat Zone must melt frosted ice into water");
            helper.assertTrue(packedKept, "Packed ice must stay");
            helper.succeed();
        });
    }

    /**
     * 加熱領域の中のダメージは、威力によらずバニラの炎上と同じ毎秒 1（2026-11-14 ユーザー: 前は燃える残り時間の数え方とかみ合わず、
     * 威力 1・3・6・8 では 0、ほかは毎秒 2 だった）。外に出たあとは 威力 × 0.6 秒ほど燃え続ける。
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

    /** Create があるとき、加熱領域は中の落ちているアイテムを一括燻製する（生の牛肉 → ステーキ）。Create がなければ飛ばす。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_create", timeoutTicks = 400)
    public static void heatZoneSmokesItemsWithCreate(GameTestHelper helper) {
        if (!CreateCompat.isCreateLoaded()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_heat_create", new Vec3(4.5, 1.0, 4.5));
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
        Vec3 itemAt = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
        ItemEntity item = new ItemEntity(level, itemAt.x, itemAt.y, itemAt.z, new ItemStack(Items.BEEF));
        item.setNeverPickUp();
        item.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(item);
        AABB around = new AABB(helper.absolutePos(CENTER)).inflate(4.0);
        Runnable cleanup = () -> {
            level.getEntitiesOfClass(ItemEntity.class, around).forEach(ItemEntity::discard);
            cleanup(helper, caster);
        };
        try {
            // 事象干渉力 4 → 速さは上限の 2 倍（Create の処理時間 150 tick を 75 tick ほどで）。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, heat(helper), 5.0, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(110, () -> {
            boolean smoked = !level.getEntitiesOfClass(ItemEntity.class, around,
                    entity -> entity.getItem().is(Items.COOKED_BEEF)).isEmpty();
            cleanup.run();
            helper.assertTrue(smoked, "The Heat Zone must smoke raw beef into steak with Create");
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

    /** 凍結領域: 中の生物は凍え、術者は凍えない。真上が空気の水源の水は氷になり、上が塞がれた水はそのまま。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_freeze", timeoutTicks = 100)
    public static void freezeZoneFreezesBeingsAndOpenWater(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_freeze_caster", new Vec3(4.5, 1.0, 4.5));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 3.5));
        basin(helper, OPEN, Blocks.WATER);
        basin(helper, COVERED, Blocks.WATER);
        helper.setBlock(COVERED.above(), Blocks.STONE);
        Runnable cleanup = () -> {
            helper.setBlock(COVERED.above(), Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            cast(caster, freeze(helper), 5.0, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(25, () -> {
            int huskFrozen = husk.getTicksFrozen();
            int casterFrozen = caster.getTicksFrozen();
            boolean openFrozen = helper.getBlockState(OPEN).is(Blocks.ICE);
            boolean coveredKept = helper.getBlockState(COVERED).is(Blocks.WATER);
            cleanup.run();
            helper.assertTrue(huskFrozen > 0, "The husk inside the Freeze Zone must freeze, frozen ticks " + huskFrozen);
            helper.assertTrue(casterFrozen == 0, "The Freeze Zone must not freeze its own caster, frozen ticks " + casterFrozen);
            helper.assertTrue(openFrozen, "Water with air above must turn into ice");
            helper.assertTrue(coveredKept, "Water with a block above must stay water");
            helper.succeed();
        });
    }

    /** 凍結領域に湿潤領域を重ねると、濡れた生物は凍えきるまでの時間が半分になる（凍えの値が倍の速さでたまる）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_freeze_soak", timeoutTicks = 100)
    public static void soakedBeingsFreezeTwiceAsFast(GameTestHelper helper) {
        // 凍えの差し引きは 凍結の威力 × 4（威力 5 で 20、10 で 40）、濡れていれば 2 倍、威力 10 の吹雪で 2 倍。
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
            // 凍結領域は x 0〜4、湿潤領域は x 0〜2 だけ（濡れるのは片方の生物）。
            cast(caster, soak(helper, new BlockPos(1, 1, 2)), 3.0, 0.0);
            cast(caster, freeze(helper, CENTER), 5.0, 5.0);
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
     * Mekanism Generators があるとき、乱流領域の中の風力発電機は屋根の下でも発電し、威力 10 なら空の下の 2 倍になる。
     * 外の風力発電機は屋根の下で止まったまま。Mekanism Generators がなければ飛ばす。
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
        // 風力発電機は 4 ブロック上（y 5、発電機のてっぺん）から空が見えるかを見る。中と外の 2 台の上（x 0〜7）に、すぐ上の y 6 で
        // 屋根をかけ、3 台目は空の下に置く。乱流領域は x 0〜4 なので、外の 1 台（x 6）は入らない。
        for (int x = 0; x <= 7; x++) {
            for (int z = 0; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 6, z), Blocks.STONE);
            }
        }
        helper.setBlock(inside, windGenerator.get());
        helper.setBlock(outside, windGenerator.get());
        helper.setBlock(openSky, windGenerator.get());
        Runnable cleanup = () -> {
            helper.setBlock(inside, Blocks.AIR);
            helper.setBlock(outside, Blocks.AIR);
            helper.setBlock(openSky, Blocks.AIR);
            for (int x = 0; x <= 7; x++) {
                for (int z = 0; z <= 4; z++) {
                    helper.setBlock(new BlockPos(x, 6, z), Blocks.AIR);
                }
            }
            cleanup(helper, caster);
        };
        try {
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
            cleanup.run();
            helper.assertTrue(outsideMultiplier == 0.0,
                    "A Wind Generator outside the zone must stay still under a roof, multiplier " + outsideMultiplier);
            helper.assertTrue(skyMultiplier > 0.0, "A Wind Generator under the open sky must run, multiplier " + skyMultiplier);
            // 2026-11-14 ユーザー: 上乗せは床の広さ ÷ 64 台ぶんまで。一辺 5（25 ブロック）なら 1 台に 25/64 ぶん。
            double expected = skyMultiplier * (1.0 + (EntityTurbulenceZone.windBoostFor(10.0) - 1.0) * (25.0 / 64.0));
            helper.assertTrue(Math.abs(insideMultiplier - expected) < 1.0E-6,
                    "A Wind Generator in a size-5, Power 10 Turbulence Zone must get 25/64 of the doubling, "
                            + insideMultiplier + " vs " + expected);
            helper.succeed();
        });
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
                // 普通の火は 1 秒に 1。熱乱流は威力 10 で 0.5 秒ごとに 3（当たった直後の火のダメージは無敵時間で消える）。
                helper.assertTrue(stormedLoss >= 6.0F && stormedLoss > plainLoss,
                        "The Heat Storm must hurt more than plain fire: storm " + stormedLoss + ", plain " + plainLoss);
            } else {
                // 凍結領域だけでは、凍えきるまで（数秒）ダメージを与えない。
                // 威力 10 の吹雪は 0.5 秒ごとに 3（弱い乱流が重なっても、最も強い乱流で決まる）。
                helper.assertTrue(stormedLoss >= 6.0F, "The Blizzard must hurt the being inside, lost " + stormedLoss);
                helper.assertTrue(plainLoss == 0.0F, "Someone else's turbulence must not make a Blizzard, lost " + plainLoss);
            }
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
    private static void castTurbulenceOver(ServerPlayer caster, BlockPos from, int xs, int ys, int zs)
            throws SpellRuntimeException {
        castTurbulenceOver(caster, from, xs, ys, zs, 10.0);
    }

    /** {@code caster} が、ブロック {@code from}（角）から x×y×z ブロックの、威力 {@code power} の乱流領域を置く。 */
    private static void castTurbulenceOver(ServerPlayer caster, BlockPos from, int xs, int ys, int zs, double power)
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
                    return 32.0;
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
            cast(owner, interference(helper, CENTER), 5.0, 1.0);
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
            cast(owner, interference(helper, new BlockPos(1, 1, 2)), 3.0, 1.0);
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
     * 隠し要素: 氷炎地獄。同じ術者の加熱領域と凍結領域が同じ大きさで 1 つの直方体を二分すると、両方の中の生物にダメージ
     * （凍結側は凍えきる前から）。初めて起こした術者に隠し進捗。重なり・隙間・大きさ違いでは起きない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_inferno", timeoutTicks = 100)
    public static void heatAndFreezeSplittingOneBoxStartTheInferno(GameTestHelper helper) throws SpellRuntimeException {
        ServerLevel level = helper.getLevel();
        BlockPos hot = helper.absolutePos(new BlockPos(1, 1, 2));
        BlockPos cold = helper.absolutePos(new BlockPos(4, 1, 2));
        helper.assertTrue(InfernoZones.splitsOneBox(ZoneArea.cube(hot, 3), ZoneArea.cube(cold, 3)),
                "Two 3-block cubes side by side split one box");
        helper.assertFalse(InfernoZones.splitsOneBox(ZoneArea.cube(hot, 3), ZoneArea.cube(cold.east(), 3)),
                "A gap between the zones must not count");
        helper.assertFalse(InfernoZones.splitsOneBox(ZoneArea.cube(hot, 3), ZoneArea.cube(cold.west(), 3)),
                "Overlapping zones must not count");
        helper.assertFalse(InfernoZones.splitsOneBox(ZoneArea.cube(hot, 3), ZoneArea.cube(cold.east(), 5)),
                "Zones of different sizes must not count");
        helper.assertFalse(InfernoZones.splitsOneBox(ZoneArea.cube(hot, 3), ZoneArea.cube(cold.north(), 3)),
                "Zones sliding past each other must not count");
        // 面で触れ合うかの判定（相殺・接触の演出用）: 大きさ違いでも、ずれていても、面で触れていれば触れ合う。
        helper.assertTrue(InfernoZones.contactFace(ZoneArea.cube(hot, 3).bounds(), ZoneArea.cube(cold, 3).bounds()) != null,
                "Two halves of one box touch");
        helper.assertTrue(InfernoZones.contactFace(ZoneArea.cube(hot, 3).bounds(), ZoneArea.cube(cold.east(), 5).bounds()) != null,
                "Zones of different sizes side by side touch");
        helper.assertTrue(InfernoZones.contactFace(ZoneArea.cube(hot, 3).bounds(), ZoneArea.cube(cold.east(2), 3).bounds()) == null,
                "Zones with a gap do not touch");
        helper.assertTrue(InfernoZones.contactFace(ZoneArea.cube(hot, 3).bounds(), ZoneArea.cube(cold.west(), 3).bounds()) == null,
                "Overlapping zones do not merely touch");

        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_inferno_caster", new Vec3(8.5, 1.0, 2.5));
        Husk scorched = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 1.0, 2.5)), 100.0F);
        Husk frozen = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(4.5, 1.0, 2.5)), 100.0F);
        float scorchedBefore = scorched.getHealth();
        float frozenBefore = frozen.getHealth();
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(25, () -> {
            boolean heatInferno = SpellGramIndex.all(level, EntityHeatZone.class).stream().anyMatch(EntityHeatZone::isInferno);
            boolean coldInferno = SpellGramIndex.all(level, EntityFreezeZone.class).stream().anyMatch(EntityFreezeZone::isInferno);
            float scorchedLoss = scorchedBefore - scorched.getHealth();
            float frozenLoss = frozenBefore - frozen.getHealth();
            var advancement = level.getServer().getAdvancements().get(PsitweaksAdvancements.INFERNO);
            boolean awarded = advancement != null && caster.getAdvancements().getOrStartProgress(advancement).isDone();
            EntityHeatZone hotZone = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                    .filter(zone -> zone instanceof EntityHeatZone).map(zone -> (EntityHeatZone) zone).findFirst().orElse(null);
            EntityFreezeZone coldZone = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                    .filter(zone -> zone instanceof EntityFreezeZone).map(zone -> (EntityFreezeZone) zone).findFirst().orElse(null);
            double pooled = hotZone == null || coldZone == null ? -1.0 : hotZone.heatTotalPerTick(true);
            double expected = hotZone == null || coldZone == null ? 0.0
                    : EntityHeatZone.heatTotalPerTick(hotZone.getPlacementCost(), hotZone.getLifetimeTicks(), hotZone.getMachineSpeed())
                    + EntityHeatZone.heatTotalPerTick(coldZone.getPlacementCost(), coldZone.getLifetimeTicks(), coldZone.getMachineSpeed());
            cleanup.run();
            helper.assertTrue(heatInferno && coldInferno, "Both zones must turn into the Inferno");
            // 普通の火は 1 秒に 1。灼熱は 0.5 秒ごとに 4。
            helper.assertTrue(scorchedLoss >= 4.0F, "The scorching half must hurt the being inside, lost " + scorchedLoss);
            // 普通の凍結領域は凍えきるまで（3.5 秒）ダメージを与えない。
            helper.assertTrue(frozenLoss >= 4.0F, "The freezing half must hurt the being inside, lost " + frozenLoss);
            helper.assertTrue(awarded, "The caster must get the hidden Inferno advancement");
            // 2026-11-15 ユーザー（案 A）: 灼熱側の熱の総量は、加熱領域と凍結領域のコストの合計で決まる。
            helper.assertTrue(Math.abs(pooled - expected) < 1.0E-6,
                    "The scorching Heat Zone's heat total must come from both zones' costs: " + pooled + " vs " + expected);
            helper.succeed();
        });
    }

    /**
     * 氷炎地獄（2026-11-15 ユーザー、案 A）: 極寒側が Mekanism の機械から奪った熱は、灼熱側の加熱領域へ移る（灼熱側の Mekanism の
     * 機械に送る）。Mekanism がなければ飛ばす。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_inferno_moved_heat", timeoutTicks = 100)
    public static void infernoMovesTheColdSidesHeatToTheHotSide(GameTestHelper helper) {
        Optional<Block> heater = block("mekanism:resistive_heater");
        if (!MekanismCompat.isMekanismLoaded() || heater.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_inferno_moved_heat", new Vec3(8.5, 1.0, 2.5));
        BlockPos coldHeater = new BlockPos(4, 1, 2);
        BlockPos hotHeater = new BlockPos(1, 1, 2);
        helper.setBlock(coldHeater, heater.get());
        helper.setBlock(hotHeater, heater.get());
        Runnable cleanup = () -> {
            helper.setBlock(coldHeater, Blocks.AIR);
            helper.setBlock(hotHeater, Blocks.AIR);
            cleanup(helper, caster);
        };
        try {
            // 極寒側の発熱器を 800 K まで温めておく。
            MekanismGameplayCompat.supplyZoneHeat(level, List.of(level.getBlockEntity(helper.absolutePos(coldHeater))),
                    new java.util.IdentityHashMap<>(), 1.0E9, 800.0, at -> true);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(40, () -> {
            double moved = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                    .filter(zone -> zone instanceof EntityHeatZone).mapToDouble(zone -> ((EntityHeatZone) zone).movedHeatReceived()).sum();
            double coldTemp = MekanismGameplayCompat.temperatureAt(level, helper.absolutePos(coldHeater));
            cleanup.run();
            helper.assertTrue(moved > 0.0, "The scorching Heat Zone must receive the heat the freezing half took away, got " + moved);
            helper.assertTrue(coldTemp < 790.0, "The freezing half must cool the heater, " + coldTemp + " K");
            helper.succeed();
        });
    }

    /** 氷炎地獄の灼熱になった加熱領域は、Create の一括製錬をする（原料の鉄 → 鉄インゴット）。Create がなければ飛ばす。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_inferno_create", timeoutTicks = 400)
    public static void scorchingHeatZoneBlastsItemsWithCreate(GameTestHelper helper) {
        if (!CreateCompat.isCreateLoaded()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_inferno_create", new Vec3(8.5, 1.0, 2.5));
        helper.setBlock(new BlockPos(1, 0, 2), Blocks.STONE);
        Vec3 itemAt = helper.absoluteVec(new Vec3(1.5, 1.0, 2.5));
        ItemEntity item = new ItemEntity(level, itemAt.x, itemAt.y, itemAt.z, new ItemStack(Items.RAW_IRON));
        item.setNeverPickUp();
        item.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(item);
        AABB around = new AABB(helper.absolutePos(CENTER)).inflate(5.0);
        Runnable cleanup = () -> {
            level.getEntitiesOfClass(ItemEntity.class, around).forEach(ItemEntity::discard);
            cleanup(helper, caster);
        };
        try {
            // 事象干渉力 4 → 速さは上限の 2 倍（Create の処理時間 150 tick を 75 tick ほどで）。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(4.0D);
            cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 10.0);
            cast(caster, freeze(helper, new BlockPos(4, 1, 2)), 3.0, 10.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(120, () -> {
            boolean blasted = !level.getEntitiesOfClass(ItemEntity.class, around,
                    entity -> entity.getItem().is(Items.IRON_INGOT)).isEmpty();
            cleanup.run();
            helper.assertTrue(blasted, "The scorching Heat Zone must blast raw iron into an iron ingot with Create");
            helper.succeed();
        });
    }

    /**
     * 設計 A: 凍結領域は中の Mekanism の熱を持つ設備を周囲の温度に向けて冷やす（外の設備より速く冷える、周囲の温度より下には
     * 冷えない）。加熱領域と重なったブロックの設備は冷えない（打ち消し合う）。Mekanism がなければ飛ばす。
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
            // 凍結領域は x 0〜2・z 1〜3（干渉力 4 → 速さ 2、威力 10 で毎 tick 4%）。cancelled の 1 ブロックだけ加熱領域も重ねる。
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
            helper.assertTrue(cooledTemp > 290.0, "It must not cool below the ambient temperature: " + temps);
            helper.assertTrue(cancelledTemp > cooledTemp + 50.0,
                    "A heater where the Heat Zone cancels the Freeze Zone must not be cooled: " + temps);
            helper.succeed();
        });
    }

    /** 設計 B: 加熱領域の中のブレイズバーナーは燃料なしで「加熱」になり、外のものは変わらない。Create がなければ飛ばす。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner", timeoutTicks = 100)
    public static void heatZoneKindlesBlazeBurners(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.KINDLE);
    }

    /** 設計 B: 加熱領域が消えると、ブレイズバーナーは数 tick で元の段に戻る。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_revert", timeoutTicks = 100)
    public static void heatZoneBurnerRevertsWhenTheZoneEnds(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.REVERT);
    }

    /** 設計 B: 氷炎地獄の灼熱の側のブレイズバーナーは「極度の加熱」になる。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_inferno", timeoutTicks = 100)
    public static void scorchingHeatZoneSuperheatsBlazeBurners(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.INFERNO);
    }

    /**
     * 熱の総量（2026-11-14 ユーザー）: ブレイズバーナーは 1 台 200 J/t（2026-11-15）を熱の総量から払い、払える台数だけ加熱になる。1 台ぶんしか
     * ない領域では、中心に近い 1 台だけが加熱になる。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_budget", timeoutTicks = 100)
    public static void heatZoneKindlesOnlyTheBurnersItCanPayFor(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.BUDGET);
    }

    /**
     * 威力は 10 より上も入れられる（2026-11-15 ユーザー）: 一辺 3 でも威力 100（コスト 410 → 約 246 J/t）ならバーナー 1 台を
     * 加熱にできる。1 か所あたりの強さ（上限の温度）は威力 10 で頭打ち。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_small", timeoutTicks = 100)
    public static void smallHeatZoneWithHighPowerKindlesABurner(GameTestHelper helper) {
        int cost = com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickHeatZone.costFor(9, 100.0);
        helper.assertTrue(cost == 410, "Size 3, Power 100 must cost 410, got " + cost);
        burnerTest(helper, BurnerCase.SMALL);
    }

    /** 設計 B: 凍結領域と重なって打ち消されたところのブレイズバーナーは変わらない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_heat_burner_cancel", timeoutTicks = 100)
    public static void cancelledHeatZoneLeavesBlazeBurnersAlone(GameTestHelper helper) {
        burnerTest(helper, BurnerCase.CANCEL);
    }

    private enum BurnerCase { KINDLE, REVERT, INFERNO, CANCEL, BUDGET, SMALL }

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
        // 加熱領域は (1, 1, 2) を中心にした一辺 9（x -3〜5）。熱の総量でバーナー 1 台（200 J/t）だけ払えるよう、干渉力 2（速さ 1）・
        // 威力 10（コスト 374 → 約 224 J/t）にする。灼熱は凍結領域のコストの分も足して約 449 J/t で、極度の加熱（400 J/t）1 台ぶん。
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
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            switch (mode) {
                case KINDLE, REVERT, BUDGET -> cast(caster, heat(helper, new BlockPos(1, 1, 2)), 9.0, 10.0);
                // 一辺 3（x 0〜2）・威力 100。外のバーナー (8, 1, 2) は外。
                case SMALL -> cast(caster, heat(helper, new BlockPos(1, 1, 2)), 3.0, 100.0);
                // 氷炎地獄: 加熱 x -3〜5、凍結 x 6〜14（同じ大きさで二分）。
                case INFERNO -> {
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
        helper.runAfterDelay(mode == BurnerCase.INFERNO ? 25 : 14, () -> {
            String insideLevel = propertyValue(helper.getBlockState(inside), "blaze");
            if (mode == BurnerCase.INFERNO) {
                // 極度の加熱は熱の総量から払う（2026-11-14、前は耐久値が余分に減った）。
                float worn = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                        .filter(zone -> zone instanceof EntityHeatZone).map(zone -> zone.getWorn()).findFirst().orElse(0.0F);
                helper.assertTrue(worn == 0.0F, "Superheating a Blaze Burner must not wear the Heat Zone down any more, worn " + worn);
                var superheat = level.getServer().getAdvancements()
                        .get(com.moratan251.psitweaks.common.handler.PsitweaksAdvancements.INFERNO_SUPERHEAT);
                helper.assertTrue(superheat != null && caster.getAdvancements().getOrStartProgress(superheat).isDone(),
                        "Superheating a Blaze Burner must award the hidden advancement");
            }
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
            if (mode == BurnerCase.SMALL) {
                EntityHeatZone zone = SpellGramIndex.ofCaster(level, caster.getUUID()).stream()
                        .filter(object -> object instanceof EntityHeatZone).map(object -> (EntityHeatZone) object).findFirst().orElse(null);
                float zonePower = zone == null ? -1.0F : zone.getPower();
                double maxTemperature = zone == null ? -1.0 : zone.maxTemperature();
                cleanup.run();
                helper.assertTrue("kindled".equals(insideLevel), "A size-3, Power 100 Heat Zone must kindle the burner, was " + insideLevel);
                helper.assertTrue(zonePower == 10.0F && maxTemperature == 1000.0,
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
                case KINDLE -> "kindled";
                case INFERNO -> "seething";
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
     * 設計 C: Sable があるとき、加重領域が機体を押すためのフックが今の Sable の版で揃い、機体のない場所では何も押さない。
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
     * 他人どうしの奪い合い（2026-11-14 ユーザー、案 2）: 種類を問わず、重なったところでは事象干渉力の高い側の領域だけが効く。
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

    /**
     * 領域干渉の威力は奪い合いの事象干渉力に 威力 × 5% を上乗せし、勝てば重なったところでは持ち主の側の領域（ほかの種類も）が
     * 効く。干渉力 2・威力 10 の領域干渉（3.0）は、干渉力 2.8 の他人に勝つ。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contest_bonus", timeoutTicks = 40)
    public static void interferenceZonePowerWinsTheContestForItsOwner(GameTestHelper helper) {
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "zone_contest_bonus_owner", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer rival = GameTestPlayers.inLevel(helper, "zone_contest_bonus_rival", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            cleanup(helper, owner);
            cleanup(helper, rival);
        };
        try {
            owner.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            rival.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.8D);
            cast(rival, soak(helper, CENTER), 5.0, 0.0);
            cast(owner, soak(helper, new BlockPos(5, 1, 2)), 3.0, 0.0);
            SpellGramZone rivalSoak = only(helper, rival, EntitySoakZone.class);
            SpellGramZone ownerSoak = only(helper, owner, EntitySoakZone.class);
            BlockPos overlap = helper.absolutePos(new BlockPos(4, 1, 2));
            helper.assertTrue(rivalSoak.isInsideAndActive(overlap) && !ownerSoak.isInsideAndActive(overlap),
                    "Precondition: the rival with higher interference holds the overlap");

            cast(owner, interference(helper, new BlockPos(5, 1, 2)), 3.0, 10.0);
            EntityInterferenceZone zone = only(helper, owner, EntityInterferenceZone.class);
            helper.assertTrue(Math.abs(zone.contestInterference() - 3.0) < 1.0E-9,
                    "Power 10 must add 50% to the interference in contests, got " + zone.contestInterference());
            helper.assertTrue(ownerSoak.isInsideAndActive(overlap) && !rivalSoak.isInsideAndActive(overlap),
                    "With the Interference Zone's bonus, the owner's zones must take the overlap");
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
            cast(owner, interference(helper, CENTER), 5.0, 1.0);
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
     * 領域干渉の規模とコスト（2026-11-14 ユーザー、仮）: 規模 = 10 + 広さ / 5 ×（1 + 威力 / 10）、コスト = 50 + 広さ × 2 ×
     * （1 + 威力 / 10）。威力を省けば今までと同じ。サイズ 5（広さ 25）で、威力なし 15・100、威力 10 で 20・150。
     */
    @GameTest(template = TEMPLATE)
    public static void interferenceZoneCostGrowsWithPower(GameTestHelper helper) throws Exception {
        // 2026-11-14 ユーザー: 威力は必須の 1〜10、規模 = 10 + ⌊広さ × 1.35 ×（1 + 威力 / 10）⌋。
        int[] low = interferenceStats(1.0);
        int[] full = interferenceStats(10.0);
        int[] below = interferenceStats(0.5);
        helper.assertTrue(low[0] == 47 && low[1] == 105, "At Power 1: Potency 47, Cost 105, got " + low[0] + ", " + low[1]);
        helper.assertTrue(full[0] == 77 && full[1] == 150, "At Power 10: Potency 77, Cost 150, got " + full[0] + ", " + full[1]);
        helper.assertTrue(below[0] == low[0] && below[1] == low[1], "A Power below 1 must count as 1");
        helper.succeed();
    }

    /** サイズ 5・威力 {@code power}（null なら省く）の領域干渉の {規模, コスト}。 */
    private static int[] interferenceStats(Double power) throws Exception {
        var piece = new com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickInterferenceZone(new Spell()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getParamEvaluation(SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return (T) Double.valueOf(5.0);
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? (T) power : null;
            }
        };
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
