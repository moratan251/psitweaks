package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.SpellBlockProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 作動式: 加速領域（Acceleration Zone）の範囲。範囲の中の育つもの（タグ {@link #ACCELERATABLE}）のランダムティックを速める。
 * <ul>
 *   <li>毎 tick、範囲の体積 × randomTickSpeed ÷ 4096 × 中の威力 個（端数は確率で）のブロックを選び、タグに入っていて
 *   ランダムティックする状態なら {@link BlockState#randomTick} を呼ぶ（{@link #picksPerTick}）。バニラのランダムティックと
 *   合わせて (1 + 中の威力) 倍になる。randomTickSpeed が 0 なら何もしない。</li>
 *   <li>同じ術者のほかの領域のランダムなブロックの変化も、重なったところでは同じ倍率で速くなる
 *   （{@link SpellGramZone#acceleratedByOwnZones}）。</li>
 *   <li>ブロックエンティティと生き物には効かない。</li>
 * </ul>
 * 同じ種類が重なったところでは、威力の一番高いものだけが効く。
 */
public class EntityAccelerationZone extends SpellGramZone {
    /** 辺の色: 若葉の緑。 */
    public static final int OUTLINE_COLOR = 0x6BFF8E;
    /** 速めるブロック（許可リスト）。 */
    public static final TagKey<Block> ACCELERATABLE = TagKey.create(Registries.BLOCK, Psitweaks.location("zone_acceleratable"));
    /** 威力の既定値（置く前に設定しなかったとき、中の威力）。 */
    public static final float DEFAULT_POWER = 5.0F;
    /** バニラのランダムティックで、1 つの区画から 1 tick に randomTickSpeed 個を選ぶときの区画のブロック数（16×16×16）。 */
    public static final double SECTION_VOLUME = 4096.0;
    private static final int MAX_SPARKLES_PER_TICK = 4;

    /** 中の威力（作動式に入れた威力 + 4、5〜14）。 */
    private float power = DEFAULT_POWER;

    public EntityAccelerationZone(EntityType<? extends EntityAccelerationZone> type, Level level) {
        super(type, level);
    }

    public EntityAccelerationZone(Level level) {
        this(PsitweaksEntities.ACCELERATION_ZONE.get(), level);
    }

    /** 置く前に呼ぶ。{@code power} は中の威力（作動式に入れた威力 + 4）。 */
    public void setPower(float power) {
        this.power = Math.max(0.0F, Math.min(power, MAX_EFFECT_POWER));
        SpellGramIndex.touch();
    }

    public float getPower() {
        return power;
    }

    /** ランダムティックの速さの倍率: バニラの分と合わせて 1 + 中の威力。 */
    public float multiplier() {
        return 1.0F + power;
    }

    /** 体積 {@code volume} の範囲で、1 tick に選ぶブロックの数の見込み: 体積 × randomTickSpeed ÷ 4096 × 中の威力。 */
    public static double picksPerTick(int volume, int randomTickSpeed, float power) {
        return Math.max(0, volume) * Math.max(0, randomTickSpeed) / SECTION_VOLUME * Math.max(0.0F, power);
    }

    /** ゲームルール randomTickSpeed と中の威力から、ブロック 1 つが 1 tick に選ばれる見込み。 */
    @Override
    protected double randomBlockTickChance(ServerLevel level) {
        return picksPerTick(1, level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING), power);
    }

    @Override
    protected boolean acceleratedByOwnZones() {
        return false;
    }

    @Override
    protected boolean hasRandomBlockTicks() {
        return true;
    }

    @Override
    protected boolean mayRandomTick(BlockState state) {
        return state.is(ACCELERATABLE) && state.isRandomlyTicking();
    }

    /** 選ばれたブロックにランダムティックを 1 回与える。同じ種類が重なったところは一番強いものだけ。 */
    @Override
    protected void randomBlockTick(ServerLevel level, Player caster, BlockPos pos, BlockPos world, BlockState state) {
        if (!isStrongestAt(world) || !SpellBlockProtection.mayChange(level, caster, pos)) {
            return;
        }
        state.randomTick(level, pos, level.random);
    }

    /** 同じ種類が重なったところでは、威力の一番高いものだけが効く。 */
    @Override
    protected double stackStrength() {
        return power;
    }

    /** 生き物には効かない。 */
    @Override
    protected boolean affects(Entity entity) {
        return false;
    }

    @Override
    protected void affect(Entity target) {
    }

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
    }

    /** 照準の表示: 加速の倍率。 */
    @Override
    public List<Component> hudLines() {
        return List.of(Component.translatable("psitweaks.hud.zone_acceleration", hudNumber(multiplier())));
    }

    /**
     * 領域 {@code zone} と範囲の箱が重なる、同じ術者の加速領域（サーバー側、終わったものと {@code zone} 自身は除く）。
     * {@link SpellGramZone} のランダムなブロックの変化を速めるのに使う。
     */
    public static List<EntityAccelerationZone> boostersOf(SpellGramZone zone) {
        UUID caster = zone.getCasterUuid();
        if (caster == null) {
            return List.of();
        }
        List<EntityAccelerationZone> boosters = new ArrayList<>(0);
        for (SpellGramObject object : SpellGramIndex.ofCaster(zone.level(), caster)) {
            if (object != zone && object instanceof EntityAccelerationZone booster && !booster.isEnded()
                    && booster.getArea().boxOverlaps(zone.getArea())) {
                boosters.add(booster);
            }
        }
        return boosters;
    }

    /** 加速領域 {@code boosters} のうち、世界の位置 {@code world} で効いているものの一番大きい倍率（なければ 1）。 */
    public static double multiplierAt(List<EntityAccelerationZone> boosters, BlockPos world) {
        double best = 1.0;
        for (EntityAccelerationZone booster : boosters) {
            if (!booster.isEnded() && booster.isInsideAndActive(world)) {
                best = Math.max(best, booster.multiplier());
            }
        }
        return best;
    }

    @Override
    protected void writeZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(power);
    }

    @Override
    protected void readZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        power = buffer.readFloat();
    }

    /** 骨粉を撒いたときと同じ緑のきらめきを、範囲の中にまばらに出す。 */
    @Override
    protected void spawnClientParticles() {
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int sparkles = particleCount(Math.min(MAX_SPARKLES_PER_TICK, Math.max(1.0, volume / 300.0)));
        for (int i = 0; i < sparkles; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
