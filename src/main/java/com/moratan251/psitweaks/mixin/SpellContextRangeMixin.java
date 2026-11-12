package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityInterferenceZone;
import com.moratan251.psitweaks.common.spells.SpellRange;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.internal.SpatialHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;

/**
 * 術式の射程の判定。他者の領域干渉の中は対象にできず、射程は {@link SpellRange}（干渉距離延長デバイス・サードアイデバイス・
 * 自律詠唱機の干渉距離アップグレード）で延びる。
 */
@Mixin(value = SpellContext.class, remap = false)
public abstract class SpellContextRangeMixin {
    @Shadow
    public Player caster;

    @Shadow
    public Entity focalPoint;

    @Inject(method = "isInRadius(Lvazkii/psi/api/internal/Vector3;)Z", at = @At("HEAD"), cancellable = true)
    private void psitweaks$extendVectorRange(Vector3 vec, CallbackInfoReturnable<Boolean> cir) {
        if (vec == null) {
            return;
        }
        psitweaks$checkRange(vec.x, vec.y, vec.z, cir);
    }

    @Inject(method = "isInRadius(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void psitweaks$extendEntityRange(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // 術者自身・焦点でも、他者の領域干渉の中にいれば対象にできない（Psi はこの 2 つを射程を見ずに通すので、その前に見る）。
        if (entity != null) {
            Vec3 center = entity.getBoundingBox().getCenter();
            if (psitweaks$interfered(center.x, center.y, center.z)) {
                cir.setReturnValue(false);
                return;
            }
        }
        if (entity != null && SpellRange.ignoresRange(caster)) {
            cir.setReturnValue(true);
        }
        // ほかは Psi が isInRadius(DDD) へ回すので、延びた射程はそちらで見る。
    }

    @Inject(method = "isInRadius(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void psitweaks$extendCoordinateRange(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        psitweaks$checkRange(x, y, z, cir);
    }

    private void psitweaks$checkRange(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (psitweaks$interfered(x, y, z)) {
            cir.setReturnValue(false);
            return;
        }
        if (SpellRange.ignoresRange(caster)) {
            cir.setReturnValue(true);
            return;
        }
        Boolean extended = psitweaks$isInExtendedRange(x, y, z);
        if (extended != null) {
            cir.setReturnValue(extended);
            return;
        }
        Boolean sableResult = SableRangeCompat.isInRadius(focalPoint, x, y, z);
        if (sableResult != null) {
            cir.setReturnValue(sableResult);
        }
    }

    /**
     * 作動式: 領域干渉: 他者の、術者より事象干渉力の高い領域干渉の中は対象にできない（サーバー側だけ。射程を広げる装備より
     * 先に見る）。
     */
    private boolean psitweaks$interfered(double x, double y, double z) {
        return caster != null && !caster.level().isClientSide
                && EntityInterferenceZone.blocks(caster.level(), x, y, z, caster);
    }

    /**
     * 射程が Psi の射程より延びているときの判定。距離は Psi と同じく焦点からで、Sable の機体の中の点も Psi と同じ
     * {@link SpatialHelper} で測る。延びていなければ null（Psi の判定に任せる）。
     */
    private Boolean psitweaks$isInExtendedRange(double x, double y, double z) {
        if (caster == null || focalPoint == null) {
            return null;
        }
        double range = SpellRange.maxDistance(caster);
        if (range <= SpellContext.MAX_DISTANCE) {
            return null;
        }
        return SpatialHelper.distanceSquared(focalPoint.level(), x, y, z, focalPoint.position()) <= range * range;
    }
}
