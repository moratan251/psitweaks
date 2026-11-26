package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.constant.PieceConstantSignedWrapper;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGravityZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickParade;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import com.mojang.datafixers.util.Either;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.spell.SpellCompiler;
import vazkii.psi.common.spell.constant.PieceConstantNumber;

/** Constant: Signed Wrapper: what it outputs, and which inputs it may feed. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SignedWrapperGameTests {
    private static final String TEMPLATE = "psi110_empty";

    private SignedWrapperGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void signedWrapperKeepsTheSignWithinTheConstant(GameTestHelper helper) throws Exception {
        helper.assertTrue(near(output(-7.0, 5.0), -5.0) && near(output(3.0, 5.0), 3.0) && near(output(-2.0, 5.0), -2.0),
                "With Constant 5, Target must come out within -5..5 with its own sign");
        helper.assertTrue(near(output(9.0, -5.0), 5.0) && near(output(-2.0, -5.0), -2.0),
                "A negative Constant must bound Target by its absolute value only");
        helper.assertTrue(isPositiveZero(output(4.0, 0.0)) && isPositiveZero(output(-4.0, 0.0)),
                "Constant 0 must give +0.0 regardless of Target's sign");
        helper.assertTrue(isPositiveZero(output(-0.0, 5.0)), "-0.0 Target must normalize to +0.0");
        helper.assertTrue(near(evaluation(5.0), 5.0), "At compile time it must count as its Constant");
        helper.assertTrue(throwsNullTarget(null, 5.0) && throwsNullTarget(2.0, null),
                "Null runtime inputs must raise SpellRuntimeException.NULL_TARGET instead of NPE");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void signedWrapperFeedsParadeLikeItsConstant(GameTestHelper helper) {
        Spell plain = spell("signed_wrapper_plain");
        paradeAt(plain);
        place(plain, number(plain, 5.0), 3, 4);

        Spell wrapped = spell("signed_wrapper_parade");
        paradeAt(wrapped);
        place(wrapped, wrapper(wrapped), 3, 4);
        place(wrapped, number(wrapped, -7.0), 2, 4);
        place(wrapped, number(wrapped, 5.0), 3, 3);

        var plainResult = compile(helper, plain);
        var wrappedResult = compile(helper, wrapped);
        helper.assertTrue(plainResult.left().isPresent(), "The plain Parade must compile: " + error(plainResult));
        helper.assertTrue(wrappedResult.left().isPresent(), "Parade fed by a Signed Wrapper must compile: " + error(wrappedResult));
        int plainCost = plainResult.left().orElseThrow().metadata.getStat(EnumSpellStat.COST);
        int wrappedCost = wrappedResult.left().orElseThrow().metadata.getStat(EnumSpellStat.COST);
        helper.assertTrue(wrappedCost == plainCost && plainCost == (int) PieceTrickParade.costFor(10, 5),
                "The cost must go by the Constant: " + wrappedCost + " vs " + plainCost);

        // Through a connector as well.
        Spell connected = spell("signed_wrapper_parade_connector");
        paradeAt(connected);
        place(connected, connector(connected), 3, 4);
        place(connected, wrapper(connected), 2, 4);
        place(connected, number(connected, -7.0), 1, 4);
        place(connected, number(connected, 5.0), 2, 3);
        var connectedResult = compile(helper, connected);
        helper.assertTrue(connectedResult.left().isPresent(),
                "Parade fed through a connector must compile: " + error(connectedResult));

        // Trick: Blink takes a negative Distance at the same stats as well.
        Spell blink = spell("signed_wrapper_blink");
        SpellPiece trickBlink = psiPiece(blink, "trick_blink");
        connect(trickBlink, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.RIGHT);
        connect(trickBlink, "psi.spellparam.distance", SpellParam.Side.LEFT);
        place(blink, trickBlink, 4, 4);
        place(blink, psiPiece(blink, "selector_caster"), 5, 4);
        place(blink, wrapper(blink), 3, 4);
        place(blink, number(blink, -7.0), 2, 4);
        place(blink, number(blink, 5.0), 3, 3);
        var blinkResult = compile(helper, blink);
        helper.assertTrue(blinkResult.left().isPresent(), "Trick: Blink fed by a Signed Wrapper must compile: " + error(blinkResult));

        // Constant 0 is also allowed on an input that accepts both signs.
        Spell blinkZero = spell("signed_wrapper_blink_zero");
        SpellPiece trickBlinkZero = psiPiece(blinkZero, "trick_blink");
        connect(trickBlinkZero, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.RIGHT);
        connect(trickBlinkZero, "psi.spellparam.distance", SpellParam.Side.LEFT);
        place(blinkZero, trickBlinkZero, 4, 4);
        place(blinkZero, psiPiece(blinkZero, "selector_caster"), 5, 4);
        place(blinkZero, wrapper(blinkZero), 3, 4);
        place(blinkZero, number(blinkZero, -7.0), 2, 4);
        place(blinkZero, number(blinkZero, 0.0), 3, 3);
        var blinkZeroResult = compile(helper, blinkZero);
        helper.assertTrue(blinkZeroResult.left().isPresent(),
                "Trick: Blink fed by a Constant 0 Signed Wrapper must compile: " + error(blinkZeroResult));

        // Trick: Add Motion sets a metadata flag and takes either sign at the same stats.
        Spell addMotion = spell("signed_wrapper_add_motion");
        SpellPiece trickMotion = psiPiece(addMotion, "trick_add_motion");
        connect(trickMotion, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.RIGHT);
        connect(trickMotion, "psi.spellparam.direction", SpellParam.Side.BOTTOM);
        connect(trickMotion, "psi.spellparam.speed", SpellParam.Side.LEFT);
        place(addMotion, trickMotion, 4, 4);
        place(addMotion, psiPiece(addMotion, "selector_caster"), 5, 4);
        SpellPiece look = psiPiece(addMotion, "operator_entity_look");
        connect(look, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.RIGHT);
        place(addMotion, look, 4, 5);
        place(addMotion, psiPiece(addMotion, "selector_caster"), 5, 5);
        place(addMotion, wrapper(addMotion), 3, 4);
        place(addMotion, number(addMotion, -3.0), 2, 4);
        place(addMotion, number(addMotion, 2.0), 3, 3);
        var addMotionResult = compile(helper, addMotion);
        helper.assertTrue(addMotionResult.left().isPresent(),
                "Trick: Add Motion fed by a Signed Wrapper must compile: " + error(addMotionResult));

        // Trick: Gravity Zone allows negative Power in Matrix mode, but rejects it in Vector mode.
        Spell gravityMatrix = spell("signed_wrapper_gravity_matrix");
        PieceTrickGravityZone matrixZone = (PieceTrickGravityZone) piece(gravityMatrix, Psitweaks.location("trick_gravity_zone"));
        matrixZone.setModeOption(PsitweaksModeOptions.MATRIX);
        connect(matrixZone, PsitweaksSpellParams.SIZE, SpellParam.Side.RIGHT);
        connect(matrixZone, SpellParam.GENERIC_NAME_POWER, SpellParam.Side.LEFT);
        place(gravityMatrix, matrixZone, 4, 4);
        place(gravityMatrix, number(gravityMatrix, 3.0), 5, 4);
        place(gravityMatrix, wrapper(gravityMatrix), 3, 4);
        place(gravityMatrix, number(gravityMatrix, -4.0), 2, 4);
        place(gravityMatrix, number(gravityMatrix, 2.0), 3, 3);
        var gravityMatrixResult = compile(helper, gravityMatrix);
        helper.assertTrue(gravityMatrixResult.left().isPresent(),
                "Gravity Zone in Matrix mode must accept Signed Wrapper: " + error(gravityMatrixResult));

        Spell gravityVector = spell("signed_wrapper_gravity_vector");
        PieceTrickGravityZone vectorZone = (PieceTrickGravityZone) piece(gravityVector, Psitweaks.location("trick_gravity_zone"));
        connect(vectorZone, PsitweaksSpellParams.SIZE, SpellParam.Side.RIGHT);
        connect(vectorZone, SpellParam.GENERIC_NAME_DIRECTION, SpellParam.Side.BOTTOM);
        connect(vectorZone, SpellParam.GENERIC_NAME_POWER, SpellParam.Side.LEFT);
        place(gravityVector, vectorZone, 4, 4);
        place(gravityVector, number(gravityVector, 3.0), 5, 4);
        SpellPiece dirLook = psiPiece(gravityVector, "operator_entity_look");
        connect(dirLook, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.RIGHT);
        place(gravityVector, dirLook, 4, 5);
        place(gravityVector, psiPiece(gravityVector, "selector_caster"), 5, 5);
        place(gravityVector, wrapper(gravityVector), 3, 4);
        place(gravityVector, number(gravityVector, -4.0), 2, 4);
        place(gravityVector, number(gravityVector, 2.0), 3, 3);
        var gravityVectorResult = compile(helper, gravityVector);
        helper.assertTrue(isSignedWrapperError(gravityVectorResult),
                "Gravity Zone in Vector mode must refuse Signed Wrapper: " + error(gravityVectorResult));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void signedWrapperRefusesOtherConstantInputs(GameTestHelper helper) {
        // Trick: Delay checks its Time for its sign only when the spell is compiled.
        Spell direct = spell("signed_wrapper_delay");
        delayAt(direct);
        place(direct, wrapper(direct), 3, 4);
        place(direct, number(direct, -7.0), 2, 4);
        place(direct, number(direct, 5.0), 3, 3);
        var directResult = compile(helper, direct);
        helper.assertTrue(isSignedWrapperError(directResult),
                "Feeding Trick: Delay's Time must not compile: " + error(directResult));

        Spell connected = spell("signed_wrapper_delay_connector");
        delayAt(connected);
        place(connected, connector(connected), 3, 4);
        place(connected, wrapper(connected), 2, 4);
        place(connected, number(connected, -7.0), 1, 4);
        place(connected, number(connected, 5.0), 2, 3);
        var connectedResult = compile(helper, connected);
        helper.assertTrue(isSignedWrapperError(connectedResult),
                "A connector in between must not let it through: " + error(connectedResult));

        // Psi's Constant: Wrapper would pass the value on to inputs this check does not see.
        Spell rewrapped = spell("signed_wrapper_rewrapped");
        SpellPiece blinkAfter = psiPiece(rewrapped, "trick_blink");
        connect(blinkAfter, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.RIGHT);
        connect(blinkAfter, "psi.spellparam.distance", SpellParam.Side.LEFT);
        place(rewrapped, blinkAfter, 4, 4);
        place(rewrapped, psiPiece(rewrapped, "selector_caster"), 5, 4);
        SpellPiece psiWrapper = psiPiece(rewrapped, "constant_wrapper");
        connect(psiWrapper, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.BOTTOM);
        connect(psiWrapper, "psi.spellparam.constant", SpellParam.Side.LEFT);
        place(rewrapped, psiWrapper, 3, 4);
        place(rewrapped, number(rewrapped, 3.0), 3, 5);
        place(rewrapped, wrapper(rewrapped), 2, 4);
        place(rewrapped, number(rewrapped, -7.0), 1, 4);
        place(rewrapped, number(rewrapped, 5.0), 2, 3);
        var rewrappedResult = compile(helper, rewrapped);
        helper.assertTrue(isSignedWrapperError(rewrappedResult),
                "Constant: Wrapper must not take it as its Constant: " + error(rewrappedResult));

        // An input that takes any value may read it like any other number.
        Spell debug = spell("signed_wrapper_debug");
        SpellPiece trickDebug = psiPiece(debug, "trick_debug");
        connect(trickDebug, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.LEFT);
        place(debug, trickDebug, 4, 4);
        place(debug, wrapper(debug), 3, 4);
        place(debug, number(debug, -7.0), 2, 4);
        place(debug, number(debug, 5.0), 3, 3);
        var debugResult = compile(helper, debug);
        helper.assertTrue(debugResult.left().isPresent(), "Trick: Debug must read it: " + error(debugResult));

        // Constant 0 must still fail on inputs that allow 0 but reject negative constants (Trick: Detonate).
        Spell detonateZero = spell("signed_wrapper_detonate_zero");
        SpellPiece detonate = psiPiece(detonateZero, "trick_detonate");
        connect(detonate, SpellParam.GENERIC_NAME_RADIUS, SpellParam.Side.LEFT);
        place(detonateZero, detonate, 4, 4);
        place(detonateZero, wrapper(detonateZero), 3, 4);
        place(detonateZero, number(detonateZero, -2.0), 2, 4);
        place(detonateZero, number(detonateZero, 0.0), 3, 3);
        var detonateZeroResult = compile(helper, detonateZero);
        helper.assertTrue(isSignedWrapperError(detonateZeroResult),
                "Constant 0 must still be refused on Trick: Detonate: " + error(detonateZeroResult));

        // Validation must run before SpellCompiler's empty-name check so errors appear while editing an unnamed spell.
        Spell unnamed = spell("");
        delayAt(unnamed);
        place(unnamed, wrapper(unnamed), 3, 4);
        place(unnamed, number(unnamed, -7.0), 2, 4);
        place(unnamed, number(unnamed, 5.0), 3, 3);
        var unnamedResult = compile(helper, unnamed);
        helper.assertTrue(isSignedWrapperError(unnamedResult),
                "An invalid target must report ERROR_TARGET even before the spell is named: " + error(unnamedResult));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void signedWrapperIgnoresUnreachableReadersAndPreservesOtherErrors(GameTestHelper helper) {
        // An unreachable Constant: Wrapper pointing at the Signed Wrapper must not break a valid spell.
        Spell withUnused = spell("signed_wrapper_unused_reader");
        SpellPiece blink = psiPiece(withUnused, "trick_blink");
        connect(blink, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.RIGHT);
        connect(blink, "psi.spellparam.distance", SpellParam.Side.LEFT);
        place(withUnused, blink, 4, 4);
        place(withUnused, psiPiece(withUnused, "selector_caster"), 5, 4);
        place(withUnused, wrapper(withUnused), 3, 4);
        place(withUnused, number(withUnused, -7.0), 2, 4);
        place(withUnused, number(withUnused, 5.0), 3, 3);
        SpellPiece unusedWrapper = psiPiece(withUnused, "constant_wrapper");
        connect(unusedWrapper, "psi.spellparam.constant", SpellParam.Side.TOP);
        place(withUnused, unusedWrapper, 3, 5);
        var unusedResult = compile(helper, withUnused);
        helper.assertTrue(unusedResult.left().isPresent(),
                "An unreachable Constant: Wrapper must not block compilation: " + error(unusedResult));

        // When Trick: Debug compiles before a broken Trick: Parade sharing the same Signed Wrapper, Parade's own error
        // must be reported at Parade's coordinates instead of being masked as ERROR_TARGET.
        Spell brokenParade = spell("signed_wrapper_shared_broken_parade");
        SpellPiece parade = piece(brokenParade, Psitweaks.location("trick_parade"));
        connect(parade, SpellParam.GENERIC_NAME_NUMBER, SpellParam.Side.LEFT);
        connect(parade, SpellParam.GENERIC_NAME_TIME, SpellParam.Side.RIGHT);
        place(brokenParade, parade, 4, 4);
        place(brokenParade, number(brokenParade, 0.0), 5, 4);
        place(brokenParade, wrapper(brokenParade), 3, 4);
        place(brokenParade, number(brokenParade, -7.0), 2, 4);
        place(brokenParade, number(brokenParade, 5.0), 3, 3);
        SpellPiece debug = psiPiece(brokenParade, "trick_debug");
        connect(debug, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.TOP);
        place(brokenParade, debug, 3, 5);
        var brokenParadeResult = compile(helper, brokenParade);
        helper.assertTrue(hasErrorAt(brokenParadeResult, SpellCompilationException.NON_POSITIVE_INTEGER, 4, 4),
                "Broken Parade Time must report NON_POSITIVE_INTEGER at (4,4), got: " + errorDetail(brokenParadeResult));

        // A wrong-type constant parameter (ParamConstantString on Trick: Jump) must report INVALID_PARAM at Jump.
        Spell wrongType = spell("signed_wrapper_wrong_constant_type");
        SpellPiece jump = piece(wrongType, Psitweaks.location("trick_jump"));
        connect(jump, PsitweaksSpellParams.LABEL, SpellParam.Side.LEFT);
        place(wrongType, jump, 4, 4);
        place(wrongType, piece(wrongType, Psitweaks.location("jump_anchor")), 5, 5);
        place(wrongType, wrapper(wrongType), 3, 4);
        place(wrongType, number(wrongType, -7.0), 2, 4);
        place(wrongType, number(wrongType, 5.0), 3, 3);
        SpellPiece debug2 = psiPiece(wrongType, "trick_debug");
        connect(debug2, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.TOP);
        place(wrongType, debug2, 3, 5);
        var wrongTypeResult = compile(helper, wrongType);
        helper.assertTrue(hasErrorAt(wrongTypeResult, SpellCompilationException.INVALID_PARAM, 4, 4),
                "Wrong constant parameter type must report INVALID_PARAM at (4,4), got: " + errorDetail(wrongTypeResult));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void signedWrapperMovesPlayerBackwardThroughCompiledBlink(GameTestHelper helper) {
        Spell blink = spell("signed_wrapper_exec_blink");
        SpellPiece trickBlink = psiPiece(blink, "trick_blink");
        connect(trickBlink, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.RIGHT);
        connect(trickBlink, "psi.spellparam.distance", SpellParam.Side.LEFT);
        place(blink, trickBlink, 4, 4);
        place(blink, psiPiece(blink, "selector_caster"), 5, 4);
        place(blink, wrapper(blink), 3, 4);
        place(blink, number(blink, -7.0), 2, 4);
        place(blink, number(blink, 5.0), 3, 3);

        var compiledResult = compile(helper, blink);
        helper.assertTrue(compiledResult.left().isPresent(), "Compiled Blink must succeed: " + error(compiledResult));
        CompiledSpell compiled = compiledResult.left().orElseThrow();

        ServerPlayer player = GameTestPlayers.inLevel(helper, "SignedWrapperCaster", new Vec3(4.5, 2.0, 8.5));
        try {
            double startZ = player.getZ();
            SpellContext context = new SpellContext().setPlayer(player).setCompiledSpell(compiled);
            compiled.safeExecute(context);
            Object evaluated = context.evaluatedObjects[3][4];
            helper.assertTrue(evaluated instanceof Number n && near(n.doubleValue(), -5.0),
                    "Signed Wrapper must evaluate to -5.0 in compiled execution, got: " + evaluated);
            helper.assertTrue(near(player.getZ() - startZ, -5.0),
                    "Facing +Z with Distance -5 must move the player 5 blocks toward -Z, deltaZ=" + (player.getZ() - startZ));
        } finally {
            GameTestPlayers.remove(helper, player);
        }
        helper.succeed();
    }

    private static double output(double target, double constant) throws Exception {
        Map<String, Double> values = new HashMap<>();
        values.put(SpellParam.GENERIC_NAME_TARGET, target);
        values.put("psi.spellparam.constant", constant);
        PieceConstantSignedWrapper piece = new PieceConstantSignedWrapper(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return values.get(param.name);
            }
        };
        return ((Number) piece.execute(new SpellContext())).doubleValue();
    }

    private static boolean throwsNullTarget(Double target, Double constant) {
        Map<String, Double> values = new HashMap<>();
        values.put(SpellParam.GENERIC_NAME_TARGET, target);
        values.put("psi.spellparam.constant", constant);
        PieceConstantSignedWrapper piece = new PieceConstantSignedWrapper(new Spell()) {
            @Override
            public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return values.get(param.name);
            }
        };
        try {
            piece.execute(new SpellContext());
            return false;
        } catch (SpellRuntimeException e) {
            return SpellRuntimeException.NULL_TARGET.equals(e.getMessage());
        }
    }

    private static double evaluation(double constant) throws SpellCompilationException {
        PieceConstantSignedWrapper piece = new PieceConstantSignedWrapper(new Spell()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getParamEvaluation(SpellParam<?> param) {
                return param.name.equals("psi.spellparam.constant") ? (T) Double.valueOf(constant) : null;
            }
        };
        return ((Number) piece.evaluate()).doubleValue();
    }

    private static Spell spell(String name) {
        Spell spell = new Spell();
        spell.name = name;
        return spell;
    }

    /** Trick: Parade at (4, 4), Number from the left and Time 10 from the right. */
    private static SpellPiece paradeAt(Spell spell) {
        SpellPiece parade = piece(spell, Psitweaks.location("trick_parade"));
        connect(parade, SpellParam.GENERIC_NAME_NUMBER, SpellParam.Side.LEFT);
        connect(parade, SpellParam.GENERIC_NAME_TIME, SpellParam.Side.RIGHT);
        place(spell, parade, 4, 4);
        place(spell, number(spell, 10.0), 5, 4);
        return parade;
    }

    /** Trick: Delay at (4, 4), Time from the left. */
    private static void delayAt(Spell spell) {
        SpellPiece delay = psiPiece(spell, "trick_delay");
        connect(delay, SpellParam.GENERIC_NAME_TIME, SpellParam.Side.LEFT);
        place(spell, delay, 4, 4);
    }

    /** Target from the left, Constant from above. */
    private static SpellPiece wrapper(Spell spell) {
        SpellPiece wrapper = piece(spell, Psitweaks.location("constant_signed_wrapper"));
        connect(wrapper, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.LEFT);
        connect(wrapper, "psi.spellparam.constant", SpellParam.Side.TOP);
        return wrapper;
    }

    /** A connector that passes on what is on its left. */
    private static SpellPiece connector(Spell spell) {
        SpellPiece connector = psiPiece(spell, "connector");
        connect(connector, SpellParam.GENERIC_NAME_TARGET, SpellParam.Side.LEFT);
        return connector;
    }

    private static SpellPiece number(Spell spell, double value) {
        PieceConstantNumber piece = (PieceConstantNumber) psiPiece(spell, "constant_number");
        piece.valueStr = value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
        return piece;
    }

    private static SpellPiece psiPiece(Spell spell, String id) {
        return piece(spell, PsiAPI.location(id));
    }

    private static SpellPiece piece(Spell spell, ResourceLocation id) {
        return PsiAPI.SPELL_PIECE_REGISTRY.get(id).create(spell);
    }

    private static void connect(SpellPiece piece, String param, SpellParam.Side side) {
        SpellParam<?> target = piece.params.get(param);
        if (target == null) {
            throw new IllegalStateException(piece.getRegistryKey() + " has no parameter " + param);
        }
        piece.paramSides.put(target, side);
    }

    private static void place(Spell spell, SpellPiece piece, int x, int y) {
        piece.x = x;
        piece.y = y;
        piece.isInGrid = true;
        spell.grid.gridData[x][y] = piece;
    }

    private static Either<CompiledSpell, SpellCompilationException> compile(GameTestHelper helper, Spell spell) {
        return new SpellCompiler().compile(spell, helper.getLevel().registryAccess(), null);
    }

    private static boolean isSignedWrapperError(Either<CompiledSpell, SpellCompilationException> result) {
        return result.right().map(e -> PieceConstantSignedWrapper.ERROR_TARGET.equals(e.getMessage())).orElse(false);
    }

    private static String error(Either<CompiledSpell, SpellCompilationException> result) {
        return result.right().map(Throwable::getMessage).orElse("none");
    }

    private static boolean hasErrorAt(Either<CompiledSpell, SpellCompilationException> result,
            String message, int x, int y) {
        return result.right()
                .map(e -> message.equals(e.getMessage()) && e.location != null && e.location.getLeft() == x && e.location.getRight() == y)
                .orElse(false);
    }

    private static String errorDetail(Either<CompiledSpell, SpellCompilationException> result) {
        return result.right()
                .map(e -> e.getMessage() + (e.location == null ? "" : " at (" + e.location.getLeft() + "," + e.location.getRight() + ")"))
                .orElse("none");
    }

    private static boolean isPositiveZero(double value) {
        return Double.doubleToRawLongBits(value) == Double.doubleToRawLongBits(0.0);
    }

    private static boolean near(double actual, double expected) {
        return Math.abs(actual - expected) < 1.0E-9;
    }
}
