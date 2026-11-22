package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.ZoneRadiationCompat;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityContaminationZone;
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

/**
 * 汚染領域（作動式: 汚染領域、Mekanism のみ）。Mekanism がなければ、ピースなどが登録されていないことだけ
 * 確かめる（Mekanism があって放射線が切ってあれば落とす）。範囲はたいてい、テンプレートの (1, 1, 1) を中心にした一辺 3
 * （x・y・z 0〜2）。威力 10 で被ばく量の上限は 1.4 mSv、1 秒に 0.28 mSv ずつ上がる。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ContaminationZoneGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String RADIATION_OFF_BATCH = "psitweaks_zone_contamination_radiation_off";
    private static final double LIMIT = 1.4E-3;
    private static final double STEP = LIMIT / 5.0;
    /** 被ばくしていないとみなす量（Sv）。Mekanism はどの生き物にも 1.0E-7 Sv の自然の被ばく量を持たせる。1 回の量（0.28 mSv）よりずっと小さい。 */
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

    /** 上限は 中の威力 × 0.1 mSv、1 回に上限の 1/5、上限を超えない分だけ足す。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contamination_numbers")
    public static void contaminationNumbersFollowPower(GameTestHelper helper) {
        assertClose(helper, EntityContaminationZone.doseLimitFor(5.0F), 5.0E-4, "limit at Power 1");
        assertClose(helper, EntityContaminationZone.doseLimitFor(14.0F), LIMIT, "limit at Power 10");
        assertClose(helper, EntityContaminationZone.doseStep(0.0, LIMIT), STEP, "a fifth of the limit");
        assertClose(helper, EntityContaminationZone.doseStep(1.3E-3, LIMIT), 1.0E-4, "only up to the limit");
        assertClose(helper, EntityContaminationZone.doseStep(1.5E-3, LIMIT), 0.0, "nothing above the limit");
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
     * 範囲の中のハスクは上限まで被ばくし、上限で止まる（威力 10、6 秒で上限に届く）。術者と範囲の外のハスクは被ばくしない。
     * 放射線フィルタのかかったハスクも被ばくしない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_contamination_dose", timeoutTicks = 200)
    public static void contaminationRaisesTheDoseUpToTheLimit(GameTestHelper helper) {
        if (!requireRadiation(helper)) {
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_contaminate", new Vec3(0.5, 0.0, 0.5));
        Husk inside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 0.0, 1.5));
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
            helper.assertTrue(in > LIMIT * 0.8 && in <= LIMIT * (1.0 + 1.0E-9),
                    "The husk inside must reach the limit " + LIMIT + " Sv and stop there, got " + in);
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
        Husk inside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 0.0, 1.5));
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
