package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.common.entities.combat.EntityChainLightning;
import com.moratan251.psitweaks.common.entities.combat.EntityDancingBlade;
import com.moratan251.psitweaks.common.entities.combat.EntityGravityWell;
import com.moratan251.psitweaks.common.entities.combat.EntityGroundShockwave;
import com.moratan251.psitweaks.common.entities.combat.EntityKamaitachiBlade;
import com.moratan251.psitweaks.common.entities.combat.EntityStarfallMeteor;
import com.moratan251.psitweaks.common.spells.SpellWarning;
import com.moratan251.psitweaks.common.spells.combat.BarrierShield;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickBarrier;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickBurst;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickChainLightning;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickDancingBlades;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGravityCollapse;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickKamaitachi;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickMineGenesis;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickStarfall;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Trick: Barrier's shield and the combat tricks of 1.0.0: who they hit, how much, when, and what they cost.
 * The tests that spawn hostile mobs run in their own batches so seeking spells never reach another test's mobs.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CombatSpellGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final String BATCH = "psitweaks_combat";
    private static final String TARGET = SpellParam.GENERIC_NAME_TARGET;
    private static final String POWER = SpellParam.GENERIC_NAME_POWER;
    private static final String TIME = SpellParam.GENERIC_NAME_TIME;
    private static final String POSITION = SpellParam.GENERIC_NAME_POSITION;
    private static final String DIRECTION = SpellParam.GENERIC_NAME_DIRECTION;
    private static final String RADIUS = SpellParam.GENERIC_NAME_RADIUS;
    private static final String CHAINS = "psitweaks.spellparam.chains";
    private static final String COUNT = "psitweaks.spellparam.count";
    private static final float EPSILON = 1.0E-3F;
    /** Health of the creatures {@link #tough} prepares. */
    private static final float TOUGH_HEALTH = 200.0F;

    private interface CompileAction {
        void run() throws SpellCompilationException;
    }

    private interface CastAction {
        void run() throws SpellRuntimeException;
    }

    private static FakePlayer caster(GameTestHelper helper, BlockPos relative, String name) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(relative)));
        return player;
    }

    /** A goal-less husk without armour, so damage arrives exactly; frozen in place unless {@code physics}. */
    private static Husk husk(GameTestHelper helper, Vec3 relative, boolean physics) {
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, relative);
        husk.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        husk.setNoAi(!physics);
        return husk;
    }

    private static Husk husk(GameTestHelper helper, BlockPos relative, boolean physics) {
        return husk(helper, Vec3.atBottomCenterOf(relative), physics);
    }

    /** Gives {@code entity} {@link #TOUGH_HEALTH} health, so the heavy-hitting tricks do not kill it outright. */
    private static <T extends LivingEntity> T tough(T entity) {
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(TOUGH_HEALTH);
        entity.setHealth(TOUGH_HEALTH);
        return entity;
    }

    private static Vector3 vector(Vec3 vec) {
        return new Vector3(vec.x, vec.y, vec.z);
    }

    private static boolean near(double actual, double expected) {
        return Math.abs(actual - expected) < EPSILON;
    }

    private static float shield(LivingEntity entity) {
        BarrierShield shield = entity.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD);
        return shield == null ? 0.0F : shield.amount();
    }

    private static int[] stats(SpellPiece piece) throws SpellCompilationException {
        var metadata = new SpellMetadata();
        piece.addToMetadata(metadata);
        return new int[]{metadata.getStat(EnumSpellStat.POTENCY), metadata.getStat(EnumSpellStat.COST)};
    }

    /** The error key compiling fails with, or null when it compiles. */
    private static String compileError(CompileAction action) {
        try {
            action.run();
            return null;
        } catch (SpellCompilationException failure) {
            return failure.getMessage();
        }
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

    private static PieceTrickBarrier barrier(Map<String, ?> values) {
        return new PieceTrickBarrier(new Spell()) {
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

    private static PieceTrickMineGenesis mineGenesis(Map<String, ?> values) {
        return new PieceTrickMineGenesis(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceTrickBurst burst(Map<String, ?> values) {
        return new PieceTrickBurst(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceTrickKamaitachi kamaitachi(Map<String, ?> values) {
        return new PieceTrickKamaitachi(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceTrickDancingBlades dancingBlades(Map<String, ?> values) {
        return new PieceTrickDancingBlades(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceTrickStarfall starfall(Map<String, ?> values) {
        return new PieceTrickStarfall(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceTrickGravityCollapse gravityCollapse(Map<String, ?> values) {
        return new PieceTrickGravityCollapse(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void barrierShieldTakesHitsBeforeHealth(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "barrier-caster");
        var context = new SpellContext().setPlayer(player);
        var husk = husk(helper, new BlockPos(2, 0, 2), false);
        var sources = helper.getLevel().damageSources();

        barrier(Map.of(TARGET, husk, POWER, 2.0, TIME, 10.0)).execute(context);
        BarrierShield raised = husk.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD);
        helper.assertTrue(raised != null && near(raised.amount(), 20) && near(raised.maxAmount(), 20),
                "Power 2 must raise a shield of 20: " + raised);

        husk.hurt(sources.generic(), 8.0F);
        helper.assertTrue(near(husk.getHealth(), 20) && near(shield(husk), 12),
                "The shield must take the whole hit: health " + husk.getHealth() + ", shield " + shield(husk));

        husk.invulnerableTime = 0;
        barrier(Map.of(TARGET, husk, POWER, 1.0, TIME, 10.0)).execute(context);
        helper.assertTrue(near(shield(husk), 10), "Casting again must replace the shield, not add to it: " + shield(husk));

        husk.hurt(sources.generic(), 15.0F);
        helper.assertTrue(near(husk.getHealth(), 15) && husk.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD) == null,
                "Damage beyond the shield must go through and break it: health " + husk.getHealth());

        husk.invulnerableTime = 0;
        barrier(Map.of(TARGET, husk, POWER, 1.0, TIME, 10.0)).execute(context);
        husk.hurt(sources.starve(), 4.0F);
        helper.assertTrue(near(husk.getHealth(), 11) && near(shield(husk), 10),
                "Starvation must bypass the shield: health " + husk.getHealth() + ", shield " + shield(husk));

        // Within the hurt cooldown only the part of a hit above the last one counts, as in vanilla.
        husk.hurt(sources.generic(), 6.0F);
        helper.assertTrue(near(husk.getHealth(), 11) && near(shield(husk), 8),
                "During the cooldown the shield must pay only 6 - 4: shield " + shield(husk));
        husk.hurt(sources.generic(), 3.0F);
        helper.assertTrue(near(husk.getHealth(), 11) && near(shield(husk), 8),
                "A weaker hit during the cooldown must cost nothing: shield " + shield(husk));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void barrierShieldFadesAfterItsTime(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "barrier-timer");
        var husk = husk(helper, new BlockPos(2, 0, 2), false);
        barrier(Map.of(TARGET, husk, POWER, 1.0, TIME, 1.0)).execute(new SpellContext().setPlayer(player));
        helper.assertTrue(near(shield(husk), 10), "Shield not raised");
        helper.runAtTickTime(10, () -> helper.assertTrue(near(shield(husk), 10), "The shield faded too early"));
        helper.succeedWhen(() -> helper.assertTrue(husk.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD) == null,
                "A one-second shield must be gone after its time"));
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void barrierShieldLetsPoisonThrough(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "barrier-poison");
        var husk = husk(helper, new BlockPos(2, 0, 2), false);
        barrier(Map.of(TARGET, husk, POWER, 1.0, TIME, 10.0)).execute(new SpellContext().setPlayer(player));

        husk.hurt(helper.getLevel().damageSources().source(NeoForgeMod.POISON_DAMAGE), 1.0F);
        helper.assertTrue(near(husk.getHealth(), 19) && near(shield(husk), 10),
                "Poison must bypass the shield like withering: health " + husk.getHealth() + ", shield " + shield(husk));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void barrierShieldBreakKeepsTheWholeHitForTheCooldown(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "barrier-break");
        var husk = husk(helper, new BlockPos(2, 0, 2), false);
        var sources = helper.getLevel().damageSources();
        barrier(Map.of(TARGET, husk, POWER, 1.0, TIME, 10.0)).execute(new SpellContext().setPlayer(player));

        husk.hurt(sources.generic(), 14.0F);
        helper.assertTrue(near(husk.getHealth(), 16) && husk.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD) == null,
                "A hit of 14 must break the shield of 10 and pass 4: health " + husk.getHealth());

        // The rest of the cooldown is measured against the whole 14, not the 4 that went past the shield.
        husk.hurt(sources.generic(), 12.0F);
        helper.assertTrue(near(husk.getHealth(), 16),
                "A hit of 12 during the cooldown must cost nothing: health " + husk.getHealth());
        husk.hurt(sources.generic(), 17.0F);
        helper.assertTrue(near(husk.getHealth(), 13),
                "A hit of 17 during the cooldown must pass only 17 - 14: health " + husk.getHealth());

        husk.invulnerableTime = 0;
        husk.hurt(sources.generic(), 5.0F);
        helper.assertTrue(near(husk.getHealth(), 8), "After the cooldown a hit counts in full: health " + husk.getHealth());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void combatTrickStatsFollowTheirFormulas(GameTestHelper helper) throws Exception {
        int[] barrier = stats(barrier(Map.of(POWER, 2.0, TIME, 10.0)));
        helper.assertTrue(barrier[0] == 90 && barrier[1] == 320, "Barrier P2 T10: 20+15P²+T / 40+60P²+4T, got " + barrier[0] + "/" + barrier[1]);
        helper.assertTrue(SpellCompilationException.NON_POSITIVE_INTEGER.equals(compileError(
                () -> stats(barrier(Map.of(POWER, 1.5, TIME, 10.0))))), "Barrier Power must be a whole number");

        int[] chain = stats(chainLightning(Map.of(POWER, 2.0, CHAINS, 3.0)));
        helper.assertTrue(chain[0] == 230 && chain[1] == 630, "Chain Lightning P2 C3, got " + chain[0] + "/" + chain[1]);
        int[] longChain = stats(chainLightning(Map.of(POWER, 2.0, CHAINS, 20.0)));
        helper.assertTrue(longChain[0] == 380 && longChain[1] == 1030, "Chains must be capped at 8, got " + longChain[0] + "/" + longChain[1]);
        helper.assertTrue(SpellCompilationException.NON_POSITIVE_VALUE.equals(compileError(
                () -> stats(chainLightning(Map.of(POWER, 0.0, CHAINS, 3.0))))), "Chain Lightning Power must be positive");
        helper.assertTrue(SpellCompilationException.NON_POSITIVE_INTEGER.equals(compileError(
                () -> stats(chainLightning(Map.of(POWER, 1.0, CHAINS, 2.5))))), "Chains must be a whole number");

        int[] mine = stats(mineGenesis(Map.of(POWER, 1.0, RADIUS, 3.0)));
        helper.assertTrue(mine[0] == 185 && mine[1] == 630, "Mine Genesis P1 R3, got " + mine[0] + "/" + mine[1]);
        int[] burst = stats(burst(Map.of(POWER, 1.0)));
        helper.assertTrue(burst[0] == 1240 && burst[1] == 1050, "Burst P1, got " + burst[0] + "/" + burst[1]);
        int[] kamaitachi = stats(kamaitachi(Map.of(POWER, 1.0)));
        helper.assertTrue(kamaitachi[0] == 120 && kamaitachi[1] == 420, "Kamaitachi P1, got " + kamaitachi[0] + "/" + kamaitachi[1]);
        int[] blades = stats(dancingBlades(Map.of(COUNT, 2.0, POWER, 1.0)));
        helper.assertTrue(blades[0] == 840 && blades[1] == 440, "Dancing Blades N2 P1, got " + blades[0] + "/" + blades[1]);
        int[] starfall = stats(starfall(Map.of(POWER, 1.0, COUNT, 13.0)));
        helper.assertTrue(starfall[0] == 1160 && starfall[1] == 7680, "Meteors must be capped at 12, got " + starfall[0] + "/" + starfall[1]);
        int[] gravity = stats(gravityCollapse(Map.of(POWER, 1.0, RADIUS, 20.0)));
        helper.assertTrue(gravity[0] == 1370 && gravity[1] == 6150, "Gravity radius must be capped at 10, got " + gravity[0] + "/" + gravity[1]);
        helper.succeed();
    }

    /**
     * The smallest Dancing Blades and Starfall need a heavy psimetal CAD (more than flashmetal's 800, within the beta's
     * 1000); the smallest Burst and Gravity Collapse need a psycheonic metal CAD (more than the heavy alpha's 1200,
     * within 1500).
     */
    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void largeCombatTricksNeedHighTierCads(GameTestHelper helper) throws Exception {
        int blades = stats(dancingBlades(Map.of(COUNT, 1.0, POWER, 0.5)))[0];
        helper.assertTrue(blades > 800 && blades <= 1000, "Smallest Dancing Blades must need heavy psimetal, got " + blades);
        int starfall = stats(starfall(Map.of(POWER, 0.5, COUNT, 1.0)))[0];
        helper.assertTrue(starfall > 800 && starfall <= 1000, "Smallest Starfall must need heavy psimetal, got " + starfall);
        int burst = stats(burst(Map.of(POWER, 0.5)))[0];
        helper.assertTrue(burst > 1200 && burst <= 1500, "Smallest Burst must need psycheonic metal, got " + burst);
        int gravity = stats(gravityCollapse(Map.of(POWER, 0.5, RADIUS, 3.0)))[0];
        helper.assertTrue(gravity > 1200 && gravity <= 1500, "Smallest Gravity Collapse must need psycheonic metal, got " + gravity);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void combatTricksSpareTheCasterSideAndPickOnlyHostiles(GameTestHelper helper) {
        var player = caster(helper, new BlockPos(0, 0, 0), "combat-rules");
        var husk = husk(helper, new BlockPos(1, 0, 1), false);
        var pet = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(3, 0, 1));
        pet.setTame(true, false);
        pet.setOwnerUUID(player.getUUID());
        Wolf angry = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(3, 0, 3));
        angry.setPersistentAngerTarget(player.getUUID());
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 0, 3));
        var stand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(2, 0, 2));
        var other = caster(helper, new BlockPos(4, 0, 4), "combat-other");

        helper.assertTrue(CombatSpellHelper.canHarm(player, husk, false) && CombatSpellHelper.canHarm(player, pig, false),
                "Mobs and animals can be hit");
        helper.assertTrue(!CombatSpellHelper.canHarm(player, player, false), "The caster is never hit");
        helper.assertTrue(!CombatSpellHelper.canHarm(player, pet, false), "The caster's pets are never hit");
        helper.assertTrue(!CombatSpellHelper.canHarm(player, stand, false), "Armor stands are never hit");
        helper.assertTrue(!CombatSpellHelper.canHarm(player, other, true), "Safe to Players must spare players");
        other.setGameMode(GameType.CREATIVE);
        helper.assertTrue(!CombatSpellHelper.canHarm(player, other, false), "Creative players are never hit");

        helper.assertTrue(CombatSpellHelper.isHostileTarget(player, husk, false), "Monsters are hostile");
        helper.assertTrue(CombatSpellHelper.isHostileTarget(player, angry, false), "A wolf angry at the caster is hostile");
        helper.assertTrue(!CombatSpellHelper.isHostileTarget(player, pig, false), "Animals are not hostile");
        helper.assertTrue(!CombatSpellHelper.isHostileTarget(player, pet, false), "Pets are not hostile");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH + "_chain")
    public static void chainLightningJumpsBetweenHostilesOnly(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 4), "chain-caster");
        // 9 + 9 x Power would nearly kill a plain husk, so give them room to show each hit.
        var first = tough(husk(helper, new BlockPos(1, 0, 1), false));
        var second = tough(husk(helper, new BlockPos(3, 0, 1), false));
        var third = tough(husk(helper, new BlockPos(3, 0, 3), false));
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 0, 3));
        pig.setNoAi(true);

        chainLightning(Map.of(TARGET, first, POWER, 1.0, CHAINS, 8.0)).execute(new SpellContext().setPlayer(player));
        float d1 = TOUGH_HEALTH - first.getHealth();
        helper.assertTrue(near(d1, 18) || near(d1, 27), "The first hit must deal 9 + 9 x Power (x1.5 when wet): " + d1);
        helper.assertTrue(near(second.getHealth(), TOUGH_HEALTH) && near(third.getHealth(), TOUGH_HEALTH),
                "The jumps must land after the first hit, not with it");
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(second.getHealth() < TOUGH_HEALTH - EPSILON, "The bolt must jump to the second husk"))
                .thenExecute(() -> helper.assertTrue(near(third.getHealth(), TOUGH_HEALTH), "The third husk must be hit after the second"))
                .thenWaitUntil(() -> helper.assertTrue(third.getHealth() < TOUGH_HEALTH - EPSILON, "The bolt must jump to the third husk"))
                .thenExecute(() -> {
                    float d2 = TOUGH_HEALTH - second.getHealth();
                    float d3 = TOUGH_HEALTH - third.getHealth();
                    helper.assertTrue(near(d2 / d1, 0.5) && near(d3 / d1, 0.25),
                            "Each jump must deal half of the one before: " + d1 + ", " + d2 + ", " + d3);
                    helper.assertTrue(near(pig.getHealth(), pig.getMaxHealth()), "The bolt must not jump to animals");
                })
                .thenWaitUntil(() -> helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityChainLightning.class,
                        helper.getBounds().inflate(4)).isEmpty(), "The chain must be gone once every jump has landed"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void targetedCombatTricksSkipFallenTargetsWithAWarning(GameTestHelper helper) {
        List<Component> messages = new ArrayList<>();
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fallen-caster")) {
            @Override
            public void sendSystemMessage(Component message) {
                messages.add(message);
            }
        };
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(0, 0, 4))));
        var context = new SpellContext().setPlayer(player);
        // Health 0: the husk is playing its death animation, which Psi's verifyEntity would call immune.
        var fallen = husk(helper, new BlockPos(1, 0, 1), false);
        fallen.setHealth(0.0F);
        var bystander = husk(helper, new BlockPos(3, 0, 1), false);
        helper.assertTrue(castError(() -> chainLightning(Map.of(TARGET, fallen, POWER, 1.0, CHAINS, 3.0)).execute(context)) == null,
                "Chain Lightning must skip a fallen target without an error");
        helper.assertTrue(castError(() -> burst(Map.of(TARGET, fallen, POWER, 1.0)).execute(context)) == null,
                "Burst must skip a fallen target without an error");
        helper.assertTrue(castError(() -> barrier(Map.of(TARGET, fallen, POWER, 1.0, TIME, 10.0)).execute(context)) == null,
                "Barrier must skip a fallen target without an error");
        helper.assertTrue(near(bystander.getHealth(), 20) && shield(fallen) == 0.0F,
                "Nothing must happen when the target has fallen");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityChainLightning.class, helper.getBounds().inflate(4)).isEmpty(),
                "No chain must start from a fallen target");
        helper.assertTrue(messages.size() == 3 && messages.stream().allMatch(message -> message.getContents()
                        instanceof TranslatableContents contents && SpellWarning.TARGET_FALLEN.equals(contents.getKey())),
                "Each skip must warn the caster, got " + messages);

        // An Error Suppressor hides warnings as it hides Psi's errors.
        var suppressed = new CompiledSpell(new Spell());
        suppressed.metadata.errorsSuppressed = true;
        var quietContext = new SpellContext().setPlayer(player).setCompiledSpell(suppressed);
        messages.clear();
        helper.assertTrue(castError(() -> burst(Map.of(TARGET, fallen, POWER, 1.0)).execute(quietContext)) == null,
                "Burst must skip a fallen target without an error when errors are suppressed");
        helper.assertTrue(messages.isEmpty(), "An Error Suppressor must hide the warning, got " + messages);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void chainLightningJumpsHalveDownToOne(GameTestHelper helper) {
        helper.assertTrue(near(PieceTrickChainLightning.jumpDamage(18.0F, 0), 18) && near(PieceTrickChainLightning.jumpDamage(18.0F, 1), 9)
                && near(PieceTrickChainLightning.jumpDamage(18.0F, 3), 2.25), "Each jump must halve the damage");
        helper.assertTrue(near(PieceTrickChainLightning.jumpDamage(18.0F, 5), 1) && near(PieceTrickChainLightning.jumpDamage(18.0F, 8), 1),
                "The jumps must not fall below 1");
        helper.assertTrue(near(PieceTrickChainLightning.jumpDamage(0.6F, 3), 0.6),
                "A first hit weaker than 1 must not grow along the chain");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void burstPiercesArmorAndSplashesOnKill(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 2), "burst-caster");
        var context = new SpellContext().setPlayer(player);
        var armored = tough(helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(1, 0, 0)));
        armored.setNoAi(true);
        armored.getAttribute(Attributes.ARMOR).setBaseValue(20.0);
        var golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(4, 0, 0));
        golem.setNoAi(true);
        var doomed = husk(helper, new BlockPos(0, 0, 4), false);
        doomed.setHealth(5.0F);
        var neighbour = tough(husk(helper, new BlockPos(1, 0, 4), false));

        burst(Map.of(TARGET, armored, POWER, 1.0)).execute(context);
        helper.assertTrue(near(armored.getHealth(), TOUGH_HEALTH - 66), "Burst must deal 36 + 30 x Power through armor: " + armored.getHealth());
        burst(Map.of(TARGET, golem, POWER, 1.0)).execute(context);
        helper.assertTrue(near(golem.getMaxHealth() - golem.getHealth(), 16.5), "Golems must take a quarter: "
                + (golem.getMaxHealth() - golem.getHealth()));
        helper.assertTrue(near(neighbour.getHealth(), TOUGH_HEALTH), "No splash without a kill");

        burst(Map.of(TARGET, doomed, POWER, 1.0)).execute(context);
        // 40% of 66, 1 block away out of 3 with half damage at the edge.
        float splash = 66.0F * 0.4F * (1.0F - 0.5F / 3.0F);
        helper.assertTrue(!doomed.isAlive() && near(TOUGH_HEALTH - neighbour.getHealth(), splash),
                "A kill must splash " + splash + ", got " + (TOUGH_HEALTH - neighbour.getHealth()));
        helper.assertTrue(near(armored.getHealth(), TOUGH_HEALTH - 66), "The splash must stay within 3 blocks");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void mineGenesisLaunchesOnlyGroundedCreatures(GameTestHelper helper) throws Exception {
        // The helper's y = 0 is the layer under the test box; the ground below it has its top at y = 0.
        var player = caster(helper, new BlockPos(0, 0, 4), "mine-caster");
        var context = new SpellContext().setPlayer(player);
        // 16 + 12 x Power would kill a plain husk, so give it room to show the ring hits only once.
        var grounded = tough(husk(helper, new BlockPos(3, 0, 2), true));
        var floating = husk(helper, new Vec3(1.5, 1.5, 2.5), false);
        Vec3 center = helper.absoluteVec(new Vec3(2.5, 0.0, 2.5));

        helper.assertTrue(PieceTrickMineGenesis.ERROR_NO_GROUND.equals(castError(() -> mineGenesis(Map.of(
                POSITION, vector(center.add(0, 3.6, 0)), POWER, 1.0, RADIUS, 3.0)).execute(context))),
                "A position more than 3 blocks above the ground must fail");
        mineGenesis(Map.of(POSITION, vector(center), POWER, 1.0, RADIUS, 3.0)).execute(context);
        double groundY = grounded.getY();
        // 28 at the centre, falling to 70% at the radius: 1 block of 3 out.
        float expected = 28.0F * (1.0F - 0.3F / 3.0F);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertTrue(near(TOUGH_HEALTH - grounded.getHealth(), expected), "Grounded husk must take " + expected
                            + ", took " + (TOUGH_HEALTH - grounded.getHealth()));
                    helper.assertTrue(grounded.getY() > groundY + 0.3, "Grounded husk must be thrown up");
                })
                .thenWaitUntil(() -> helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityGroundShockwave.class,
                        helper.getBounds().inflate(4)).isEmpty(), "The shockwave must fade after spreading"))
                .thenExecute(() -> {
                    helper.assertTrue(near(floating.getHealth(), 20), "A creature in the air must be left alone: health "
                            + floating.getHealth() + " from " + floating.getLastDamageSource() + ", on ground " + floating.onGround()
                            + " at " + floating.position().subtract(helper.absoluteVec(Vec3.ZERO)));
                    helper.assertTrue(near(TOUGH_HEALTH - grounded.getHealth(), expected), "The ring hits each creature once");
                    helper.assertTrue(player.isAlive(), "Caster gone");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void kamaitachiCutsEveryCreatureInItsPathOnce(GameTestHelper helper) throws Exception {
        for (int y = 0; y < 4; y++) {
            for (int z = 0; z < 5; z++) {
                helper.setBlock(new BlockPos(4, y, z), Blocks.STONE);
            }
        }
        var player = caster(helper, new BlockPos(0, 0, 0), "kamaitachi-caster");
        var near = husk(helper, new BlockPos(1, 0, 2), false);
        var far = husk(helper, new BlockPos(2, 0, 2), false);
        var aside = husk(helper, new BlockPos(2, 0, 0), false);
        kamaitachi(Map.of(POSITION, vector(helper.absoluteVec(new Vec3(0.5, 1.0, 2.5))), DIRECTION, new Vector3(1, 0, 0),
                POWER, 1.0)).execute(new SpellContext().setPlayer(player));
        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertTrue(near(near.getHealth(), 11) && near(far.getHealth(), 11),
                            "Both husks in the path must be cut once for 4.5 + 4.5 x Power: " + near.getHealth() + ", " + far.getHealth());
                    helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityKamaitachiBlade.class,
                            helper.getBounds().inflate(4)).isEmpty(), "The blade must break on the wall");
                })
                .thenIdle(5)
                .thenExecute(() -> {
                    helper.assertTrue(near(near.getHealth(), 11) && near(far.getHealth(), 11), "Each creature is cut only once");
                    helper.assertTrue(near(aside.getHealth(), 20), "A husk 2 blocks to the side of a 1.75-wide blade is missed");
                    helper.assertTrue(player.isAlive(), "Caster gone");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH + "_blades", timeoutTicks = 160)
    public static void dancingBladesStrikeHostilesAndReplaceOldOnes(GameTestHelper helper) throws Exception {
        // The orbit (1.6 blocks) must stay inside the box: only the box's chunks are sure to tick.
        var player = caster(helper, new BlockPos(2, 0, 2), "blades-caster");
        var context = new SpellContext().setPlayer(player);
        var husk = husk(helper, new BlockPos(4, 0, 4), false);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(0, 0, 4));
        pig.setNoAi(true);
        AABB area = helper.getBounds().inflate(8);

        dancingBlades(Map.of(COUNT, 3.0, POWER, 1.0)).execute(context);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityDancingBlade.class, area).size() == 3, "Three blades expected");
        dancingBlades(Map.of(COUNT, 2.0, POWER, 1.0)).execute(context);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityDancingBlade.class, area).size() == 2,
                "Casting again must replace the blades");
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(husk.getHealth() <= 12 + EPSILON,
                        "Two cuts of 2 + 2 x Power expected, health " + husk.getHealth()))
                .thenWaitUntil(() -> {
                    var left = helper.getLevel().getEntitiesOfClass(EntityDancingBlade.class, area);
                    helper.assertTrue(left.isEmpty(), "Blades must fade after two cuts each: " + left.stream()
                            .map(blade -> blade.tickCount + "t state " + blade.getState() + " at "
                                    + blade.position().subtract(helper.absoluteVec(Vec3.ZERO)))
                            .toList() + ", husk " + husk.getHealth());
                })
                .thenExecute(() -> {
                    helper.assertTrue(near(husk.getHealth(), 4), "Two blades x two cuts x 4 expected, health " + husk.getHealth());
                    helper.assertTrue(near(pig.getHealth(), pig.getMaxHealth()), "Blades must leave animals alone");
                    helper.assertTrue(player.isAlive(), "Caster gone");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH + "_starfall")
    public static void starfallCallsMeteorsAroundThePosition(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 4), "starfall-caster");
        Vec3 center = helper.absoluteVec(new Vec3(2.5, 0.0, 2.5));
        starfall(Map.of(POSITION, vector(center), POWER, 1.0, COUNT, 3.0)).execute(new SpellContext().setPlayer(player));
        List<EntityStarfallMeteor> meteors = helper.getLevel().getEntitiesOfClass(EntityStarfallMeteor.class,
                new AABB(center, center).inflate(8));
        meteors.sort(Comparator.comparingInt(EntityStarfallMeteor::getDelay));
        try {
            helper.assertTrue(meteors.size() == 3, "Three meteors expected, got " + meteors.size());
            helper.assertTrue(meteors.get(0).getDelay() == 20 && meteors.get(1).getDelay() == 25 && meteors.get(2).getDelay() == 30,
                    "Meteors must land 1 s after the cast, then every 5 ticks");
            Vec3 first = meteors.get(0).getTarget();
            helper.assertTrue(near(first.x, center.x) && near(first.z, center.z) && near(first.y, center.y),
                    "The first meteor must land on the ground at the centre: " + first);
            for (EntityStarfallMeteor meteor : meteors) {
                Vec3 target = meteor.getTarget();
                helper.assertTrue(Math.hypot(target.x - center.x, target.z - center.z) <= 4.0 + EPSILON,
                        "Meteors must land within 4 blocks: " + target);
                helper.assertTrue(meteor.getStart().y > target.y + 20, "Meteors must fall from the sky");
            }
        } finally {
            meteors.forEach(EntityStarfallMeteor::discard);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void starfallMeteorBurnsWhatItHits(GameTestHelper helper) {
        var player = caster(helper, new BlockPos(0, 0, 4), "meteor-caster");
        var hit = husk(helper, new BlockPos(2, 0, 2), false);
        var spared = husk(helper, new BlockPos(4, 0, 4), false);
        Vec3 target = helper.absoluteVec(new Vec3(2.5, 0.0, 2.5));
        var meteor = new EntityStarfallMeteor(helper.getLevel(), target.add(0, 8, 0), target, 0);
        meteor.configure(player, false, 9.0F, CombatSpellHelper.DEFAULT_SPELL_COLOR);
        helper.getLevel().addFreshEntity(meteor);
        helper.succeedWhen(() -> {
            helper.assertTrue(hit.getHealth() < 20 - 7, "The husk under the meteor must be hit, health " + hit.getHealth());
            helper.assertTrue(hit.getRemainingFireTicks() > 0, "The husk under the meteor must burn");
            helper.assertTrue(meteor.isRemoved(), "The meteor must be gone after landing");
            helper.assertTrue(near(spared.getHealth(), 20) && spared.getRemainingFireTicks() <= 0,
                    "A husk 2.7 blocks away must be outside the blast");
            helper.assertTrue(player.isAlive(), "Caster gone");
        });
    }

    @GameTest(template = TEMPLATE, batch = BATCH, timeoutTicks = 200)
    public static void gravityCollapsePullsInThenCrushes(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 4), "gravity-caster");
        // 60 + 50 x Power would kill a plain husk, so give it room to show the collapse hits only once.
        var husk = tough(husk(helper, new BlockPos(4, 0, 2), true));
        husk.getAttribute(Attributes.ARMOR).setBaseValue(20.0);
        double startX = husk.getX();
        Vec3 center = helper.absoluteVec(new Vec3(2.5, 1.5, 2.5));
        gravityCollapse(Map.of(POSITION, vector(center), POWER, 1.0, RADIUS, 4.0)).execute(new SpellContext().setPlayer(player));
        helper.startSequence()
                .thenExecuteAfter(15, () -> {
                    helper.assertTrue(husk.getX() < startX - 0.4, "The well must drag the husk in: moved " + (startX - husk.getX()));
                    helper.assertTrue(near(husk.getHealth(), TOUGH_HEALTH), "No damage before the collapse");
                })
                .thenWaitUntil(() -> helper.assertTrue(husk.getHealth() < TOUGH_HEALTH - 55 + EPSILON,
                        "The collapse must crush through armor for at least half of 60 + 50 x Power, health " + husk.getHealth()))
                .thenWaitUntil(() -> helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityGravityWell.class,
                        helper.getBounds().inflate(4)).isEmpty(), "The well must close after collapsing"))
                .thenExecute(() -> helper.assertTrue(player.isAlive() && husk.getHealth() > TOUGH_HEALTH - 110 - EPSILON,
                        "The collapse hits once, health " + husk.getHealth()))
                .thenSucceed();
    }
}
