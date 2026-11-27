package com.moratan251.psitweaks.common.gametest;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import net.minecraft.gametest.framework.TestFunction;
import org.slf4j.Logger;

/**
 * GameTest サーバー（{@code runGameTestServer}）で走らせるテストを、システムプロパティ {@code psitweaks.gametest.classes} に
 * 並べたクラスのものだけに絞る。値はクラスの単純名のカンマ区切りで、大文字小文字は区別しない（例: {@code AdvancementGameTests}）。
 * <p>
 * {@code @PrefixGameTestTemplate(false)} のクラスはテスト名にクラス名が入らないので、登録のときに各テストのクラスを覚えておく
 * （GameTestRegistryClassFilterMixin）。絞り込みは GameTestServer.create の引数で行う（GameTestServerClassFilterMixin）。
 * どちらの mixin も、プロパティが空でないときだけ当たる（PsitweaksMixinPlugin、build.gradle の gameTestServer の run が
 * Gradle プロパティから渡す）。
 */
public final class GameTestClassFilter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROPERTY = "psitweaks.gametest.classes";
    /** 登録された TestFunction → 宣言しているクラスの単純名。絞り込みが済んだら空にする。 */
    private static final Map<TestFunction, String> CLASS_OF_TEST = new IdentityHashMap<>();

    private GameTestClassFilter() {
    }

    public static void remember(TestFunction test, Class<?> testClass) {
        CLASS_OF_TEST.put(test, testClass.getSimpleName());
    }

    /**
     * 指定したクラスのテストだけを返す。知らないクラス名が 1 つでもあれば、テストを 1 件も走らせずに終了コード 1 で止める
     * （GameTest サーバーの起動失敗は Gradle の結果に出ないため、例外ではなく終了コードで知らせる）。
     */
    public static Collection<TestFunction> keepChosenClasses(Collection<TestFunction> tests) {
        Map<String, String> chosen = new LinkedHashMap<>();
        for (String name : System.getProperty(PROPERTY, "").split(",")) {
            if (!name.isBlank()) {
                chosen.put(name.trim().toLowerCase(Locale.ROOT), name.trim());
            }
        }
        if (chosen.isEmpty()) {
            return tests;
        }

        TreeSet<String> known = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        TreeSet<String> kept = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        List<TestFunction> chosenTests = new ArrayList<>();
        for (TestFunction test : tests) {
            String testClass = CLASS_OF_TEST.get(test);
            if (testClass == null) {
                continue;
            }
            known.add(testClass);
            if (chosen.containsKey(testClass.toLowerCase(Locale.ROOT))) {
                chosenTests.add(test);
                kept.add(testClass);
            }
        }
        CLASS_OF_TEST.clear();

        List<String> unknown = chosen.entrySet().stream()
                .filter(entry -> !known.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        if (!unknown.isEmpty()) {
            LOGGER.error("{}: no GameTest class named {}. Known classes: {}", PROPERTY, unknown, known);
            System.exit(1);
        }
        LOGGER.info("{}: running {} of {} GameTests, from {}", PROPERTY, chosenTests.size(), tests.size(), kept);
        return chosenTests;
    }
}
