package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorFormatString;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorStoredEntity;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickPhononMaser;
import com.moratan251.psitweaks.common.spells.util.WildcardStringMatcher;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellListLimits;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaksqol.api.StringSpellHelper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

/**
 * Spells built only from constants must not stop or crash the server: Operator: Format String, the wildcard match,
 * stats that do not fit an int, and lists joined to themselves. Also checks that Trick: Radiation Filter's mixin
 * reaches Mekanism.
 */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellLimitGameTests {
    private static final String TEMPLATE = "connector_empty";
    private static final long SLOW_NANOS = 1_000_000_000L;

    private SpellLimitGameTests() {
    }

    @SuppressWarnings("unchecked")
    private static <T> T evaluation(Map<String, ?> values, SpellParam<?> param) {
        Object value = values.get(param.name);
        return (T) (value instanceof Number number ? Double.valueOf(number.doubleValue()) : value);
    }

    private static PieceOperatorFormatString formatString(String template, List<String> arguments) {
        Map<String, String> values = Map.of(PsitweaksSpellParams.VALUE1, arguments.get(0),
                PsitweaksSpellParams.VALUE2, arguments.get(1), PsitweaksSpellParams.VALUE3, arguments.get(2));
        var piece = new PieceOperatorFormatString(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.get(param.name); }
        };
        piece.replaceValue(template);
        return piece;
    }

    private static int[] stats(SpellPiece piece) throws SpellCompilationException {
        var metadata = new SpellMetadata();
        piece.addToMetadata(metadata);
        return new int[]{metadata.getStat(EnumSpellStat.POTENCY), metadata.getStat(EnumSpellStat.COST)};
    }

    private static String compileError(SpellPiece piece) {
        try {
            stats(piece);
            return null;
        } catch (SpellCompilationException failure) {
            return failure.getMessage();
        }
    }

    private static PieceTrickPhononMaser phononMaser(double power, double time) {
        Map<String, Double> values = Map.of("psi.spellparam.power", power, "psi.spellparam.time", time);
        return new PieceTrickPhononMaser(new Spell()) {
            @Override public <T> T getParamEvaluation(SpellParam<?> param) { return evaluation(values, param); }
        };
    }

    private static PieceSelectorStoredEntity storedEntity(double number) {
        return new PieceSelectorStoredEntity(new Spell()) {
            @Override @SuppressWarnings("unchecked")
            public <T> T getParamEvaluation(SpellParam<?> param) { return (T) Double.valueOf(number); }
        };
    }

    @GameTest(template = TEMPLATE)
    public static void formatStringReplacesOnceAndStopsAtTheLengthLimit(GameTestHelper helper) throws Exception {
        var context = new SpellContext().setPlayer(new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fmt")));

        Object once = formatString("{1}-{2}-{3}", List.of("{2}{3}", "B", "C")).execute(context);
        helper.assertTrue("{2}{3}-B-C".equals(once), "Text from an argument was replaced again: " + once);

        // Each {1} brings in {2}s and each {2} brings in {3}s: chained, this grew past the memory.
        String template = "{1}".repeat(StringSpellHelper.MAX_CONSTANT_STRING_LENGTH / 3);
        String manySeconds = "{2}".repeat(300);
        String manyThirds = "{3}".repeat(300);
        long start = System.nanoTime();
        Object grown = formatString(template, List.of(manySeconds, manyThirds, "x".repeat(300))).execute(context);
        long elapsed = System.nanoTime() - start;
        helper.assertTrue(grown instanceof String text && text.length() == StringSpellHelper.MAX_STRING_LENGTH
                        && text.startsWith(manySeconds),
                "The result should be the first arguments cut at the length limit");
        helper.assertTrue(elapsed < SLOW_NANOS, "Formatting took " + elapsed / 1_000_000 + " ms");

        helper.assertTrue("a{4}{x}{".equals(PieceOperatorFormatString.format("{2}{1}{4}{x}{", new String[]{"", "a", ""})),
                "Only {1} to {3} are placeholders");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void wildcardMatchKeepsItsMeaningAndDoesNotBacktrack(GameTestHelper helper) {
        helper.assertTrue(WildcardStringMatcher.compile("minecraft:*_ore").matches("minecraft:iron_ore"), "* should match any text");
        helper.assertTrue(!WildcardStringMatcher.compile("minecraft:*_ore").matches("minecraft:iron_ore_block"), "A full match must reach the end");
        helper.assertTrue(WildcardStringMatcher.compilePartial("ore").matches("minecraft:iron_ore_block"), "A partial match may be anywhere");
        helper.assertTrue(WildcardStringMatcher.compile("[ab]?c").matches("bxc") && !WildcardStringMatcher.compile("[ab]?c").matches("cxc"),
                "[ab] should match one of a or b, ? any one character");
        helper.assertTrue(WildcardStringMatcher.compile("a\\*b").matches("a*b") && !WildcardStringMatcher.compile("a\\*b").matches("axb"),
                "\\* should be a literal *");
        helper.assertTrue(WildcardStringMatcher.compile("[]").matches("[]") && WildcardStringMatcher.compile("[a").matches("[a"),
                "An empty or unclosed [ should be literal");
        helper.assertTrue(WildcardStringMatcher.compile("Ab").matches("Ab") && !WildcardStringMatcher.compile("Ab").matches("ab"),
                "The match should keep case");
        helper.assertTrue(WildcardStringMatcher.compile("line*end").matches("line\nend"), "* should match a line break too");

        // With a regular expression these took seconds (150 characters) to minutes (300).
        String value = "a".repeat(StringSpellHelper.MAX_STRING_LENGTH);
        long start = System.nanoTime();
        boolean partial = WildcardStringMatcher.compilePartial("*a*a*a*b").matches(value);
        boolean full = WildcardStringMatcher.compile("*a*a*a*b").matches(value);
        boolean longPiece = WildcardStringMatcher.compile("*" + "a".repeat(4095) + "b*").matches(value);
        long elapsed = System.nanoTime() - start;
        helper.assertTrue(!partial && !full && !longPiece, "Nothing ends in b here");
        helper.assertTrue(elapsed < SLOW_NANOS, "Matching took " + elapsed / 1_000_000 + " ms");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void statsThatDoNotFitStopCompilingInsteadOfGoingNegative(GameTestHelper helper) throws Exception {
        int[] normal = stats(phononMaser(2, 3));
        helper.assertTrue(normal[0] >= 850 && normal[1] >= 4200, "Power 2 for 3 seconds should cost 4200 and need potency 850");

        // 2.2e7 x 1 x 700 is past the int range: it used to wrap to a negative cost.
        helper.assertTrue(SpellCompilationException.STAT_OVERFLOW.equals(compileError(phononMaser(2.2e7, 1))),
                "Phonon Maser's oversized cost should stop compiling");
        helper.assertTrue(SpellCompilationException.STAT_OVERFLOW.equals(compileError(phononMaser(1e300, 1))),
                "A huge power should stop compiling");
        helper.assertTrue(SpellCompilationException.STAT_OVERFLOW.equals(compileError(storedEntity(4e8))),
                "Selector: Stored Entity's oversized potency should stop compiling");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void listsJoinedToThemselvesStopAtTheLimit(GameTestHelper helper) {
        var strings = StringListWrapper.make(List.of("ab"));
        var numbers = NumberListWrapper.make(List.of(1.0D));
        var vectors = VectorListWrapper.make(List.of(new Vector3(1, 2, 3)));
        for (int i = 0; i < 40; i++) {
            strings = StringListWrapper.make(doubled(strings.asList()));
            numbers = NumberListWrapper.make(doubled(numbers.asList()));
            vectors = VectorListWrapper.make(doubled(vectors.asList()));
        }
        helper.assertTrue(strings.size() == SpellListLimits.MAX_ELEMENTS, "String List size " + strings.size());
        helper.assertTrue(numbers.size() == SpellListLimits.MAX_ELEMENTS, "Number List size " + numbers.size());
        helper.assertTrue(vectors.size() == SpellListLimits.MAX_ELEMENTS, "Vector List size " + vectors.size());

        String longest = "x".repeat(StringSpellHelper.MAX_STRING_LENGTH);
        var longStrings = StringListWrapper.make(Collections.nCopies(100, longest));
        helper.assertTrue(longStrings.size() == SpellListLimits.MAX_STRING_LIST_CHARACTERS / StringSpellHelper.MAX_STRING_LENGTH,
                "Long strings should stop at the character limit, got " + longStrings.size());
        helper.assertTrue(StringListWrapper.make(List.of("a", "b", "c")).size() == 3, "Short lists should stay as they are");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void radiationFilterRaisesMekanismRadiationResistance(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("mekanism")) {
            helper.succeed();
            return;
        }
        Class<?> radiationUtil = Class.forName("mekanism.common.lib.radiation.RadiationUtil");
        Method resistance = radiationUtil.getMethod("getRadiationResistance", LivingEntity.class);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        double before = (Double) resistance.invoke(null, zombie);
        zombie.addEffect(new MobEffectInstance(PsitweaksEffects.RADIATION_FILTER, 200, 0));
        double after = (Double) resistance.invoke(null, zombie);
        helper.assertTrue(before < 1.0D, "A bare zombie should not resist radiation fully, got " + before);
        helper.assertTrue(after == 1.0D, "Radiation Filter should make the resistance 1.0, got " + after
                + " (the mixin on RadiationUtil did not apply)");
        helper.succeed();
    }

    private static <T> List<T> doubled(List<T> values) {
        List<T> result = new ArrayList<>(values);
        result.addAll(values);
        return result;
    }
}
