package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.compat.SubLevelZoneMath;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.RegionMatrix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
 * 行列の中にあるブロックの真ん中のブロックを中心にした一辺 {@code size} ブロックの立方体に入るもの。立方体の数え方は演算子: 直方体領域と同じ
 * （奇数は中心から対称、偶数は正の向きに 1 つ多い）なので、直方体領域の Number モードに同じサイズを入れると同じ範囲になる。
 * ブロックは整数の座標（ブロックの角）を点として行列で判定する（演算子: 領域ベクトルリスト化・作動式: 領域表示のブロックの
 * モードと同じ）。
 * <p>
 * 領域を置くときは {@link #boxed()} を通し、軸にそっていない行列も輪郭の箱（{@link #bounds()}）のとおりに効かせる。
 */
public final class ZoneArea {
    static final ZoneArea EMPTY = new ZoneArea(null, null, BlockPos.ZERO, false, 0, 0, 0, 0, -1, -1, -1);
    private static final double EPSILON = 1.0E-9;
    /**
     * 中心のブロックの座標の上限（絶対値）。ワールドの端（3000 万）よりずっと遠く、サイズの立方体を足しても int に収まる。
     */
    private static final double MAX_CENTER = 1 << 30;

    @Nullable
    private final MatrixValue matrix;
    @Nullable
    private final RegionMatrix region;
    private final BlockPos center;
    /** 中心のブロックを決めて渡したか（機体の座標から写した範囲。同期のときに中心も送る）。 */
    private final boolean fixedCenter;
    private final int size;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    private ZoneArea(@Nullable MatrixValue matrix, @Nullable RegionMatrix region, BlockPos center, boolean fixedCenter,
                     int size, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.matrix = matrix;
        this.region = region;
        this.center = center;
        this.fixedCenter = fixedCenter;
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
        return of(matrix, size, null);
    }

    /** {@code fixedCenter} が null でなければ、それを中心のブロックにする（サイズの立方体もそこを中心にする）。 */
    private static ZoneArea of(MatrixValue matrix, int size, @Nullable BlockPos fixedCenter) throws SpellRuntimeException {
        RegionMatrix region = RegionMatrix.parse(matrix);
        Vector3 far = region.corner(true, true, true);
        Vector3 start = region.corner(false, false, false);

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
        // ブロックの番号は double のまま数える。行列の座標は int の範囲を超えられ、int にするとあふれる。
        double firstX = Math.ceil(lowX - EPSILON);
        double firstY = Math.ceil(lowY - EPSILON);
        double firstZ = Math.ceil(lowZ - EPSILON);
        double lastX = Math.floor(highX + EPSILON);
        double lastY = Math.floor(highY + EPSILON);
        double lastZ = Math.floor(highZ + EPSILON);
        // 中心は行列の中にあるブロックの真ん中。行列の真ん中の点を切り捨てると、端数のある行列（平行移動行列で半端に
        // ずらしたものなど）では中にあるブロックとずれ、サイズちょうどの行列でも 1 列欠ける。
        BlockPos center = fixedCenter != null ? fixedCenter : new BlockPos(
                middle(firstX, lastX, start.x + far.x),
                middle(firstY, lastY, start.y + far.y),
                middle(firstZ, lastZ, start.z + far.z));
        int edge = Math.max(0, size);
        // 演算子: 直方体領域と同じ: 中心から負の向きに (edge - 1) / 2、残りを正の向きに。
        int low = -((edge - 1) / 2);
        int high = low + edge - 1;
        // 中心は MAX_CENTER の内側なので、中心 ± サイズは int に収まる。行列のブロックの端（first・last）との重なりだけを残す
        // （その軸にブロックがなければ first > last で空になる。int にするときは端で止まる）。
        return new ZoneArea(matrix, region, center, fixedCenter != null, edge,
                (int) Math.max(center.getX() + low, firstX),
                (int) Math.max(center.getY() + low, firstY),
                (int) Math.max(center.getZ() + low, firstZ),
                (int) Math.min(center.getX() + high, lastX),
                (int) Math.min(center.getY() + high, lastY),
                (int) Math.min(center.getZ() + high, lastZ));
    }

    /**
     * 1 つの軸で、ブロック {@code first}〜{@code last} の真ん中（数が偶数なら負の側。サイズの立方体は正の向きに 1 つ多いので、
     * サイズちょうどならちょうど収まる）。その軸にブロックがなければ、行列の真ん中の点（{@code startPlusFar} の半分）を
     * 切り捨てたもの。
     * <p>
     * 中心が {@link #MAX_CENTER} より遠い範囲は、どの術者の射程にも入らない（ワールドの端より遠い）ので、射程外のエラーにする。
     * 中心を int にするとあふれ、別の場所（-1 など）を中心にしてしまう。
     */
    private static int middle(double first, double last, double startPlusFar) throws SpellRuntimeException {
        double middle = first > last ? Math.floor(startPlusFar * 0.5 + EPSILON) : Math.floor((first + last) * 0.5);
        if (!(Math.abs(middle) <= MAX_CENTER)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        return (int) middle;
    }

    /**
     * 機体（Sable のサブレベル）の座標で作った範囲を、その機体の姿勢 {@code pose}（行優先 4×4、機体の座標の点 → 世界の座標の点）で
     * 世界の座標へ写す。写した範囲には、中心が機体の座標の範囲（見た目の箱）に入る世界の
     * ブロックが入る。
     * <p>
     * 行列が軸にそっていれば（演算子: 直方体領域・行列を省いたとき）、サイズで切り詰めた後の箱をそのまま写す（傾いた範囲の角が
     * 世界の立方体で欠けないよう、立方体は写した範囲が全部入る大きさにする）。軸にそっていない行列でも、サイズの立方体に全部
     * 入っていれば（写した範囲をもう一度写すときなど）行列をそのまま写して同じ扱いにする。立方体からはみ出す傾いた行列だけは、
     * 行列そのものを写し、サイズの立方体は写した先の座標で切る。
     * <p>
     * 世界の範囲を機体の座標へ写すとき（範囲に重なる機体のブロックにも効かせる）は、姿勢の逆行列を渡す。
     */
    public ZoneArea transformedBy(double[] pose) throws SpellRuntimeException {
        if (isEmpty()) {
            return this;
        }
        double[] source = matrix4x4();
        double[] toWorld = SubLevelZoneMath.cornerAffine(pose);
        boolean exact = SubLevelZoneMath.isAxisAligned(source);
        double[] world = SubLevelZoneMath.multiply(toWorld, exact
                ? SubLevelZoneMath.cuboid(minX - 0.5, minY - 0.5, minZ - 0.5, maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1)
                : source);
        exact = exact || SubLevelZoneMath.enclosingEdge(source) <= size;
        // 中心は、写した範囲の見た目の中心が入るブロック（ZoneArea の決め方のままだと、傾くと 1 ブロックずれることがある）。
        int[] middle = SubLevelZoneMath.visualCenter(world);
        int edge = exact ? SubLevelZoneMath.enclosingEdge(world, middle) : size;
        return of(new MatrixValue(4, 4, world), edge, new BlockPos(middle[0], middle[1], middle[2]));
    }

    /**
     * 輪郭の箱（{@link #bounds()}）のブロックを全部入れた範囲（領域は輪郭の線のとおりに効く）。行列が軸に
     * そっていないとき（演算子: 回転行列で回した行列、傾いた機体から写した範囲）は、行列の中だけを範囲にすると輪郭の箱の角が
     * 欠けるので、輪郭の箱の行列に置き換える。中心のブロックとサイズは変えない。軸にそった行列はもともと輪郭のとおりなので、
     * そのまま返す。
     * <p>
     * 置く範囲（世界の座標）に使う。機体の座標へ写した範囲（{@link #transformedBy} に姿勢の逆行列を渡したもの）は、世界の輪郭の
     * 箱を機体の向きで表したものなので、これを通さない。
     */
    public ZoneArea boxed() throws SpellRuntimeException {
        if (isEmpty() || SubLevelZoneMath.isAxisAligned(matrix4x4())) {
            return this;
        }
        return of(new MatrixValue(4, 4, SubLevelZoneMath.cuboid(minX - 0.5, minY - 0.5, minZ - 0.5,
                maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1)), size, center);
    }

    /** 行列を 4×4（行優先）にしたもの。3×4 なら 4 行目に 0 0 0 1 を足す。 */
    private double[] matrix4x4() {
        double[] values = new double[16];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                values[row * 4 + col] = row < matrix.rows() ? matrix.get(row, col) : (col == 3 ? 1.0 : 0.0);
            }
        }
        return values;
    }

    /** 範囲にブロックが 1 つもないか（囲む箱が空）。 */
    public boolean isEmpty() {
        return region == null || minX > maxX || minY > maxY || minZ > maxZ;
    }

    /** ブロック {@code pos} が範囲を囲む箱（{@link #bounds}）の中か。新しいオブジェクトを作らずに確かめる。 */
    public boolean withinBox(BlockPos pos) {
        return !isEmpty() && pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    /** 範囲を囲む箱どうしが、少なくとも 1 ブロック重なっているか。 */
    public boolean boxOverlaps(ZoneArea other) {
        return !isEmpty() && !other.isEmpty() && minX <= other.maxX && other.minX <= maxX && minY <= other.maxY
                && other.minY <= maxY && minZ <= other.maxZ && other.minZ <= maxZ;
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

    /** 範囲の中心のブロック（行列の中にあるブロックの真ん中）。 */
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
        buffer.writeBoolean(fixedCenter);
        if (fixedCenter) {
            buffer.writeBlockPos(center);
        }
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
        BlockPos fixedCenter = buffer.readBoolean() ? buffer.readBlockPos() : null;
        try {
            return of(new MatrixValue(rows, cols, values), size, fixedCenter);
        } catch (SpellRuntimeException e) {
            return EMPTY;
        }
    }
}
