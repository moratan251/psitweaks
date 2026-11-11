package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.SubLevelZoneMath;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * 領域の範囲の 1 つの見え方（2026-11-11）: 世界の範囲そのもの、または範囲に重なった機体（Sable のサブレベル）の座標へ写した範囲。
 * 機体のブロック（機械・氷など）は機体用の空間の座標にあるので、機体ごとに範囲を写して、その中のブロックにも効かせる。
 * {@link #worldPos} は、その範囲のブロックの位置を、領域干渉・乱流などの判定に使う世界の位置に直す。
 *
 * @param area    この見え方での範囲（世界、または機体の座標）
 * @param toWorld 機体の座標の点 → 世界の座標の点（行優先 4×4）。世界の範囲なら null
 */
public record ZoneRegion(ZoneArea area, double @Nullable [] toWorld) {
    public static ZoneRegion world(ZoneArea area) {
        return new ZoneRegion(area, null);
    }

    /** 機体の座標へ写した範囲か。 */
    public boolean onSubLevel() {
        return toWorld != null;
    }

    /** この範囲のブロック {@code pos} の、世界の座標での位置（ブロックの中心を写したところのブロック）。 */
    public BlockPos worldPos(BlockPos pos) {
        if (toWorld == null) {
            return pos;
        }
        double[] point = SubLevelZoneMath.transformPoint(toWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return BlockPos.containing(point[0], point[1], point[2]);
    }
}
