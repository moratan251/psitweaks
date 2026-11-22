package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.effects.EffectTorpor;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityDecelerationZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneProjectiles;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickDecelerationZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickZoneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moratan251.psitweaks.common.gametest.ZoneCasting.castOrClean;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.cleanup;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.paramValue;
import static com.moratan251.psitweaks.common.gametest.ZoneCasting.zonesOf;

/**
 * 減速領域（作動式: 減速領域）。範囲はたいてい、テンプレートの (1, 2, 2) を中心にした一辺 3（x 0〜2、y 1〜3、z 1〜3）で、
 * テンプレートの x 3〜4 は範囲の外。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DecelerationZoneGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final BlockPos CENTER = new BlockPos(1, 2, 2);

    private DecelerationZoneGameTests() {
    }

    /**
     * 鈍化のレベルは 中の威力 × 5 で、移動速度・飛ぶ速さは 中の威力 × 5%、攻撃速度は 中の威力 × 3% 下がる。プレイヤー以外の
     * 速さは毎 tick 1 − 中の威力 × 0.025 倍、飛び道具は 1 − 中の威力 × 0.04 倍。照準の表示は移動の下げ幅と飛び道具の倍率。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_deceleration_numbers")
    public static void decelerationNumbersFollowPower(GameTestHelper helper) {
        helper.assertTrue(EntityDecelerationZone.amplifierFor(5.0F) == 24, "Power 1 must give Torpor level 25");
        helper.assertTrue(EntityDecelerationZone.amplifierFor(14.0F) == 69, "Power 10 must give Torpor level 70");
        assertClose(helper, EntityDecelerationZone.mobDragFor(14.0F), 0.65, "mob drag at Power 10");
        assertClose(helper, EntityDecelerationZone.projectileDragFor(5.0F), 0.8, "projectile drag at Power 1");
        assertClose(helper, EntityDecelerationZone.projectileDragFor(14.0F), 0.44, "projectile drag at Power 10");
        Map<Holder<Attribute>, Double> amounts = new HashMap<>();
        PsitweaksEffects.TORPOR.value().createModifiers(69, (attribute, modifier) -> amounts.put(attribute, modifier.amount()));
        assertClose(helper, amounts.getOrDefault(Attributes.MOVEMENT_SPEED, 0.0), -0.70, "movement speed at level 70");
        assertClose(helper, amounts.getOrDefault(Attributes.FLYING_SPEED, 0.0), -0.70, "flying speed at level 70");
        assertClose(helper, amounts.getOrDefault(Attributes.ATTACK_SPEED, 0.0), -0.42, "attack speed at level 70");
        assertClose(helper, EffectTorpor.MOVEMENT_PER_LEVEL * 25, -0.25, "movement speed at level 25");
        helper.succeed();
    }

    /** 威力 1: 中の生き物の移動速度は 75% になる。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_deceleration_power1", timeoutTicks = 100)
    public static void decelerationPower1SlowsBeings(GameTestHelper helper) {
        slowsBeingsTest(helper, 1.0, 0.75, "25", "0.80");
    }

    /** 威力 10: 中の生き物の移動速度は 30% になる。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_deceleration_power10", timeoutTicks = 100)
    public static void decelerationPower10SlowsBeings(GameTestHelper helper) {
        slowsBeingsTest(helper, 10.0, 0.30, "70", "0.44");
    }

    private static void slowsBeingsTest(GameTestHelper helper, double power, double speedShare, String shownMove,
                                        String shownProjectile) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_decel_" + (int) power, new Vec3(1.5, 2.0, 2.5));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(0.5, 2.0, 1.5));
        double base = husk.getAttributeValue(Attributes.MOVEMENT_SPEED);
        Runnable cleanup = () -> {
            husk.discard();
            cleanup(helper, caster);
        };
        castOrClean(caster, deceleration(helper), 3.0, power, cleanup);
        helper.runAfterDelay(5, () -> {
            double speed = husk.getAttributeValue(Attributes.MOVEMENT_SPEED);
            boolean casterSlowed = caster.hasEffect(PsitweaksEffects.TORPOR);
            List<Object> shown = List.of(((TranslatableContents) zonesOf(helper, caster, EntityDecelerationZone.class).get(0)
                    .hudLines().get(0).getContents()).getArgs());
            cleanup.run();
            helper.assertTrue(Math.abs(speed - base * speedShare) < 1.0E-6,
                    "Movement speed must be " + speedShare + " of " + base + ", got " + speed);
            helper.assertFalse(casterSlowed, "The Deceleration Zone must spare its own caster");
            helper.assertTrue(shown.equals(List.of(shownMove, shownProjectile)), "The HUD must show " + shownMove + " and "
                    + shownProjectile + ", got " + shown);
            helper.succeed();
        });
    }

    /**
     * 飛び道具: 持ち主のない矢は範囲の中で毎 tick 遅くなり、外の矢と術者の矢は変わらない（どれも重力なし、ゆっくり動かす）。
     * 魔法の弾は遅くしない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_deceleration_arrows", timeoutTicks = 100)
    public static void decelerationSlowsOthersArrowsInsideOnly(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_decel_arrows", new Vec3(3.5, 1.0, 0.5));
        Arrow inside = arrow(helper, new Vec3(0.5, 2.5, 1.5), null);
        Arrow outside = arrow(helper, new Vec3(3.5, 2.5, 1.5), null);
        Arrow own = arrow(helper, new Vec3(1.5, 2.5, 1.5), caster);
        Runnable cleanup = () -> {
            inside.discard();
            outside.discard();
            own.discard();
            cleanup(helper, caster);
        };
        castOrClean(caster, deceleration(helper), 3.0, 10.0, cleanup);
        EntityDecelerationZone zone = zonesOf(helper, caster, EntityDecelerationZone.class).get(0);
        var magic = PsitweaksEntities.TUNNELER_ARROW.get().create(helper.getLevel());
        boolean magicSlowed = magic != null && zone.slowsProjectile(magic);
        boolean magicType = ZoneProjectiles.isSpellProjectileType(PsitweaksEntities.TUNNELER_ARROW.get())
                && !ZoneProjectiles.isSpellProjectileType(EntityType.ARROW);
        helper.runAfterDelay(5, () -> {
            double insideSpeed = inside.getDeltaMovement().length();
            double outsideSpeed = outside.getDeltaMovement().length();
            double ownSpeed = own.getDeltaMovement().length();
            cleanup.run();
            helper.assertTrue(outsideSpeed > 0.04, "The arrow outside must keep its speed, got " + outsideSpeed);
            helper.assertTrue(ownSpeed > 0.04, "The caster's own arrow must keep its speed, got " + ownSpeed);
            helper.assertTrue(insideSpeed < outsideSpeed * 0.2, "The arrow inside must slow down, got " + insideSpeed
                    + " against " + outsideSpeed);
            helper.assertTrue(magicType && !magicSlowed, "Magic projectiles must not be slowed");
            helper.succeed();
        });
    }

    /** ボス（#c:bosses）には効かない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_deceleration_bosses")
    public static void decelerationSparesBosses(GameTestHelper helper) {
        var wither = EntityType.WITHER.create(helper.getLevel());
        var dragon = EntityType.ENDER_DRAGON.create(helper.getLevel());
        var husk = EntityType.HUSK.create(helper.getLevel());
        helper.assertTrue(wither != null && EntityDecelerationZone.isExempt(wither), "The Wither must be spared");
        helper.assertTrue(dragon != null && EntityDecelerationZone.isExempt(dragon), "The Ender Dragon must be spared");
        helper.assertTrue(husk != null && !EntityDecelerationZone.isExempt(husk), "A husk must be slowed");
        helper.succeed();
    }

    /**
     * プレイヤー以外の生き物は、毎 tick 速さも下がる（威力 10 で 0.65 倍）。重力のないハスク 2 体に同じ横の速さを与え、範囲の中の
     * ものだけが外のものより遅くなる。範囲を出ると鈍化は 15 tick で切れ、移動速度が戻る。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_zone_deceleration_mobs", timeoutTicks = 100)
    public static void decelerationDragsMobsAndWearsOffOutside(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "zone_decel_mobs", new Vec3(3.5, 1.0, 4.5));
        Husk inside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(1.5, 2.0, 2.5));
        Husk outside = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 2.0, 2.5));
        double base = inside.getAttributeValue(Attributes.MOVEMENT_SPEED);
        for (Husk husk : List.of(inside, outside)) {
            husk.setNoGravity(true);
        }
        Runnable cleanup = () -> {
            inside.discard();
            outside.discard();
            cleanup(helper, caster);
        };
        castOrClean(caster, deceleration(helper), 3.0, 10.0, cleanup);
        helper.runAfterDelay(3, () -> {
            inside.setDeltaMovement(0.2, 0.0, 0.0);
            outside.setDeltaMovement(0.2, 0.0, 0.0);
        });
        helper.runAfterDelay(5, () -> {
            double insideSpeed = Math.abs(inside.getDeltaMovement().x);
            double outsideSpeed = Math.abs(outside.getDeltaMovement().x);
            MobEffectInstance torpor = inside.getEffect(PsitweaksEffects.TORPOR);
            if (outsideSpeed <= 0.0 || insideSpeed >= outsideSpeed * 0.8 || torpor == null) {
                cleanup.run();
                helper.fail("The husk inside must be dragged and get Torpor: " + insideSpeed + " against " + outsideSpeed
                        + ", Torpor " + torpor);
                return;
            }
            Vec3 away = helper.absoluteVec(new Vec3(4.5, 2.0, 0.5));
            inside.teleportTo(away.x, away.y, away.z);
        });
        helper.runAfterDelay(25, () -> {
            boolean stillSlowed = inside.hasEffect(PsitweaksEffects.TORPOR);
            double speed = inside.getAttributeValue(Attributes.MOVEMENT_SPEED);
            cleanup.run();
            helper.assertFalse(stillSlowed, "Torpor must wear off after leaving");
            helper.assertTrue(Math.abs(speed - base) < 1.0E-6, "Movement speed must come back to " + base + ", got " + speed);
            helper.succeed();
        });
    }

    private static Arrow arrow(GameTestHelper helper, Vec3 relative, ServerPlayer owner) {
        Arrow arrow = new Arrow(EntityType.ARROW, helper.getLevel());
        Vec3 at = helper.absoluteVec(relative);
        arrow.setPos(at.x, at.y, at.z);
        arrow.setNoGravity(true);
        arrow.setDeltaMovement(0.0, 0.0, 0.05);
        if (owner != null) {
            arrow.setOwner(owner);
        }
        helper.getLevel().addFreshEntity(arrow);
        return arrow;
    }

    private static void assertClose(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-6, what + ": expected " + expected + ", got " + actual);
    }

    private static PieceTrickZoneBase<?> deceleration(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(CENTER);
        return new PieceTrickDecelerationZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return paramValue(param, center);
            }
        };
    }
}
