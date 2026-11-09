package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityFlareCircle;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityIceCircle;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramCircle;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.spell.trick.entity.PieceTrickAddMotion;
import vazkii.psi.common.spell.trick.entity.PieceTrickBlink;

import java.util.List;

/**
 * 魔法式オブジェクト（{@link com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject}）に共通の扱い:
 * ふつうの攻撃では壊れず、保存せず、耐久値は受け口でだけ減る。Psi の運動追加・瞬間移動では誰も動かせない。
 * <p>
 * プレイヤーをワールドに入れるので、他のテストと同時に動かないようバッチを分ける。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellGramGameTests {
    private static final String TEMPLATE = "connector_empty";

    private SpellGramGameTests() {
    }

    /**
     * 円は剣などの攻撃で壊れず、押されず、保存しない。耐久値は術式で削られ（減らす側の事象干渉力を掛ける）、削った分だけ
     * 残りの存続時間も短くなり、尽きると消える。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_integrity", timeoutTicks = 100)
    public static void circlesIgnoreOrdinaryAttacksAndEndWhenTheirIntegrityRunsOut(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sg_int_caster", new Vec3(0.5, 1.0, 0.5));
        EntityIceCircle circle = place(helper, new EntityIceCircle(level), caster, new Vec3(2.5, 1.0, 2.5));
        try {
            circle.setMaxIntegrity(10.0F);
            helper.assertFalse(circle.hurt(level.damageSources().playerAttack(caster), 1_000.0F) || circle.isRemoved(),
                    "An ordinary attack broke the circle");
            helper.assertTrue(near(circle.getIntegrity(), 10.0F), "An ordinary attack wore the circle down");
            helper.assertFalse(circle.shouldBeSaved() || circle.isPickable() || circle.isPushable(),
                    "The circle should not be saved, picked or pushed");
            // 事象干渉力 2 の術者が 2 だけ削ると 4 減る。
            caster.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(2.0D);
            float removed = SpellGramIntegrity.reduce(circle, 2.0F, caster);
            helper.assertTrue(near(removed, 4.0F) && !circle.isRemoved() && near(circle.getIntegrity(), 6.0F),
                    "The reducer's interference did not scale the wear: removed " + removed + ", integrity "
                            + circle.getIntegrity());
            circle.reduceIntegrity(6.0F);
            helper.assertTrue(circle.isRemoved(), "The circle stayed after its integrity ran out");
            helper.succeed();
        } finally {
            circle.discard();
            GameTestPlayers.remove(helper, caster);
        }
    }

    /**
     * 円の耐久値は時間とともに一定の割合で減り、存続時間の終わりにちょうど尽きて消える。術式で半分削った円は、残りを時間で
     * 使い切ったところ（存続時間の半ば）で消える。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_decay", timeoutTicks = 100)
    public static void integrityDrainsOverTheLifetime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sg_decay_caster", new Vec3(0.5, 1.0, 0.5));
        EntityFlareCircle circle = place(helper, new EntityFlareCircle(level), caster, new Vec3(2.5, 1.0, 2.5));
        EntityFlareCircle worn = place(helper, new EntityFlareCircle(level), caster, new Vec3(2.5, 1.0, 4.5));
        for (EntityFlareCircle placed : List.of(circle, worn)) {
            placed.setLifetimeTicks(20);
            placed.setMaxIntegrity(10.0F);
        }
        worn.reduceIntegrity(5.0F);
        Runnable cleanup = () -> {
            circle.discard();
            worn.discard();
            GameTestPlayers.remove(helper, caster);
        };
        helper.runAfterDelay(8, () -> {
            float integrity = circle.getIntegrity();
            boolean alive = !circle.isRemoved() && !worn.isRemoved();
            if (!alive || integrity < 5.0F || integrity > 7.0F) {
                cleanup.run();
                helper.fail("At 8 of 20 ticks the circles should stand and the untouched one hold about 6: " + integrity
                        + (alive ? "" : " (one is gone)"));
            }
        });
        helper.runAfterDelay(14, () -> {
            boolean circleAlive = !circle.isRemoved();
            boolean wornGone = worn.isRemoved();
            if (!circleAlive || !wornGone) {
                cleanup.run();
                helper.fail("At 14 of 20 ticks only the worn circle should be gone: untouched alive " + circleAlive
                        + ", worn gone " + wornGone);
            }
        });
        helper.runAfterDelay(25, () -> {
            boolean removed = circle.isRemoved();
            cleanup.run();
            helper.assertTrue(removed, "The circle stayed after its lifetime ran out");
            helper.succeed();
        });
    }

    /** 術者を引き直せなくなった（ログアウトした）円は、次の tick に消える。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_caster_gone", timeoutTicks = 100)
    public static void circlesEndWhenTheirCasterIsGone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sg_gone_caster", new Vec3(0.5, 1.0, 0.5));
        EntityFlareCircle circle = place(helper, new EntityFlareCircle(level), caster, new Vec3(2.5, 1.0, 2.5));
        helper.runAfterDelay(2, () -> {
            try {
                helper.assertFalse(circle.isRemoved(), "The circle ended while its caster was online");
                GameTestPlayers.remove(helper, caster);
            } catch (RuntimeException e) {
                circle.discard();
                GameTestPlayers.remove(helper, caster);
                throw e;
            }
        });
        helper.runAfterDelay(4, () -> {
            boolean removed = circle.isRemoved();
            circle.discard();
            helper.assertTrue(removed, "The circle stayed after its caster was gone");
            helper.succeed();
        });
    }

    /** 作動式: 運動追加(単)・瞬間移動(単) では、他人の円も自分の円も動かない（動かす仕組みは専用の作動式として保留中）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_move", timeoutTicks = 100)
    public static void psiMovementTricksDoNotMoveSpellGrams(GameTestHelper helper) throws SpellRuntimeException {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "sg_move_owner", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "sg_move_other", new Vec3(1.5, 1.0, 0.5));
        EntityFlareCircle circle = place(helper, new EntityFlareCircle(level), owner, new Vec3(2.5, 1.0, 2.5));
        Runnable cleanup = () -> {
            circle.discard();
            List.of(owner, other).forEach(player -> GameTestPlayers.remove(helper, player));
        };
        Vec3 at = circle.position();
        try {
            for (ServerPlayer mover : List.of(other, owner)) {
                SpellContext context = new SpellContext().setPlayer(mover);
                PieceTrickBlink.blink(context, circle, 2.0);
                PieceTrickAddMotion.addMotion(context, circle, new Vector3(1.0, 0.0, 0.0), 1.0);
                helper.assertTrue(circle.position().equals(at), mover.getScoreboardName() + "'s Blink moved the circle");
            }
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw e;
        }
        helper.runAfterDelay(5, () -> {
            Vec3 now = circle.position();
            Vec3 motion = circle.getDeltaMovement();
            cleanup.run();
            helper.assertTrue(now.equals(at) && motion.lengthSqr() < 1.0E-7, "Add Motion moved the circle");
            helper.succeed();
        });
    }

    private static boolean near(float value, float expected) {
        return Math.abs(value - expected) < 1.0E-3F;
    }

    /** 動かない（存続 10 秒、ダメージなし）円を {@code relative} に置く。 */
    private static <T extends SpellGramCircle> T place(GameTestHelper helper, T circle, ServerPlayer caster, Vec3 relative) {
        Vec3 at = helper.absoluteVec(relative);
        circle.setPos(at.x, at.y + 0.01, at.z);
        circle.setCaster(caster);
        circle.setLifetimeTicks(200);
        helper.getLevel().addFreshEntity(circle);
        return circle;
    }
}
