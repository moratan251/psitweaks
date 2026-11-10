package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.RegionMatrix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * 領域型の魔法式オブジェクトの範囲: 領域行列（3×4 / 4×4、1〜3 列目が辺、4 列目が始点）の中にあるブロックのうち、
 * 行列の中心のブロックを中心にした一辺 {@code size} ブロックの立方体に入るもの。立方体の数え方は演算子: 直方体領域と同じ
 * （奇数は中心から対称、偶数は正の向きに 1 つ多い）なので、直方体領域の Number モードに同じサイズを入れると同じ範囲になる。
 * ブロックは整数の座標（ブロックの角）を点として行列で判定する（演算子: 領域ベクトルリスト化・作動式: 領域表示のブロックの
 * モードと同じ）。
 */
public final class ZoneArea {
    static final ZoneArea EMPTY = new ZoneArea(null, null, BlockPos.ZERO, 0, 0, 0, 0, -1, -1, -1);
    private static final double EPSILON = 1.0E-9;

    @Nullable
    private final MatrixValue matrix;
    @Nullable
    private final RegionMatrix region;
    private final BlockPos center;
    private final int size;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    private ZoneArea(@Nullable MatrixValue matrix, @Nullable RegionMatrix region, BlockPos center, int size,
                     int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.matrix = matrix;
        this.region = region;
        this.center = center;
        this.size = size;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /**
     * ブロック {@code center} を中心にした一辺 {@code size} ブロックの立方体（演算子: 直方体領域の Number モードで同じサイズを
     * 入れたときと同じ範囲）。領域型の作動式で行列を省いたときに使う。
     */
    public static ZoneArea cube(BlockPos center, int size) throws SpellRuntimeException {
        int edge = Math.max(1, size);
        int low = -((edge - 1) / 2);
        return of(new MatrixValue(4, 4, new double[]{
                edge, 0, 0, center.getX() + low - 0.5,
                0, edge, 0, center.getY() + low - 0.5,
                0, 0, edge, center.getZ() + low - 0.5,
                0, 0, 0, 1}), edge);
    }

    /** 領域行列と一辺のブロック数から範囲を作る。行列の形が正しくなければ、領域行列の演算子と同じエラー。 */
    public static ZoneArea of(MatrixValue matrix, int size) throws SpellRuntimeException {
        RegionMatrix region = RegionMatrix.parse(matrix);
        Vector3 far = region.corner(true, true, true);
        Vector3 start = region.corner(false, false, false);
        BlockPos center = BlockPos.containing(
                Math.floor((start.x + far.x) * 0.5 + EPSILON),
                Math.floor((start.y + far.y) * 0.5 + EPSILON),
                Math.floor((start.z + far.z) * 0.5 + EPSILON));

        double lowX = Double.POSITIVE_INFINITY;
        double lowY = Double.POSITIVE_INFINITY;
        double lowZ = Double.POSITIVE_INFINITY;
        double highX = Double.NEGATIVE_INFINITY;
        double highY = Double.NEGATIVE_INFINITY;
        double highZ = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 8; i++) {
            Vector3 corner = region.corner((i & 1) != 0, (i & 2) != 0, (i & 4) != 0);
            lowX = Math.min(lowX, corner.x);
            lowY = Math.min(lowY, corner.y);
            lowZ = Math.min(lowZ, corner.z);
            highX = Math.max(highX, corner.x);
            highY = Math.max(highY, corner.y);
            highZ = Math.max(highZ, corner.z);
        }
        int edge = Math.max(0, size);
        // 演算子: 直方体領域と同じ: 中心から負の向きに (edge - 1) / 2、残りを正の向きに。
        int low = -((edge - 1) / 2);
        int high = low + edge - 1;
        return new ZoneArea(matrix, region, center, edge,
                Math.max(center.getX() + low, Mth.ceil(lowX - EPSILON)),
                Math.max(center.getY() + low, Mth.ceil(lowY - EPSILON)),
                Math.max(center.getZ() + low, Mth.ceil(lowZ - EPSILON)),
                Math.min(center.getX() + high, Mth.floor(highX + EPSILON)),
                Math.min(center.getY() + high, Mth.floor(highY + EPSILON)),
                Math.min(center.getZ() + high, Mth.floor(highZ + EPSILON)));
    }

    /** 範囲にブロックが 1 つもないか（囲む箱が空）。 */
    public boolean isEmpty() {
        return region == null || minX > maxX || minY > maxY || minZ > maxZ;
    }

    /** ブロック {@code pos} が範囲の中か。 */
    public boolean contains(BlockPos pos) {
        if (isEmpty()) {
            return false;
        }
        if (pos.getX() < minX || pos.getX() > maxX || pos.getY() < minY || pos.getY() > maxY
                || pos.getZ() < minZ || pos.getZ() > maxZ) {
            return false;
        }
        return region.contains(new Vector3(pos.getX(), pos.getY(), pos.getZ()));
    }

    /**
     * 範囲の中のブロックエンティティ（デポ・ベルト、Mekanism の機械など）。範囲にかかる読み込み済みのチャンクの
     * ブロックエンティティから探す（範囲の全ブロックは見ない）。
     */
    public List<BlockEntity> blockEntities(Level level) {
        List<BlockEntity> found = new ArrayList<>();
        if (isEmpty()) {
            return found;
        }
        int minChunkX = SectionPos.blockToSectionCoord(minX);
        int maxChunkX = SectionPos.blockToSectionCoord(maxX);
        int minChunkZ = SectionPos.blockToSectionCoord(minZ);
        int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!blockEntity.isRemoved() && contains(blockEntity.getBlockPos())) {
                        found.add(blockEntity);
                    }
                }
            }
        }
        return found;
    }

    /** 行列の中心のブロック。 */
    public BlockPos center() {
        return center;
    }

    /** 立方体の一辺のブロック数。 */
    public int size() {
        return size;
    }

    /** 範囲を囲む箱（ブロックの外側の面まで）。 */
    public AABB bounds() {
        if (isEmpty()) {
            return new AABB(center);
        }
        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(matrix != null);
        if (matrix == null) {
            return;
        }
        buffer.writeVarInt(matrix.rows());
        buffer.writeVarInt(matrix.cols());
        for (int row = 0; row < matrix.rows(); row++) {
            for (int col = 0; col < matrix.cols(); col++) {
                buffer.writeDouble(matrix.get(row, col));
            }
        }
        buffer.writeVarInt(size);
    }

    static ZoneArea read(RegistryFriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return EMPTY;
        }
        int rows = buffer.readVarInt();
        int cols = buffer.readVarInt();
        if (rows < 3 || rows > 4 || cols != 4) {
            return EMPTY;
        }
        double[] values = new double[rows * cols];
        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.readDouble();
        }
        int size = buffer.readVarInt();
        try {
            return of(new MatrixValue(rows, cols, values), size);
        } catch (SpellRuntimeException e) {
            return EMPTY;
        }
    }
}
