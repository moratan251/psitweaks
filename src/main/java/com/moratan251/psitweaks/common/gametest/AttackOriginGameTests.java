package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.EntityAquaCutterProjectile;
import com.moratan251.psitweaks.common.entities.EntityBlazeBall;
import com.moratan251.psitweaks.common.entities.EntityDryIceProjectile;
import com.moratan251.psitweaks.common.entities.combat.EntityChainLightning;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickAquaCutter;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickBlazeBall;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickChainLightning;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickDryMeteor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Where the attack tricks that used to fire only from the caster start and which way they go: Trick: Blaze Ball,
 * Trick: Aqua Cutter and Trick: Dry Meteor take an optional Position and Direction (left out, each fires as it did
 * before the inputs existed), and Trick: Chain Lightning a required Position.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AttackOriginGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String BATCH = "psitweaks_attack_origin";
    private static final String POWER = SpellParam.GENERIC_NAME_POWER;
    private static final String POSITION = SpellParam.GENERIC_NAME_POSITION;
    private static final String DIRECTION = SpellParam.GENERIC_NAME_DIRECTION;
    private static final double EPSILON = 1.0E-3;
    private static final float TOUGH_HEALTH = 200.0F;

    private AttackOriginGameTests() {
    }

    /** One of the projectile tricks: how to build it with fixed inputs, and the projectile it fires. */
    private record Shooter(String name, Function<Map<String, ?>, SpellPiece> piece,
                           Class<? extends Projectile> projectile) {
    }

    private static final List<Shooter> SHOOTERS = List.of(
            new Shooter("Blaze Ball", AttackOriginGameTests::blazeBall, EntityBlazeBall.class),
            new Shooter("Aqua Cutter", AttackOriginGameTests::aquaCutter, EntityAquaCutterProjectile.class),
            new Shooter("Dry Meteor", AttackOriginGameTests::dryMeteor, EntityDryIceProjectile.class));

    private interface CastAction {
        void run() throws SpellRuntimeException;
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void projectileTricksFireFromThePositionInTheDirection(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "origin-given");
        // The caster looks straight up: only the inputs aim the shots.
        player.setXRot(-90.0F);
        Vec3 from = helper.absoluteVec(new Vec3(2.5, 2.0, 2.5));
        Vec3 way = new Vec3(1.0, 0.0, 1.0).normalize();
        for (Shooter shooter : SHOOTERS) {
            shooter.piece().apply(inputs(from, new Vector3(2.0, 0.0, 2.0))).execute(new SpellContext().setPlayer(player));
            Projectile shot = onlyShot(helper, shooter);
            helper.assertTrue(near(CombatSpellHelper.center(shot), from),
                    shooter.name() + " must start with its middle at the position: " + CombatSpellHelper.center(shot));
            helper.assertTrue(near(shot.getDeltaMovement().normalize(), way),
                    shooter.name() + " must fly in the direction: " + shot.getDeltaMovement());
            shot.discard();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void projectileTricksWithoutInputsFireFromTheCastersEyes(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 0), "origin-default");
        // Looking along +z, slightly down.
        player.setYRot(0.0F);
        player.setYHeadRot(0.0F);
        player.setXRot(10.0F);
        Vec3 look = player.getLookAngle();
        for (Shooter shooter : SHOOTERS) {
            shooter.piece().apply(Map.of(POWER, 1.0)).execute(new SpellContext().setPlayer(player));
            Projectile shot = onlyShot(helper, shooter);
            // Blaze Ball leaves half a block ahead of the caster; the others from the caster's own position.
            double ahead = shooter.projectile() == EntityBlazeBall.class ? 0.5 : 0.0;
            Vec3 expected = new Vec3(player.getX() + look.x * ahead, player.getEyeY() - 0.1, player.getZ() + look.z * ahead);
            helper.assertTrue(near(shot.position(), expected),
                    shooter.name() + " without inputs must start where it did before: " + shot.position() + ", " + expected);
            helper.assertTrue(near(shot.getDeltaMovement().normalize(), look),
                    shooter.name() + " without inputs must fly where the caster looks: " + shot.getDeltaMovement());
            shot.discard();
        }

        // Direction alone: from the caster's eyes as before, the given way.
        player.setXRot(-90.0F);
        Vec3 way = new Vec3(-1.0, 0.0, 0.0);
        for (Shooter shooter : SHOOTERS) {
            Map<String, Object> values = new HashMap<>();
            values.put(POWER, 1.0);
            values.put(DIRECTION, new Vector3(-3.0, 0.0, 0.0));
            shooter.piece().apply(values).execute(new SpellContext().setPlayer(player));
            Projectile shot = onlyShot(helper, shooter);
            double ahead = shooter.projectile() == EntityBlazeBall.class ? 0.5 : 0.0;
            Vec3 expected = new Vec3(player.getX() + way.x * ahead, player.getEyeY() - 0.1, player.getZ() + way.z * ahead);
            helper.assertTrue(near(shot.position(), expected),
                    shooter.name() + " with only a direction must start at the caster: " + shot.position());
            helper.assertTrue(near(shot.getDeltaMovement().normalize(), way),
                    shooter.name() + " with only a direction must fly that way: " + shot.getDeltaMovement());
            shot.discard();
        }

        // Position alone: from the position, where the caster looks (straight up).
        Vec3 from = helper.absoluteVec(new Vec3(1.5, 1.0, 3.5));
        for (Shooter shooter : SHOOTERS) {
            Map<String, Object> values = new HashMap<>();
            values.put(POWER, 1.0);
            values.put(POSITION, vector(from));
            shooter.piece().apply(values).execute(new SpellContext().setPlayer(player));
            Projectile shot = onlyShot(helper, shooter);
            helper.assertTrue(near(CombatSpellHelper.center(shot), from),
                    shooter.name() + " with only a position must start there: " + CombatSpellHelper.center(shot));
            helper.assertTrue(near(shot.getDeltaMovement().normalize(), player.getLookAngle()),
                    shooter.name() + " with only a position must fly where the caster looks: " + shot.getDeltaMovement());
            shot.discard();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void projectileTricksRejectAZeroDirectionAndAFarPosition(GameTestHelper helper) {
        var player = caster(helper, new BlockPos(0, 0, 0), "origin-errors");
        var far = caster(helper, new BlockPos(0, 0, 80), "origin-far");
        Vec3 inside = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
        for (Shooter shooter : SHOOTERS) {
            helper.assertTrue(SpellRuntimeException.NULL_VECTOR.equals(castError(() -> shooter.piece()
                            .apply(inputs(inside, new Vector3(0.0, 0.0, 0.0))).execute(new SpellContext().setPlayer(player)))),
                    shooter.name() + " must fail on a zero direction");
            helper.assertTrue(SpellRuntimeException.OUTSIDE_RADIUS.equals(castError(() -> shooter.piece()
                            .apply(inputs(inside, new Vector3(1.0, 0.0, 0.0))).execute(new SpellContext().setPlayer(far)))),
                    shooter.name() + " must fail on a position out of the spell radius");
            helper.assertTrue(shots(helper, shooter).isEmpty(), shooter.name() + " must fire nothing when it fails");
        }
        helper.succeed();
    }

    /**
     * Chain Lightning picks its first target from the position. The two husks stand so that each is in the direction from
     * only one of two positions: one beside the caster, and the caster's eyes.
     */
    @GameTest(template = TEMPLATE, batch = BATCH + "_chain")
    public static void chainLightningStrikesFromThePosition(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "origin-chain");
        player.setXRot(-90.0F);
        var context = new SpellContext().setPlayer(player);
        // Straight ahead (+x) of the caster's eyes, but 45 degrees off the direction from the position.
        var ahead = tough(husk(helper, new BlockPos(4, 0, 0)));
        // 53 degrees off the direction from the caster's eyes, but straight ahead from the position.
        var aside = tough(husk(helper, new BlockPos(3, 0, 4)));
        Vec3 from = helper.absoluteVec(new Vec3(0.5, 1.0, 4.5));
        Vector3 east = new Vector3(1.0, 0.0, 0.0);

        Map<String, Object> fromPosition = new HashMap<>();
        fromPosition.put(POSITION, vector(from));
        fromPosition.put(DIRECTION, east);
        fromPosition.put(POWER, 1.0);
        chainLightning(fromPosition).execute(context);
        helper.assertTrue(aside.getHealth() < TOUGH_HEALTH - EPSILON, "The bolt must strike what is ahead of the position");
        helper.assertTrue(near(ahead.getHealth(), TOUGH_HEALTH), "What is ahead of the caster may only be reached by a branch");
        discardBranches(helper);

        float asideHealth = aside.getHealth();
        chainLightning(Map.of(POSITION, vector(player.getEyePosition()), DIRECTION, east, POWER, 1.0)).execute(context);
        helper.assertTrue(ahead.getHealth() < TOUGH_HEALTH - EPSILON,
                "From the caster's eyes the bolt must strike what is ahead of them");
        helper.assertTrue(near(aside.getHealth(), asideHealth), "From the caster's eyes the husk aside must not be struck first");
        discardBranches(helper);

        helper.assertTrue(SpellRuntimeException.NULL_VECTOR.equals(castError(() -> chainLightning(
                Map.of(DIRECTION, east, POWER, 1.0)).execute(context))), "Position is required");

        var far = caster(helper, new BlockPos(0, 0, 80), "origin-chain-far");
        helper.assertTrue(SpellRuntimeException.OUTSIDE_RADIUS.equals(castError(() -> chainLightning(fromPosition)
                .execute(new SpellContext().setPlayer(far)))), "A position out of the spell radius must fail");
        helper.succeed();
    }

    private static Map<String, Object> inputs(Vec3 position, Vector3 direction) {
        Map<String, Object> values = new HashMap<>();
        values.put(POWER, 1.0);
        values.put(POSITION, vector(position));
        values.put(DIRECTION, direction);
        return values;
    }

    private static List<? extends Projectile> shots(GameTestHelper helper, Shooter shooter) {
        return helper.getLevel().getEntitiesOfClass(shooter.projectile(), helper.getBounds().inflate(8.0));
    }

    private static Projectile onlyShot(GameTestHelper helper, Shooter shooter) {
        var shots = shots(helper, shooter);
        helper.assertTrue(shots.size() == 1, shooter.name() + " must fire one shot, got " + shots.size());
        return shots.get(0);
    }

    private static void discardBranches(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(EntityChainLightning.class, helper.getBounds().inflate(8.0))
                .forEach(Entity::discard);
    }

    private static FakePlayer caster(GameTestHelper helper, BlockPos relative, String name) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(relative)));
        return player;
    }

    /** A goal-less, frozen husk without armour. */
    private static Husk husk(GameTestHelper helper, BlockPos relative) {
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, relative);
        husk.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        husk.setNoAi(true);
        return husk;
    }

    private static Husk tough(Husk husk) {
        husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(TOUGH_HEALTH);
        husk.setHealth(TOUGH_HEALTH);
        return husk;
    }

    private static Vector3 vector(Vec3 vec) {
        return new Vector3(vec.x, vec.y, vec.z);
    }

    private static boolean near(double actual, double expected) {
        return Math.abs(actual - expected) < EPSILON;
    }

    private static boolean near(Vec3 actual, Vec3 expected) {
        return actual.distanceTo(expected) < EPSILON;
    }

    /** The error key casting fails with, or null when it succeeds. */
    private static String castError(CastAction action) {
        try {
            action.run();
            return null;
        } catch (SpellRuntimeException failure) {
            return failure.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T evaluation(Map<String, ?> values, SpellParam<?> param) {
        Object value = values.get(param.name);
        return (T) (value instanceof Number number ? Double.valueOf(number.doubleValue()) : value);
    }

    // Pieces whose parameters read fixed values, both when cast and when compiled.

    private static SpellPiece blazeBall(Map<String, ?> values) {
        return new PieceTrickBlazeBall(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static SpellPiece aquaCutter(Map<String, ?> values) {
        return new PieceTrickAquaCutter(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static SpellPiece dryMeteor(Map<String, ?> values) {
        return new PieceTrickDryMeteor(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceTrickChainLightning chainLightning(Map<String, ?> values) {
        return new PieceTrickChainLightning(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }
}
