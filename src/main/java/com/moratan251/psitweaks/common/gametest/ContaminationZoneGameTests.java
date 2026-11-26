package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.ZoneRadiationCompat;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityContaminationZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.spells.PsitweaksSpells;
import com.moratan251.psitweaks.common.spells.SpellWarning;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickContaminationZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.moratan251.psitweaks.common.gametest.ZoneCasting.cast;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.castOrClean;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.cleanup;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.paramValue;
import static com.moratan251.psitweaks.common.gametest.ZoneElementGameTests.castTurbulenceOver;

/**
 * 汚染領域（作動式: 汚染領域、Mekanism のみ）。Mekanism がなければ、ピースなどが登録されていないことだけ
 * 確かめる（Mekanism があって放射線が切ってあれば落とす）。範囲はたいてい、テンプレートの (1, 1, 1) を中心にした一辺 3
 * （x・y・z 0〜2）。威力 10 で範囲の中の線量率は 1,000 Sv/h、被ばく量は 1 秒に約 0.28 Sv ずつ上がる。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ContaminationZoneGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String RADIATION_OFF_BATCH = "psitweaks_zone_contamination_radiation_off";
    /** 威力 10 の範囲の中の線量率（Sv/h）。 */
    private static final double RATE = 1000.0;
    /** 威力 10 の範囲が 1 回（1 秒ぶん）で足す被ばく量（Sv）。 */
    private static final double STEP = RATE / 3600.0;
    /** 被ばくしていないとみなす量（Sv）。Mekanism はどの生き物にも 1.0E-7 Sv の自然の被ばく量を持たせる。1 回の量（0.28 Sv）よりずっと小さい。 */
    private static final double UNTOUCHED = 1.0E-6;
    private static boolean savedRadiation = true;

    private ContaminationZoneGameTests() {
    }

    @BeforeBatch(batch = RADIATION_OFF_BATCH)
    public static void turnRadiationOff(ServerLevel level) {
        if (MekanismCompat.isMekanismLoaded()) {
            savedRadiation = MekanismRadiationSwitch.isEnabled();
            MekanismRadiationSwitch.set(false);
        }
    }

    @AfterBatch(batch = RADIATION_OFF_BATCH)
    public static void restoreRadiation(ServerLevel level) {
        if (MekanismCompat.isMekanismLoaded()) {
            MekanismRadiationSwitch.set(savedRadiation);
        }
    }

    /** 線量率は入れた威力 1 で 1 Sv/h、3 上がるごとに 10 倍。1 回（1 秒ぶん）に足すのは 線量率 ÷ 3600。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contamination_numbers")
    public static void contaminationNumbersFollowPower(GameTestHelper helper) {
        assertClose(helper, EntityContaminationZone.doseRateFor(5.0F), 1.0, "dose rate at Power 1");
        assertClose(helper, EntityContaminationZone.doseRateFor(6.0F), Math.cbrt(10.0), "dose rate at Power 2");
        assertClose(helper, EntityContaminationZone.doseRateFor(8.0F), 10.0, "dose rate at Power 4");
        assertClose(helper, EntityContaminationZone.doseRateFor(11.0F), 100.0, "dose rate at Power 7");
        assertClose(helper, EntityContaminationZone.doseRateFor(14.0F), RATE, "dose rate at Power 10");
        assertClose(helper, EntityContaminationZone.doseFor(5.0F), 1.0 / 3600.0, "a second's dose at Power 1");
        assertClose(helper, EntityContaminationZone.doseFor(14.0F), STEP, "a second's dose at Power 10");
        helper.succeed();
    }

    /** Mekanism があるときだけ、汚染領域・除染のピース・プログラム・エンティティが登録される。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contamination_registration")
    public static void radiationZonesNeedMekanism(GameTestHelper helper) {
        boolean mekanism = MekanismCompat.isMekanismLoaded();
        helper.assertTrue((PsitweaksSpells.TRICK_CONTAMINATION_ZONE != null) == mekanism
                && (PsitweaksSpells.TRICK_DECONTAMINATION != null) == mekanism, "The pieces must exist only with Mekanism");
        helper.assertTrue((PsitweaksItems.PROGRAM_CONTAMINATION_ZONE != null) == mekanism
                && (PsitweaksItems.PROGRAM_DECONTAMINATION != null) == mekanism, "The programs must exist only with Mekanism");
        helper.assertTrue((PsitweaksEntities.CONTAMINATION_ZONE != null) == mekanism
                && (PsitweaksEntities.DECONTAMINATION != null) == mekanism, "The entities must exist only with Mekanism");
        helper.succeed();
    }

    /**
     * 範囲の中のハスクは、中にいるあいだ 1 秒ごとに 線量率 ÷ 3600 ずつ被ばくし続ける（威力 10、7.5 秒で 5〜7 回ぶん。上限は
     * ない）。術者と範囲の外のハスクは被ばくしない。放射線フィルタのかかったハスクも被ばくしない。中のハスクは、放射線の
     * ダメージで倒れないように体力を増やしておく。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contamination_dose", timeoutTicks = 200)
    public static void contaminationKeepsRaisingTheDose(GameTestHelper helper) {
        if (!requireRadiation(helper)) {
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_contaminate", new Vec3(0.5, 0.0, 0.5));
        Husk inside = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 0.0, 1.5)), 100.0F);
        Husk filtered = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 0.0, 0.5));
        Husk outside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(4.5, 0.0, 4.5));
        filtered.addEffect(new MobEffectInstance(PsitweaksEffects.RADIATION_FILTER, 1000));
        Runnable cleanup = () -> {
            inside.discard();
            filtered.discard();
            outside.discard();
            cleanup(helper, caster);
        };
        castOrClean(caster, contamination(helper, new BlockPos(1, 1, 1)), 3.0, 10.0, cleanup);
        helper.runAfterDelay(150, () -> {
            double in = ZoneRadiationCompat.exposure(inside);
            double shielded = ZoneRadiationCompat.exposure(filtered);
            double out = ZoneRadiationCompat.exposure(outside);
            double own = ZoneRadiationCompat.exposure(caster);
            cleanup.run();
            helper.assertTrue(in > STEP * 4.5 && in <= STEP * 8.0,
                    "The husk inside must gain " + STEP + " Sv every second for as long as it stays, got " + in);
            helper.assertTrue(shielded < UNTOUCHED, "Radiation Filter must block it, got " + shielded);
            helper.assertTrue(out < UNTOUCHED, "The husk outside must not be exposed, got " + out);
            helper.assertTrue(own < UNTOUCHED, "The caster must be spared, got " + own);
            helper.succeed();
        });
    }

    /** 存続時間（「時間」2 秒）が過ぎたら、もう足さない。2 秒のあいだに足すのは 2 回まで。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contamination_lifetime", timeoutTicks = 200)
    public static void contaminationStopsWhenItEnds(GameTestHelper helper) {
        if (!requireRadiation(helper)) {
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_contaminate_end", new Vec3(4.5, 0.0, 4.5));
        Husk inside = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 0.0, 1.5)), 100.0F);
        Runnable cleanup = () -> {
            inside.discard();
            cleanup(helper, caster);
        };
        try {
            cast(caster, contamination(helper, new BlockPos(1, 1, 1)), 3.0, 10.0, 2.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(120, () -> {
            double dose = ZoneRadiationCompat.exposure(inside);
            boolean ended = SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).isEmpty();
            cleanup.run();
            helper.assertTrue(ended, "The zone must end after its Time");
            helper.assertTrue(dose > STEP * 0.5 && dose <= STEP * 2.0 + UNTOUCHED,
                    "Only the steps within the Time may be given, got " + dose);
            helper.succeed();
        });
    }

    /** Mekanism の設定で放射線が切ってあると、汚染領域も除染の核も置かずに警告する。 */
    @GameTest(template = TEMPLATE, batch = RADIATION_OFF_BATCH)
    public static void radiationZonesWarnWhileRadiationIsOff(GameTestHelper helper) throws Exception {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        List<Component> messages = new ArrayList<>();
        var caster = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "zone_radiation_off")) {
            @Override
            public void sendSystemMessage(Component message) {
                messages.add(message);
            }
        };
        caster.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 0, 2))));
        cast(caster, contamination(helper, new BlockPos(1, 1, 1)), 3.0, 10.0);
        DecontaminationGameTests.cast(caster, DecontaminationGameTests.decontamination(helper, new Vec3(1.5, 1.5, 1.5)), 10.0, 0.0);
        boolean placed = !SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).isEmpty();
        SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).forEach(zone -> zone.discard());
        long warnings = messages.stream().filter(message -> message.getContents() instanceof TranslatableContents contents
                && SpellWarning.RADIATION_DISABLED.equals(contents.getKey())).count();
        helper.assertFalse(placed, "No zone may be placed while radiation is off");
        helper.assertTrue(warnings == 2, "Both tricks must warn, got " + messages);
        helper.succeed();
    }

    /**
     * 汚染流（同じ術者の乱流領域と重なったところ）: 照射は続けたまま、0.5 秒ごとにダメージを足す。範囲は汚染が x・y・z 0〜2、
     * 乱流（威力 40、サイズは 16 までなのでコスト 4555 で強さ 40）が x 0〜1。汚染の威力 10 は強さを 40 で頭打ちにするので、1 回の
     * 基本のダメージは 6。
     * 乱流のない x 2 のハスクは、被ばくするだけで汚染流のダメージを受けない（Mekanism の放射線そのもののダメージは除く）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contaminated_stream", timeoutTicks = 100)
    public static void contaminatedStreamHurtsWhileItKeepsExposing(GameTestHelper helper) {
        if (!requireRadiation(helper)) {
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_contaminated_stream", new Vec3(4.5, 0.0, 4.5));
        Husk stormed = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(0.5, 0.0, 1.5)), 100.0F);
        Husk plain = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 0.0, 1.5)), 100.0F);
        Runnable cleanup = () -> {
            stormed.discard();
            plain.discard();
            cleanup(helper, caster);
        };
        try {
            cast(caster, contamination(helper, new BlockPos(1, 1, 1)), 3.0, 10.0);
            castTurbulenceOver(caster, helper.absolutePos(new BlockPos(0, 0, 0)), 2, 3, 3, 40.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        float stormedBefore = stormed.getHealth();
        float plainBefore = plain.getHealth();
        helper.runAfterDelay(25, () -> {
            float stormedLoss = stormedBefore - stormed.getHealth();
            float plainLoss = plainBefore - plain.getHealth();
            String plainHurtBy = plain.getLastDamageSource() == null ? "nothing" : plain.getLastDamageSource().getMsgId();
            double stormedDose = ZoneRadiationCompat.exposure(stormed);
            double plainDose = ZoneRadiationCompat.exposure(plain);
            EntityContaminationZone zone = SpellGramIndex.all(level, EntityContaminationZone.class).stream()
                    .filter(found -> caster.getUUID().equals(found.getCasterUuid())).findFirst().orElse(null);
            boolean storm = zone != null && zone.isStorm();
            int outline = zone == null ? 0 : zone.getOutlineColor();
            float combo = zone == null ? 0.0F : zone.comboAttackPowerAt(stormed.blockPosition(), zone.getPower(),
                    EntityTurbulenceZone.PARTNER_ATTACK_POWER_PER_POWER);
            cleanup.run();
            helper.assertTrue(storm, "The caster's own turbulence must turn the zone into a Contaminated Stream");
            helper.assertTrue(outline == EntityContaminationZone.STORM_OUTLINE_COLOR, "The stream must draw its green outline");
            helper.assertTrue(combo == 40.0F, "Power 10 must let the stream reach attack power 40, got " + combo);
            // Mekanism の放射線そのもの（被ばく量に応じてときどき 1）は、どちらのハスクにも入りうる。汚染流は 1 回 6 弱。
            helper.assertTrue(stormedLoss >= plainLoss + 3.0F,
                    "The Contaminated Stream must hurt the husk inside: stream " + stormedLoss + ", plain " + plainLoss);
            helper.assertTrue(plainLoss < 3.0F && !"psitweaks.contaminated_stream".equals(plainHurtBy),
                    "Without turbulence the zone must not hurt with the stream, lost " + plainLoss + " by " + plainHurtBy);
            helper.assertTrue(stormedDose >= STEP * 0.5 && plainDose >= STEP * 0.5,
                    "Both husks must still be exposed: stream " + stormedDose + ", plain " + plainDose);
            helper.succeed();
        });
    }

    /**
     * 汚染流の強さは、乱流の強さを汚染領域の入れた威力 × 4 で頭打ちにしたもの（熱乱流・吹雪と同じ）。威力 40 の乱流（強さ 40）でも、
     * 威力 1 の汚染領域なら 4、1 回の基本のダメージは 0.6。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contaminated_stream_limit", timeoutTicks = 60)
    public static void contaminationPowerLimitsTheStream(GameTestHelper helper) {
        if (!requireRadiation(helper)) {
            return;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_contaminated_stream_limit", new Vec3(4.5, 0.0, 4.5));
        Runnable cleanup = () -> cleanup(helper, caster);
        try {
            cast(caster, contamination(helper, new BlockPos(1, 1, 1)), 3.0, 1.0);
            castTurbulenceOver(caster, helper.absolutePos(new BlockPos(0, 0, 0)), 3, 3, 3, 40.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(15, () -> {
            BlockPos inside = helper.absolutePos(new BlockPos(1, 1, 1));
            EntityContaminationZone zone = SpellGramIndex.all(level, EntityContaminationZone.class).stream()
                    .filter(found -> caster.getUUID().equals(found.getCasterUuid())).findFirst().orElseThrow();
            float storm = zone.stormAttackPowerAt(inside);
            float combo = zone.comboAttackPowerAt(inside, zone.getPower(), EntityTurbulenceZone.PARTNER_ATTACK_POWER_PER_POWER);
            cleanup.run();
            helper.assertTrue(storm == 40.0F, "The turbulence alone must reach attack power 40, was " + storm);
            helper.assertTrue(combo == 4.0F, "A Contamination Zone of Power 1 must limit the attack power to 4, was " + combo);
            helper.assertTrue(Math.abs(SpellGramZone.STORM_DAMAGE_PER_POWER * combo - 0.6F) < 1.0E-6F,
                    "A hit must be 0.6 before multipliers");
            helper.succeed();
        });
    }

    /** Mekanism の設定で放射線が切ってあると、汚染領域が置けないので、乱流を重ねても汚染流は起きない（落ちない）。 */
    @GameTest(template = TEMPLATE, batch = RADIATION_OFF_BATCH, timeoutTicks = 100)
    public static void noContaminatedStreamWhileRadiationIsOff(GameTestHelper helper) throws Exception {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_contaminated_stream_off", new Vec3(4.5, 0.0, 4.5));
        Husk husk = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 0.0, 1.5)), 100.0F);
        Runnable cleanup = () -> {
            husk.discard();
            cleanup(helper, caster);
        };
        try {
            cast(caster, contamination(helper, new BlockPos(1, 1, 1)), 3.0, 10.0);
            castTurbulenceOver(caster, helper.absolutePos(new BlockPos(0, 0, 0)), 3, 3, 3);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        float before = husk.getHealth();
        helper.runAfterDelay(25, () -> {
            float loss = before - husk.getHealth();
            boolean contaminated = SpellGramIndex.all(helper.getLevel(), EntityContaminationZone.class).stream()
                    .anyMatch(found -> caster.getUUID().equals(found.getCasterUuid()));
            cleanup.run();
            helper.assertFalse(contaminated, "No Contamination Zone may be placed while radiation is off");
            helper.assertTrue(loss == 0.0F, "The turbulence alone must not hurt, lost " + loss);
            helper.succeed();
        });
    }

    /** Mekanism がなければ通す（false を返す）。Mekanism があって放射線が切ってあれば落とす。 */
    private static boolean requireRadiation(GameTestHelper helper) {
        if (!MekanismCompat.isMekanismLoaded()) {
            helper.succeed();
            return false;
        }
        helper.assertTrue(ZoneRadiationCompat.isRadiationEnabled(), "Mekanism's radiation must be on for this test");
        return true;
    }

    private static void assertClose(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-12, what + ": expected " + expected + ", got " + actual);
    }

    private static PieceTrickZoneBase<?> contamination(GameTestHelper helper, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        return new PieceTrickContaminationZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }
}
