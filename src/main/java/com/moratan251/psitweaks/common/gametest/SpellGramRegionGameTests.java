package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.region.EntitySpellGramRegion;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramCapacity;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramRegionIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramRegions;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramWetness;
import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 領域型の魔法式オブジェクト（試作）: 命中判定、性質の反応、湿潤、回収、置ける数、術者がいなくなったとき。
 * 作動式を通さず、エンティティを直接置いて確かめる。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellGramRegionGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final float TOUGH = 100.0F;
    /** 入った直後のプレイヤーの無敵（60 tick）が切れるまで。 */
    private static final int AFTER_SPAWN_INVULNERABILITY = 61;
    /** 置いてから最初の効き目（{@link EntitySpellGramRegion#PULSE_INTERVAL_TICKS}）が出るまで。 */
    private static final int AFTER_FIRST_PULSE = EntitySpellGramRegion.PULSE_INTERVAL_TICKS + 3;

    /** 灼熱の領域は、怪物と（PvP が有効なら）他のプレイヤーを焼き、術者のペットとチームメイトは焼かない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_targets_spellgram", timeoutTicks = 200)
    public static void scorchingRegionFollowsTheSharedRule(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean pvp = level.getServer().isPvpAllowed();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sgr_a_caster", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer mate = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "sgr_a_mate", new Vec3(3.0, 1.0, 3.0)), TOUGH);
        ServerPlayer stranger = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "sgr_a_other", new Vec3(2.0, 1.0, 3.0)), TOUGH);
        PlayerTeam team = GameTestPlayers.team(helper, "sgr_a_team", true, caster.getScoreboardName(), mate.getScoreboardName());
        Wolf ownPet = GameTestPlayers.tough(GameTestPlayers.pet(helper, caster.getUUID(), new Vec3(2.0, 1.0, 2.0)), TOUGH);
        Husk husk = GameTestPlayers.tough(frozenHusk(helper, new Vec3(3.0, 1.0, 2.0)), TOUGH);
        List<EntitySpellGramRegion> placed = new ArrayList<>();
        Runnable cleanup = () -> {
            placed.forEach(EntitySpellGramRegion::discard);
            level.getServer().setPvpAllowed(pvp);
            GameTestPlayers.removeTeam(helper, team);
            List.of(caster, mate, stranger).forEach(player -> GameTestPlayers.remove(helper, player));
            helper.killAllEntitiesOfClass(Wolf.class);
        };
        float petHealth = ownPet.getHealth();
        float mateHealth = mate.getHealth();
        float strangerHealth = stranger.getHealth();
        float huskHealth = husk.getHealth();
        helper.runAfterDelay(AFTER_SPAWN_INVULNERABILITY, guarded(cleanup, () -> {
            level.getServer().setPvpAllowed(true);
            EntitySpellGramRegion region = region(helper, caster, new Vec3(2.5, 1.0, 2.5), 3.0);
            region.addProperty(SpellGramProperty.FIRE, 1.0, 1.0, 10.0F);
            level.addFreshEntity(region);
            placed.add(region);
        }));
        helper.runAfterDelay(AFTER_SPAWN_INVULNERABILITY + AFTER_FIRST_PULSE, guarded(cleanup, () -> {
            helper.assertTrue(husk.getHealth() < huskHealth, "The Scorching region did not burn the monster");
            helper.assertTrue(stranger.getHealth() < strangerHealth, "The Scorching region spared another player with PvP on");
            helper.assertTrue(ownPet.getHealth() == petHealth, "The Scorching region burned its caster's pet");
            helper.assertTrue(mate.getHealth() == mateHealth, "The Scorching region burned a team mate with friendly fire on");
            cleanup.run();
            helper.succeed();
        }));
    }

    /** 灼熱と凍結は打ち消し合い、打ち消した分だけ湿潤になる。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram")
    public static void scorchingAndFreezingTurnIntoWetness(GameTestHelper helper) {
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sgr_b_caster", new Vec3(0.5, 1.0, 0.5));
        EntitySpellGramRegion region = region(helper, caster, new Vec3(2.5, 1.0, 2.5), 3.0);
        try {
            region.addProperty(SpellGramProperty.FIRE, 2.0, 1.0, 20.0F);
            region.addProperty(SpellGramProperty.ICE, 0.5, 1.0, 5.0F);
            assertPower(helper, region, SpellGramProperty.FIRE, 1.5, "after a little Freezing");
            assertPower(helper, region, SpellGramProperty.WET, 0.5, "after a little Freezing");
            helper.assertTrue(!region.hasProperty(SpellGramProperty.ICE), "Freezing stayed next to a stronger Scorching");

            region.addProperty(SpellGramProperty.ICE, 2.0, 1.0, 20.0F);
            helper.assertTrue(!region.hasProperty(SpellGramProperty.FIRE), "Scorching stayed after a stronger Freezing");
            assertPower(helper, region, SpellGramProperty.ICE, 0.5, "after a stronger Freezing");
            assertPower(helper, region, SpellGramProperty.WET, 2.0, "after a stronger Freezing");
            helper.assertTrue(region.getMaxIntegrity() == 46.0F, "Integrity did not add up: " + region.getMaxIntegrity());
        } finally {
            region.discard();
            GameTestPlayers.remove(helper, caster);
        }
        helper.succeed();
    }

    /** 湿潤の領域は、中の生き物の火を消し、出てからもしばらく濡れたままにする。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram", timeoutTicks = 100)
    public static void wetRegionSoaksAndPutsOutFire(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sgr_c_caster", new Vec3(0.5, 1.0, 0.5));
        Husk husk = GameTestPlayers.tough(frozenHusk(helper, new Vec3(2.5, 1.0, 2.5)), TOUGH);
        husk.setRemainingFireTicks(200);
        EntitySpellGramRegion region = region(helper, caster, new Vec3(2.5, 1.0, 2.5), 3.0);
        region.addProperty(SpellGramProperty.WET, 1.0, 1.0, 10.0F);
        level.addFreshEntity(region);
        Runnable cleanup = () -> {
            region.discard();
            GameTestPlayers.remove(helper, caster);
        };
        helper.runAfterDelay(AFTER_FIRST_PULSE, guarded(cleanup, () -> {
            helper.assertTrue(!husk.isOnFire(), "The Wet region did not put out the fire");
            helper.assertTrue(SpellGramWetness.isWet(husk), "The Wet region did not soak the monster");
            region.discard();
        }));
        helper.runAfterDelay(AFTER_FIRST_PULSE + 20, guarded(cleanup, () -> {
            helper.assertTrue(SpellGramWetness.isWet(husk), "The monster dried at once after the region ended");
            cleanup.run();
            helper.succeed();
        }));
    }

    /** 回収は自分の領域型だけを解き、置ける数を超えると古いものから解ける。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram")
    public static void recallAndCountLimitOnlyTouchTheirOwnRegions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sgr_d_caster", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer other = GameTestPlayers.inLevel(helper, "sgr_d_other", new Vec3(4.5, 1.0, 4.5));
        List<EntitySpellGramRegion> placed = new ArrayList<>();
        try {
            int max = SpellGramCapacity.maxRegions();
            for (int i = 0; i <= max; i++) {
                SpellGramCapacity.reserve(new SpellContext().setPlayer(caster), 0, 0, null);
                EntitySpellGramRegion region = region(helper, caster, new Vec3(1.5 + i * 0.1, 1.0, 1.5), 2.0);
                level.addFreshEntity(region);
                placed.add(region);
            }
            EntitySpellGramRegion others = region(helper, other, new Vec3(4.5, 1.0, 4.5), 2.0);
            level.addFreshEntity(others);
            placed.add(others);

            helper.assertTrue(placed.get(0).isRemoved(), "The oldest region stayed past the count limit");
            helper.assertTrue(SpellGramRegionIndex.objectsOf(CasterIds.idOf(caster)).size() == max,
                    "The caster did not keep exactly the count limit");

            int recalled = SpellGramRegions.recallAll(CasterIds.idOf(caster));
            helper.assertTrue(recalled == max, "Recall ended " + recalled + " regions instead of " + max);
            helper.assertTrue(SpellGramRegionIndex.objectsOf(CasterIds.idOf(caster)).isEmpty(), "A region of the caster stayed");
            helper.assertTrue(!others.isRemoved(), "Recall ended another caster's region");
        } catch (SpellRuntimeException e) {
            throw new IllegalStateException("Reserving a region failed", e);
        } finally {
            placed.forEach(EntitySpellGramRegion::discard);
            GameTestPlayers.remove(helper, caster);
            GameTestPlayers.remove(helper, other);
        }
        helper.succeed();
    }

    /** 術者がいなくなると、領域型は解ける。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram", timeoutTicks = 100)
    public static void regionEndsWhenTheCasterIsGone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "sgr_e_caster", new Vec3(0.5, 1.0, 0.5));
        EntitySpellGramRegion region = region(helper, caster, new Vec3(2.5, 1.0, 2.5), 3.0);
        level.addFreshEntity(region);
        GameTestPlayers.remove(helper, caster);
        helper.runAfterDelay(25, () -> {
            boolean ended = region.isRemoved();
            region.discard();
            helper.assertTrue(ended, "The region stayed after its caster left");
            helper.succeed();
        });
    }

    private static EntitySpellGramRegion region(GameTestHelper helper, ServerPlayer caster, Vec3 relative, double radius) {
        EntitySpellGramRegion region = new EntitySpellGramRegion(helper.getLevel());
        Vec3 at = helper.absoluteVec(relative);
        region.setPos(at.x, at.y + 0.01, at.z);
        region.configure(caster, AttackRules.Options.of(false), radius, 1.0F, 0xFFFFFF);
        return region;
    }

    private static void assertPower(GameTestHelper helper, EntitySpellGramRegion region, SpellGramProperty property,
                                    double expected, String when) {
        double actual = region.getPropertyPower(property);
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-6,
                property + " power was " + actual + " instead of " + expected + " " + when);
    }

    private static Husk frozenHusk(GameTestHelper helper, Vec3 relative) {
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, relative);
        husk.setNoAi(true);
        return husk;
    }

    /** 失敗したら後始末をしてから、その失敗を GameTest へ伝える。 */
    private static Runnable guarded(Runnable cleanup, Runnable step) {
        return () -> {
            try {
                step.run();
            } catch (RuntimeException | Error e) {
                cleanup.run();
                throw e;
            }
        };
    }
}
