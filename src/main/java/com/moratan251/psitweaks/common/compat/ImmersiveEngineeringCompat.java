package com.moratan251.psitweaks.common.compat;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

/**
 * Immersive Engineering との連携の入り口。IE のクラスに触れる処理は {@link ImmersiveEngineeringWindmills} に置き、ここでは IE が
 * 入っているかを確かめてから呼ぶ（IE がなくてもこのクラスは読み込める）。
 */
public final class ImmersiveEngineeringCompat {
    private static final String MOD_ID = "immersiveengineering";

    private ImmersiveEngineeringCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * {@code blockEntity} が、作動式: 乱流領域の風を受け取れる IE の風車か: 羽の回る円の中が空いていて、後ろに IE の動力ダイナモが
     * 風車の向きでつながっている。IE がなければ偽。
     */
    public static boolean isWindReceivingWindmill(@Nullable BlockEntity blockEntity) {
        return blockEntity != null && isLoaded() && ImmersiveEngineeringWindmills.canTakeZoneWind(blockEntity);
    }
}
