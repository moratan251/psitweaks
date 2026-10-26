package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.caster.AutonomousCasterHost;
import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.entities.EntityBlazeBall;
import com.moratan251.psitweaks.common.entities.EntityPhononMaserBeam;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityFlareCircle;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickBurst;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGuillotine;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 攻撃術式の命中判定（{@link CombatSpellHelper#canHarm}）。術者側（本人・ペット・同じチームとチームメイトのペット）と
 * 防具立てには当てず、プレイヤーは PvP と受ける側のチームで決まる。自律詠唱機は所有者のチームの一員として扱う。
 * <p>
 * プレイヤーをワールドに入れ、サーバーの PvP を切り替えるテストは、他のテストと同時に動かないようバッチを分ける。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AttackTargetGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final float TOUGH = 100.0F;
    /** 入った直後のプレイヤーの無敵（60 tick）が切れるまで。 */
    private static final int AFTER_SPAWN_INVULNERABILITY = 61;

    // ---- 判定の表 ----

    /** 手で詠唱した攻撃: 自分・自分のペット・チームメイト（フレンドリーファイアに関係なく）・その飼いペット・防具立てには当てない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_targets_rules")
    public static void handCastAttacksFollowTheSharedRule(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean pvp = level.getServer().isPvpAllowed();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "tgt_hand", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer mate = GameTestPlayers.inLevel(helper, "tgt_hand_mate", new Vec3(1.5, 1.0, 0.5));
        ServerPlayer stranger = GameTestPlayers.inLevel(helper, "tgt_hand_other", new Vec3(2.5, 1.0, 0.5));
        PlayerTeam team = GameTestPlayers.team(helper, "tgt_hand_team", false, caster.getScoreboardName(), mate.getScoreboardName());
        try {
            Wolf ownPet = GameTestPlayers.pet(helper, caster.getUUID(), new Vec3(0.5, 1.0, 2.5));
            Wolf matePet = GameTestPlayers.pet(helper, mate.getUUID(), new Vec3(1.5, 1.0, 2.5));
            Wolf strangerPet = GameTestPlayers.pet(helper, stranger.getUUID(), new Vec3(2.5, 1.0, 2.5));
            ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, new Vec3(3.5, 1.0, 2.5));
            Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 3.5));
            husk.setNoAi(true);

            level.getServer().setPvpAllowed(true);
            for (boolean friendlyFire : new boolean[]{false, true}) {
                team.setAllowFriendlyFire(friendlyFire);
                String when = " (friendly fire " + friendlyFire + ")";
                helper.assertTrue(!CombatSpellHelper.canHarm(caster, caster, false), "The caster was a target" + when);
                helper.assertTrue(!CombatSpellHelper.canHarm(caster, ownPet, false), "The caster's pet was a target" + when);
                helper.assertTrue(!CombatSpellHelper.canHarm(caster, mate, false), "A team mate was a target" + when);
                helper.assertTrue(!CombatSpellHelper.canHarm(caster, matePet, false), "A team mate's pet was a target" + when);
                helper.assertTrue(CombatSpellHelper.canHarm(caster, stranger, false), "Another player was spared with PvP on" + when);
                helper.assertTrue(CombatSpellHelper.canHarm(caster, strangerPet, false), "Another player's pet was spared" + when);
                helper.assertTrue(!CombatSpellHelper.canHarm(caster, stand, false), "An armor stand was a target" + when);
                helper.assertTrue(CombatSpellHelper.canHarm(caster, husk, false), "A monster was spared" + when);
                helper.assertTrue(CombatSpellHelper.isAutoTarget(caster, stranger, false, true)
                                && !CombatSpellHelper.isAutoTarget(caster, mate, false, true),
                        "Indiscriminate auto-aim did not follow the rule" + when);
            }
            // PvP が無効なら「プレイヤーに安全」が付く（SpellSafetyUtils）。
            level.getServer().setPvpAllowed(false);
            helper.assertTrue(!CombatSpellHelper.canHarm(caster, stranger, true) && !CombatSpellHelper.canHarm(caster, stranger, false),
                    "Another player was a target with PvP off");
            helper.assertTrue(CombatSpellHelper.canHarm(caster, strangerPet, true), "Another player's pet was spared with PvP off");
            helper.succeed();
        } finally {
            level.getServer().setPvpAllowed(pvp);
            GameTestPlayers.removeTeam(helper, team);
            List.of(caster, mate, stranger).forEach(player -> GameTestPlayers.remove(helper, player));
        }
    }

    /**
     * 自律詠唱機: 所有者のチームの一員として、所有者・所有者のペット・所有者のチームメイトとそのペットには当てない。
     * 他のプレイヤーには PvP が有効なら当てる。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_targets_rules")
    public static void machineAttacksCountAsTheOwnersTeam(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean pvp = level.getServer().isPvpAllowed();
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "tgt_owner", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer mate = GameTestPlayers.inLevel(helper, "tgt_owner_mate", new Vec3(1.5, 1.0, 0.5));
        ServerPlayer stranger = GameTestPlayers.inLevel(helper, "tgt_owner_other", new Vec3(2.5, 1.0, 0.5));
        PlayerTeam team = GameTestPlayers.team(helper, "tgt_owner_team", false, owner.getScoreboardName(), mate.getScoreboardName());
        OwnerHost host = new OwnerHost(owner);
        AutonomousCasterPlayer machine = new AutonomousCasterPlayer(level, UUID.randomUUID(), owner.getUUID(), host);
        try {
            Wolf ownerPet = GameTestPlayers.pet(helper, owner.getUUID(), new Vec3(0.5, 1.0, 2.5));
            Wolf matePet = GameTestPlayers.pet(helper, mate.getUUID(), new Vec3(1.5, 1.0, 2.5));
            Wolf strangerPet = GameTestPlayers.pet(helper, stranger.getUUID(), new Vec3(2.5, 1.0, 2.5));

            helper.assertTrue(machine.getTeam() == team, "The machine was not on its owner's team");
            level.getServer().setPvpAllowed(true);
            for (boolean friendlyFire : new boolean[]{false, true}) {
                team.setAllowFriendlyFire(friendlyFire);
                String when = " (friendly fire " + friendlyFire + ")";
                helper.assertTrue(!CombatSpellHelper.canHarm(machine, owner, false), "The owner was a target" + when);
                helper.assertTrue(!CombatSpellHelper.canHarm(machine, ownerPet, false), "The owner's pet was a target" + when);
                helper.assertTrue(!CombatSpellHelper.canHarm(machine, mate, false), "The owner's team mate was a target" + when);
                helper.assertTrue(!CombatSpellHelper.canHarm(machine, matePet, false), "A pet of the owner's team mate was a target" + when);
                helper.assertTrue(CombatSpellHelper.canHarm(machine, stranger, false), "Another player was spared with PvP on" + when);
                helper.assertTrue(CombatSpellHelper.canHarm(machine, strangerPet, false), "Another player's pet was spared" + when);
                helper.assertTrue(CombatSpellHelper.isAutoTarget(machine, stranger, false, true),
                        "Indiscriminate auto-aim of the machine skipped another player" + when);
            }
            level.getServer().setPvpAllowed(false);
            helper.assertTrue(!CombatSpellHelper.canHarm(machine, stranger, true), "Another player was a target with PvP off");

            // 所有者の名前がわからない機械はどのチームにも入らない。
            host.name = null;
            helper.assertTrue(machine.getTeam() == null, "A machine with an unknown owner joined a team");
            helper.succeed();
        } finally {
            machine.detach();
            level.getServer().setPvpAllowed(pvp);
            GameTestPlayers.removeTeam(helper, team);
            List.of(owner, mate, stranger).forEach(player -> GameTestPlayers.remove(helper, player));
        }
    }

    /** 攻撃術式の飛び道具は、撃った直後に限らず術者側をすり抜ける。 */
    @GameTest(template = TEMPLATE)
    public static void attackProjectilesPassThroughTheCastersSide(GameTestHelper helper) {
        FakePlayer caster = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "[psitweaks_shooter]"));
        Wolf ownPet = GameTestPlayers.pet(helper, caster.getUUID(), new Vec3(1.5, 1.0, 1.5));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new Vec3(3.5, 1.0, 1.5));
        husk.setNoAi(true);
        EntityBlazeBall ball = new EntityBlazeBall(PsitweaksEntities.BLAZE_BALL.get(), helper.getLevel());
        ball.setOwner(caster);
        try {
            helper.assertTrue(!CombatSpellHelper.canProjectileHarm(ball, caster, false), "A Blaze Ball could hit its caster");
            helper.assertTrue(!CombatSpellHelper.canProjectileHarm(ball, ownPet, false), "A Blaze Ball could hit its caster's pet");
            helper.assertTrue(CombatSpellHelper.canProjectileHarm(ball, husk, false), "A Blaze Ball could not hit a monster");
            helper.succeed();
        } finally {
            ball.discard();
            helper.killAllEntitiesOfClass(Wolf.class);
        }
    }

    // ---- 系統ごとの実際の命中（A: 戦闘魔法、B: 光線、C: 術式円、D: その他） ----

    /** A: 機械の作動式: 爆裂は、PvP が有効なら他のプレイヤーに当たり、所有者のチームメイトには当たらない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_targets_combat", timeoutTicks = 200)
    public static void machineBurstHitsStrangersButNotTheOwnersTeam(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean pvp = level.getServer().isPvpAllowed();
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "tgt_a_owner", new Vec3(4.5, 1.0, 4.5));
        ServerPlayer mate = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "tgt_a_mate", new Vec3(3.0, 1.0, 1.0)), TOUGH);
        ServerPlayer stranger = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "tgt_a_other", new Vec3(3.0, 1.0, 3.0)), TOUGH);
        PlayerTeam team = GameTestPlayers.team(helper, "tgt_a_team", false, owner.getScoreboardName(), mate.getScoreboardName());
        AutonomousCasterPlayer machine = new AutonomousCasterPlayer(level, UUID.randomUUID(), owner.getUUID(), new OwnerHost(owner));
        Runnable cleanup = () -> {
            machine.detach();
            level.getServer().setPvpAllowed(pvp);
            GameTestPlayers.removeTeam(helper, team);
            List.of(owner, mate, stranger).forEach(player -> GameTestPlayers.remove(helper, player));
        };
        helper.runAfterDelay(AFTER_SPAWN_INVULNERABILITY, guarded(cleanup, () -> {
            level.getServer().setPvpAllowed(true);
            float mateHealth = mate.getHealth();
            float strangerHealth = stranger.getHealth();
            castBurstAt(helper, machine, mate);
            castBurstAt(helper, machine, stranger);
            helper.assertTrue(mate.getHealth() == mateHealth, "Burst from the machine hit its owner's team mate");
            helper.assertTrue(stranger.getHealth() < strangerHealth, "Burst from the machine spared another player with PvP on");
            cleanup.run();
            helper.succeed();
        }));
    }

    /** B: 作動式: フォノンメーザーは、フレンドリーファイアが有効でもチームメイトに当たらない。機械の光線は他のプレイヤーに当たる。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_targets_beam", timeoutTicks = 200)
    public static void beamsSpareTeamMatesEvenWithFriendlyFire(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean pvp = level.getServer().isPvpAllowed();
        // 線は2本（z は float の座標がずれない整数。docs/pitfalls.md PIT-055）: z=1 に術者の光線とチームメイト・奥のハスク、
        // z=3 に機械の光線と他のプレイヤー・奥のハスク。光線はそれぞれの胴の高さを通す。
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "tgt_b_caster", new Vec3(0.5, 1.0 + 0.9 - 1.62, 1.0));
        caster.moveTo(caster.getX(), caster.getY(), caster.getZ(), -90.0F, 0.0F);
        caster.setYHeadRot(-90.0F);
        ServerPlayer mate = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "tgt_b_mate", new Vec3(2.5, 1.0, 1.0)), TOUGH);
        ServerPlayer stranger = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "tgt_b_other", new Vec3(2.5, 1.0, 3.0)), TOUGH);
        PlayerTeam team = GameTestPlayers.team(helper, "tgt_b_team", true, caster.getScoreboardName(), mate.getScoreboardName());
        Husk mateLine = frozenHusk(helper, new Vec3(4.0, 1.0, 1.0));
        Husk strangerLine = frozenHusk(helper, new Vec3(4.0, 1.0, 3.0));
        AutonomousCasterPlayer machine = new AutonomousCasterPlayer(level, UUID.randomUUID(), caster.getUUID(), new OwnerHost(caster));
        Vec3 eye = helper.absoluteVec(new Vec3(0.5, 1.9, 3.0));
        machine.moveTo(eye.x, eye.y - machine.getEyeHeight(), eye.z, -90.0F, 0.0F);
        machine.setYHeadRot(-90.0F);
        List<Entity> beams = new ArrayList<>();
        Runnable cleanup = () -> {
            beams.forEach(Entity::discard);
            machine.detach();
            level.getServer().setPvpAllowed(pvp);
            GameTestPlayers.removeTeam(helper, team);
            List.of(caster, mate, stranger).forEach(player -> GameTestPlayers.remove(helper, player));
        };
        float mateHealth = mate.getHealth();
        float strangerHealth = stranger.getHealth();
        float huskHealth = mateLine.getHealth();
        helper.runAfterDelay(AFTER_SPAWN_INVULNERABILITY, guarded(cleanup, () -> {
            level.getServer().setPvpAllowed(true);
            for (LivingEntity shooter : List.of(caster, machine)) {
                EntityPhononMaserBeam beam = new EntityPhononMaserBeam(level, shooter, 1, 40);
                level.addFreshEntity(beam);
                beams.add(beam);
            }
        }));
        helper.runAfterDelay(AFTER_SPAWN_INVULNERABILITY + 5, guarded(cleanup, () -> {
            helper.assertTrue(mateLine.getHealth() < huskHealth && strangerLine.getHealth() < huskHealth,
                    "The Phonon Maser beams missed their lines");
            helper.assertTrue(mate.getHealth() == mateHealth, "The Phonon Maser beam hit a team mate with friendly fire on");
            helper.assertTrue(stranger.getHealth() < strangerHealth, "The machine's Phonon Maser beam spared another player with PvP on");
            cleanup.run();
            helper.succeed();
        }));
    }

    /** C: 作動式: ファイアサークルは、術者のペットとチームメイト（フレンドリーファイアが有効でも）に当たらない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_targets_circle", timeoutTicks = 200)
    public static void circlesSpareTheCastersPetsAndTeamMates(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean pvp = level.getServer().isPvpAllowed();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "tgt_c_caster", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer mate = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "tgt_c_mate", new Vec3(3.0, 1.0, 3.0)), TOUGH);
        ServerPlayer stranger = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "tgt_c_other", new Vec3(2.0, 1.0, 3.0)), TOUGH);
        PlayerTeam team = GameTestPlayers.team(helper, "tgt_c_team", true, caster.getScoreboardName(), mate.getScoreboardName());
        Wolf ownPet = GameTestPlayers.tough(GameTestPlayers.pet(helper, caster.getUUID(), new Vec3(2.0, 1.0, 2.0)), TOUGH);
        Husk husk = GameTestPlayers.tough(frozenHusk(helper, new Vec3(3.0, 1.0, 2.0)), TOUGH);
        EntityFlareCircle circle = new EntityFlareCircle(level);
        Runnable cleanup = () -> {
            circle.discard();
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
            Vec3 center = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
            circle.setPos(center.x, center.y + 0.01, center.z);
            circle.setCaster(caster);
            circle.setSafeToPlayers(false);
            circle.setLifetimeTicks(40);
            circle.configureAreaDamage(2.0F, 2.0F, 2);
            level.addFreshEntity(circle);
        }));
        helper.runAfterDelay(AFTER_SPAWN_INVULNERABILITY + 6, guarded(cleanup, () -> {
            helper.assertTrue(husk.getHealth() < huskHealth && stranger.getHealth() < strangerHealth,
                    "The Flare Circle did not burn the monster and the other player with PvP on");
            helper.assertTrue(ownPet.getHealth() == petHealth, "The Flare Circle burned its caster's pet");
            helper.assertTrue(mate.getHealth() == mateHealth, "The Flare Circle burned a team mate with friendly fire on");
            cleanup.run();
            helper.succeed();
        }));
    }

    /** D: 作動式: ギロチンは、術者のペット・チームメイトに効かず、他のプレイヤーには PvP が有効なときだけ効く。機械は所有者のチームメイトに効かない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_targets_other", timeoutTicks = 200)
    public static void guillotineFollowsTheSharedRule(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean pvp = level.getServer().isPvpAllowed();
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "tgt_d_caster", new Vec3(0.5, 1.0, 0.5));
        ServerPlayer mate = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "tgt_d_mate", new Vec3(2.5, 1.0, 0.5)), TOUGH);
        ServerPlayer stranger = GameTestPlayers.tough(GameTestPlayers.inLevel(helper, "tgt_d_other", new Vec3(2.5, 1.0, 2.5)), TOUGH);
        PlayerTeam team = GameTestPlayers.team(helper, "tgt_d_team", true, caster.getScoreboardName(), mate.getScoreboardName());
        Wolf ownPet = GameTestPlayers.tough(GameTestPlayers.pet(helper, caster.getUUID(), new Vec3(0.5, 1.0, 2.5)), TOUGH);
        AutonomousCasterPlayer machine = new AutonomousCasterPlayer(level, UUID.randomUUID(), caster.getUUID(), new OwnerHost(caster));
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        machine.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        Runnable cleanup = () -> {
            machine.detach();
            level.getServer().setPvpAllowed(pvp);
            GameTestPlayers.removeTeam(helper, team);
            List.of(caster, mate, stranger).forEach(player -> GameTestPlayers.remove(helper, player));
            helper.killAllEntitiesOfClass(Wolf.class);
        };
        helper.runAfterDelay(AFTER_SPAWN_INVULNERABILITY, guarded(cleanup, () -> {
            level.getServer().setPvpAllowed(true);
            assertGuillotine(helper, caster, ownPet, false, "the caster's pet");
            assertGuillotine(helper, caster, mate, false, "a team mate with friendly fire on");
            assertGuillotine(helper, caster, stranger, true, "another player with PvP on");
            assertGuillotine(helper, machine, mate, false, "the owner's team mate (machine)");
            assertGuillotine(helper, machine, stranger, true, "another player with PvP on (machine)");
            level.getServer().setPvpAllowed(false);
            assertGuillotine(helper, caster, stranger, false, "another player with PvP off");
            assertGuillotine(helper, machine, stranger, false, "another player with PvP off (machine)");
            cleanup.run();
            helper.succeed();
        }));
    }

    // ---- 道具 ----

    private static void assertGuillotine(GameTestHelper helper, LivingEntity caster, LivingEntity target, boolean hits, String what) {
        float health = target.getHealth();
        target.invulnerableTime = 0;
        var guillotine = new PieceTrickGuillotine(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return target;
            }
        };
        try {
            guillotine.execute(new SpellContext().setPlayer((Player) caster));
        } catch (SpellRuntimeException e) {
            throw new IllegalStateException("Guillotine failed on " + what, e);
        }
        boolean hit = target.getHealth() < health;
        helper.assertTrue(hit == hits, "Guillotine " + (hits ? "spared " : "hit ") + what);
    }

    /** 機械をハスクの胴の高さ・距離 2.5 に置いて向けてから、作動式: 爆裂（威力 1）を撃たせる。 */
    private static void castBurstAt(GameTestHelper helper, AutonomousCasterPlayer machine, LivingEntity target) {
        Vec3 aim = target.position().add(0.0, target.getBbHeight() / 2.0, 0.0);
        Vec3 eye = aim.add(-2.0, 0.0, 0.0);
        machine.moveTo(eye.x, eye.y - machine.getEyeHeight(), eye.z, 0.0F, 0.0F);
        machine.lookAt(EntityAnchorArgument.Anchor.EYES, aim);
        target.invulnerableTime = 0;
        var burst = new PieceTrickBurst(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return 1.0;
            }
        };
        try {
            burst.execute(new SpellContext().setPlayer(machine));
        } catch (SpellRuntimeException e) {
            throw new IllegalStateException("Burst failed", e);
        }
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

    /** 機械の代わりに、所有者の UUID と名前だけを答える。 */
    private static final class OwnerHost implements AutonomousCasterHost {
        private final UUID owner;
        private String name;

        OwnerHost(ServerPlayer owner) {
            this.owner = owner.getUUID();
            this.name = owner.getScoreboardName();
        }

        @Override
        public void onCasterMessage(Component message) {
        }

        @Override
        public void onSpamlessMessage(Component message, int replacementKey) {
        }

        @Override
        public int availablePsi() {
            return 0;
        }

        @Override
        public void onBreakLoop() {
        }

        @Override
        public void onInsufficientPsi(int cost, int available) {
        }

        @Override
        public Optional<UUID> ideaStorageOwner() {
            return Optional.of(owner);
        }

        @Override
        public Optional<String> ownerLanguage() {
            return Optional.empty();
        }

        @Override
        public Optional<String> ownerName() {
            return Optional.ofNullable(name);
        }
    }
}
