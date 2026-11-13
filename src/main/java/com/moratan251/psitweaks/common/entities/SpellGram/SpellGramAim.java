package com.moratan251.psitweaks.common.entities.SpellGram;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 視線の先の魔法式オブジェクトの核を探す（2026-11-12、耐久値の表示に使う）。魔法式オブジェクトはふつうの照準では選べない
 * （{@code isPickable} が偽）ので、核の当たりの箱（{@link #coreBox}）と視線を自分で比べる。ブロックに遮られていれば選ばない。
 */
public final class SpellGramAim {
    /** 視線が届く距離（ブロック）。 */
    public static final double RANGE = 24.0;
    /** 核の当たりの箱の半分の大きさ（ブロック）。領域の核の魔法陣とほぼ同じ大きさ。 */
    public static final double CORE_HALF = 0.6;

    private SpellGramAim() {
    }

    /** 核の当たりの箱: オブジェクトの位置を中心にした一辺 {@link #CORE_HALF} × 2。 */
    public static AABB coreBox(SpellGramObject object) {
        return new AABB(object.position(), object.position()).inflate(CORE_HALF);
    }

    /** {@code viewer} の視線の先の、ブロックに遮られていない魔法式オブジェクトの核のうち最も近いもの（{@link #RANGE} まで）。 */
    @Nullable
    public static SpellGramObject aimedAt(Level level, Entity viewer, float partialTicks) {
        return aimedAt(level, viewer, partialTicks, RANGE);
    }

    /** {@link #aimedAt(Level, Entity, float)} を、視線が届く距離 {@code range} で探す。 */
    @Nullable
    public static SpellGramObject aimedAt(Level level, Entity viewer, float partialTicks, double range) {
        Vec3 eye = viewer.getEyePosition(partialTicks);
        Vec3 end = eye.add(viewer.getViewVector(partialTicks).scale(range));
        HitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, viewer));
        double limit = block.getType() == HitResult.Type.MISS ? range * range : eye.distanceToSqr(block.getLocation());
        SpellGramObject nearest = null;
        for (SpellGramObject object : level.getEntitiesOfClass(SpellGramObject.class, new AABB(eye, end).inflate(CORE_HALF + 1.0))) {
            if (object.isRemoved() || object.isEnded()) {
                continue;
            }
            AABB core = coreBox(object);
            Optional<Vec3> hit = core.contains(eye) ? Optional.of(eye) : core.clip(eye, end);
            if (hit.isPresent()) {
                double distance = eye.distanceToSqr(hit.get());
                if (distance < limit) {
                    limit = distance;
                    nearest = object;
                }
            }
        }
        return nearest;
    }
}
