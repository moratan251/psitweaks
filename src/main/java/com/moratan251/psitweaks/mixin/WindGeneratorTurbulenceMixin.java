package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mekanism Generators の風力発電機と作動式: 乱流領域（加算方式）。風力発電機の発電に、乱流の風を足す:
 * 受け取った風量 1 WU ごとに {@link EntityTurbulenceZone#WIND_JOULES_PER_WU}（37.5 J/t = 15 FE/t）。乱流の風は人工のものなので、
 * 高さによらない（Mekanism の発電は高さで 60〜480 J/t）。屋根の下（上 4 ブロックの位置から空が見えない）の発電機は乱流の風
 * だけで動き、空の下の発電機には上乗せになる。Mekanism の発電の倍率は「発電量 ÷ 最小の発電量（設定 generationMin）」なので、
 * 足す倍率は WU × 37.5 ÷ 最小の発電量。上が液体のときはそのまま止まる。Mekanism Generators がないときは
 * {@link PsitweaksMixinPlugin} が当てない。
 */
@Pseudo
@Mixin(targets = "mekanism.generators.common.tile.TileEntityWindGenerator", remap = false)
public abstract class WindGeneratorTurbulenceMixin {
    /** 風力発電機のブロックから、空と液体を見る位置までの高さ（Mekanism の getMultiplier と同じ）。 */
    private static final int PSITWEAKS$SKY_CHECK_HEIGHT = 4;

    @Inject(method = "getMultiplier", at = @At("RETURN"), cancellable = true, require = 0)
    private void psitweaks$turbulenceAddsWind(CallbackInfoReturnable<Double> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }
        double units = EntityTurbulenceZone.windUnitsAt(level, self.getBlockPos());
        if (units <= 0.0 || !level.getFluidState(self.getBlockPos().above(PSITWEAKS$SKY_CHECK_HEIGHT)).isEmpty()) {
            return;
        }
        long minimum = mekanism.generators.common.config.MekanismGeneratorsConfig.generators.windGenerationMin.get();
        if (minimum > 0L) {
            cir.setReturnValue(cir.getReturnValue() + units * EntityTurbulenceZone.WIND_JOULES_PER_WU / minimum);
        }
    }
}
