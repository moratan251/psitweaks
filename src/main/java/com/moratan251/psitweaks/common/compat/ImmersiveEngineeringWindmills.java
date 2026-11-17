package com.moratan251.psitweaks.common.compat;

import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillBlockEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Immersive Engineering の風車と作動式: 乱流領域。IE のクラスに触れるので、IE が入っているときだけ呼ぶ
 * （{@link ImmersiveEngineeringCompat} と {@code WindmillTurbulenceMixin} から）。
 * <p>
 * 乱流の風は風車の回る速さ（天気・帆羽・前の空気）には乗せず、受け取った風量 1 WU ごとに
 * {@link EntityTurbulenceZone#WIND_FE_PER_WU} FE/t ぶんの回転を、風車とは別に後ろの動力ダイナモへ入れる。動力ダイナモは回転を
 * 設定 {@code dynamo_output} ÷ 3 倍の FE にするので、その倍率で割り戻して、設定によらず 1 WU = 15 FE/t にする。
 */
public final class ImmersiveEngineeringWindmills {
    private static final ResourceLocation WINDMILL = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "windmill");
    private static final ResourceLocation DYNAMO = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "dynamo");
    /** 羽の回る円の半径（IE の {@code WindmillBlockEntity#computeTurnSpeed} と同じ、中心から上下左右 4 マス）。 */
    private static final int ROTOR_RADIUS = 4;
    /** 動力ダイナモは回転 1 を {@code dynamo_output} ÷ この値 FE にする。 */
    private static final double DYNAMO_OUTPUT_DIVISOR = 3.0;
    /** 動力ダイナモが FE を整数に切り捨てるので、15 × WU がちょうど整数のときに 1 少なくならないよう足す分。 */
    private static final double ROUNDING_MARGIN = 1.0E-6;
    /** 風車の回る速さ（1 tick あたりの回転数）を IE の既定の設定の FE/t にする倍率（IE の {@code tickServer} の 2400）。 */
    private static final double FE_PER_TURN_SPEED = 2400.0;
    /** クライアントで、乱流の中の風車に足して見せる回る速さ: 1 台の上限の風を受けているときの速さ。 */
    public static final double CLIENT_TURN_SPEED =
            EntityTurbulenceZone.MAX_WU_PER_WINDMILL * EntityTurbulenceZone.WIND_FE_PER_WU / FE_PER_TURN_SPEED;

    private ImmersiveEngineeringWindmills() {
    }

    /** {@code blockEntity} が IE の風車そのもの（継承したものは除く）か。 */
    public static boolean isWindmill(BlockEntity blockEntity) {
        return blockEntity instanceof WindmillBlockEntity && WINDMILL.equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()));
    }

    /**
     * 乱流の風を受け取れるか: IE の風車で、羽の回る円の中（中心を除く）が空気で、後ろ（風車の向きの反対）に IE の動力ダイナモが
     * 回転を受け取る向きで置かれている。動力ダイナモは IE のものだけ（ほかの mod の回転の受け手は FE への換算が分からない）。
     */
    static boolean canTakeZoneWind(BlockEntity blockEntity) {
        if (!isWindmill(blockEntity) || !(blockEntity instanceof WindmillBlockEntity windmill)) {
            return false;
        }
        Level level = windmill.getLevel();
        if (level == null) {
            return false;
        }
        Direction facing = windmill.getFacing();
        BlockPos pos = windmill.getBlockPos();
        if (!rotorClear(level, pos, facing)) {
            return false;
        }
        BlockPos behind = pos.relative(facing.getOpposite());
        BlockEntity dynamo = level.getBlockEntity(behind);
        return dynamo != null && DYNAMO.equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(dynamo.getType()))
                && level.getCapability(IRotationAcceptor.CAPABILITY, behind, facing) != null;
    }

    /**
     * 風車 {@code pos}（向き {@code facing}）の羽の回る円の中が、中心を除いて空気か。IE の {@code computeTurnSpeed} の初めの
     * 確かめと同じ形（上下 4 段、段ごとの幅は 4・4・3・2・1）。前の空気（IE の自然の風）は見ない。
     */
    public static boolean rotorClear(Level level, BlockPos pos, Direction facing) {
        if (facing.getAxis() == Direction.Axis.Y) {
            return false;
        }
        Direction side = facing.getClockWise();
        for (int up = -ROTOR_RADIUS; up <= ROTOR_RADIUS; up++) {
            int width = rotorHalfWidth(Math.abs(up));
            for (int across = -width; across <= width; across++) {
                if ((up != 0 || across != 0) && !level.isEmptyBlock(pos.relative(side, across).above(up))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int rotorHalfWidth(int up) {
        return switch (up) {
            case 4 -> 1;
            case 3 -> 2;
            case 2 -> 3;
            default -> 4;
        };
    }

    /** IE の風車 {@code blockEntity} の向き（クライアントの見た目用）。 */
    public static Direction facingOf(BlockEntity blockEntity) {
        return ((WindmillBlockEntity) blockEntity).getFacing();
    }

    /**
     * 乱流の風 {@code units} WU ぶんの回転を、動力ダイナモ {@code dynamo} に入れる（1 WU = {@link EntityTurbulenceZone#WIND_FE_PER_WU}
     * FE/t、IE の設定 {@code dynamo_output} によらない）。{@code dynamo_output} が 0 なら何もしない。
     */
    public static void turnDynamo(@Nullable IRotationAcceptor dynamo, double units) {
        if (dynamo == null || units <= 0.0) {
            return;
        }
        double output = IEServerConfig.MACHINES.dynamo_output.get();
        if (output <= 0.0) {
            return;
        }
        double energy = units * EntityTurbulenceZone.WIND_FE_PER_WU;
        dynamo.inputRotation((energy + ROUNDING_MARGIN) * DYNAMO_OUTPUT_DIVISOR / output);
    }
}
