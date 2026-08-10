package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 術者の向きを軸方向に丸めた基準座標系(右・上・前)。
 */
final class CasterAxialBasis {

    record TargetFace(CasterAxialBasis basis, Vector3 blockPosition) {
    }

    final Direction right;
    final Direction up;
    final Direction forward;

    private CasterAxialBasis(Direction right, Direction up, Direction forward) {
        this.right = right;
        this.up = up;
        this.forward = forward;
    }

    /**
     * ヨーのみを見る基準座標系。ピッチは無視され、前は常に水平になる。
     */
    static CasterAxialBasis of2D(Entity caster) {
        Direction yawFacing = Direction.fromYRot(caster.getYRot());
        return new CasterAxialBasis(yawFacing.getClockWise(Direction.Axis.Y), Direction.UP, yawFacing);
    }

    /**
     * ピッチも含む基準座標系。前は視線を6方角に丸めた方向で、
     * 前が垂直の時は上がヨー基準の水平方向になる(上 = 右 × 前 と等価)。
     */
    static CasterAxialBasis of3D(Entity caster) {
        Direction yawFacing = Direction.fromYRot(caster.getYRot());
        Direction right = yawFacing.getClockWise(Direction.Axis.Y);
        Vec3 look = caster.getLookAngle();
        Direction forward = Direction.getNearest(look.x, look.y, look.z);
        Direction up = forward.getAxis().isVertical()
                ? (forward == Direction.DOWN ? yawFacing : yawFacing.getOpposite())
                : Direction.UP;
        return new CasterAxialBasis(right, up, forward);
    }

    /**
     * 術者の通常レイキャストが命中した面を基準にする座標系。
     * 前はブロックの内側を向き、側面では上がワールド上方向になる。
     * 上下面では面内の回転を術者の水平向きから決める。
     */
    static TargetFace targetFace(SpellContext context) throws SpellRuntimeException {
        Vector3 origin = Vector3.fromEntity(context.caster).add(0, context.caster.getEyeHeight(), 0);
        Vector3 look = new Vector3(context.caster.getLookAngle());
        BlockHitResult hit = RaycastHelper.raycast(
                context.caster,
                origin,
                look,
                SpellContext.MAX_DISTANCE,
                RaycastHelper.Mode.NORMAL
        );
        if (hit.getType() == HitResult.Type.MISS) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        CasterAxialBasis basis = ofFace(hit.getDirection(), Direction.fromYRot(context.caster.getYRot()));
        return new TargetFace(basis, Vector3.fromBlockPos(hit.getBlockPos()));
    }

    static CasterAxialBasis ofFace(Direction faceNormal, Direction yawFacing) {
        Direction forward = faceNormal.getOpposite();
        if (!forward.getAxis().isVertical()) {
            return new CasterAxialBasis(
                    forward.getClockWise(Direction.Axis.Y),
                    Direction.UP,
                    forward
            );
        }

        Direction horizontalFacing = yawFacing.getAxis().isHorizontal() ? yawFacing : Direction.SOUTH;
        Direction right = horizontalFacing.getClockWise(Direction.Axis.Y);
        Direction up = forward == Direction.DOWN ? horizontalFacing : horizontalFacing.getOpposite();
        return new CasterAxialBasis(right, up, forward);
    }
}
