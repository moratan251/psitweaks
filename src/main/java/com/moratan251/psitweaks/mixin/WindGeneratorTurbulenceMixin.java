package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mekanism Generators の風力発電機と作動式: 乱流領域。風力発電機は上 4 ブロックの位置から空が見えないと発電しないが、
 * 乱流領域の中にあれば屋内でも、空が見えるときと同じ（高さに応じた）だけ発電させ、さらに出力を乱流の威力に応じて
 * 上乗せする（{@link EntityTurbulenceZone#windBoostAt}、威力 10 で 2 倍）。風力発電機がもともと動かないディメンション
 * （ネザーなど）と、上が液体のときはそのまま止まる。Mekanism Generators がないときは {@link PsitweaksMixinPlugin} が当てない。
 */
@Pseudo
@Mixin(targets = "mekanism.generators.common.tile.TileEntityWindGenerator", remap = false)
public abstract class WindGeneratorTurbulenceMixin {
    /** 風力発電機のブロックから、空を見る位置までの高さ（Mekanism の getMultiplier と同じ）。 */
    private static final int PSITWEAKS$SKY_CHECK_HEIGHT = 4;

    @Redirect(method = "getMultiplier",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;canSeeSky(Lnet/minecraft/core/BlockPos;)Z"),
            require = 0)
    private boolean psitweaks$turbulenceCountsAsSky(Level level, BlockPos skyCheckPos) {
        return level.canSeeSky(skyCheckPos)
                || EntityTurbulenceZone.blowsAt(level, skyCheckPos.below(PSITWEAKS$SKY_CHECK_HEIGHT));
    }

    @Inject(method = "getMultiplier", at = @At("RETURN"), cancellable = true, require = 0)
    private void psitweaks$turbulenceBoostsOutput(CallbackInfoReturnable<Double> cir) {
        double multiplier = cir.getReturnValue();
        if (multiplier <= 0.0) {
            return;
        }
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }
        double boost = EntityTurbulenceZone.windBoostAt(level, self.getBlockPos());
        if (boost > 1.0) {
            cir.setReturnValue(multiplier * boost);
        }
    }
}
