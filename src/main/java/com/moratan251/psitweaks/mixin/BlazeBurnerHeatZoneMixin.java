package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityHeatZone;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create のブレイズバーナーと作動式: 加熱領域。ブレイズバーナーが自分の加熱の段を決める
 * {@code getHeatLevel()} の戻り値を、加熱領域が熱量から払っているときだけ「加熱」（KINDLED）に引き上げる（領域からは極度の
 * 加熱にしない。極度の加熱はブレイズケーキに任せる）。燃料には触らないので、領域が消えると次の tick に
 * 元の段へ戻り、ブレイズバーナーの中身には何も残らない。空のブレイズバーナー（NONE）と、すでに加熱以上のものは変えない。
 * Create がないときは {@link PsitweaksMixinPlugin} が当てない。
 */
@Pseudo
@Mixin(targets = "com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity", remap = false)
public abstract class BlazeBurnerHeatZoneMixin {
    @Inject(method = "getHeatLevel", at = @At("RETURN"), cancellable = true, require = 0)
    private void psitweaks$heatZoneRaisesHeatLevel(CallbackInfoReturnable<BlazeBurnerBlock.HeatLevel> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        BlazeBurnerBlock.HeatLevel current = cir.getReturnValue();
        if (current == null || current == BlazeBurnerBlock.HeatLevel.NONE
                || current.isAtLeast(BlazeBurnerBlock.HeatLevel.KINDLED)) {
            return;
        }
        if (EntityHeatZone.isBurnerPowered(level, self.getBlockPos())) {
            cir.setReturnValue(BlazeBurnerBlock.HeatLevel.KINDLED);
        }
    }
}
