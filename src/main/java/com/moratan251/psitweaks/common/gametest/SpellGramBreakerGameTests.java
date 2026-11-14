package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySoakZone;
import com.moratan251.psitweaks.common.entities.combat.EntityPsiBullet;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGramDemolition;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickPsiBullet;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickSoakZone;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 魔法式オブジェクトを削る作動式（2026-11-14 ユーザー）: サイ・ブリットと術式解体。どちらも耐久値を「量 × 術者の事象干渉力」
 * だけ減らし（干渉力 1 の試験では量のとおり）、術者自身の魔法式オブジェクトは削らない。
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellGramBreakerGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final float EPSILON = 0.05F;

    private SpellGramBreakerGameTests() {
    }

    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_breakers")
    public static void breakerTricksHaveTheirStats(GameTestHelper helper) throws Exception {
        SpellMetadata bullet = new SpellMetadata();
        psiBullet(Map.of(SpellParam.GENERIC_NAME_POWER, 1.0)).addToMetadata(bullet);
        helper.assertTrue(bullet.getStat(EnumSpellStat.POTENCY) == 120 && bullet.getStat(EnumSpellStat.COST) == 420,
                "Psi Bullet at Power 1 must cost what Air Bullet does (120 / 420), got "
                        + bullet.getStat(EnumSpellStat.POTENCY) + " / " + bullet.getStat(EnumSpellStat.COST));
        SpellMetadata demolition = new SpellMetadata();
        new PieceTrickGramDemolition(new Spell()).addToMetadata(demolition);
        helper.assertTrue(demolition.getStat(EnumSpellStat.POTENCY) == 750 && demolition.getStat(EnumSpellStat.COST) == 7500,
                "Gram Demolition is 750 / 7500, got " + demolition.getStat(EnumSpellStat.POTENCY) + " / "
                        + demolition.getStat(EnumSpellStat.COST));
        helper.succeed();
    }

    /** サイ・ブリットは最初に当たった生物に 2 + 威力 を与えて止まる。後ろの生物には届かない。 */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_breakers_creature")
    public static void psiBulletHitsTheFirstCreature(GameTestHelper helper) throws Exception {
        FakePlayer caster = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "psi-bullet-creature"));
        caster.moveTo(helper.absoluteVec(new Vec3(0.5, 0.0, 2.5)));
        Husk hit = husk(helper, new Vec3(2.5, 0.0, 2.5));
        Husk behind = husk(helper, new Vec3(3.5, 0.0, 2.5));
        Vec3 start = helper.absoluteVec(new Vec3(0.5, 1.0, 2.5));
        psiBullet(Map.of(SpellParam.GENERIC_NAME_POSITION, new Vector3(start.x, start.y, start.z),
                SpellParam.GENERIC_NAME_DIRECTION, new Vector3(1, 0, 0), SpellParam.GENERIC_NAME_POWER, 1.0))
                .execute(new SpellContext().setPlayer(caster));
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(near(hit.getHealth(), 17.0F),
                        "The first husk must take 2 + Power (3): health " + hit.getHealth()))
                .thenIdle(5)
                .thenExecute(() -> {
                    helper.assertTrue(near(hit.getHealth(), 17.0F), "The husk is hit only once");
                    helper.assertTrue(near(behind.getHealth(), 20.0F), "The bullet must stop at the first husk: " + behind.getHealth());
                    helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityPsiBullet.class, helper.getBounds().inflate(4)).isEmpty(),
                            "The bullet must be gone");
                })
                .thenSucceed();
    }

    /**
     * サイ・ブリットは他人の魔法式オブジェクトの核に当たると耐久値を 8 + 2 × 威力 減らして止まる。手前にある術者自身の領域の核は
     * すり抜ける。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_breakers_bullet", timeoutTicks = 60)
    public static void psiBulletWearsAnotherPlayersSpellGramObject(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "psi_bullet_owner", new Vec3(4.5, 1.0, 2.5));
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "psi_bullet_caster", new Vec3(2.5, 1.0, 2.5));
        List<ServerPlayer> players = List.of(owner, caster);
        Runnable cleanup = () -> players.forEach(player -> {
            SpellGramIndex.ofCaster(level, player.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, player);
        });
        SpellGramObject others;
        SpellGramObject own;
        try {
            others = placeSoak(owner, 1.0);
            own = placeSoak(caster, 1.0);
            others.setMaxIntegrity(100.0F);
            own.setMaxIntegrity(100.0F);
            // 二人とも弾の線からどく。
            Vec3 aside = helper.absoluteVec(new Vec3(0.5, 1.0, 0.5));
            caster.moveTo(aside.x, aside.y, aside.z);
            Vec3 away = helper.absoluteVec(new Vec3(4.5, 1.0, 4.5));
            owner.moveTo(away.x, away.y, away.z);
            // 術者自身の領域の核を通り抜けて、他人の領域の核へ。
            Vec3 start = own.position().subtract(2.0, 0.0, 0.0);
            psiBullet(Map.of(SpellParam.GENERIC_NAME_POSITION, new Vector3(start.x, start.y, start.z),
                    SpellParam.GENERIC_NAME_DIRECTION, new Vector3(1, 0, 0), SpellParam.GENERIC_NAME_POWER, 1.0))
                    .execute(new SpellContext().setPlayer(caster));
        } catch (RuntimeException | SpellRuntimeException failure) {
            cleanup.run();
            throw new RuntimeException(failure);
        }
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(own.getIntegrity() - others.getIntegrity() > 5.0F,
                        "The bullet must reach the other player's zone: " + others.getIntegrity()))
                .thenIdle(5)
                .thenExecute(() -> {
                    try {
                        // 2 つの領域は同じ tick に同じ耐久値で置いたので、時間で減る分は同じ。差が弾で減った分。
                        helper.assertTrue(near(own.getIntegrity() - others.getIntegrity(), 10.0F),
                                "8 + 2 x Power (10) must come off the other player's zone: " + others.getIntegrity()
                                        + " against " + own.getIntegrity());
                        helper.assertTrue(own.getIntegrity() > 95.0F, "The caster's own zone must be flown through: "
                                + own.getIntegrity());
                        helper.assertTrue(level.getEntitiesOfClass(EntityPsiBullet.class, helper.getBounds().inflate(4)).isEmpty(),
                                "The bullet must stop at the core it hit");
                    } finally {
                        cleanup.run();
                    }
                })
                .thenSucceed();
    }

    /**
     * 術式解体は、視線の先 16 ブロック・半角 30 度の円錐の中の魔法式オブジェクトの耐久値を 300 ずつ減らし、同じ範囲の生物に 6 を
     * 与える。後ろ・横のものと、術者自身の魔法式オブジェクトはそのまま。
     */
    @GameTest(template = TEMPLATE, batch = "psitweaks_spellgram_breakers_demolition")
    public static void gramDemolitionWearsWhatIsInFront(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = GameTestPlayers.inLevel(helper, "demolition_owner", new Vec3(5.5, 1.0, 2.5));
        ServerPlayer caster = GameTestPlayers.inLevel(helper, "demolition_caster", new Vec3(3.5, 1.0, 2.5));
        List<ServerPlayer> players = List.of(owner, caster);
        Runnable cleanup = () -> players.forEach(player -> {
            SpellGramIndex.ofCaster(level, player.getUUID()).forEach(Entity::discard);
            GameTestPlayers.remove(helper, player);
        });
        try {
            List<SpellGramObject> zones = new ArrayList<>();
            SpellGramObject front = placeSoakAt(helper, owner, new Vec3(5.5, 1.0, 2.5), zones);
            SpellGramObject farFront = placeSoakAt(helper, owner, new Vec3(14.5, 1.0, 2.5), zones);
            SpellGramObject behind = placeSoakAt(helper, owner, new Vec3(-2.5, 1.0, 2.5), zones);
            SpellGramObject side = placeSoakAt(helper, owner, new Vec3(1.5, 1.0, 6.5), zones);
            SpellGramObject own = placeSoakAt(helper, caster, new Vec3(3.5, 1.0, 2.5), zones);
            for (SpellGramObject zone : zones) {
                zone.setMaxIntegrity(1000.0F);
            }
            Husk hit = husk(helper, new Vec3(4.5, 0.0, 3.0));
            Husk back = husk(helper, new Vec3(-0.5, 0.0, 2.5));
            Vec3 at = helper.absoluteVec(new Vec3(0.5, 1.0, 2.5));
            caster.moveTo(at.x, at.y, at.z);
            caster.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(front.getX(), caster.getEyeY(), front.getZ()));

            new PieceTrickGramDemolition(new Spell()).execute(new SpellContext().setPlayer(caster));

            helper.assertTrue(near(front.getIntegrity(), 700.0F) && near(farFront.getIntegrity(), 700.0F),
                    "Zones in front within 16 blocks must lose 300: " + front.getIntegrity() + ", " + farFront.getIntegrity());
            helper.assertTrue(near(behind.getIntegrity(), 1000.0F) && near(side.getIntegrity(), 1000.0F),
                    "Zones behind or to the side must be left alone: " + behind.getIntegrity() + ", " + side.getIntegrity());
            helper.assertTrue(near(own.getIntegrity(), 1000.0F), "The caster's own zone must be left alone: " + own.getIntegrity());
            helper.assertTrue(near(hit.getHealth(), 14.0F), "A creature in front must take 6: " + hit.getHealth());
            helper.assertTrue(near(back.getHealth(), 20.0F), "A creature behind must be left alone: " + back.getHealth());
            helper.assertTrue(PieceTrickGramDemolition.inCone(Vec3.ZERO, new Vec3(1, 0, 0), new Vec3(10, 0, 5.5), 0.0)
                            && !PieceTrickGramDemolition.inCone(Vec3.ZERO, new Vec3(1, 0, 0), new Vec3(10, 0, 6.0), 0.0)
                            && !PieceTrickGramDemolition.inCone(Vec3.ZERO, new Vec3(1, 0, 0), new Vec3(16.5, 0, 0), 0.0),
                    "The cone is 30 degrees around the line of sight and 16 blocks long");
        } catch (RuntimeException | SpellRuntimeException failure) {
            cleanup.run();
            throw new RuntimeException(failure);
        }
        cleanup.run();
        helper.succeed();
    }

    private static PieceTrickPsiBullet psiBullet(Map<String, ?> values) {
        return new PieceTrickPsiBullet(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return values.get(param.name);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T getParamEvaluation(SpellParam<?> param) {
                Object value = values.get(param.name);
                return (T) (value instanceof Number number ? Double.valueOf(number.doubleValue()) : null);
            }
        };
    }

    /** 湿潤領域（一辺 {@code size}、行列なし）を {@code caster} の位置に置く。 */
    private static SpellGramObject placeSoak(ServerPlayer caster, double size) throws SpellRuntimeException {
        List<SpellGramObject> before = SpellGramIndex.ofCaster(caster.level(), caster.getUUID());
        var piece = new PieceTrickSoakZone(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return param.name.equals(PsitweaksSpellParams.SIZE) ? size : null;
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

    private static SpellGramObject placeSoakAt(GameTestHelper helper, ServerPlayer caster, Vec3 relative,
                                               List<SpellGramObject> placed) throws SpellRuntimeException {
        Vec3 at = helper.absoluteVec(relative);
        caster.moveTo(at.x, at.y, at.z);
        SpellGramObject zone = placeSoak(caster, 1.0);
        placed.add(zone);
        return zone;
    }

    private static Husk husk(GameTestHelper helper, Vec3 relative) {
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, relative);
        husk.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        husk.setNoAi(true);
        return husk;
    }

    private static boolean near(float actual, float expected) {
        return Math.abs(actual - expected) < EPSILON;
    }
}
