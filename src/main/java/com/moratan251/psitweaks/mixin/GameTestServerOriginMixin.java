package com.moratan251.psitweaks.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * GameTest サーバー（{@code runGameTestServer}）で、テストを並べ始める場所を x = z = 0 に固定する（高さはバニラのまま）。
 * バニラは起動のたびに x・z を ±14,999,992 のどこかに選ぶ。±8,388,608（2^23）を超えると Sable の機体の位置が 1 ブロック単位に
 * 丸まり、機体を使うテストは細かい確認を飛ばすしかなくなる（x か z が超えるのは約 7 割の実行）。原点の近くなら毎回確かめられ、
 * 落ちたときに同じ場所で流し直せる。
 * <p>
 * システムプロパティ {@code psitweaks.gametest.fixedOrigin=true} のときだけ当てる（{@link PsitweaksMixinPlugin}、
 * build.gradle の gameTestServer の run が付ける）。配布版や、開発クライアントの {@code /test} には関係しない。
 */
@Mixin(GameTestServer.class)
public abstract class GameTestServerOriginMixin {
    @ModifyVariable(method = "startTests", at = @At("STORE"), ordinal = 0)
    private BlockPos psitweaks$startAtTheOrigin(BlockPos randomStart) {
        return new BlockPos(0, randomStart.getY(), 0);
    }
}
