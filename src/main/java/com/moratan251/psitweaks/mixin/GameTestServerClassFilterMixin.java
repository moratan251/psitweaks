package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.gametest.GameTestClassFilter;
import java.util.Collection;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.TestFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * GameTest サーバーに渡すテストを、システムプロパティ {@code psitweaks.gametest.classes} に並べたクラスのものだけにする
 * （{@link GameTestClassFilter}）。サーバーの Main は登録済みの全テストを GameTestServer.create に渡す。
 * <p>
 * プロパティが空でないときだけ当てる（{@link PsitweaksMixinPlugin}、build.gradle の gameTestServer の run が付ける）。
 */
@Mixin(GameTestServer.class)
public abstract class GameTestServerClassFilterMixin {
    @ModifyVariable(method = "create", at = @At("HEAD"), argsOnly = true)
    private static Collection<TestFunction> psitweaks$keepChosenClasses(Collection<TestFunction> tests) {
        return GameTestClassFilter.keepChosenClasses(tests);
    }
}
