package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 術者の向きを軸方向に丸めた基準座標系（右・上・前）。
 */
final class CasterAxialBasis {
    final Direction right;
    final Direction up;
    final Direction forward;

    private CasterAxialBasis(Direction right, Direction up, Direction forward) {
        this.right = right;
        this.up = up;
        this.forward = forward;
    }

    static CasterAxialBasis of2D(Entity caster) {
        Direction yawFacing = Direction.fromYRot(caster.getYRot());
        return new CasterAxialBasis(yawFacing.getClockWise(Direction.Axis.Y), Direction.UP, yawFacing);
    }

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
}

