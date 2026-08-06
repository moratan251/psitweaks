package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import vazkii.psi.api.internal.Vector3;

public final class RaycastHelper {
    private RaycastHelper() {
    }

    public enum Mode {
        NORMAL(ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE),
        WEAK(ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY),
        STRONG(ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);

        private final ClipContext.Block blockMode;
        private final ClipContext.Fluid fluidMode;

        Mode(ClipContext.Block blockMode, ClipContext.Fluid fluidMode) {
            this.blockMode = blockMode;
            this.fluidMode = fluidMode;
        }
    }

    public static BlockHitResult raycast(Entity entity, Vector3 origin, Vector3 ray, double length, Mode mode) {
        Vector3 end = origin.copy().add(ray.copy().normalize().multiply(length));
        return entity.level().clip(new ClipContext(
                origin.toVec3D(),
                end.toVec3D(),
                mode.blockMode,
                mode.fluidMode,
                entity
        ));
    }
}

