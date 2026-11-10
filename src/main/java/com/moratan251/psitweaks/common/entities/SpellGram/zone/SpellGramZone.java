package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 領域型の魔法式オブジェクト（○○ Zone）の基底。範囲は {@link ZoneArea}（領域行列を、その中心のブロックから ±大きさの
 * 立方体で切り詰めたブロックの集まり）。生物は当たり判定の中心があるブロックで判定する。
 * <p>
 * 範囲の中の生物への効き目は {@link #affect}（{@link #effectInterval} tick ごと）。描画は範囲を囲む箱
 * （{@link #getOutline}）と、種類ごとの中の演出（{@link #spawnClientParticles}）。
 */
public abstract class SpellGramZone extends SpellGramObject {
    private ZoneArea area = ZoneArea.EMPTY;
    private int color = 0xFFFFFF;
    /** 描画だけが使う: 床の魔法陣に前のフレームで選んだ画像の段（-1 はまだない）。同期も保存もしない。 */
    private int floorCircleLevel = -1;

    protected SpellGramZone(EntityType<? extends SpellGramZone> type, Level level) {
        super(type, level);
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

    /** 範囲の中の生物 1 体への効き目（サーバー側）。 */
    protected abstract void affect(LivingEntity target);

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
        if (area.isEmpty() || tickCount % Math.max(1, effectInterval()) != 0) {
            return;
        }
        for (LivingEntity target : livingInside()) {
            affect(target);
        }
    }

    /** 範囲の中にいる生物（当たり判定の中心があるブロックで判定）。 */
    public List<LivingEntity> livingInside() {
        return level().getEntitiesOfClass(LivingEntity.class, area.bounds(),
                entity -> entity.isAlive() && area.contains(BlockPos.containing(entity.getBoundingBox().getCenter())));
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
