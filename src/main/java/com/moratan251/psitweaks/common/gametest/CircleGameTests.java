package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.OptionalSpellPieceCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityFlareCircle;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityIceCircle;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramCircle;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityInterferenceZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySoakZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickCircleFollow;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickFlareCircle;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSpellGramCircleBase;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * サークル（ファイアサークル・アイスサークル）の決まりと、作動式: サークル追従。
 * <p>
 * プレイヤーをワールドに入れるので、他のテストと同時に動かないようバッチを分ける。テンプレート（5×4×5）の外には何も置かない。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CircleGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final float TOUGH = 200.0F;
    private static final String NOT_CIRCLE = "psitweaks.spellerror.not_circle";
    private static final String NOT_OWN = "psitweaks.spellerror.not_own_spellgram";

    private CircleGameTests() {
    }

    // ---- 作動式: サークル追従 ----

    /** 術者を追従すると、毎 tick 術者の位置に移り、近くの相手に当たる。放すとその場に残り、術者が動いても動かない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_follow_caster", timeoutTicks = 60)
    public static void followsTheCasterHitsAndStaysWhenReleased(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_follow_caster", new Vec3(1.5, 1.0, 1.5));
        Husk husk = bootedHusk(helper, new Vec3(1.5, 1.0, 2.5));
        // 初めの位置からは届かない（半径 1.2、相手までおよそ 2.2）。
        EntityIceCircle circle = place(helper, new EntityIceCircle(level), caster, new Vec3(3.5, 1.0, 3.5), 1.2F, 2.0F, 2, 1.0F);
        Runnable cleanup = () -> cleanup(helper, List.of(caster), husk);
        float start = husk.getHealth();
        try {
            follow(caster, circle, caster);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        Vec3[] releasedAt = new Vec3[1];
        helper.runAfterDelay(8, guarded(cleanup, () -> {
            helper.assertTrue(circle.isEntityFollowMode() && circle.position().equals(caster.position()),
                    "The circle must sit at the caster's position: " + circle.position() + " vs " + caster.position());
            helper.assertTrue(husk.getHealth() < start, "The following circle must hit the creature next to the caster");
            try {
                follow(caster, circle, null);
            } catch (SpellRuntimeException e) {
                throw new RuntimeException(e);
            }
            releasedAt[0] = circle.position();
            Vec3 away = helper.absoluteVec(new Vec3(3.5, 1.0, 0.5));
            caster.moveTo(away.x, away.y, away.z);
        }));
        helper.runAfterDelay(14, guarded(cleanup, () -> {
            helper.assertTrue(!circle.isEntityFollowMode() && circle.position().equals(releasedAt[0]) && !circle.isRemoved(),
                    "A released circle must stay where it was: " + circle.position() + " vs " + releasedAt[0]);
            cleanup.run();
            helper.succeed();
        }));
    }

    /** 追従で動くと、動いた距離 1 ブロックにつき存続時間 1 tick 分の耐久値を使う（最初に飛ぶ分も含む）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_follow_wear", timeoutTicks = 60)
    public static void followingWearsIntegrityByDistance(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_wear_caster", new Vec3(3.5, 1.0, 3.5));
        EntityFlareCircle circle = place(helper, new EntityFlareCircle(level), caster, new Vec3(0.5, 1.0, 0.5), 1.0F, 0.0F, 10, 1.0F);
        // 存続 1000 tick・耐久値 1000 なので、1 ブロック動くと 1 減る。
        circle.setLifetimeTicks(1000);
        circle.setMaxIntegrity(1000.0F);
        Runnable cleanup = () -> cleanup(helper, List.of(caster));
        double jump = circle.position().distanceTo(caster.position());
        try {
            follow(caster, circle, caster);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(3, guarded(cleanup, () -> {
            helper.assertTrue(Math.abs(circle.getWorn() - jump) < 0.01,
                    "The first jump must use its distance: worn " + circle.getWorn() + ", jumped " + jump);
            Vec3 next = helper.absoluteVec(new Vec3(3.5, 1.0, 0.5));
            caster.moveTo(next.x, next.y, next.z);
        }));
        helper.runAfterDelay(6, guarded(cleanup, () -> {
            helper.assertTrue(Math.abs(circle.getWorn() - (jump + 3.0)) < 0.01,
                    "Following the caster 3 blocks must use 3 more: worn " + circle.getWorn() + ", expected " + (jump + 3.0));
            cleanup.run();
            helper.succeed();
        }));
    }

    /**
     * 対象と追従先の確かめ: 他人のサークル・領域・混ざったリストはエラーで何も変えない。術者以外の生き物・防具立て・魔法式
     * オブジェクト・ほかの生き物が乗ったボート・射程の外は追従先にできない。対象を省くと一番新しい自分のサークル、自分のサークルが
     * なければエラー。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_follow_errors", timeoutTicks = 40)
    public static void followChecksItsTargets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_err_caster", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "circle_err_other", new Vec3(4.5, 1.0, 4.5));
        List<Entity> extra = new ArrayList<>();
        Runnable cleanup = () -> cleanup(helper, List.of(caster, other), extra.toArray(Entity[]::new));
        try {
            EntityIceCircle own = place(helper, new EntityIceCircle(level), caster, new Vec3(1.5, 1.0, 1.5), 1.0F, 0.0F, 10, 1.0F);
            EntityIceCircle others = place(helper, new EntityIceCircle(level), other, new Vec3(3.5, 1.0, 3.5), 1.0F, 0.0F, 10, 1.0F);
            EntitySoakZone zone = new EntitySoakZone(level);
            placeZone(zone, ZoneArea.cube(helper.absolutePos(new BlockPos(2, 1, 2)), 1), caster, 1.0);
            Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 0.5));
            husk.setNoAi(true);
            ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, new Vec3(0.5, 1.0, 2.5));
            Boat ridden = boat(helper, new Vec3(2.5, 1.0, 4.5), false);
            Husk rider = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 4.5));
            rider.setNoAi(true);
            rider.startRiding(ridden, true);
            Boat far = boat(helper, new Vec3(2.5, 41.0, 2.5), true);
            Boat free = boat(helper, new Vec3(4.5, 1.0, 0.5), false);
            extra.addAll(List.of(husk, stand, ridden, rider, far, free));

            helper.assertTrue(NOT_OWN.equals(followError(caster, others, null)),
                    "Someone else's circle must be refused, got " + followError(caster, others, null));
            String mixed = followError(caster, EntityListWrapper.make(List.of(own, others)), null);
            helper.assertTrue(NOT_OWN.equals(mixed) && !own.isEntityFollowMode(),
                    "A list with someone else's circle must fail and change nothing, got " + mixed);
            helper.assertTrue(NOT_CIRCLE.equals(followError(caster, zone, null)),
                    "A zone must be refused as not a circle, got " + followError(caster, zone, null));
            for (Entity invalid : List.of(husk, stand, others)) {
                String error = followError(caster, own, invalid);
                helper.assertTrue(PieceTrickCircleFollow.ERROR_INVALID_FOLLOW_TARGET.equals(error),
                        invalid.getType() + " must not be a follow target, got " + error);
            }
            helper.assertTrue(PieceTrickCircleFollow.ERROR_FOLLOW_TARGET_RIDDEN.equals(followError(caster, own, ridden)),
                    "A boat ridden by a creature must be refused, got " + followError(caster, own, ridden));
            helper.assertTrue(SpellRuntimeException.OUTSIDE_RADIUS.equals(followError(caster, own, far)),
                    "A follow target out of range must be refused, got " + followError(caster, own, far));
            helper.assertTrue(!own.isEntityFollowMode(), "A refused follow must change nothing");

            // 対象を省くと、一番新しい自分のサークル。
            EntityIceCircle newest = place(helper, new EntityIceCircle(level), caster, new Vec3(1.5, 1.0, 3.5), 1.0F, 0.0F, 10, 1.0F);
            follow(caster, null, free);
            helper.assertTrue(newest.isEntityFollowMode() && newest.getFollowTargetEntity() == free && !own.isEntityFollowMode(),
                    "Leaving out the target must pick the newest own circle");
            follow(caster, newest, null);
            helper.assertTrue(!newest.isEntityFollowMode(), "Leaving out the follow target must let the circle go");

            others.discard();
            helper.assertTrue(SpellRuntimeException.NULL_TARGET.equals(followError(other, null, null)),
                    "Without any circle, leaving out the target must fail, got " + followError(other, null, null));
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /**
     * 追従先のボートに術者が乗っても追従は続くが、ほかの生き物が乗ると放す。追従先が消えても放す。放したサークルはその場に残る。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_follow_boat", timeoutTicks = 60)
    public static void boatsRiddenByOthersOrRemovedAreLetGo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_boat_caster", new Vec3(0.5, 1.0, 0.5));
        Boat ridden = boat(helper, new Vec3(1.5, 1.0, 3.5), false);
        Boat removed = boat(helper, new Vec3(3.5, 1.0, 1.5), false);
        Husk rider = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(4.5, 1.0, 4.5));
        rider.setNoAi(true);
        EntityIceCircle onRidden = place(helper, new EntityIceCircle(level), caster, new Vec3(2.5, 1.0, 2.5), 1.0F, 0.0F, 10, 1.0F);
        EntityIceCircle onRemoved = place(helper, new EntityIceCircle(level), caster, new Vec3(2.5, 1.0, 2.5), 1.0F, 0.0F, 10, 1.0F);
        Runnable cleanup = () -> cleanup(helper, List.of(caster), ridden, removed, rider);
        try {
            follow(caster, onRidden, ridden);
            follow(caster, onRemoved, removed);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        Vec3[] removedAt = new Vec3[1];
        helper.runAfterDelay(3, guarded(cleanup, () -> {
            helper.assertTrue(onRidden.isEntityFollowMode() && onRidden.position().distanceTo(ridden.position()) < 1.0E-3
                    && onRemoved.isEntityFollowMode(), "Both circles must follow their boats");
            caster.startRiding(ridden, true);
            removedAt[0] = onRemoved.position();
            removed.discard();
        }));
        helper.runAfterDelay(6, guarded(cleanup, () -> {
            helper.assertTrue(onRidden.isEntityFollowMode(), "The caster riding the boat must not stop the following");
            helper.assertTrue(!onRemoved.isEntityFollowMode() && onRemoved.position().equals(removedAt[0]),
                    "A circle whose boat is gone must stay where it was");
            rider.startRiding(ridden, true);
        }));
        helper.runAfterDelay(9, guarded(cleanup, () -> {
            helper.assertTrue(!onRidden.isEntityFollowMode() && !onRidden.isRemoved(),
                    "A creature other than the caster riding the boat must make the circle let go");
            cleanup.run();
            helper.succeed();
        }));
    }

    /** 術者以外の追従先が術者から 32 ブロックより遠くへ行くと放し、サークルは最後の場所に残る。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_follow_leash", timeoutTicks = 60)
    public static void followTargetsTooFarFromTheCasterAreLetGo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_leash_caster", new Vec3(0.5, 1.0, 0.5));
        Boat boat = boat(helper, new Vec3(2.5, 1.0, 2.5), true);
        EntityIceCircle circle = place(helper, new EntityIceCircle(level), caster, new Vec3(2.5, 1.0, 2.5), 1.0F, 0.0F, 10, 1.0F);
        circle.setMaxIntegrity(1.0E6F);
        Runnable cleanup = () -> cleanup(helper, List.of(caster), boat);
        double baseY = boat.getY();
        try {
            follow(caster, circle, boat);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(3, guarded(cleanup, () -> boat.setPos(boat.getX(), baseY + 30.0, boat.getZ())));
        helper.runAfterDelay(6, guarded(cleanup, () -> {
            helper.assertTrue(circle.isEntityFollowMode() && Math.abs(circle.getY() - boat.getY()) < 1.0E-6,
                    "Within 32 blocks of the caster the circle must keep following: " + circle.getY() + " vs " + boat.getY());
            boat.setPos(boat.getX(), baseY + 40.0, boat.getZ());
        }));
        helper.runAfterDelay(9, guarded(cleanup, () -> {
            helper.assertTrue(!circle.isEntityFollowMode() && Math.abs(circle.getY() - (baseY + 30.0)) < 1.0E-6,
                    "Beyond 32 blocks the circle must let go and stay: y " + circle.getY());
            cleanup.run();
            helper.succeed();
        }));
    }

    // ---- サークルの決まり ----

    /**
     * 同じ術者の同じ種類のサークルが重なったところでは、一番強いもの（威力の高いもの）だけが当たる。種類の違うサークルと、他人の
     * サークルはそれぞれ当たる。相手は濡らして燃えず、革のブーツで凍えないので、減った体力は当たったダメージの合計。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_overlap", timeoutTicks = 60)
    public static void overlappingCirclesOfOneKindHitOnceWithTheStrongest(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_overlap_caster", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "circle_overlap_other", new Vec3(4.5, 1.0, 4.5));
        Husk husk = bootedHusk(helper, new Vec3(2.5, 1.0, 2.5));
        CombatStatuses.soak(husk, 1000);
        Vec3 at = new Vec3(2.5, 1.0, 2.5);
        place(helper, new EntityIceCircle(level), caster, at, 2.0F, 1.0F, 10, 1.0F);
        place(helper, new EntityIceCircle(level), caster, at, 2.0F, 3.0F, 10, 3.0F);
        place(helper, new EntityFlareCircle(level), caster, at, 2.0F, 2.0F, 10, 1.0F);
        place(helper, new EntityIceCircle(level), other, at, 2.0F, 4.0F, 10, 5.0F);
        Runnable cleanup = () -> cleanup(helper, List.of(caster, other), husk);
        float start = husk.getHealth();
        helper.runAfterDelay(15, guarded(cleanup, () -> {
            float lost = start - husk.getHealth();
            // 自分のアイスサークルは強いほうの 3 だけ、ファイアサークル 2、他人のアイスサークル 4。
            // ハスクの防具 3（素の 2 と革のブーツの 1）で、それぞれ 2.82、1.84、3.84 に減る。
            helper.assertTrue(Math.abs(lost - 8.5F) < 1.0E-3F,
                    "One hit of each kind and caster, the strongest of own Ice Circles, must take 8.5 after armor, took " + lost);
            helper.assertTrue(husk.getRemainingFireTicks() <= 0, "The wet creature must not catch fire");
            cleanup.run();
            helper.succeed();
        }));
    }

    /**
     * 中心が、勝てない他人の領域干渉の中にあるサークルは、当たらない（消えず、時間は進む）。干渉力の低い他人の領域干渉と、自分の
     * 領域干渉では止まらない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_interference", timeoutTicks = 60)
    public static void interferenceZonesThatWinSuppressCircles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_intf_caster", new Vec3(2.5, 1.0, 1.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "circle_intf_other", new Vec3(2.5, 1.0, 3.0));
        Husk underStrong = bootedHusk(helper, new Vec3(0.5, 1.0, 0.5));
        Husk underWeak = bootedHusk(helper, new Vec3(4.5, 1.0, 0.5));
        Husk underOwn = bootedHusk(helper, new Vec3(2.5, 1.0, 4.5));
        Runnable cleanup = () -> cleanup(helper, List.of(caster, other), underStrong, underWeak, underOwn);
        EntityIceCircle suppressed;
        try {
            suppressed = place(helper, new EntityIceCircle(level), caster, new Vec3(0.5, 1.0, 0.5), 1.0F, 2.0F, 5, 1.0F);
            EntityIceCircle weak = place(helper, new EntityIceCircle(level), caster, new Vec3(4.5, 1.0, 0.5), 1.0F, 2.0F, 5, 1.0F);
            EntityIceCircle own = place(helper, new EntityIceCircle(level), caster, new Vec3(2.5, 1.0, 4.5), 1.0F, 2.0F, 5, 1.0F);
            for (EntityIceCircle circle : List.of(suppressed, weak, own)) {
                circle.setPlacedInterference(1.0);
            }
            // 他人の強い領域干渉: 1 ×（1 + 威力 1 × 5%）= 1.05 > 1。弱いもの: 0.5。自分のもの: 強いが自分の側。
            EntityInterferenceZone strongZone = new EntityInterferenceZone(level);
            strongZone.configurePower(1.0F);
            placeZone(strongZone, ZoneArea.cube(helper.absolutePos(new BlockPos(0, 1, 0)), 1), other, 1.0);
            placeZone(new EntityInterferenceZone(level), ZoneArea.cube(helper.absolutePos(new BlockPos(4, 1, 0)), 1), other, 0.5);
            EntityInterferenceZone ownZone = new EntityInterferenceZone(level);
            ownZone.configurePower(10.0F);
            placeZone(ownZone, ZoneArea.cube(helper.absolutePos(new BlockPos(2, 1, 4)), 1), caster, 5.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        float strongStart = underStrong.getHealth();
        float weakStart = underWeak.getHealth();
        float ownStart = underOwn.getHealth();
        EntityIceCircle stopped = suppressed;
        int lifetime = stopped.getLifetimeTicks();
        helper.runAfterDelay(15, guarded(cleanup, () -> {
            helper.assertTrue(stopped.isSuppressed() && underStrong.getHealth() == strongStart,
                    "A circle inside a stronger foreign Interference Zone must not hit");
            helper.assertTrue(!stopped.isRemoved() && stopped.getRemainingTicks() < lifetime,
                    "A suppressed circle must stay while its time runs on");
            helper.assertTrue(underWeak.getHealth() < weakStart, "A weaker foreign Interference Zone must not stop the circle");
            helper.assertTrue(underOwn.getHealth() < ownStart, "The caster's own Interference Zone must not stop the circle");
            cleanup.run();
            helper.succeed();
        }));
    }

    /** ファイアサークルは濡れた相手を燃やさないが、ダメージは与える。乾いた相手は燃やす。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_fire_wet", timeoutTicks = 60)
    public static void fireCirclesDamageButDoNotIgniteWetTargets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_wet_caster", new Vec3(0.5, 1.0, 0.5));
        Husk wet = GameTestPlayers.tough(frozenHusk(helper, new Vec3(2.0, 1.0, 2.5)), TOUGH);
        Husk dry = GameTestPlayers.tough(frozenHusk(helper, new Vec3(3.0, 1.0, 2.5)), TOUGH);
        CombatStatuses.soak(wet, 1000);
        place(helper, new EntityFlareCircle(level), caster, new Vec3(2.5, 1.0, 2.5), 2.0F, 1.0F, 5, 1.0F);
        Runnable cleanup = () -> cleanup(helper, List.of(caster), wet, dry);
        float wetStart = wet.getHealth();
        helper.runAfterDelay(8, guarded(cleanup, () -> {
            helper.assertTrue(wet.getHealth() < wetStart && wet.getRemainingFireTicks() <= 0 && CombatStatuses.isSoaked(wet),
                    "A wet creature must take the damage without catching fire");
            helper.assertTrue(dry.getRemainingFireTicks() > 0, "A dry creature must catch fire");
            cleanup.run();
            helper.succeed();
        }));
    }

    /**
     * アイスサークルの氷渡り: 円の中の、中心の段と 1 つ下の段の、真上が空気でエンティティのいない水源は薄氷になる。上が塞がった
     * 水・防具立てのいる水・加熱領域の中の水はそのまま。もとからある薄氷の年齢は 0 に戻る。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_frost", timeoutTicks = 60)
    public static void iceCirclesFrostOpenWater(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_frost_caster", new Vec3(4.5, 2.0, 4.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "circle_frost_other", new Vec3(0.5, 2.0, 4.5));
        stoneLayer(helper);
        BlockPos open = new BlockPos(1, 1, 1);
        BlockPos covered = new BlockPos(2, 1, 1);
        BlockPos occupied = new BlockPos(3, 1, 1);
        BlockPos heated = new BlockPos(1, 1, 2);
        BlockPos frosted = new BlockPos(1, 1, 3);
        BlockPos upper = new BlockPos(3, 2, 3);
        for (BlockPos water : List.of(open, covered, occupied, heated)) {
            helper.setBlock(water, Blocks.WATER);
        }
        helper.setBlock(covered.above(), Blocks.STONE);
        helper.setBlock(frosted, Blocks.FROSTED_ICE.defaultBlockState().setValue(FrostedIceBlock.AGE, 2));
        // 中心の段（y = 2）の水: 下と四方を石で囲む。
        for (BlockPos wall : List.of(upper.west(), upper.east(), upper.north(), upper.south())) {
            helper.setBlock(wall, Blocks.STONE);
        }
        helper.setBlock(upper, Blocks.WATER);
        ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, new Vec3(3.5, 1.0, 1.5));
        Runnable cleanup = () -> cleanup(helper, List.of(caster, other), stand);
        try {
            placeZone(new EntityHeatZone(level), ZoneArea.cube(helper.absolutePos(heated), 1), other, 1.0);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        place(helper, new EntityIceCircle(level), caster, new Vec3(2.5, 2.0, 2.5), 1.5F, 0.0F, 10, 1.0F);
        helper.runAfterDelay(15, guarded(cleanup, () -> {
            helper.assertTrue(isFrostedYoung(helper.getBlockState(open)), "Open water below the circle must turn into Frosted Ice");
            helper.assertTrue(isFrostedYoung(helper.getBlockState(upper)), "Open water at the circle's level must turn into Frosted Ice");
            helper.assertTrue(helper.getBlockState(covered).is(Blocks.WATER), "Water with a block above must stay water");
            helper.assertTrue(helper.getBlockState(occupied).is(Blocks.WATER), "Water with an armor stand in it must stay water");
            helper.assertTrue(helper.getBlockState(heated).is(Blocks.WATER), "Water inside a Heat Zone must stay water");
            helper.assertTrue(isFrostedYoung(helper.getBlockState(frosted)), "Frosted Ice inside the circle must be kept at age 0");
            cleanup.run();
            helper.succeed();
        }));
    }

    /**
     * 水に浮いているボートを追従しているアイスサークルは薄氷を作らない（漕いでいるボートを閉じ込めない）。放すと作る。
     * 内側の 3×3 を水にした池で、ボートは隅に寄せて浮かべ、反対の隅の水はボートに触れない。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_frost_boat", timeoutTicks = 80)
    public static void iceCirclesLeaveTheWaterUnderAFloatingBoat(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_frost_boat_caster", new Vec3(4.5, 2.0, 4.5));
        stoneLayer(helper);
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.WATER);
            }
        }
        BlockPos water = new BlockPos(3, 1, 3);
        Boat boat = boat(helper, new Vec3(1.75, 1.6, 1.75), false);
        EntityIceCircle circle = place(helper, new EntityIceCircle(level), caster, new Vec3(1.75, 1.6, 1.75), 3.0F, 0.0F, 10, 1.0F);
        Runnable cleanup = () -> cleanup(helper, List.of(caster), boat);
        try {
            follow(caster, circle, boat);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(15, guarded(cleanup, () -> {
            helper.assertTrue(boat.isInWater(), "The boat must float in the pool for this test");
            helper.assertTrue(circle.isEntityFollowMode() && helper.getBlockState(water).is(Blocks.WATER),
                    "A circle following a boat on water must not freeze the water");
            try {
                follow(caster, circle, null);
            } catch (SpellRuntimeException e) {
                throw new RuntimeException(e);
            }
        }));
        helper.runAfterDelay(30, guarded(cleanup, () -> {
            helper.assertTrue(isFrostedYoung(helper.getBlockState(water)), "Once let go, the circle must freeze the water");
            cleanup.run();
            helper.succeed();
        }));
    }

    /** 氷の上のボートを追従しているアイスサークルは、周りの水面を薄氷にする（氷上ボートが滑り続けられる）。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_frost_ice_boat", timeoutTicks = 60)
    public static void iceCirclesFreezeTheWaterAroundABoatOnIce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_ice_boat_caster", new Vec3(4.5, 2.0, 4.5));
        stoneLayer(helper);
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.ICE);
        List<BlockPos> waters = List.of(new BlockPos(1, 1, 1), new BlockPos(3, 1, 3));
        waters.forEach(pos -> helper.setBlock(pos, Blocks.WATER));
        Boat boat = boat(helper, new Vec3(2.5, 2.0, 2.5), false);
        EntityIceCircle circle = place(helper, new EntityIceCircle(level), caster, new Vec3(2.5, 2.0, 2.5), 1.5F, 0.0F, 10, 1.0F);
        Runnable cleanup = () -> cleanup(helper, List.of(caster), boat);
        try {
            follow(caster, circle, boat);
        } catch (RuntimeException | SpellRuntimeException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        helper.runAfterDelay(15, guarded(cleanup, () -> {
            helper.assertTrue(!boat.isInWater() && circle.isEntityFollowMode(), "The circle must follow a boat that is not in water");
            for (BlockPos water : waters) {
                helper.assertTrue(isFrostedYoung(helper.getBlockState(water)),
                        "Water around a boat on ice must turn into Frosted Ice: " + water);
            }
            cleanup.run();
            helper.succeed();
        }));
    }

    /** 「時間」は存続時間・耐久値・コストに比例する（コストは最低 50）。耐久値の上限は時間によらず、最大の時間の耐久値の 2 倍。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_time", timeoutTicks = 40)
    public static void timeScalesLifetimeIntegrityAndCost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_time_caster", new Vec3(0.5, 1.0, 0.5));
        Runnable cleanup = () -> cleanup(helper, List.of(caster));
        try {
            helper.assertTrue(PieceTrickSpellGramCircleBase.costFor(1.0, 60.0) == 3200
                            && PieceTrickSpellGramCircleBase.costFor(1.0, 30.0) == 1600
                            && PieceTrickSpellGramCircleBase.costFor(1.0, 120.0) == 6400
                            && PieceTrickSpellGramCircleBase.costFor(1.0, 500.0) == 6400
                            && PieceTrickSpellGramCircleBase.costFor(0.5, 1.0) == 50,
                    "The cost must be 1600 × (1 + Power) × Time ÷ 60, at least 50, with the Time at most 120");
            for (double power : new double[]{0.5, 1.0, 2.0, 4.0, 10.0}) {
                float damage = PieceTrickSpellGramCircleBase.damageFor(power);
                int cost = PieceTrickSpellGramCircleBase.costFor(power, 60.0);
                helper.assertTrue(damage == 1.0F + (float) power && cost == Math.round(damage * 1600.0F),
                        "The damage must be 1 + Power and the cost must follow it at Power " + power + ": " + damage + ", " + cost);
            }
            int[] halfTime = stats(fireCircle(helper, 1.0, 30.0));
            int[] noTime = stats(fireCircle(helper, 1.0, null));
            helper.assertTrue(halfTime[0] == 300 && halfTime[1] == 1600 && noTime[0] == 300 && noTime[1] == 3200,
                    "Compiling must give potency 300 and the timed cost: 30 s " + halfTime[0] + "/" + halfTime[1]
                            + ", left out " + noTime[0] + "/" + noTime[1]);

            float base = SpellGramIntegrity.initial(50.0F, caster, 1.0);
            SpellGramCircle half = cast(caster, fireCircle(helper, 1.0, 30.0));
            SpellGramCircle full = cast(caster, fireCircle(helper, 1.0, null));
            helper.assertTrue(half.getLifetimeTicks() == 600 && full.getLifetimeTicks() == 1200,
                    "The lifetime must follow the Time: " + half.getLifetimeTicks() + ", " + full.getLifetimeTicks());
            helper.assertTrue(near(half.getMaxIntegrity(), base * 0.5F) && near(full.getMaxIntegrity(), base),
                    "The integrity must follow the Time: " + half.getMaxIntegrity() + ", " + full.getMaxIntegrity() + " (base " + base + ")");
            helper.assertTrue(near(half.getIntegrityCap(), base * 4.0F) && near(full.getIntegrityCap(), base * 4.0F),
                    "The integrity cap must be twice the integrity of the longest Time, whatever the Time");
            helper.assertTrue(near(half.getPlacementCost(), 1600.0F) && near(full.getPlacementCost(), 3200.0F)
                            && near(half.getPower(), 1.0F),
                    "The placement cost must be the real cost: " + half.getPlacementCost() + ", " + full.getPlacementCost());
        } catch (RuntimeException | SpellRuntimeException | SpellCompilationException e) {
            cleanup.run();
            throw new RuntimeException(e);
        }
        cleanup.run();
        helper.succeed();
    }

    /**
     * 作動式: サークル追従は新しい ID で登録され、魔法式オブジェクトの操作と一緒に解禁される。規模 40・コスト 150。入力は対象と
     * 追従先で、どちらも省ける（モードは持たない）。古い作動式: 魔法式追従の ID は登録されず、読み込むと取り除かれる。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_follow_piece", timeoutTicks = 40)
    public static void circleFollowPieceIsRegistered(GameTestHelper helper) throws SpellCompilationException {
        var id = Psitweaks.location("trick_circle_follow");
        helper.assertTrue(PsiAPI.SPELL_PIECE_REGISTRY.get(id) != null, "Trick: Circle Follow must be registered");
        helper.assertTrue(PsiAPI.getPieceGroup(helper.getLevel().registryAccess(), id).orElseThrow().key().location()
                .equals(Psitweaks.location("spellgram_control")), "Trick: Circle Follow must be unlocked with the SpellGram controls");
        var piece = (PieceTrickCircleFollow) PsiAPI.SPELL_PIECE_REGISTRY.get(id).create(new Spell());
        int[] stats = stats(piece);
        helper.assertTrue(stats[0] == 40 && stats[1] == 150, "Potency and cost must be 40 and 150: " + stats[0] + "/" + stats[1]);
        SpellParam<?> targetParam = piece.params.get(SpellParam.GENERIC_NAME_TARGET);
        SpellParam<?> followParam = piece.params.get(PieceTrickCircleFollow.PARAM_FOLLOW_TARGET);
        helper.assertTrue(piece.params.size() == 2 && targetParam != null && followParam != null
                        && targetParam.canDisable && followParam.canDisable,
                "The inputs must be the target and the follow target, both optional");
        helper.assertFalse(piece instanceof com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable,
                "Trick: Circle Follow must not have modes");

        var oldId = Psitweaks.location("trick_set_spellgram_follow_target");
        helper.assertFalse(PsiAPI.SPELL_PIECE_REGISTRY.containsKey(oldId), "The old follow piece must stay unregistered");
        CompoundTag old = new CompoundTag();
        old.putString("key", oldId.toString());
        helper.assertTrue(OptionalSpellPieceCompat.isRemovalMarker(SpellPiece.createFromNBT(new Spell(), old)),
                "Spells with the old follow piece must load with it taken out");
        helper.succeed();
    }

    // ---- 道具 ----

    /** 動かない円を {@code relative} に置く（存続 400 tick、耐久値 1000）。 */
    /**
     * サークル・領域・ビームの当て方（{@link AttackRules#harmWithoutCooldown}）は相手に無敵時間を残さない。あとから当てた
     * 単発の攻撃は満額入り、単発の攻撃どうしの無敵時間はそのまま効く。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_periodic_no_cooldown")
    public static void periodicHitsLeaveNoCooldownForOtherAttacks(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "no_cooldown_caster", new Vec3(0.5, 1.0, 0.5));
        Husk husk = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 2.5)), TOUGH);
        husk.setNoAi(true);
        husk.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        husk.setHealth(TOUGH);
        Runnable cleanup = () -> cleanup(helper, List.of(caster), husk);
        guarded(cleanup, () -> {
            DamageSource source = caster.damageSources().indirectMagic(caster, caster);
            float start = husk.getHealth();
            AttackRules.harmWithoutCooldown(caster, husk, AttackRules.Options.DEFAULT, source, 6.0F);
            float afterPeriodic = husk.getHealth();
            int cooldown = husk.invulnerableTime;
            AttackRules.harm(caster, husk, AttackRules.Options.DEFAULT, source, 29.0F);
            float afterSpell = husk.getHealth();
            AttackRules.harmWithoutCooldown(caster, husk, AttackRules.Options.DEFAULT, source, 6.0F);
            float afterSecondPeriodic = husk.getHealth();
            AttackRules.harm(caster, husk, AttackRules.Options.DEFAULT, source, 29.0F);
            float afterRepeat = husk.getHealth();
            cleanup.run();
            helper.assertTrue(Math.abs(start - afterPeriodic - 6.0F) < 1.0E-3F && cooldown == 0,
                    "A periodic hit must deal 6 and leave no cooldown, dealt " + (start - afterPeriodic) + ", cooldown " + cooldown);
            helper.assertTrue(Math.abs(afterPeriodic - afterSpell - 29.0F) < 1.0E-3F,
                    "A spell right after a periodic hit must deal its full 29, dealt " + (afterPeriodic - afterSpell));
            helper.assertTrue(Math.abs(afterSpell - afterSecondPeriodic - 6.0F) < 1.0E-3F,
                    "A periodic hit inside the spell's cooldown must still deal 6, dealt " + (afterSpell - afterSecondPeriodic));
            helper.assertTrue(Math.abs(afterSecondPeriodic - afterRepeat) < 1.0E-3F,
                    "The same spell again inside its own cooldown must deal nothing, dealt " + (afterSecondPeriodic - afterRepeat));
            helper.succeed();
        }).run();
    }

    /** サークルが当たったあと、相手に無敵時間が残らない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_circle_no_cooldown", timeoutTicks = 60)
    public static void circleHitsLeaveNoCooldown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "circle_no_cooldown_caster", new Vec3(0.5, 1.0, 0.5));
        Husk husk = GameTestPlayers.tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(2.5, 1.0, 2.5)), TOUGH);
        husk.setNoAi(true);
        CombatStatuses.soak(husk, 1000);
        place(helper, new EntityFlareCircle(level), caster, new Vec3(2.5, 1.0, 2.5), 2.0F, 2.0F, 10, 1.0F);
        Runnable cleanup = () -> cleanup(helper, List.of(caster), husk);
        float start = husk.getHealth();
        int[] cooldownAfterHit = {-1};
        helper.onEachTick(() -> {
            if (cooldownAfterHit[0] < 0 && husk.getHealth() < start) {
                cooldownAfterHit[0] = husk.invulnerableTime;
            }
        });
        helper.runAfterDelay(25, guarded(cleanup, () -> {
            cleanup.run();
            helper.assertTrue(cooldownAfterHit[0] == 0, "A circle hit must leave no hurt cooldown, left " + cooldownAfterHit[0]);
            helper.succeed();
        }));
    }

    private static <T extends SpellGramCircle> T place(GameTestHelper helper, T circle, ServerPlayer caster, Vec3 relative,
                                                       float radius, float damage, int interval, float power) {
        Vec3 at = helper.absoluteVec(relative);
        circle.setPos(at.x, at.y + 0.01, at.z);
        circle.setCaster(caster);
        circle.setPower(power);
        circle.setSafeToPlayers(false);
        circle.setLifetimeTicks(400);
        circle.setMaxIntegrity(1000.0F);
        circle.configureAreaDamage(radius, damage, interval);
        helper.getLevel().addFreshEntity(circle);
        return circle;
    }

    private static void placeZone(SpellGramZone zone, ZoneArea area, ServerPlayer caster, double interference) {
        zone.configureZone(area, 0xFFFFFF);
        zone.setMachineSpeed(SpellGramZone.machineSpeedFor(interference));
        zone.setPlacement(caster.getUUID(), interference);
        zone.setCaster(caster);
        zone.setLifetimeTicks(1000);
        zone.setMaxIntegrity(1.0E6F);
        caster.level().addFreshEntity(zone);
    }

    private static Boat boat(GameTestHelper helper, Vec3 relative, boolean floating) {
        Vec3 at = helper.absoluteVec(relative);
        Boat boat = new Boat(helper.getLevel(), at.x, at.y, at.z);
        boat.setNoGravity(floating);
        helper.getLevel().addFreshEntity(boat);
        return boat;
    }

    private static Husk frozenHusk(GameTestHelper helper, Vec3 relative) {
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, relative);
        husk.setNoAi(true);
        return husk;
    }

    /** 動かず、革のブーツで凍えない（アイスサークルのダメージだけを数えられる）丈夫なハスク。 */
    private static Husk bootedHusk(GameTestHelper helper, Vec3 relative) {
        Husk husk = GameTestPlayers.tough(frozenHusk(helper, relative), TOUGH);
        husk.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        return husk;
    }

    /** y = 0・1 の段をすべて石にする（y = 1 に置く水を囲む）。 */
    private static void stoneLayer(GameTestHelper helper) {
        for (int y = 0; y <= 1; y++) {
            for (int x = 0; x < 5; x++) {
                for (int z = 0; z < 5; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        }
    }

    private static boolean isFrostedYoung(BlockState state) {
        return state.is(Blocks.FROSTED_ICE) && state.getValue(FrostedIceBlock.AGE) == 0;
    }

    /** 作動式: サークル追従を唱える。{@code destination} が null なら放す。 */
    private static void follow(ServerPlayer caster, @Nullable Object target, @Nullable Entity destination)
            throws SpellRuntimeException {
        var piece = new PieceTrickCircleFollow(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                if (param.name.equals(SpellParam.GENERIC_NAME_TARGET)) {
                    return target;
                }
                return param.name.equals(PieceTrickCircleFollow.PARAM_FOLLOW_TARGET) ? destination : null;
            }
        };
        piece.execute(new SpellContext().setPlayer(caster));
    }

    private static String followError(ServerPlayer caster, @Nullable Object target, @Nullable Entity destination) {
        try {
            follow(caster, target, destination);
            return "no error";
        } catch (SpellRuntimeException e) {
            return e.getMessage();
        }
    }

    /** 位置 (2.5, 1, 2.5)・威力・時間（null なら省く）を、唱えたときも組んだときも読むファイアサークル。 */
    private static PieceTrickFlareCircle fireCircle(GameTestHelper helper, double power, @Nullable Double seconds) {
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
        return new PieceTrickFlareCircle(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return value(param);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T getParamEvaluation(SpellParam<?> param) {
                return (T) value(param);
            }

            private Object value(SpellParam<?> param) {
                return switch (param.name) {
                    case SpellParam.GENERIC_NAME_POSITION -> new Vector3(at.x, at.y, at.z);
                    case SpellParam.GENERIC_NAME_POWER -> Double.valueOf(power);
                    case SpellParam.GENERIC_NAME_TIME -> seconds;
                    default -> null;
                };
            }
        };
    }

    /** {@code piece} を唱え、置いた円を返す。 */
    private static SpellGramCircle cast(ServerPlayer caster, PieceTrickFlareCircle piece) throws SpellRuntimeException {
        List<SpellGramObject> before = SpellGramIndex.ofCaster(caster.level(), caster.getUUID());
        piece.execute(new SpellContext().setPlayer(caster));
        for (SpellGramObject object : SpellGramIndex.ofCaster(caster.level(), caster.getUUID())) {
            if (object instanceof SpellGramCircle circle && !before.contains(object)) {
                return circle;
            }
        }
        throw new IllegalStateException("The circle was not placed");
    }

    private static int[] stats(SpellPiece piece) throws SpellCompilationException {
        var meta = new SpellMetadata();
        piece.addToMetadata(meta);
        return new int[]{meta.getStat(EnumSpellStat.POTENCY), meta.getStat(EnumSpellStat.COST)};
    }

    private static boolean near(float value, float expected) {
        return Math.abs(value - expected) < 1.0E-3F * Math.max(1.0F, Math.abs(expected));
    }

    /** 術者たちの魔法式オブジェクトと、ほかのエンティティを消し、術者たちを外す。 */
    private static void cleanup(GameTestHelper helper, List<ServerPlayer> casters, Entity... extra) {
        for (ServerPlayer caster : casters) {
            if (caster.isPassenger()) {
                caster.stopRiding();
            }
            SpellGramIndex.ofCaster(helper.getLevel(), caster.getUUID()).forEach(Entity::discard);
        }
        for (Entity entity : extra) {
            entity.discard();
        }
        casters.forEach(caster -> GameTestPlayers.remove(helper, caster));
    }

    /** 失敗したら後始末をしてから、その失敗を GameTest へ伝える。 */
    private static Runnable guarded(Runnable cleanup, Runnable step) {
        return () -> {
            try {
                step.run();
            } catch (RuntimeException | AssertionError e) {
                cleanup.run();
                throw e;
            }
        };
    }
}
