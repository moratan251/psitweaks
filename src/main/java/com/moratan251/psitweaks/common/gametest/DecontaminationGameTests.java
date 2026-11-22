package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.ZoneRadiationCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityDecontamination;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickDecontamination;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickInterferenceZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.List;

import static com.moratan251.psitweaks.common.gametest.ZoneCasting.cleanup;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.paramValue;

/**
 * 作動式: 除染（Mekanism のみ）。Mekanism がなければ何も確かめずに通す（Mekanism があって放射線が切ってあれば落とす）。核は
 * テンプレートの (1.5, 1.5, 1.5) に置き、威力 10（コスト 1050、30 秒）: 線源の除染能力は毎秒 1050 × 0.5 ÷ 30 = 17.5 Sv/h。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DecontaminationGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final Vec3 CORE = new Vec3(1.5, 1.5, 1.5);
    private static final double POWER = 10.0;
    private static final double SOURCE_CAPACITY = 1050 * 0.5 / 30.0;

    private DecontaminationGameTests() {
    }

    /**
     * コストは (50 + 威力 × 100) × 時間 ÷ 30（威力は 1〜1000）、規模は 50 + min(威力, 10) × 30。除染能力はコスト ÷ 存続時間。
     * 届いている量は 強さ ÷ max(1, 核までの距離²)。配り方は重みに比例で、下限を下回る相手は全部引き、使い切れなかった分は残りへ
     * 1 回だけ配り直す。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_decontamination_numbers")
    public static void decontaminationNumbersFollowPower(GameTestHelper helper) {
        helper.assertTrue(PieceTrickDecontamination.costFor(10.0, 30.0) == 1050, "Power 10 for 30 seconds must cost 1050");
        helper.assertTrue(PieceTrickDecontamination.costFor(12.5, 60.0) == 2600, "Power 12.5 for 60 seconds must cost 2600");
        helper.assertTrue(PieceTrickDecontamination.costFor(5000.0, 30.0) == 100050, "Power above 1000 must count as 1000");
        helper.assertTrue(PieceTrickDecontamination.potencyFor(5.0) == 200 && PieceTrickDecontamination.potencyFor(50.0) == 350,
                "Potency must be 50 + min(Power, 10) × 30");
        assertClose(helper, EntityDecontamination.capacityPerSecond(1050.0F, 600, 0.5), SOURCE_CAPACITY, "source capacity");
        assertClose(helper, EntityDecontamination.capacityPerSecond(2100.0F, 1200, 0.5), SOURCE_CAPACITY,
                "the capacity must not depend on the Time");
        assertClose(helper, EntityDecontamination.reachingAmount(1000.0, new BlockPos(3, 1, 1), CORE), 250.0,
                "a source 2 blocks away counts a quarter");
        assertClose(helper, EntityDecontamination.reachingAmount(1000.0, new BlockPos(1, 1, 1), CORE), 1000.0,
                "a source at the core counts whole");
        double[] weighted = EntityDecontamination.allocateWeighted(new double[]{100.0, 100.0, 0.5}, new double[]{3.0, 1.0, 1.0},
                5.0, 0.0);
        assertClose(helper, weighted[0], 3.0 + 0.5 * 3.0 / 4.0, "shares follow the weights");
        assertClose(helper, weighted[1], 1.0 + 0.5 / 4.0, "shares follow the weights");
        assertClose(helper, weighted[2], 0.5, "a small amount is taken whole");
        double[] weak = EntityDecontamination.allocateWeighted(new double[]{2.0E-5, 1000.0}, new double[]{1.0, 1.0}, 3.0,
                1.0E-5);
        assertClose(helper, weak[0], 2.0E-5, "a source left below the floor is taken whole");
        assertClose(helper, weak[1], 3.0 - 2.0E-5, "what the weak source cannot use goes to the other");
        helper.succeed();
    }

    /**
     * 半径の中の線源が、核に届いている量に比例して弱まる: 2 ブロック先（重み 1000 ÷ 4 = 250）と 5 ブロック先（1000 ÷ 25 = 40）の
     * 同じ強さの線源は、除染能力 17.5 Sv/h を 250 : 40 で分ける。半径（32 ブロック）より遠い線源は、Mekanism の自然な減衰のほかは
     * 変わらない（どれも同じだけ減衰するので、遠い線源との差で確かめる）。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_decontamination_sources", timeoutTicks = 100)
    public static void decontaminationWeakensSourcesByDistance(GameTestHelper helper) {
        if (!requireRadiation(helper)) {
            return;
        }
        BlockPos near = helper.absolutePos(new BlockPos(3, 1, 1));
        BlockPos mid = helper.absolutePos(new BlockPos(6, 1, 1));
        BlockPos far = helper.absolutePos(new BlockPos(1, 1, 41));
        List<BlockPos> sources = List.of(near, mid, far);
        sources.forEach(pos -> ZoneRadiationCompat.addSource(helper.getLevel(), pos, 1000.0));
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "decontamination_sources", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            sources.forEach(pos -> ZoneRadiationCompat.removeSource(helper.getLevel(), pos));
            cleanup(helper, caster);
        };
        castOrClean(caster, decontamination(helper, CORE), POWER, cleanup);
        // 除染は置いてから 20 tick ごと: 30 tick のあいだに 1 回。
        helper.runAfterDelay(30, () -> {
            double nearby = ZoneRadiationCompat.sourceAt(helper.getLevel(), near);
            double middle = ZoneRadiationCompat.sourceAt(helper.getLevel(), mid);
            double distant = ZoneRadiationCompat.sourceAt(helper.getLevel(), far);
            Object count = ((TranslatableContents) coreOf(helper, caster).hudLines().get(1).getContents()).getArgs()[0];
            cleanup.run();
            double nearShare = SOURCE_CAPACITY * 250.0 / 290.0;
            double midShare = SOURCE_CAPACITY * 40.0 / 290.0;
            helper.assertTrue(distant > 995.0 && distant <= 1000.0,
                    "A source beyond the radius must only decay naturally, got " + distant);
            helper.assertTrue(Math.abs((distant - nearby) - nearShare) < nearShare * 0.02,
                    "The near source must lose about " + nearShare + " Sv/h, lost " + (distant - nearby));
            helper.assertTrue(Math.abs((distant - middle) - midShare) < midShare * 0.02,
                    "The farther source must lose about " + midShare + " Sv/h, lost " + (distant - middle));
            helper.assertTrue(Integer.valueOf(2).equals(count), "The HUD must count the 2 sources in range, got " + count);
            helper.succeed();
        });
    }

    /** 半径の中の弱い線源は消える（10 µSv/h を下回る）。半径より遠いものは残る。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_decontamination_weak", timeoutTicks = 100)
    public static void decontaminationClearsWeakSourcesInRange(GameTestHelper helper) {
        if (!requireRadiation(helper)) {
            return;
        }
        BlockPos near = helper.absolutePos(new BlockPos(0, 1, 0));
        BlockPos edge = helper.absolutePos(new BlockPos(21, 1, 1));
        BlockPos far = helper.absolutePos(new BlockPos(1, 1, 41));
        List<BlockPos> sources = List.of(near, edge, far);
        sources.forEach(pos -> ZoneRadiationCompat.addSource(helper.getLevel(), pos, 2.0E-5));
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "decontamination_weak", new Vec3(4.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            sources.forEach(pos -> ZoneRadiationCompat.removeSource(helper.getLevel(), pos));
            cleanup(helper, caster);
        };
        castOrClean(caster, decontamination(helper, CORE), POWER, cleanup);
        helper.runAfterDelay(30, () -> {
            double nearby = ZoneRadiationCompat.sourceAt(helper.getLevel(), near);
            double atEdge = ZoneRadiationCompat.sourceAt(helper.getLevel(), edge);
            double distant = ZoneRadiationCompat.sourceAt(helper.getLevel(), far);
            cleanup.run();
            helper.assertTrue(nearby == 0.0 && atEdge == 0.0,
                    "Weak sources within the radius must disappear, left " + nearby + " and " + atEdge);
            helper.assertTrue(distant > 0.0, "A source beyond the radius must stay");
            helper.succeed();
        });
    }

    /**
     * 半径の中のプレイヤー（術者も）の被ばく量は減らさない。照準の表示は除染能力と半径の中の線源の 2 行。被ばく量は Mekanism の
     * 自然な減衰（約 1 秒に 1 回 0.9995 倍）だけ減る。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_decontamination_dose", timeoutTicks = 100)
    public static void decontaminationLeavesTheDoseAlone(GameTestHelper helper) {
        if (!requireRadiation(helper)) {
            return;
        }
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "decontamination_dose", new Vec3(2.5, 0.0, 2.5));
        ZoneRadiationCompat.setExposure(caster, 1.0);
        castOrClean(caster, decontamination(helper, CORE), POWER, () -> cleanup(helper, caster));
        helper.runAfterDelay(30, () -> {
            double dose = ZoneRadiationCompat.exposure(caster);
            List<String> keys = coreOf(helper, caster).hudLines().stream()
                    .map(line -> ((TranslatableContents) line.getContents()).getKey()).toList();
            cleanup(helper, caster);
            helper.assertTrue(dose > 0.998, "Decontamination must not lower the dose, left " + dose);
            helper.assertTrue(keys.equals(List.of("psitweaks.hud.decontamination.sources", "psitweaks.hud.decontamination.left")),
                    "The HUD must show the capacity and the sources in range only, got " + keys);
            helper.succeed();
        });
    }

    /** 核が、事象干渉力の高い他人の領域干渉の中にある間は止まり、線源を弱めない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_decontamination_interference", timeoutTicks = 100)
    public static void decontaminationStopsInsideStrongerInterference(GameTestHelper helper) {
        if (!requireRadiation(helper)) {
            return;
        }
        BlockPos near = helper.absolutePos(new BlockPos(3, 1, 1));
        ZoneRadiationCompat.addSource(helper.getLevel(), near, 1000.0);
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "decontamination_weaker", new Vec3(4.5, 1.0, 4.5));
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "decontamination_interference", new Vec3(0.5, 1.0, 4.5));
        Runnable cleanup = () -> {
            ZoneRadiationCompat.removeSource(helper.getLevel(), near);
            cleanup(helper, caster);
            cleanup(helper, owner);
        };
        try {
            owner.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(3.0D);
            cast(caster, decontamination(helper, CORE), POWER, 0.0);
            ZoneCasting.cast(owner, interference(helper, new BlockPos(1, 1, 1)), 3.0, 0.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(30, () -> {
            double left = ZoneRadiationCompat.sourceAt(helper.getLevel(), near);
            boolean suppressed = coreOf(helper, caster).isSuppressed();
            cleanup.run();
            helper.assertTrue(suppressed, "The core inside a stronger Interference Zone must be stopped");
            helper.assertTrue(left > 995.0, "A stopped core must not weaken sources, left " + left);
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

    private static EntityDecontamination coreOf(GameTestHelper helper, ServerPlayer caster) {
        return SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).stream()
                .filter(EntityDecontamination.class::isInstance).map(EntityDecontamination.class::cast).findFirst()
                .orElseThrow(() -> new IllegalStateException("No decontamination core"));
    }

    private static void assertClose(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-9 * Math.max(1.0, Math.abs(expected)),
                what + ": expected " + expected + ", got " + actual);
    }

    /** 置く前に {@link #cast} が決める入力: 威力と時間（秒、0 以下なら省く）。 */
    private static final ThreadLocal<double[]> POWER_AND_TIME = ThreadLocal.withInitial(() -> new double[]{POWER, 0.0});

    /** 入力を {@link #cast} の値で返す除染のピース。核はテンプレートの {@code relative} に置く。 */
    static PieceTrickDecontamination decontamination(GameTestHelper helper, Vec3 relative) {
        Vec3 at = helper.absoluteVec(relative);
        return new PieceTrickDecontamination(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                double[] powerAndTime = POWER_AND_TIME.get();
                if (param.name.equals(SpellParam.GENERIC_NAME_POSITION)) {
                    return new Vector3(at.x, at.y, at.z);
                }
                if (param.name.equals(SpellParam.GENERIC_NAME_TIME)) {
                    return powerAndTime[1] > 0.0 ? powerAndTime[1] : null;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_POWER) ? powerAndTime[0] : null;
            }
        };
    }

    /** 術者 {@code caster} として、威力 {@code power}・時間 {@code seconds}（0 以下なら省く）で除染を唱える。 */
    static void cast(ServerPlayer caster, PieceTrickDecontamination piece, double power, double seconds) throws SpellRuntimeException {
        POWER_AND_TIME.set(new double[]{power, seconds});
        try {
            piece.execute(new SpellContext().setPlayer(caster));
        } finally {
            POWER_AND_TIME.remove();
        }
    }

    private static void castOrClean(ServerPlayer caster, PieceTrickDecontamination piece, double power, Runnable cleanup) {
        try {
            cast(caster, piece, power, 0.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
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
}
