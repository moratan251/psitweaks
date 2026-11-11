package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.SpellBlockProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 領域型の魔法式オブジェクト（○○ Zone）の基底。範囲は {@link ZoneArea}（領域行列を、その中心のブロックから ±大きさの
 * 立方体で切り詰めたブロックの集まり）。エンティティは当たり判定の中心があるブロックで判定する。
 * <p>
 * 範囲の中のエンティティへの効き目は {@link #affect}（{@link #effectInterval} tick ごと）。何に効くかは種類ごと
 * （{@link #affects}、既定は生き物だけ）で、魔法式オブジェクトにはどの領域も効かない。描画は範囲を囲む箱
 * （{@link #getOutline}）と、種類ごとの中の演出（{@link #spawnClientParticles}）。
 * <p>
 * ブロックへの効き目（{@link #affectBlock}）は、範囲を 1 tick に 1 段ずつ下から見ていき、一周したらまた下から。
 * 読み込まれていないチャンクは見ない。保護を確かめるのは種類ごと（術者がプレイヤーのときだけ呼ぶ）。
 * <p>
 * 他 mod の機械を動かす効き目（Create の加工、Mekanism の熱）の速さは、置いた時点の事象干渉力で決まる
 * （{@link #machineSpeedFor}）。事象干渉力はユーティリティの効き目に効かない方針の例外（2026-11-10 ユーザー、一括洗浄）。
 * <p>
 * 他者の領域干渉が重なったブロックでは、その領域干渉のほうが置いた時点の事象干渉力が高ければ、効き目が止まる
 * （{@link #isActiveAt}、{@link EntityInterferenceZone}）。
 */
public abstract class SpellGramZone extends SpellGramObject {
    /** 氷炎地獄が起きているか（{@link InfernoZones}）。演出のためクライアントへ送る。 */
    private static final EntityDataAccessor<Boolean> DATA_INFERNO =
            SynchedEntityData.defineId(SpellGramZone.class, EntityDataSerializers.BOOLEAN);
    /** 同じ術者の乱流領域と重なっているか（熱乱流・吹雪などの演出のためクライアントへ送る）。 */
    private static final EntityDataAccessor<Boolean> DATA_STORM =
            SynchedEntityData.defineId(SpellGramZone.class, EntityDataSerializers.BOOLEAN);

    private ZoneArea area = ZoneArea.EMPTY;
    /** 氷炎地獄を続けるゲーム時刻（これより前なら起きている）。サーバー側だけ。 */
    private long infernoUntil = Long.MIN_VALUE;
    /** 氷炎地獄の強さ（両側の威力のうち小さいほう）。サーバー側だけ。 */
    private float infernoPower;
    private int color = 0xFFFFFF;
    /** 描画だけが使う: 床の魔法陣に前のフレームで選んだ画像の段（-1 はまだない）。同期も保存もしない。 */
    private int floorCircleLevel = -1;
    /** 次にブロックを見る段（y）。範囲の外なら下の段から。サーバー側だけ。 */
    private int scanStep;
    /** 他 mod の機械を動かす速さ（基準の何倍か）。置いた時点の事象干渉力で決まる。サーバー側だけ。 */
    private float machineSpeed = 1.0F;
    private float machineProgress;
    /** 術者が使ってよいブロック（保護）か。機械（デポ・ベルト、Mekanism の機械）ごとに一度だけ確かめる。サーバー側だけ。 */
    private final Map<BlockPos, Boolean> mayUseCache = new HashMap<>();
    /**
     * 置いた人の側（術者の {@code getUUID()}。自律詠唱機はその所有者の UUID を名乗るので、所有者と同じ側になる）。
     * 領域干渉が「他者」かを見分けるのに使う。サーバー側だけ。
     */
    private UUID ownerSide;
    /** 置いた時点の術者の事象干渉力。領域干渉と比べる。サーバー側だけ。 */
    private double placedInterference = 1.0;

    /** 機械を動かす速さの基準: 事象干渉力 1 あたりの倍率（仮）。 */
    public static final float MACHINE_SPEED_PER_INTERFERENCE = 0.5F;
    /** 機械を動かす速さの上限（基準の何倍か、仮）。 */
    public static final float MAX_MACHINE_SPEED = 2.0F;

    protected SpellGramZone(EntityType<? extends SpellGramZone> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSpellGramSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_INFERNO, false);
        builder.define(DATA_STORM, false);
    }

    /** 同じ術者の乱流領域と重なっているか（クライアントでも分かる）。 */
    public boolean isStorm() {
        return entityData.get(DATA_STORM);
    }

    /**
     * 乱流の威力 1 あたりの、組み合わせで機械を動かす速さの上乗せ（Create の加工・Mekanism の熱・吹雪の凍え、仮）。
     * 威力 10 で 2 倍（2026-11-11 ユーザー: 組み合わせの強さは乱流の威力で決まり、重なっていれば最も強いもの）。
     */
    public static final double STORM_BOOST_PER_POWER = 0.1;
    /** 乱流の威力 1 あたりの、組み合わせのダメージ（1 回、倍率を掛ける前、仮）。威力 10 で 3（氷炎地獄の 4 より弱い）。 */
    public static final float STORM_DAMAGE_PER_POWER = 0.3F;

    /** 乱流ごとの、組み合わせで機械を動かす上乗せの進み（乱流のエンティティの番号ごと）。サーバー側だけ。 */
    private final Map<Integer, Float> stormProgress = new HashMap<>();

    /**
     * ブロック {@code pos} で、この領域を強めている同じ術者の乱流領域（サーバー側）。重なっていれば威力の最も強いもの
     * （同じならエンティティの番号の小さいもの）。吹いていない・この領域が効かないブロックなら null。
     */
    @org.jetbrains.annotations.Nullable
    public EntityTurbulenceZone stormAt(BlockPos pos) {
        if (level().isClientSide || !isInsideAndActive(pos)) {
            return null;
        }
        EntityTurbulenceZone strongest = null;
        for (SpellGramObject object : SpellGramIndex.ofCaster(level(), getCasterUuid())) {
            if (object instanceof EntityTurbulenceZone turbulence && !turbulence.isEnded()
                    && turbulence.isInsideAndActive(pos)
                    && (strongest == null || turbulence.getPower() > strongest.getPower()
                    || (turbulence.getPower() == strongest.getPower() && turbulence.getId() < strongest.getId()))) {
                strongest = turbulence;
            }
        }
        return strongest;
    }

    /** ブロック {@code pos} で、この領域を強めている乱流の威力（{@link #stormAt}、なければ 0）。 */
    public float stormPowerAt(BlockPos pos) {
        EntityTurbulenceZone storm = stormAt(pos);
        return storm == null ? 0.0F : storm.getPower();
    }

    /**
     * ブロック {@code pos} で、この領域に同じ術者の乱流領域の風が吹いているか（サーバー側）。熱乱流・吹雪・暴風雨は
     * ここが真のブロックだけ。
     */
    public boolean isStormAt(BlockPos pos) {
        return stormAt(pos) != null;
    }

    /** 威力 {@code power} の乱流が、組み合わせで機械を動かす速さに上乗せする分（威力 10 で +1、つまり 2 倍）。 */
    public static double stormBoost(double power) {
        return Math.max(0.0, power) * STORM_BOOST_PER_POWER;
    }

    /** この領域と範囲が重なっている、同じ術者の乱流領域（サーバー側）。 */
    protected List<EntityTurbulenceZone> overlappingStorms() {
        List<EntityTurbulenceZone> storms = new java.util.ArrayList<>();
        for (SpellGramObject object : SpellGramIndex.ofCaster(level(), getCasterUuid())) {
            if (object instanceof EntityTurbulenceZone turbulence && turbulence != this && !turbulence.isEnded()
                    && turbulence.getArea().bounds().intersects(area.bounds())) {
                storms.add(turbulence);
            }
        }
        return storms;
    }

    /**
     * 毎 tick 呼ぶ: 重なっている乱流ごとに、機械を動かす上乗せ（速さ {@code machineSpeed} × {@link #stormBoost}）を進め、
     * たまった回数ぶん {@code step} を呼ぶ（その乱流が最も強いブロックだけを動かすのは呼ぶ側）。
     */
    protected void forEachStormStep(float machineSpeed, java.util.function.BiConsumer<EntityTurbulenceZone, Integer> step) {
        List<EntityTurbulenceZone> storms = overlappingStorms();
        stormProgress.keySet().removeIf(id -> storms.stream().noneMatch(storm -> storm.getId() == id));
        for (EntityTurbulenceZone storm : storms) {
            float progress = stormProgress.getOrDefault(storm.getId(), 0.0F)
                    + (float) (machineSpeed * stormBoost(storm.getPower()));
            int steps = (int) Math.floor(progress);
            stormProgress.put(storm.getId(), progress - steps);
            if (steps > 0) {
                step.accept(storm, steps);
            }
        }
    }

    /**
     * クライアント側の演出用: この領域と、見えている乱流領域の重なり（術者は見分けない。{@link #isStorm} が真のときだけ呼ぶ）。
     */
    protected List<AABB> stormBoxes() {
        List<AABB> boxes = new java.util.ArrayList<>();
        AABB own = getOutline();
        for (EntityTurbulenceZone turbulence : level().getEntitiesOfClass(EntityTurbulenceZone.class, own.inflate(1.0))) {
            if (turbulence != this) {
                AABB overlap = ZoneAttacks.overlap(own, turbulence.getOutline());
                if (overlap != null && overlap.getSize() > 0.0) {
                    boxes.add(overlap);
                }
            }
        }
        return boxes;
    }

    /** 同じ術者の乱流領域が重なっているかを確かめ直す（サーバー側、ときどき）。 */
    protected void refreshStorm() {
        boolean storm = false;
        for (SpellGramObject object : SpellGramIndex.ofCaster(level(), getCasterUuid())) {
            if (object instanceof EntityTurbulenceZone turbulence && turbulence != this && !turbulence.isEnded()
                    && turbulence.getArea().bounds().intersects(area.bounds())) {
                storm = true;
                break;
            }
        }
        if (storm != isStorm()) {
            entityData.set(DATA_STORM, storm);
        }
    }

    /** 氷炎地獄が起きているか（両側。クライアントでも分かる）。 */
    public boolean isInferno() {
        return entityData.get(DATA_INFERNO);
    }

    /** 氷炎地獄の強さ（両側の威力のうち小さいほう、サーバー側）。 */
    float getInfernoPower() {
        return infernoPower;
    }

    /** ゲーム時刻 {@code gameTime} に氷炎地獄が起きているか（サーバー側）。 */
    boolean isInfernoAt(long gameTime) {
        return gameTime < infernoUntil;
    }

    /** 氷炎地獄を {@code until}（ゲーム時刻）まで続ける（サーバー側）。 */
    void extendInferno(long until, float power) {
        infernoUntil = Math.max(infernoUntil, until);
        infernoPower = power;
        if (!isInferno() && isInfernoAt(level().getGameTime())) {
            entityData.set(DATA_INFERNO, true);
        }
    }

    /** 床の魔法陣に前のフレームで選んだ画像の段（クライアントの描画用）。 */
    public int getFloorCircleLevel() {
        return floorCircleLevel;
    }

    public void setFloorCircleLevel(int level) {
        this.floorCircleLevel = level;
    }

    /** 置く前に呼ぶ。範囲と、輪郭の色（術者の CAD の色）。位置は範囲の中心のブロックの中心にする。 */
    public void configureZone(ZoneArea area, int color) {
        this.area = area;
        this.color = color & 0xFFFFFF;
        Vec3 center = Vec3.atCenterOf(area.center());
        setPos(center.x, center.y, center.z);
    }

    public ZoneArea getArea() {
        return area;
    }

    /**
     * 置いた時点の事象干渉力から、他 mod の機械を動かす速さ（基準の何倍か）: 干渉力 1 で半分、2 で同じ、上限 2 倍（仮）。
     * 基準は、一括洗浄・一括燻製ならケース入りファン、熱なら薪木ストーブ 1 台。
     */
    public static float machineSpeedFor(double interference) {
        return (float) Math.max(0.0, Math.min(MAX_MACHINE_SPEED, interference * MACHINE_SPEED_PER_INTERFERENCE));
    }

    /** 置く前に呼ぶ。置いた人の側（術者の {@code getUUID()}）と、置いた時点の術者の事象干渉力。 */
    public void setPlacement(UUID ownerSide, double interference) {
        this.ownerSide = ownerSide;
        this.placedInterference = interference;
    }

    @org.jetbrains.annotations.Nullable
    public UUID getOwnerSide() {
        return ownerSide;
    }

    public double getPlacedInterference() {
        return placedInterference;
    }

    /**
     * ブロック {@code pos} でこの領域が効くか: 他者の、置いた時点の事象干渉力がこれより高い領域干渉が重なっていれば効かない
     * （サーバー側。クライアントでは常に真）。
     */
    public boolean isActiveAt(BlockPos pos) {
        return level().isClientSide || !EntityInterferenceZone.suppresses(level(), pos, ownerSide, placedInterference);
    }

    /** 置く前に呼ぶ。他 mod の機械を動かす速さ（{@link #machineSpeedFor}）。 */
    public void setMachineSpeed(float speed) {
        this.machineSpeed = Math.max(0.0F, speed);
    }

    public float getMachineSpeed() {
        return machineSpeed;
    }

    /**
     * 術者がブロック {@code pos} の機械（デポ・ベルト、Mekanism の機械）を使ってよいか（保護）。ブロックごとに一度だけ
     * 確かめて覚える。
     */
    protected boolean mayUseMachine(ServerLevel level, Player caster, BlockPos pos) {
        return mayUseCache.computeIfAbsent(pos.immutable(), key -> SpellBlockProtection.mayChange(level, caster, key));
    }

    /** 毎 tick 呼ぶ: 速さぶん進め、たまった分だけ基準の 1 tick ぶんの回数を返す（Create の加工を進める回数）。 */
    protected int takeMachineSteps() {
        machineProgress += machineSpeed;
        int steps = (int) Math.floor(machineProgress);
        machineProgress -= steps;
        return steps;
    }

    /** 術者の CAD の色（中心の核の魔法陣の色）。 */
    public int getColor() {
        return color;
    }

    /** 範囲の辺の色（0xRRGGBB）。種類ごとに決まっていて、何の領域か一目で分かる。 */
    public abstract int getOutlineColor();

    /** 範囲を囲む箱（ブロックの外側の面）。行列が軸にそっていれば範囲そのもの。 */
    public AABB getOutline() {
        return area.bounds();
    }

    /** 効き目を与える間隔（tick）。 */
    protected int effectInterval() {
        return 1;
    }

    /** この領域が {@code entity} に効くか。既定は生きている生き物だけ。 */
    protected boolean affects(Entity entity) {
        return entity instanceof LivingEntity living && living.isAlive();
    }

    /**
     * 術者自身には効かないか。害のある領域（加熱・凍結・乱流）は術者を巻き込まない（仮、2026-11-11）。湿潤・加重は術者にも
     * 効く。
     */
    protected boolean sparesCaster() {
        return false;
    }

    /** 範囲の中のエンティティ 1 体への効き目（サーバー側、{@link #affects} が真のものだけ）。 */
    protected abstract void affect(Entity target);

    /** ブロックへの効き目があるか。真なら {@link #affectBlock} を範囲の 1 段ずつ呼ぶ。 */
    protected boolean affectsBlocks() {
        return false;
    }

    /**
     * {@link #affectBlock} が何かするかもしれないブロックの状態か（走査で先に確かめる。区画の中身の一覧にこれが 1 つもなければ、
     * その区画は丸ごと飛ばす）。偽のブロックには affectBlock を呼ばないので、affectBlock が見る状態はすべて真にすること。
     */
    protected boolean mayAffectState(BlockState state) {
        return true;
    }

    /**
     * 範囲の中の 1 ブロックへの効き目（サーバー側、術者がプレイヤーのときだけ）。読み込まれているブロックだけ。
     * 保護（{@code SpellBlockProtection.mayChange}）は変えるときに確かめること。{@code pos} はブロックが本当にある位置
     * （機体のブロックなら機体用の空間の座標）。
     */
    protected void affectBlock(ServerLevel level, Player caster, BlockPos pos, BlockState state) {
    }

    /** この tick の範囲の見え方（{@link #regions}）。作った tick と一緒に覚える。 */
    private List<ZoneRegion> regionsCache;
    private long regionsTick = Long.MIN_VALUE;

    /**
     * この tick の範囲の見え方: 世界の範囲と、範囲に重なった機体（Sable のサブレベル）ごとにその機体の座標へ写した範囲
     * （2026-11-11 ユーザー: 機体の機械・ブロックにも効かせる）。機体は動くので tick ごとに作り直す。Sable がなければ世界の範囲
     * だけ。機械・ブロックへの効き目は、これを全部回して、範囲の判定などには {@link ZoneRegion#worldPos} を使う。
     */
    protected List<ZoneRegion> regions() {
        long now = level().getGameTime();
        if (regionsCache == null || regionsTick != now) {
            regionsCache = buildRegions();
            regionsTick = now;
        }
        return regionsCache;
    }

    private List<ZoneRegion> buildRegions() {
        List<ZoneRegion> regions = new java.util.ArrayList<>(1);
        regions.add(ZoneRegion.world(area));
        if (area.isEmpty()) {
            return regions;
        }
        for (double[] pose : com.moratan251.psitweaks.common.compat.SableRangeCompat
                .subLevelPosesIntersecting(level(), area.bounds())) {
            double[] toSubLevel = com.moratan251.psitweaks.common.compat.SubLevelZoneMath.invertAffine(pose);
            if (toSubLevel == null) {
                continue;
            }
            try {
                ZoneArea local = area.transformedBy(toSubLevel);
                if (!local.isEmpty()) {
                    regions.add(new ZoneRegion(local, pose));
                }
            } catch (vazkii.psi.api.spell.SpellRuntimeException ignored) {
                // 潰れた姿勢の機体は飛ばす。
            }
        }
        return regions;
    }

    /** 範囲の中の演出（クライアント側、毎 tick）。 */
    protected void spawnClientParticles() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && !isRemoved()) {
            spawnClientParticles();
        }
    }

    @Override
    protected void onSpellGramTickServer() {
        if (isInferno() && !isInfernoAt(level().getGameTime())) {
            entityData.set(DATA_INFERNO, false);
        }
        if (area.isEmpty()) {
            return;
        }
        if (tickCount % Math.max(1, effectInterval()) == 0) {
            for (Entity target : entitiesInside()) {
                affect(target);
            }
        }
        if (affectsBlocks() && !isEnded() && level() instanceof ServerLevel level && getCaster() instanceof Player caster) {
            for (ZoneRegion region : regions()) {
                scanLayer(level, caster, region, scanStep);
            }
            scanStep++;
        }
    }

    /**
     * 範囲 {@code region} の 1 段ぶんのブロックに {@link #affectBlock} を呼ぶ（{@code step} 回目の走査は、下から
     * {@code step} を段の数で割った余りの段）。16×16×16 の区画ごとに、空気だけの区画と、中身の一覧に
     * {@link #mayAffectState} が真のブロックが 1 つもない区画は丸ごと飛ばし、残りは区画から直接ブロックを読む。範囲・領域干渉の
     * 判定は、対象になりうるブロックにだけする（2026-11-11 の最適化。どのブロックをいつ見るかは前と同じ）。
     */
    private void scanLayer(ServerLevel level, Player caster, ZoneRegion region, int step) {
        ZoneArea scanned = region.area();
        if (scanned.isEmpty()) {
            return;
        }
        AABB box = scanned.bounds();
        int minY = (int) Math.floor(box.minY);
        int maxY = (int) Math.floor(box.maxY) - 1;
        int y = minY + Math.floorMod(step, Math.max(1, maxY - minY + 1));
        if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) {
            return;
        }
        int minX = (int) Math.floor(box.minX);
        int maxX = (int) Math.floor(box.maxX) - 1;
        int minZ = (int) Math.floor(box.minZ);
        int maxZ = (int) Math.floor(box.maxZ) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
                if (section.hasOnlyAir() || !section.maybeHas(this::mayAffectState)) {
                    continue;
                }
                int fromX = Math.max(minX, chunkX << 4);
                int toX = Math.min(maxX, (chunkX << 4) + 15);
                int fromZ = Math.max(minZ, chunkZ << 4);
                int toZ = Math.min(maxZ, (chunkZ << 4) + 15);
                for (int x = fromX; x <= toX; x++) {
                    for (int z = fromZ; z <= toZ; z++) {
                        BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                        if (!mayAffectState(state)) {
                            continue;
                        }
                        pos.set(x, y, z);
                        if (scanned.contains(pos) && isActiveAt(region.worldPos(pos))) {
                            affectBlock(level, caster, pos.immutable(), state);
                        }
                    }
                }
            }
        }
    }

    /** 範囲の中にいて、この領域が効くエンティティ（当たり判定の中心があるブロックで判定）。魔法式オブジェクトは除く。 */
    public List<Entity> entitiesInside() {
        return level().getEntities(this, area.bounds(), entity -> !(entity instanceof SpellGramObject)
                && affects(entity) && !(sparesCaster() && CasterIds.matches(entity, getCasterUuid()))
                && insideAndActive(BlockPos.containing(entity.getBoundingBox().getCenter())));
    }

    /** ブロック {@code pos} が範囲の中で、この領域が効いているか（{@link #isActiveAt}）。 */
    public boolean isInsideAndActive(BlockPos pos) {
        return insideAndActive(pos);
    }

    private boolean insideAndActive(BlockPos pos) {
        return area.contains(pos) && isActiveAt(pos);
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return area.isEmpty() ? super.getBoundingBoxForCulling() : area.bounds();
    }

    @Override
    protected void writeSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
        area.write(buffer);
        buffer.writeInt(color);
        writeZoneSpawnData(buffer);
    }

    @Override
    protected void readSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
        area = ZoneArea.read(buffer);
        color = buffer.readInt();
        readZoneSpawnData(buffer);
    }

    protected void writeZoneSpawnData(RegistryFriendlyByteBuf buffer) {
    }

    protected void readZoneSpawnData(RegistryFriendlyByteBuf buffer) {
    }
}
