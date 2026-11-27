package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.gametest.GameTestClassFilter;
import java.lang.reflect.Method;
import java.util.Collection;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * GameTest を登録するときに、各テストを宣言しているクラスを {@link GameTestClassFilter} に覚えさせる。
 * {@code @PrefixGameTestTemplate(false)} のテストは名前にクラス名が入らず、TestFunction からはクラスがわからないため。
 * <p>
 * システムプロパティ {@code psitweaks.gametest.classes} が空でないときだけ当てる（{@link PsitweaksMixinPlugin}）。
 */
@Mixin(GameTestRegistry.class)
public abstract class GameTestRegistryClassFilterMixin {
    @Inject(method = "turnMethodIntoTestFunction", at = @At("RETURN"))
    private static void psitweaks$rememberTestClass(Method testMethod, CallbackInfoReturnable<TestFunction> cir) {
        GameTestClassFilter.remember(cir.getReturnValue(), testMethod.getDeclaringClass());
    }

    @Inject(method = "useTestGeneratorMethod", at = @At("RETURN"))
    private static void psitweaks$rememberGeneratedTestClass(Method testMethod,
            CallbackInfoReturnable<Collection<TestFunction>> cir) {
        cir.getReturnValue().forEach(test -> GameTestClassFilter.remember(test, testMethod.getDeclaringClass()));
    }
}
