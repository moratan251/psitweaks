package com.moratan251.psitweaks.mixin;

import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import com.moratan251.psitweaks.common.compat.ImmersiveEngineeringWindmills;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Immersive Engineering の風車と作動式: 乱流領域。風車が受け取った乱流の風（{@link EntityTurbulenceZone#windUnitsAt}）の
 * ぶんの回転を、毎 tick、IE の自然の風とは別に後ろの動力ダイナモへ入れる（1 WU = 15 FE/t、{@link ImmersiveEngineeringWindmills#turnDynamo}）。
 * 自然の風が止まっている（前が塞がっている）風車も、羽の回る円が空いていれば乱流だけで回る。風を受け取れるかは乱流領域の側が
 * 決める（{@code EntityTurbulenceZone#computeGeneratorWind}）。
 * <p>
 * クライアントでは、乱流領域の範囲に入っていて羽の回る円が空いている風車の回る速さに、1 台の上限の風のぶんを足して見せる
 * （クライアントは受け取った風量を知らない）。IE がないときは {@link PsitweaksMixinPlugin} が当てない。
 */
@Pseudo
@Mixin(targets = "blusunrize.immersiveengineering.common.blocks.wooden.WindmillBlockEntity", remap = false)
public abstract class WindmillTurbulenceMixin {
    @Shadow
    private BlockCapabilityCache<IRotationAcceptor, ?> outputCap;

    /** サーバー: 乱流から受け取る風量（WU）。{@link EntityTurbulenceZone#WIND_COUNT_INTERVAL} tick ごとに読み直す。 */
    @Unique
    private double psitweaks$zoneWind;
    @Unique
    private long psitweaks$zoneWindReadAt = Long.MIN_VALUE;
    /** クライアント: 乱流の中で羽が回れるか（見た目用）。{@link EntityTurbulenceZone#WIND_COUNT_INTERVAL} tick ごとに調べ直す。 */
    @Unique
    private boolean psitweaks$spinsInZone;
    @Unique
    private long psitweaks$spinCheckedAt = Long.MIN_VALUE;

    /** 先頭に入れる（IE の {@code tickServer} は自然の風が止まっていると途中で戻るので、最後では呼ばれない）。 */
    @Inject(method = "tickServer", at = @At("HEAD"), require = 0)
    private void psitweaks$turbulenceTurnsDynamo(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || outputCap == null) {
            return;
        }
        long now = level.getGameTime();
        if (psitweaks$due(now, psitweaks$zoneWindReadAt)) {
            psitweaks$zoneWind = EntityTurbulenceZone.windUnitsAt(level, self.getBlockPos());
            psitweaks$zoneWindReadAt = now;
        }
        if (psitweaks$zoneWind > 0.0) {
            ImmersiveEngineeringWindmills.turnDynamo(outputCap.getCapability(), psitweaks$zoneWind);
        }
    }

    @Inject(method = "getActualTurnSpeed", at = @At("RETURN"), cancellable = true, require = 0)
    private void psitweaks$turbulenceSpinsRotor(CallbackInfoReturnable<Double> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || !level.isClientSide) {
            return;
        }
        long now = level.getGameTime();
        if (psitweaks$due(now, psitweaks$spinCheckedAt)) {
            BlockPos pos = self.getBlockPos();
            psitweaks$spinsInZone = EntityTurbulenceZone.coversOnClient(level, pos)
                    && ImmersiveEngineeringWindmills.rotorClear(level, pos, ImmersiveEngineeringWindmills.facingOf(self));
            psitweaks$spinCheckedAt = now;
        }
        if (psitweaks$spinsInZone) {
            cir.setReturnValue(cir.getReturnValue() + ImmersiveEngineeringWindmills.CLIENT_TURN_SPEED);
        }
    }

    /** 前に調べた tick {@code last}（まだなら {@link Long#MIN_VALUE}）から、調べ直す間隔がたったか（時間が戻ったときも真）。 */
    @Unique
    private static boolean psitweaks$due(long now, long last) {
        return last == Long.MIN_VALUE || now < last || now - last >= EntityTurbulenceZone.WIND_COUNT_INTERVAL;
    }
}
