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
 * Create のブレイズバーナーと作動式: 加熱領域（2026-11-11 の設計 B）。ブレイズバーナーが自分の加熱の段を決める
 * {@code getHeatLevel()} の戻り値を、加熱領域の中にあるときだけ引き上げる: 加熱領域の中は「加熱」（KINDLED）、氷炎地獄の灼熱の
 * 中は「極度の加熱」（SEETHING）。燃料には触らないので、領域が消えると次の tick に元の段へ戻り、ブレイズバーナーの中身には
 * 何も残らない。空のブレイズバーナー（NONE）と、すでに最上段のものは変えない。Create がないときは {@link PsitweaksMixinPlugin}
 * が当てない。
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
                || current.isAtLeast(BlazeBurnerBlock.HeatLevel.SEETHING)) {
            return;
        }
        int zoneHeat = EntityHeatZone.burnerHeatAt(level, self.getBlockPos());
        if (zoneHeat >= 2) {
            cir.setReturnValue(BlazeBurnerBlock.HeatLevel.SEETHING);
        } else if (zoneHeat == 1 && !current.isAtLeast(BlazeBurnerBlock.HeatLevel.KINDLED)) {
            cir.setReturnValue(BlazeBurnerBlock.HeatLevel.KINDLED);
        }
    }
}
