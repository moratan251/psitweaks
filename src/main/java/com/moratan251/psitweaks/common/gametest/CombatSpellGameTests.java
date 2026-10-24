package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.entities.combat.EntityChainLightning;
import com.moratan251.psitweaks.common.entities.combat.EntityDancingBlade;
import com.moratan251.psitweaks.common.entities.combat.EntityGravityWell;
import com.moratan251.psitweaks.common.entities.combat.EntityGroundShockwave;
import com.moratan251.psitweaks.common.entities.combat.EntityKamaitachiBlade;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.entities.combat.EntityStarfallMeteor;
import com.moratan251.psitweaks.common.items.BladePouches;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.menu.BladePouchMenu;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.SpellWarning;
import com.moratan251.psitweaks.common.spells.combat.BarrierShield;
import com.moratan251.psitweaks.common.spells.combat.BarrierShields;
import com.moratan251.psitweaks.common.spells.combat.CocytusFreezes;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import com.moratan251.psitweaks.common.spells.combat.SwordStrikes;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickActiveAirMine;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickBarrier;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickBurst;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickChainLightning;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickDancingBlades;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGravityCollapse;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickKamaitachi;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickMineGenesis;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickParade;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickStarfall;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
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
import vazkii.psi.api.spell.ISpellImmune;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityPosition;

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

    /** Turns the caster to look at the centre of {@code target}, as Burst and Chain Lightning aim by the caster's view. */
    private static void lookAt(Player caster, Entity target) {
        caster.lookAt(EntityAnchorArgument.Anchor.EYES, CombatSpellHelper.center(target));
    }

    /** Turns the caster to look straight up, where no creature stands. */
    private static void lookUp(Player caster) {
        caster.setXRot(-90.0F);
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

    private static PieceTrickActiveAirMine activeAirMine(Map<String, ?> values) {
        return new PieceTrickActiveAirMine(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceTrickParade parade(Map<String, ?> values) {
        return new PieceTrickParade(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceOperatorEntityPosition entityPosition(Entity target) {
        return new PieceOperatorEntityPosition(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return target; }
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
    public static void barrierShieldGrowsWithPhenomenonInterference(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "barrier-interference");
        player.getAttribute(PsitweaksAttributes.SPELL_DAMAGE_FACTOR).setBaseValue(1.5);
        var husk = husk(helper, new BlockPos(2, 0, 2), false);
        barrier(Map.of(TARGET, husk, POWER, 2.0)).execute(new SpellContext().setPlayer(player));
        helper.assertTrue(near(shield(husk), 30), "Power 2 at 1.5x Phenomenon Interference must raise 30, got " + shield(husk));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void barrierShieldTakesHitsBeforeHealth(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "barrier-caster");
        var context = new SpellContext().setPlayer(player);
        var husk = husk(helper, new BlockPos(2, 0, 2), false);
        var sources = helper.getLevel().damageSources();

        barrier(Map.of(TARGET, husk, POWER, 2.0)).execute(context);
        BarrierShield raised = husk.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD);
        helper.assertTrue(raised != null && near(raised.amount(), 20) && near(raised.maxAmount(), 20),
                "Power 2 must raise a shield of 20: " + raised);

        husk.hurt(sources.generic(), 8.0F);
        helper.assertTrue(near(husk.getHealth(), 20) && near(shield(husk), 12),
                "The shield must take the whole hit: health " + husk.getHealth() + ", shield " + shield(husk));

        husk.invulnerableTime = 0;
        barrier(Map.of(TARGET, husk, POWER, 1.0)).execute(context);
        helper.assertTrue(near(shield(husk), 10), "Casting again must replace the shield, not add to it: " + shield(husk));

        husk.hurt(sources.generic(), 15.0F);
        helper.assertTrue(near(husk.getHealth(), 15) && husk.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD) == null,
                "Damage beyond the shield must go through and break it: health " + husk.getHealth());

        husk.invulnerableTime = 0;
        barrier(Map.of(TARGET, husk, POWER, 1.0)).execute(context);
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
        barrier(Map.of(TARGET, husk, POWER, 1.0)).execute(new SpellContext().setPlayer(player));
        BarrierShield raised = husk.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD);
        long lasts = raised == null ? 0L : raised.expiresAt() - helper.getLevel().getGameTime();
        helper.assertTrue(lasts == 1200L, "The shield must last one minute, got " + lasts + " ticks");

        // A minute is too long to wait for here, so a one-second shield stands in for the timer.
        BarrierShields.raise(husk, 10.0F, 20, 0xFFFFFF);
        helper.runAtTickTime(10, () -> helper.assertTrue(near(shield(husk), 10), "The shield faded too early"));
        helper.succeedWhen(() -> helper.assertTrue(husk.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD) == null,
                "A one-second shield must be gone after its time"));
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void barrierShieldKeepsTheTargetFromBurning(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "barrier-fire");
        var husk = husk(helper, new BlockPos(2, 0, 2), false);
        husk.igniteForSeconds(8.0F);
        barrier(Map.of(TARGET, husk, POWER, 1.0)).execute(new SpellContext().setPlayer(player));
        helper.assertTrue(husk.getRemainingFireTicks() <= 0, "Raising the shield must put the fire out: " + husk.getRemainingFireTicks());

        husk.igniteForSeconds(8.0F);
        helper.assertTrue(husk.getRemainingFireTicks() <= 0, "A shielded target must not catch fire: " + husk.getRemainingFireTicks());

        husk.removeData(PsitweaksAttachments.BARRIER_SHIELD);
        husk.igniteForSeconds(8.0F);
        helper.assertTrue(husk.getRemainingFireTicks() == 160, "Without the shield it must catch fire: " + husk.getRemainingFireTicks());
        helper.succeed();
    }


    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void barrierShieldLetsPoisonThrough(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 0), "barrier-poison");
        var husk = husk(helper, new BlockPos(2, 0, 2), false);
        barrier(Map.of(TARGET, husk, POWER, 1.0)).execute(new SpellContext().setPlayer(player));

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
        barrier(Map.of(TARGET, husk, POWER, 1.0)).execute(new SpellContext().setPlayer(player));

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
        int[] barrier = stats(barrier(Map.of(POWER, 2.0)));
        helper.assertTrue(barrier[0] == 140 && barrier[1] == 520, "Barrier P2: 80+15P² / 280+60P², got " + barrier[0] + "/" + barrier[1]);
        helper.assertTrue(SpellCompilationException.NON_POSITIVE_INTEGER.equals(compileError(
                () -> stats(barrier(Map.of(POWER, 1.5))))), "Barrier Power must be a whole number");

        int[] chain = stats(chainLightning(Map.of(POWER, 2.0)));
        helper.assertTrue(chain[0] == 380 && chain[1] == 1030, "Chain Lightning P2: 300+40P / 790+120P, got " + chain[0] + "/" + chain[1]);
        int chainP1 = stats(chainLightning(Map.of(POWER, 1.0)))[0];
        helper.assertTrue(chainP1 <= 350, "Chain Lightning at Power 1 must fit an ebony psimetal CAD (350), got " + chainP1);
        helper.assertTrue(SpellCompilationException.NON_POSITIVE_VALUE.equals(compileError(
                () -> stats(chainLightning(Map.of(POWER, 0.0))))), "Chain Lightning Power must be positive");

        int[] mine = stats(mineGenesis(Map.of(POWER, 1.0, RADIUS, 3.0)));
        helper.assertTrue(mine[0] == 185 && mine[1] == 630, "Mine Genesis P1 R3, got " + mine[0] + "/" + mine[1]);
        int[] burst = stats(burst(Map.of(POWER, 1.0)));
        helper.assertTrue(burst[0] == 1240 && burst[1] == 1050, "Burst P1, got " + burst[0] + "/" + burst[1]);
        int[] parade = stats(parade(Map.of(POWER, 2.0, TIME, 30.0)));
        helper.assertTrue(parade[0] == 660 && parade[1] == 900, "Parade P2 T30: 300+150P+2T / 400+100P+10T, got " + parade[0] + "/" + parade[1]);
        int[] kamaitachi = stats(kamaitachi(Map.of(POWER, 1.0)));
        helper.assertTrue(kamaitachi[0] == 120 && kamaitachi[1] == 420, "Kamaitachi P1, got " + kamaitachi[0] + "/" + kamaitachi[1]);
        int[] blades = stats(dancingBlades(Map.of()));
        helper.assertTrue(blades[0] == 900 && blades[1] == 1500, "Dancing Blades has fixed stats, got " + blades[0] + "/" + blades[1]);
        int[] starfall = stats(starfall(Map.of(POWER, 1.0, COUNT, 13.0)));
        helper.assertTrue(starfall[0] == 1160 && starfall[1] == 7680, "Meteors must be capped at 12, got " + starfall[0] + "/" + starfall[1]);
        int[] gravity = stats(gravityCollapse(Map.of(POWER, 1.0, RADIUS, 30.0)));
        helper.assertTrue(gravity[0] == 1340 && gravity[1] == 10350, "Gravity radius must be capped at 24, got " + gravity[0] + "/" + gravity[1]);
        int widest = stats(gravityCollapse(Map.of(POWER, 0.5, RADIUS, 24.0)))[0];
        helper.assertTrue(widest <= 1500, "The widest Gravity Collapse must still fit a psycheonic metal CAD, got " + widest);
        helper.succeed();
    }

    /**
     * The smallest Dancing Blades and Starfall need a heavy psimetal CAD (more than flashmetal's 800, within the beta's
     * 1000); the smallest Burst and Gravity Collapse need a psycheonic metal CAD (more than the heavy alpha's 1200,
     * within 1500).
     */
    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void largeCombatTricksNeedHighTierCads(GameTestHelper helper) throws Exception {
        int blades = stats(dancingBlades(Map.of()))[0];
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

        lookAt(player, first);
        chainLightning(Map.of(POWER, 1.0)).execute(new SpellContext().setPlayer(player));
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

    @GameTest(template = TEMPLATE, batch = BATCH + "_chain")
    public static void indiscriminateChainLightningJumpsToAnyCreatureButPets(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 4), "chain-indiscriminate");
        var first = tough(husk(helper, new BlockPos(1, 0, 1), false));
        // Nearest first: the villager, then the husk and the pig. The pet and the armor stand in between are skipped.
        var villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(1, 0, 2));
        villager.setNoAi(true);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 0, 1));
        pig.setNoAi(true);
        var pet = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(2, 0, 2));
        pet.setTame(true, false);
        pet.setOwnerUUID(player.getUUID());
        var stand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(2, 0, 1));

        helper.assertTrue(CombatSpellHelper.isIndiscriminateTarget(player, pig, false)
                        && CombatSpellHelper.isIndiscriminateTarget(player, villager, false)
                        && CombatSpellHelper.isIndiscriminateTarget(player, first, false),
                "Indiscriminate picks animals, villagers and monsters");
        helper.assertTrue(!CombatSpellHelper.isIndiscriminateTarget(player, pet, false)
                        && !CombatSpellHelper.isIndiscriminateTarget(player, stand, false)
                        && !CombatSpellHelper.isIndiscriminateTarget(player, player, false),
                "Indiscriminate still spares pets, armor stands and the caster");
        helper.assertTrue(CombatSpellHelper.isIndiscriminate(1) && !CombatSpellHelper.isIndiscriminate(0)
                && !CombatSpellHelper.isIndiscriminate(null), "Only a non-zero number turns it on");

        // Looking at the husk, the villager in between is the nearest creature in front, so it is struck first.
        lookAt(player, first);
        chainLightning(Map.of(POWER, 1.0, PsitweaksSpellParams.INDISCRIMINATE, 1.0)).execute(new SpellContext().setPlayer(player));
        helper.assertTrue(!villager.isAlive() || villager.getHealth() < villager.getMaxHealth() - EPSILON,
                "An indiscriminate bolt must strike the nearest creature in front, the villager");
        helper.assertTrue(near(first.getHealth(), TOUGH_HEALTH), "The husk behind the villager must be reached by a jump");
        float petHealth = pet.getHealth();
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(first.getHealth() < TOUGH_HEALTH - EPSILON, "The bolt must jump to the husk"))
                .thenWaitUntil(() -> helper.assertTrue(!pig.isAlive() || pig.getHealth() < pig.getMaxHealth() - EPSILON,
                        "The bolt must jump on to the pig"))
                .thenWaitUntil(() -> helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityChainLightning.class,
                        helper.getBounds().inflate(4)).isEmpty(), "The chain must finish"))
                .thenExecute(() -> helper.assertTrue(near(pet.getHealth(), petHealth), "The pet must be spared"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH + "_chain_front")
    public static void chainLightningStrikesTheNearestHostileInFront(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 2), "chain-front");
        var context = new SpellContext().setPlayer(player);
        // The caster looks along +x. Nearer than anything ahead, but to the side.
        var beside = tough(husk(helper, new BlockPos(0, 0, 0), false));
        // Nearest ahead, but not hostile.
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 0, 2));
        pig.setNoAi(true);
        var ahead = tough(husk(helper, new BlockPos(4, 0, 2), false));
        // About 20 degrees off the line of sight and nearer than the husk straight ahead.
        var offAxis = tough(husk(helper, new BlockPos(3, 0, 3), false));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition().add(1.0, 0.0, 0.0));

        chainLightning(Map.of(POWER, 1.0)).execute(context);
        helper.assertTrue(offAxis.getHealth() < TOUGH_HEALTH - EPSILON,
                "The bolt must strike the nearest hostile within 30 degrees of the view, not just what is straight ahead");
        helper.assertTrue(near(ahead.getHealth(), TOUGH_HEALTH) && near(beside.getHealth(), TOUGH_HEALTH),
                "The others may only be reached by later jumps");
        helper.assertTrue(near(pig.getHealth(), pig.getMaxHealth()), "The pig in front must be passed over");

        lookUp(player);
        helper.assertTrue(SpellRuntimeException.NULL_TARGET.equals(castError(() -> chainLightning(Map.of(POWER, 1.0)).execute(context))),
                "With nothing in front the spell must fail as with no target");
        helper.getLevel().getEntitiesOfClass(EntityChainLightning.class, helper.getBounds().inflate(4)).forEach(Entity::discard);
        helper.succeed();
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
        // Off to the side, out of Chain Lightning's view, but within a jump of the fallen husk.
        var bystander = husk(helper, new BlockPos(4, 0, 4), false);
        lookAt(player, fallen);
        helper.assertTrue(castError(() -> chainLightning(Map.of(POWER, 1.0)).execute(context)) == null,
                "Chain Lightning must skip a fallen creature in front without an error");
        helper.assertTrue(castError(() -> burst(Map.of(POWER, 1.0)).execute(context)) == null,
                "Burst must skip a fallen target without an error");
        helper.assertTrue(castError(() -> barrier(Map.of(TARGET, fallen, POWER, 1.0)).execute(context)) == null,
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
        helper.assertTrue(castError(() -> burst(Map.of(POWER, 1.0)).execute(quietContext)) == null,
                "Burst must skip a fallen target without an error when errors are suppressed");
        helper.assertTrue(messages.isEmpty(), "An Error Suppressor must hide the warning, got " + messages);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void cocytusFreezesUntilSomethingWakesTheMobUp(GameTestHelper helper) {
        var husk = husk(helper, new BlockPos(1, 0, 1), true);
        var quiet = husk(helper, new BlockPos(3, 0, 1), true);
        quiet.setSilent(true);
        CocytusFreezes.freeze(helper.getLevel(), husk);
        CocytusFreezes.freeze(helper.getLevel(), quiet);
        helper.assertTrue(husk.isNoAi() && husk.isSilent() && CocytusFreezes.isFrozen(husk), "Cocytus must freeze the husk");
        // Freezing again must not forget that the husk could make sounds before.
        CocytusFreezes.freeze(helper.getLevel(), husk);

        husk.setNoAi(false);
        quiet.setNoAi(false);
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(!CocytusFreezes.isFrozen(husk) && !husk.hasData(PsitweaksAttachments.COCYTUS_FREEZE),
                            "The ice must melt once the AI is back");
                    helper.assertTrue(!husk.isSilent(), "The thawed husk must get its voice back");
                    helper.assertTrue(quiet.isSilent() && !quiet.hasData(PsitweaksAttachments.COCYTUS_FREEZE),
                            "A mob that was silent before must stay silent");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void activeAirMineStillHitsWithItsNewShockwave(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 4), "air-mine-caster");
        var husk = tough(husk(helper, new BlockPos(2, 0, 2), false));
        activeAirMine(Map.of(POSITION, vector(helper.absoluteVec(new Vec3(2.5, 1.0, 2.5))), POWER, 1.0))
                .execute(new SpellContext().setPlayer(player));
        helper.assertTrue(husk.getHealth() < TOUGH_HEALTH - EPSILON, "The blast must still hurt: " + husk.getHealth());
        helper.assertTrue(husk.getLastDamageSource() != null && husk.getLastDamageSource().is(PsitweaksDamageTypes.ACTIVE_AIR_MINE),
                "The blast must use its own damage type (shockwave death message), got " + husk.getLastDamageSource());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void attackTricksHitBossesAndRiders(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 4), "boss-caster");
        var context = new SpellContext().setPlayer(player);
        var wither = helper.spawnWithNoFreeWill(EntityType.WITHER, new BlockPos(2, 0, 2));
        var rider = husk(helper, new BlockPos(4, 0, 4), false);
        var mount = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 0, 4));
        rider.startRiding(mount, true);
        try {
            helper.assertTrue(ISpellImmune.isImmune(wither) && ISpellImmune.isImmune(rider),
                    "Psi itself treats bosses and riders as immune");
            float witherHealth = wither.getHealth();
            lookAt(player, wither);
            helper.assertTrue(castError(() -> burst(Map.of(POWER, 1.0)).execute(context)) == null,
                    "Burst must accept the Wither");
            helper.assertTrue(wither.getHealth() < witherHealth, "Burst must hurt the Wither");
            float afterBurst = wither.getHealth();
            helper.assertTrue(castError(() -> chainLightning(Map.of(POWER, 1.0)).execute(context)) == null,
                    "Chain Lightning must accept the Wither");
            helper.assertTrue(wither.getHealth() < afterBurst, "Chain Lightning must hurt the Wither");
            lookAt(player, rider);
            helper.assertTrue(castError(() -> burst(Map.of(POWER, 1.0)).execute(context)) == null
                    && rider.getHealth() < 20, "Burst must hit a creature riding another");
        } finally {
            wither.discard();
            helper.getLevel().getEntitiesOfClass(EntityChainLightning.class, helper.getBounds().inflate(4)).forEach(Entity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void paradeConjuresALookalikeThatTakesHitsForItsOwner(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 4), "parade-caster");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        Vec3 spot = helper.absoluteVec(new Vec3(3.5, 0.0, 1.5));
        parade(Map.of(POSITION, vector(spot), POWER, 1.0, TIME, 10.0)).execute(new SpellContext().setPlayer(player));
        EntityParadeDecoy decoy = Parades.activeDecoy(player);
        helper.assertTrue(decoy != null && decoy.isOwnedBy(player) && decoy.position().distanceTo(spot) < 0.5,
                "A lookalike must stand at the position");
        helper.assertTrue(decoy.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET)
                        && player.getName().getString().equals(decoy.getName().getString()),
                "The lookalike must wear the caster's gear and name");
        helper.assertTrue(decoy.getHitsLeft() == PieceTrickParade.HITS_PER_POWER, "Power 1 must last 2 hits");
        var sources = helper.getLevel().damageSources();
        helper.assertTrue(decoy.hurt(sources.generic(), 5.0F) && decoy.isAlive() && decoy.getHealth() == decoy.getMaxHealth(),
                "A hit must use up one hit, not health");
        helper.startSequence()
                .thenExecuteAfter(11, () -> {
                    decoy.hurt(sources.generic(), 5.0F);
                    helper.assertTrue(decoy.isRemoved() && Parades.activeDecoy(player) == null, "The last hit must end it");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void paradeLookalikeMimicsItsOwnerAndFacesItsEnemy(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 4), "parade-mimic");
        player.setYRot(90.0F);
        player.setYHeadRot(90.0F);
        parade(Map.of(POSITION, vector(helper.absoluteVec(new Vec3(2.5, 0.0, 2.5))), POWER, 2.0, TIME, 10.0))
                .execute(new SpellContext().setPlayer(player));
        EntityParadeDecoy decoy = Parades.activeDecoy(player);
        helper.assertTrue(decoy != null, "No lookalike");
        player.setShiftKeyDown(true);
        player.setPose(Pose.CROUCHING);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        Husk[] hunter = new Husk[1];
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(decoy.isCrouching() && decoy.getMainHandItem().is(Items.DIAMOND_SWORD),
                            "The lookalike must crouch and change gear with its owner");
                    // It wanders, so check where it wants to look rather than where its head is right now.
                    Vec3 wanted = new Vec3(decoy.getLookControl().getWantedX(), decoy.getLookControl().getWantedY(),
                            decoy.getLookControl().getWantedZ()).subtract(decoy.getEyePosition());
                    helper.assertTrue(wanted.normalize().dot(player.getLookAngle()) > 0.99,
                            "With no enemy it must look where its owner looks, wants " + wanted);
                    // A husk east of the lookalike that goes for it.
                    hunter[0] = husk(helper, new BlockPos(5, 0, 2), false);
                    hunter[0].setTarget(decoy);
                })
                // It rescans who is after it every few ticks; wait for it rather than for an exact tick.
                .thenWaitUntil(() -> {
                    var husk = hunter[0];
                    Vec3 wanted = new Vec3(decoy.getLookControl().getWantedX(), decoy.getLookControl().getWantedY(),
                            decoy.getLookControl().getWantedZ());
                    helper.assertTrue(wanted.distanceTo(husk.getEyePosition()) < 0.5,
                            "It must turn to the mob after it, wants " + wanted + ", husk at " + husk.getEyePosition());
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void paradeDrawsMobsAwayFromItsCaster(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 4), "parade-lure");
        var husk = husk(helper, new BlockPos(1, 0, 1), false);
        husk.setTarget(player);
        parade(Map.of(POSITION, vector(helper.absoluteVec(new Vec3(4.5, 0.0, 1.5))), POWER, 2.0, TIME, 10.0))
                .execute(new SpellContext().setPlayer(player));
        EntityParadeDecoy decoy = Parades.activeDecoy(player);
        helper.assertTrue(decoy != null && husk.getTarget() == decoy, "A mob after the caster must turn to the lookalike");
        husk.setTarget(player);
        helper.assertTrue(husk.getTarget() == decoy, "A mob that picks the caster again must get the lookalike");
        decoy.vanish(false);
        husk.setTarget(player);
        helper.assertTrue(husk.getTarget() == player, "Once the lookalike is gone the caster can be targeted");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void paradeHidesItsCasterFromOtherPlayersSpells(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        boolean pvp = server.isPvpAllowed();
        server.setPvpAllowed(true);
        try {
            var player = caster(helper, new BlockPos(0, 0, 4), "parade-hidden");
            // Fake players never harm players; the rival stands in for a real one while PvP is on.
            var rival = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "parade-rival")) {
                @Override
                public boolean canHarmPlayer(Player other) {
                    return true;
                }
            };
            rival.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 0, 4))));
            var own = new SpellContext().setPlayer(player);
            var theirs = new SpellContext().setPlayer(rival);
            Vec3 spot = helper.absoluteVec(new Vec3(2.5, 0.0, 0.5));
            parade(Map.of(POSITION, vector(spot), POWER, 1.0, TIME, 10.0)).execute(own);
            EntityParadeDecoy decoy = Parades.activeDecoy(player);
            helper.assertTrue(decoy != null, "No lookalike");

            helper.assertTrue(Parades.ERROR_DISGUISED.equals(castError(() -> theirs.verifyEntity(player))),
                    "Psi tricks of another player must fail on the hidden caster");
            helper.assertTrue(castError(() -> own.verifyEntity(player)) == null, "The caster's own spells must still work");
            // Burst and Chain Lightning check what they picked the same way.
            helper.assertTrue(Parades.ERROR_DISGUISED.equals(castError(() -> CombatSpellHelper.verifyTarget(theirs, player))),
                    "Attack tricks of another player must fail on the hidden caster");

            // Game mode and PvP do not matter; only team mates see through it.
            player.setGameMode(GameType.CREATIVE);
            helper.assertTrue(Parades.ERROR_DISGUISED.equals(castError(() -> theirs.verifyEntity(player))),
                    "A caster in creative mode must stay hidden");
            player.setGameMode(GameType.SURVIVAL);
            var scoreboard = helper.getLevel().getScoreboard();
            var team = scoreboard.addPlayerTeam("parade_allies_" + UUID.randomUUID().toString().substring(0, 8));
            scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
            scoreboard.addPlayerToTeam(rival.getScoreboardName(), team);
            helper.assertTrue(castError(() -> theirs.verifyEntity(player)) == null, "Team mates must see through it");
            scoreboard.removePlayerTeam(team);

            Vector3 seen = (Vector3) entityPosition(player).execute(theirs);
            Vector3 real = (Vector3) entityPosition(player).execute(own);
            helper.assertTrue(near(seen.x, decoy.getX()) && near(seen.z, decoy.getZ()), "Others must be told the lookalike's position: " + seen);
            helper.assertTrue(near(real.x, player.getX()) && near(real.z, player.getZ()), "The caster must see their own position: " + real);
        } finally {
            server.setPvpAllowed(pvp);
        }
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

        lookAt(player, armored);
        burst(Map.of(POWER, 1.0)).execute(context);
        helper.assertTrue(near(armored.getHealth(), TOUGH_HEALTH - 66), "Burst must deal 36 + 30 x Power through armor: " + armored.getHealth());
        lookAt(player, golem);
        burst(Map.of(POWER, 1.0)).execute(context);
        helper.assertTrue(near(golem.getMaxHealth() - golem.getHealth(), 16.5), "Golems must take a quarter: "
                + (golem.getMaxHealth() - golem.getHealth()));
        helper.assertTrue(near(neighbour.getHealth(), TOUGH_HEALTH), "No splash without a kill");

        lookAt(player, doomed);
        burst(Map.of(POWER, 1.0)).execute(context);
        // 40% of 66, 1 block away out of 3 with half damage at the edge.
        float splash = 66.0F * 0.4F * (1.0F - 0.5F / 3.0F);
        helper.assertTrue(!doomed.isAlive() && near(TOUGH_HEALTH - neighbour.getHealth(), splash),
                "A kill must splash " + splash + ", got " + (TOUGH_HEALTH - neighbour.getHealth()));
        helper.assertTrue(near(armored.getHealth(), TOUGH_HEALTH - 66), "The splash must stay within 3 blocks");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void burstHitsOnlyTheCreatureUnderTheCrosshair(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(0, 0, 2), "burst-aim");
        var context = new SpellContext().setPlayer(player);
        var aimed = tough(husk(helper, new BlockPos(4, 0, 2), false));
        // Nearer and only a block off the crosshair: Burst needs a precise aim, so it is spared.
        var offAim = tough(husk(helper, new BlockPos(2, 0, 3), false));
        Vec3 ahead = player.getEyePosition().add(1.0, 0.0, 0.0);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, ahead);

        burst(Map.of(POWER, 1.0)).execute(context);
        helper.assertTrue(near(aimed.getHealth(), TOUGH_HEALTH - 66), "Burst must hit the husk under the crosshair: " + aimed.getHealth());
        helper.assertTrue(near(offAim.getHealth(), TOUGH_HEALTH), "Burst must leave a husk off the crosshair alone");

        // A solid block in the line of sight hides the husk behind it.
        BlockPos wall = BlockPos.containing(player.getEyePosition().add(2.0, 0.0, 0.0));
        helper.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        helper.assertTrue(SpellRuntimeException.NULL_TARGET.equals(castError(() -> burst(Map.of(POWER, 1.0)).execute(context))),
                "With a wall in the way the spell must fail as with no target");
        helper.getLevel().setBlockAndUpdate(wall, Blocks.AIR.defaultBlockState());
        helper.assertTrue(near(aimed.getHealth(), TOUGH_HEALTH - 66), "The husk behind the wall must be unharmed");

        lookUp(player);
        helper.assertTrue(SpellRuntimeException.NULL_TARGET.equals(castError(() -> burst(Map.of(POWER, 1.0)).execute(context))),
                "Aiming at nothing must fail as with no target");
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

    @GameTest(template = TEMPLATE, batch = BATCH + "_blades", timeoutTicks = 240)
    public static void dancingBladesStrikeHostilesAndReplaceOldOnes(GameTestHelper helper) throws Exception {
        // The orbit (1.6 blocks) must stay inside the box: only the box's chunks are sure to tick.
        var player = caster(helper, new BlockPos(2, 0, 2), "blades-caster");
        var context = new SpellContext().setPlayer(player);
        var husk = husk(helper, new BlockPos(4, 0, 4), false);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(0, 0, 4));
        pig.setNoAi(true);
        AABB area = helper.getBounds().inflate(8);

        // A wooden sword swings for 1 + 3 = 4.
        ItemStack pouch = new ItemStack(PsitweaksItems.BLADE_POUCH.get());
        BladePouches.setContents(pouch, List.of(new ItemStack(Items.WOODEN_SWORD), new ItemStack(Items.WOODEN_SWORD),
                new ItemStack(Items.WOODEN_SWORD)));
        player.getInventory().setItem(9, pouch);
        dancingBlades(Map.of()).execute(context);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityDancingBlade.class, area).size() == 3,
                "Every sword in the pouch must become a blade");
        BladePouches.setContents(pouch, List.of(ItemStack.EMPTY, new ItemStack(Items.WOODEN_SWORD)));
        dancingBlades(Map.of()).execute(context);
        var blades = helper.getLevel().getEntitiesOfClass(EntityDancingBlade.class, area);
        helper.assertTrue(blades.size() == 1 && blades.get(0).getSword().is(Items.WOODEN_SWORD),
                "Casting again must replace the blades with the pouch's swords");
        helper.assertTrue(BladePouches.swords(pouch).size() == 1, "The swords must stay in the pouch");
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(husk.getHealth() <= 16 + EPSILON,
                        "A cut of a wooden sword expected, health " + husk.getHealth()))
                .thenWaitUntil(() -> {
                    var left = helper.getLevel().getEntitiesOfClass(EntityDancingBlade.class, area);
                    helper.assertTrue(left.isEmpty(), "Blades must fade after three cuts each: " + left.stream()
                            .map(blade -> blade.tickCount + "t state " + blade.getState() + " at "
                                    + blade.position().subtract(helper.absoluteVec(Vec3.ZERO)))
                            .toList() + ", husk " + husk.getHealth());
                })
                .thenExecute(() -> {
                    helper.assertTrue(near(husk.getHealth(), 8), "One blade x three cuts x 4 expected, health " + husk.getHealth());
                    helper.assertTrue(near(pig.getHealth(), pig.getMaxHealth()), "Blades must leave animals alone");
                    helper.assertTrue(player.isAlive(), "Caster gone");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void dancingBladesNeedSwordsInABladePouch(GameTestHelper helper) {
        var player = caster(helper, new BlockPos(2, 0, 2), "blades-no-pouch");
        var context = new SpellContext().setPlayer(player);
        helper.assertTrue(PieceTrickDancingBlades.ERROR_NO_SWORDS.equals(castError(() -> dancingBlades(Map.of()).execute(context))),
                "Without a pouch the spell must fail");
        player.getInventory().setItem(9, new ItemStack(PsitweaksItems.BLADE_POUCH.get()));
        helper.assertTrue(PieceTrickDancingBlades.ERROR_NO_SWORDS.equals(castError(() -> dancingBlades(Map.of()).execute(context))),
                "With an empty pouch the spell must fail");
        helper.assertTrue(!BladePouches.isSword(new ItemStack(Items.IRON_AXE)) && BladePouches.isSword(new ItemStack(Items.IRON_SWORD)),
                "Only #minecraft:swords go in a pouch");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void bladeCutsHitLikeTheirSword(GameTestHelper helper) {
        var level = helper.getLevel();
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var husk = husk(helper, new BlockPos(2, 0, 2), false);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 0, 4));
        var source = level.damageSources().generic();

        helper.assertTrue(near(SwordStrikes.meleeDamage(level, new ItemStack(Items.WOODEN_SWORD), husk, source), 4),
                "Wooden sword: 1 + 3");
        helper.assertTrue(near(SwordStrikes.meleeDamage(level, new ItemStack(Items.DIAMOND_SWORD), husk, source), 7),
                "Diamond sword: 1 + 6");
        var sharp = new ItemStack(Items.DIAMOND_SWORD);
        sharp.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 1);
        helper.assertTrue(near(SwordStrikes.meleeDamage(level, sharp, husk, source), 8), "Sharpness I adds 1");
        var smite = new ItemStack(Items.DIAMOND_SWORD);
        smite.enchant(enchantments.getOrThrow(Enchantments.SMITE), 1);
        helper.assertTrue(near(SwordStrikes.meleeDamage(level, smite, husk, source), 9.5)
                        && near(SwordStrikes.meleeDamage(level, smite, pig, source), 7),
                "Smite I adds 2.5 against the undead only");

        var fiery = new ItemStack(Items.DIAMOND_SWORD);
        fiery.enchant(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), 1);
        var player = caster(helper, new BlockPos(0, 0, 0), "blade-fire");
        var playerSource = SwordStrikes.weaponSource(level, PsitweaksDamageTypes.DANCING_BLADES, player, fiery);
        helper.assertTrue(playerSource.getWeaponItem() == fiery && playerSource.isDirect(),
                "The hit must count as a direct swing of the sword");
        SwordStrikes.afterHit(level, player, fiery, husk, playerSource, new Vec3(1, 0, 0));
        helper.assertTrue(husk.getRemainingFireTicks() > 0, "Fire Aspect must set the target alight");

        // The on-hit wear (a sword's postHurtEnemy) falls on a copy, never on the sword in the pouch.
        var worn = new ItemStack(Items.IRON_SWORD);
        worn.setDamageValue(100);
        var copy = SwordStrikes.swingCopy(worn);
        SwordStrikes.afterHit(level, player, copy, husk, playerSource, new Vec3(1, 0, 0));
        helper.assertTrue(worn.getDamageValue() == 100 && copy.getDamageValue() <= 1, "Only the copy may wear down");

        // The item's own bonus counts as in a melee swing: a mace after a 3 block fall adds 4 x 3 to its 1 + 5.
        var mace = new ItemStack(Items.MACE);
        player.fallDistance = 3.0F;
        var maceSource = SwordStrikes.weaponSource(level, PsitweaksDamageTypes.DANCING_BLADES, player, mace);
        float smash = SwordStrikes.meleeDamage(level, mace, pig, maceSource);
        helper.assertTrue(near(smash, 18), "Mace smash bonus expected, got " + smash);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void dancingBladesLeaveBrokenSwordsInThePouch(GameTestHelper helper) throws Exception {
        var player = caster(helper, new BlockPos(2, 0, 2), "blades-broken");
        var context = new SpellContext().setPlayer(player);
        AABB area = helper.getBounds().inflate(8);
        var broken = new ItemStack(Items.IRON_SWORD);
        broken.setDamageValue(broken.getMaxDamage());
        var pouch = new ItemStack(PsitweaksItems.BLADE_POUCH.get());
        BladePouches.setContents(pouch, List.of(broken));
        player.getInventory().setItem(9, pouch);
        helper.assertTrue(BladePouches.isBroken(broken) && !BladePouches.isBroken(new ItemStack(Items.IRON_SWORD)),
                "A sword with no durability left is broken");
        helper.assertTrue(PieceTrickDancingBlades.ERROR_NO_SWORDS.equals(castError(() -> dancingBlades(Map.of()).execute(context))),
                "Only broken swords: the spell must fail");
        BladePouches.setContents(pouch, List.of(broken, new ItemStack(Items.WOODEN_SWORD)));
        dancingBlades(Map.of()).execute(context);
        var blades = helper.getLevel().getEntitiesOfClass(EntityDancingBlade.class, area);
        helper.assertTrue(blades.size() == 1 && blades.get(0).getSword().is(Items.WOODEN_SWORD), "Only the intact sword deploys");
        blades.forEach(Entity::discard);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = BATCH)
    public static void bladePouchMenuKeepsToItsPouchAndLocksWhileBladesAreOut(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = caster(helper, new BlockPos(2, 0, 2), "pouch-menu");
        var first = new ItemStack(PsitweaksItems.BLADE_POUCH.get());
        var second = new ItemStack(PsitweaksItems.BLADE_POUCH.get());
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, first);
        player.getInventory().setItem(1, second);
        helper.assertTrue(BladePouches.isIdle(player, second) && !BladePouches.isIdle(player, first),
                "The pouch found second must read as idle");

        var menu = new BladePouchMenu(0, player.getInventory(), InteractionHand.MAIN_HAND);
        helper.assertTrue(menu.stillValid(player) && !menu.isDeployed(), "Fresh menu must be open and unlocked");
        menu.getSlot(0).set(new ItemStack(Items.WOODEN_SWORD));
        helper.assertTrue(BladePouches.swords(first).size() == 1 && BladePouches.swords(second).isEmpty(),
                "Edits must land on the pouch the menu was opened on");

        // Blades out: the sword slots take nothing and give nothing back.
        var blade = new EntityDancingBlade(level, player, 0, 1);
        blade.setSword(new ItemStack(Items.WOODEN_SWORD));
        level.addFreshEntity(blade);
        menu.broadcastChanges();
        helper.assertTrue(menu.isDeployed() && !menu.getSlot(0).mayPickup(player)
                        && !menu.getSlot(1).mayPlace(new ItemStack(Items.IRON_SWORD)),
                "Sword slots must lock while the blades are out");
        helper.assertTrue(menu.quickMoveStack(player, 0).isEmpty(), "Shift-click must not take a sword out while locked");
        blade.discard();
        menu.broadcastChanges();
        helper.assertTrue(!menu.isDeployed() && menu.getSlot(0).mayPickup(player), "Sword slots must unlock once the blades are gone");

        // Another stack in that hand, or another hotbar slot selected, closes the menu.
        player.setItemInHand(InteractionHand.MAIN_HAND, second);
        helper.assertTrue(!menu.stillValid(player), "Another pouch in hand must close the menu");
        player.setItemInHand(InteractionHand.MAIN_HAND, first);
        helper.assertTrue(menu.stillValid(player), "The same pouch back in hand keeps the menu");
        player.getInventory().selected = 1;
        helper.assertTrue(!menu.stillValid(player), "Selecting another hotbar slot must close the menu");
        helper.succeed();
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
