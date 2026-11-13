package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.entities.SpellGram.marker.EntityMeteorLineNight;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySoakZone;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorOwnSpellGrams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickExtendSpellGram;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickRecallSpellGram;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSoakZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 自分の魔法式オブジェクトを扱うピース（取得子: 自分の魔法式オブジェクト、作動式: 魔法式回収・魔法式延長、2026-11-11）。
 * <p>
 * プレイヤーをワールドに入れるので、他のテストと同時に動かないようバッチを分ける。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellGramControlGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String NOT_SPELLGRAM = "psitweaks.spellerror.not_spellgram";
    private static final String NOT_OWN = "psitweaks.spellerror.not_own_spellgram";

    private SpellGramControlGameTests() {
    }

    /** 選択子は自分のものだけを置いた順に返す。他人のもの・流星群の夜・消えたものは含めない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_select", timeoutTicks = 40)
    public static void ownSpellGramSelectorListsOwnObjectsInPlacementOrder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "spellgram_select_caster", new Vec3(1.5, 1.0, 1.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "spellgram_select_other", new Vec3(3.5, 1.0, 3.5));
        List<Entity> extra = new ArrayList<>();
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            SpellGramIndex.ofCaster(level, other.getUUID()).forEach(Entity::discard);
            extra.forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
            GameTestPlayers.remove(helper, other);
        };
        try {
            List<SpellGramObject> placed = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                placed.add(placeSoak(caster, 1.0));
                if (i == 0) {
                    placeSoak(other, 1.0);
                    EntityMeteorLineNight night = new EntityMeteorLineNight(level, caster.position(), 3.0);
                    night.configure(caster, AttackRules.Options.DEFAULT);
                    level.addFreshEntity(night);
                    extra.add(night);
                }
            }
            List<Entity> listed = select(caster);
            helper.assertTrue(listed.equals(new ArrayList<Entity>(placed)),
                    "The selector must list only the caster's own zones, oldest first: " + ids(listed) + " vs " + ids(placed));

            placed.get(1).end(SpellGramObject.EndReason.DISPELLED);
            List<Entity> afterEnd = select(caster);
            helper.assertTrue(afterEnd.equals(List.<Entity>of(placed.get(0), placed.get(2))),
                    "An ended object must drop out of the list: " + ids(afterEnd));
            helper.assertTrue(select(other).size() == 1, "The other player must get only their own zone");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** 魔法式回収はリストごと自分のものを消す。他人のものが混ざるとエラーで、何も消さない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_recall", timeoutTicks = 40)
    public static void recallEndsOwnObjectsAndRefusesOthers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "spellgram_recall_caster", new Vec3(1.5, 1.0, 1.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "spellgram_recall_other", new Vec3(3.5, 1.0, 3.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            SpellGramIndex.ofCaster(level, other.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
            GameTestPlayers.remove(helper, other);
        };
        try {
            SpellGramObject first = placeSoak(caster, 1.0);
            SpellGramObject second = placeSoak(caster, 1.0);
            SpellGramObject others = placeSoak(other, 1.0);

            String mixed = recallError(caster, EntityListWrapper.make(List.of(first, others)));
            helper.assertTrue(NOT_OWN.equals(mixed), "Recalling a list with someone else's zone must fail, got " + mixed);
            helper.assertTrue(!first.isEnded() && !others.isEnded(), "A failed recall must end nothing");
            String notSpellGram = recallError(caster, caster);
            helper.assertTrue(NOT_SPELLGRAM.equals(notSpellGram), "Recalling a player must fail, got " + notSpellGram);

            recall(caster, EntityListWrapper.make(new ArrayList<>(select(caster))));
            helper.assertTrue(first.isEnded() && second.isEnded(), "Recall must end every own zone in the list");
            helper.assertTrue(!others.isEnded(), "Recall must not touch someone else's zone");
            helper.assertTrue(select(caster).isEmpty(), "Recalled zones must leave the selector's list");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** 魔法式回収は射程（術者から 32 ブロック）の外にある自分の魔法式オブジェクトも消せる（2026-11-13 ユーザー）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_recall_far", timeoutTicks = 40)
    public static void recallReachesOwnObjectsOutOfRange(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "spellgram_recall_far_caster", new Vec3(1.5, 1.0, 1.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            SpellGramObject zone = placeSoak(caster, 1.0);
            Vec3 far = caster.position().add(0.0, 64.0, 0.0);
            caster.setPos(far.x, far.y, far.z);
            helper.assertTrue(!new SpellContext().setPlayer(caster).isInRadius(zone),
                    "The zone must be out of the caster's range for this test to mean anything");
            recall(caster, zone);
            helper.assertTrue(zone.isEnded(), "Recall must end an own zone out of range");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /**
     * 魔法式延長は耐久値を 置いたときの耐久値 × このピースのコスト ÷ 置いたときのコストだけ戻す。術式で削られた分から先に戻り、
     * 残りは存続時間に足され、耐久値は上限まで（2026-11-14: 領域の上限は「時間」によらず、「時間」を最大にして置いたときの
     * 耐久値の 2 倍）。他人のものは回復できない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_extend", timeoutTicks = 40)
    public static void extendRestoresIntegrityByCostShareUpToTwice(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "spellgram_extend_caster", new Vec3(1.5, 1.0, 1.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "spellgram_extend_other", new Vec3(3.5, 1.0, 3.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            SpellGramIndex.ofCaster(level, other.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
            GameTestPlayers.remove(helper, other);
        };
        try {
            // サイズ 15 の湿潤領域: 面積 225、コスト 265。威力 1（コスト 100）で 置いたときの耐久値 × 100 / 265 戻る。
            SpellGramObject zone = placeSoak(caster, 15.0, null);
            int lifetime = zone.getLifetimeTicks();
            float max = zone.getMaxIntegrity();
            float share = PieceTrickExtendSpellGram.restoreAmount(max, 1.0, 265.0F);
            helper.assertTrue(zone.getPlacementCost() == 265.0F, "The placement cost must be the zone's cost, was " + zone.getPlacementCost());

            // 術式で削られた分から先に戻る（存続時間は変わらない）。
            zone.reduceIntegrity(share * 0.5F);
            int beforeTicks = zone.getRemainingTicks();
            extend(caster, zone, 0.5);
            helper.assertTrue(zone.getWorn() < 1.0E-3F && Math.abs(zone.getRemainingTicks() - beforeTicks) <= 1,
                    "Integrity lost to spells must come back first, worn " + zone.getWorn() + ", ticks " + beforeTicks + " -> " + zone.getRemainingTicks());

            // 削られた分がなければ、戻した分だけ長く残る（耐久値は毎 tick 同じ量ずつ減るので、tick は 戻した量 ÷ 1 tick の減り）。
            float beforeIntegrity = zone.getIntegrity();
            extend(caster, zone, 1.0);
            float expectedTicks = share * lifetime / max;
            int afterOne = zone.getRemainingTicks();
            helper.assertTrue(Math.abs(zone.getIntegrity() - (beforeIntegrity + share)) < share * 0.01F
                            && Math.abs(afterOne - beforeTicks - expectedTicks) <= 1.0F,
                    "Power 1 must restore " + share + " integrity and add about " + expectedTicks + " ticks: "
                            + beforeTicks + " -> " + afterOne + ", integrity " + beforeIntegrity + " -> " + zone.getIntegrity());

            // 上限は「時間」によらず一定: 「時間」を最大（120 秒）にして置いたときの耐久値の 2 倍なので、30 秒の領域では 8 倍。
            float cap = zone.getIntegrityCap();
            helper.assertTrue(Math.abs(cap - max * 8.0F) < max * 1.0E-3F,
                    "The cap must be twice the integrity of the longest Time (8x for 30 s), was " + cap + " of " + max);
            SpellGramObject longest = placeSoak(caster, 15.0, 120.0);
            helper.assertTrue(Math.abs(longest.getIntegrityCap() - cap) < cap * 1.0E-3F
                            && Math.abs(longest.getIntegrityCap() - longest.getMaxIntegrity() * 2.0F) < cap * 1.0E-3F,
                    "The cap must not depend on the Time: " + cap + " vs " + longest.getIntegrityCap());
            extend(caster, zone, 100.0);
            helper.assertTrue(Math.abs(zone.getIntegrity() - cap) < max * 1.0E-3F && Math.abs(zone.getRemainingTicks() - lifetime * 8) <= 1,
                    "Integrity must stop at the cap, was " + zone.getIntegrity() + " of " + cap
                            + ", ticks " + zone.getRemainingTicks() + " of " + lifetime);

            SpellGramObject cheap = placeSoak(caster, 1.0, null);
            helper.assertTrue(PieceTrickExtendSpellGram.restoreAmount(1.0F, 1.0, cheap.getPlacementCost())
                            > PieceTrickExtendSpellGram.restoreAmount(1.0F, 1.0, zone.getPlacementCost()),
                    "A cheaper object must be easier to restore");

            SpellGramObject others = placeSoak(other, 15.0, null);
            int othersBefore = others.getRemainingTicks();
            String error = extendError(caster, others, 1.0);
            helper.assertTrue(NOT_OWN.equals(error), "Restoring someone else's zone must fail, got " + error);
            helper.assertTrue(others.getRemainingTicks() == othersBefore, "A failed restore must not change the zone");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /**
     * 領域の「時間」は存続時間で、置いたときの耐久値とコストも時間に比例する。耐久値の 1 tick あたりの減りは時間によらず同じなので、
     * 長く置いた領域ほど術式解体などに強い。省くと 30 秒、上限は 120 秒。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_time", timeoutTicks = 40)
    public static void zoneTimeScalesLifetimeIntegrityAndCost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "spellgram_time_caster", new Vec3(1.5, 1.0, 1.5));
        Runnable cleanup = () -> {
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
        };
        try {
            SpellGramObject plain = placeSoak(caster, 5.0, null);
            SpellGramObject longer = placeSoak(caster, 5.0, 90.0);
            SpellGramObject capped = placeSoak(caster, 5.0, 500.0);
            helper.assertTrue(plain.getLifetimeTicks() == 30 * 20 && longer.getLifetimeTicks() == 90 * 20
                            && capped.getLifetimeTicks() == PieceTrickZoneBase.MAX_TIME_SECONDS * 20,
                    "Time sets the lifetime (30 s when left out, at most 120 s): " + plain.getLifetimeTicks() + ", "
                            + longer.getLifetimeTicks() + ", " + capped.getLifetimeTicks());
            helper.assertTrue(Math.abs(longer.getMaxIntegrity() - plain.getMaxIntegrity() * 3.0F) < plain.getMaxIntegrity() * 1.0E-3F,
                    "Three times the time must give three times the integrity: " + plain.getMaxIntegrity() + " vs " + longer.getMaxIntegrity());
            helper.assertTrue(plain.getPlacementCost() == 65.0F && longer.getPlacementCost() == 195.0F
                            && PieceTrickZoneBase.timedCost(65, 45.0) == 98,
                    "The cost must grow with the time: " + plain.getPlacementCost() + ", " + longer.getPlacementCost());

            // 1 tick あたりの減りは時間によらず同じ。
            float plainPerTick = plain.getMaxIntegrity() / plain.getLifetimeTicks();
            float longerPerTick = longer.getMaxIntegrity() / longer.getLifetimeTicks();
            helper.assertTrue(Math.abs(plainPerTick - longerPerTick) < plainPerTick * 1.0E-3F,
                    "Integrity must wear away at the same rate: " + plainPerTick + " vs " + longerPerTick);

            // 同じだけ削ると、短い領域は尽き、長い領域は残る。
            float blow = plain.getMaxIntegrity() * 1.5F;
            plain.reduceIntegrity(blow);
            longer.reduceIntegrity(blow);
            helper.assertTrue(plain.isEnded() && !longer.isEnded(), "A longer zone must stand a blow that ends a shorter one");

            // 魔法式延長で戻る耐久値は、時間を変えても同じ（コストと耐久値がどちらも時間に比例するため）。
            float restorePlain = PieceTrickExtendSpellGram.restoreAmount(plain.getMaxIntegrity(), 1.0, plain.getPlacementCost());
            float restoreLonger = PieceTrickExtendSpellGram.restoreAmount(longer.getMaxIntegrity(), 1.0, longer.getPlacementCost());
            helper.assertTrue(Math.abs(restorePlain - restoreLonger) < restorePlain * 1.0E-3F,
                    "The same Power must restore the same integrity whatever the time: " + restorePlain + " vs " + restoreLonger);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /** 耐久値の表示の狙い: 視線の先の核を選び、目をそらすか間にブロックがあれば選ばない（2026-11-12）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_aim", timeoutTicks = 40)
    public static void aimFindsTheCoreInSightOnly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "spellgram_aim_caster", new Vec3(1.5, 1.0, 1.5));
        ServerPlayer viewer = GameTestPlayers.inLevel(helper, "spellgram_aim_viewer", new Vec3(4.5, 1.0, 4.5));
        BlockPos wall = new BlockPos(3, 2, 3);
        Runnable cleanup = () -> {
            helper.setBlock(wall, net.minecraft.world.level.block.Blocks.AIR);
            SpellGramIndex.ofCaster(level, caster.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, caster);
            GameTestPlayers.remove(helper, viewer);
        };
        try {
            SpellGramObject zone = placeSoak(caster, 3.0, null);
            viewer.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, zone.position());
            helper.assertTrue(com.moratan251.psitweaks.common.entities.SpellGram.SpellGramAim.aimedAt(level, viewer, 1.0F) == zone,
                    "Looking at the core must pick the zone");
            viewer.setYRot(viewer.getYRot() + 90.0F);
            viewer.yRotO = viewer.getYRot();
            viewer.setYHeadRot(viewer.getYRot());
            viewer.yHeadRotO = viewer.getYRot();
            helper.assertTrue(com.moratan251.psitweaks.common.entities.SpellGram.SpellGramAim.aimedAt(level, viewer, 1.0F) == null,
                    "Looking away must pick nothing");
            viewer.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, zone.position());
            helper.setBlock(wall, net.minecraft.world.level.block.Blocks.STONE);
            helper.assertTrue(com.moratan251.psitweaks.common.entities.SpellGram.SpellGramAim.aimedAt(level, viewer, 1.0F) == null,
                    "A block in between must hide the core");
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    private static SpellGramObject placeSoak(ServerPlayer caster, double size) throws SpellRuntimeException {
        return placeSoak(caster, size, null);
    }

    /** 行列を省いた湿潤領域を置く。{@code seconds} は「時間」（null なら省く）。 */
    private static SpellGramObject placeSoak(ServerPlayer caster, double size, @Nullable Double seconds) throws SpellRuntimeException {
        List<SpellGramObject> before = SpellGramIndex.ofCaster(caster.level(), caster.getUUID());
        var piece = new PieceTrickSoakZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(PsitweaksSpellParams.SIZE)) {
                    return size;
                }
                return param.name.equals(SpellParam.GENERIC_NAME_TIME) ? seconds : null;
            }
        };
        piece.execute(new SpellContext().setPlayer(caster));
        for (SpellGramObject object : SpellGramIndex.ofCaster(caster.level(), caster.getUUID())) {
            if (object instanceof EntitySoakZone && !before.contains(object)) {
                return object;
            }
        }
        throw new IllegalStateException("The Soak Zone was not placed");
    }

    private static List<Entity> select(ServerPlayer caster) throws SpellRuntimeException {
        Object value = new PieceSelectorOwnSpellGrams(new Spell()).execute(new SpellContext().setPlayer(caster));
        List<Entity> listed = new ArrayList<>();
        for (Entity entity : (EntityListWrapper) value) {
            listed.add(entity);
        }
        return listed;
    }

    private static void recall(ServerPlayer caster, Object target) throws SpellRuntimeException {
        var piece = new PieceTrickRecallSpellGram(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return target;
            }
        };
        piece.execute(new SpellContext().setPlayer(caster));
    }

    private static String recallError(ServerPlayer caster, Object target) {
        try {
            recall(caster, target);
            return "no error";
        } catch (SpellRuntimeException e) {
            return e.getMessage();
        }
    }

    private static void extend(ServerPlayer caster, Entity target, double power) throws SpellRuntimeException {
        var piece = new PieceTrickExtendSpellGram(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(SpellParam.GENERIC_NAME_TARGET) ? target : power;
            }
        };
        piece.execute(new SpellContext().setPlayer(caster));
    }

    private static String extendError(ServerPlayer caster, Entity target, double power) {
        try {
            extend(caster, target, power);
            return "no error";
        } catch (SpellRuntimeException e) {
            return e.getMessage();
        }
    }

    private static List<Integer> ids(List<? extends Entity> entities) {
        return entities.stream().map(Entity::getId).toList();
    }
}
