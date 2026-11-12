package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.core.BlockPos;
import com.moratan251.psitweaks.common.particles.PsitweaksParticles;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 作動式: 乱流領域（仮）の範囲。中に暴風が吹き荒れる（2026-11-11 ユーザー: 運ぶのは加重領域の役目なので、乱流は「乱して散らす」
 * ものと、ほかの領域を強める触媒にする）。
 * <ul>
 * <li>他者の飛び道具（矢・トライデント・雪玉・火の玉など）は、入ってきたときに向きをランダムに曲げられる（威力で大きく）。
 * 魔法の弾（Psi・Ars Nouveau・Iron's Spells 'n Spellbooks）はそらさない。</li>
 * <li>残留ポーションやドラゴンブレスの雲を吹き散らす（早く縮んで消える）。</li>
 * <li>Mekanism Generators の風力発電機は、範囲の中なら屋内でも動き、出力が 1 + 威力 × 0.1 倍になる
 * （{@code WindGeneratorTurbulenceMixin}）。</li>
 * <li>同じ術者のほかの領域と重なると、重なったブロックで強める: 加熱領域は熱乱流、凍結領域は吹雪、湿潤領域は一括洗浄が 2 倍
 * （{@link #blowsFor}、それぞれの領域が見る）。</li>
 * </ul>
 */
public class EntityTurbulenceZone extends SpellGramZone {
    /** 威力 1 あたりの、飛び道具を曲げる角度（度、仮）。威力 10 で最大 90 度。 */
    public static final double DEFLECT_DEGREES_PER_POWER = 9.0;
    /** 威力 1 あたりの、風力発電機の出力の上乗せ（仮）。威力 10 で 2 倍。 */
    public static final double WIND_BOOST_PER_POWER = 0.1;
    /** 雲が 1 tick に縮む半径（仮）。 */
    private static final float CLOUD_SHRINK_PER_TICK = 0.15F;
    /** そらさない魔法の弾の mod。 */
    private static final Set<String> SPELL_PROJECTILE_MODS = Set.of("psi", "psitweaks", "ars_nouveau", "irons_spellbooks");

    /** 辺の色: 風の薄緑。 */
    public static final int OUTLINE_COLOR = 0x9CF2C4;

    private float power;
    /** もう曲げた飛び道具（1 つにつき 1 回だけ曲げる）。サーバー側だけ。 */
    private final Set<UUID> deflected = new HashSet<>();

    public EntityTurbulenceZone(EntityType<? extends EntityTurbulenceZone> type, Level level) {
        super(type, level);
    }

    public EntityTurbulenceZone(Level level) {
        this(PsitweaksEntities.TURBULENCE_ZONE.get(), level);
    }

    /** 置く前に呼ぶ。 */
    public void configureWind(float power) {
        this.power = Math.max(0.0F, power);
    }

    public float getPower() {
        return power;
    }

    /** 威力 {@code power} の乱流が、風力発電機の出力を何倍にするか。 */
    public static double windBoostFor(double power) {
        return 1.0 + Math.max(0.0, power) * WIND_BOOST_PER_POWER;
    }

    /**
     * {@code level} のブロック {@code pos} に吹いている乱流で、風力発電機の出力を何倍にするか（サーバー側、術者は問わない）。
     * 吹いていなければ 0。重なっていればいちばん強いもの。
     */
    public static double windBoostAt(Level level, BlockPos pos) {
        double boost = 0.0;
        for (EntityTurbulenceZone zone : SpellGramIndex.all(level, EntityTurbulenceZone.class)) {
            if (!zone.isEnded() && zone.isInsideAndActive(pos)) {
                boost = Math.max(boost, windBoostFor(zone.getPower()));
            }
        }
        return boost;
    }

    /** {@code level} のブロック {@code pos} に乱流領域の風が吹いているか（サーバー側、術者は問わない）。 */
    public static boolean blowsAt(Level level, BlockPos pos) {
        return windBoostAt(level, pos) > 0.0;
    }

    /**
     * 術者 {@code casterId} の乱流領域の風が、ブロック {@code pos} に吹いているか（サーバー側）。重ねたときの強化
     * （熱乱流・吹雪・一括洗浄 2 倍）は、同じ術者の領域どうしだけ（2026-11-11 ユーザー）。
     */
    public static boolean blowsFor(Level level, BlockPos pos, @Nullable UUID casterId) {
        for (SpellGramObject object : SpellGramIndex.ofCaster(level, casterId)) {
            if (object instanceof EntityTurbulenceZone zone && !zone.isEnded() && zone.isInsideAndActive(pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
    }

    /** 中のエンティティを運ばない。効くのは飛び道具と雲だけ（{@link #onSpellGramTickServer}）。 */
    @Override
    protected boolean affects(Entity entity) {
        return false;
    }

    @Override
    protected void affect(Entity target) {
    }

    @Override
    protected void onSpellGramTickServer() {
        super.onSpellGramTickServer();
        if (isEnded() || getArea().isEmpty() || !(level() instanceof ServerLevel level)) {
            return;
        }
        List<Entity> inside = level.getEntities(this, getArea().bounds(),
                entity -> (entity instanceof Projectile || entity instanceof AreaEffectCloud) && entity.isAlive()
                        && isInsideAndActive(BlockPos.containing(entity.getBoundingBox().getCenter())));
        for (Entity entity : inside) {
            if (entity instanceof Projectile projectile) {
                deflect(level, projectile);
            } else if (entity instanceof AreaEffectCloud cloud && cloud.getType() == EntityType.AREA_EFFECT_CLOUD) {
                disperse(cloud);
            }
        }
    }

    /** 他者の飛び道具の向きを、入ってきたときに 1 回だけランダムに曲げる（速さは変えない）。 */
    private void deflect(ServerLevel level, Projectile projectile) {
        if (isSpellProjectile(projectile) || CasterIds.matches(projectile.getOwner(), getCasterUuid())
                || !deflected.add(projectile.getUUID())) {
            return;
        }
        Vec3 velocity = projectile.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 1.0E-3) {
            return;
        }
        double maxAngle = Math.toRadians(Math.min(90.0, power * DEFLECT_DEGREES_PER_POWER));
        double angle = maxAngle * (0.5 + 0.5 * random.nextDouble());
        // 進む向きに垂直なランダムな向きへ、angle だけ倒す。
        Vec3 forward = velocity.scale(1.0 / speed);
        Vec3 side = forward.cross(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()));
        if (side.lengthSqr() < 1.0E-6) {
            return;
        }
        side = side.normalize();
        Vec3 turned = forward.scale(Math.cos(angle)).add(side.scale(Math.sin(angle))).scale(speed);
        projectile.setDeltaMovement(turned);
        projectile.hasImpulse = true;
        float yaw = (float) (Mth.atan2(turned.x, turned.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(turned.y, turned.horizontalDistance()) * Mth.RAD_TO_DEG);
        projectile.setYRot(yaw);
        projectile.setXRot(pitch);
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(), 4,
                0.1, 0.1, 0.1, 0.02);
    }

    private static boolean isSpellProjectile(Projectile projectile) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType());
        return SPELL_PROJECTILE_MODS.contains(id.getNamespace());
    }

    /** 残留ポーション・ドラゴンブレスの雲を早く縮める。 */
    private static void disperse(AreaEffectCloud cloud) {
        float radius = cloud.getRadius() - CLOUD_SHRINK_PER_TICK;
        if (radius <= 0.5F) {
            cloud.discard();
        } else {
            cloud.setRadius(radius);
        }
    }

    @Override
    protected void onEnd(EndReason reason) {
        super.onEnd(reason);
        deflected.clear();
    }

    /** 竜巻の煙の色: 熱乱流（オレンジ）・乱流＋湿潤（青）・吹雪（水色）・雷雲（暗い紫がかった灰色）。 */
    private static final float[] HEAT_SMOKE = {1.0F, 0.55F, 0.16F};
    private static final float[] SOAK_SMOKE = {0.25F, 0.5F, 1.0F};
    private static final float[] FREEZE_SMOKE = {0.62F, 0.92F, 1.0F};
    private static final float[] CHARGE_SMOKE = {0.42F, 0.4F, 0.56F};

    @Override
    protected void spawnClientParticles() {
        if (power <= 0.0F) {
            return;
        }
        // 竜巻: 下が細く上が広い漏斗の形で、威力が大きいほど速く回る。
        AABB box = getOutline();
        double swirl = 0.25 + 0.035 * power;
        int count = ZoneParticles.count(box, 40.0, 4, 14);
        // 同じ術者のほかの領域と重なっているとき（熱乱流・乱流＋湿潤・吹雪）: 重なっている体積の割合だけ、煙をその色にする。
        double heat = stormShare(box, EntityHeatZone.class);
        double soak = stormShare(box, EntitySoakZone.class);
        double freeze = stormShare(box, EntityFreezeZone.class);
        double charge = stormShare(box, EntityChargedZone.class);
        double total = heat + soak + freeze + charge;
        if (total > 1.0) {
            heat /= total;
            soak /= total;
            freeze /= total;
            charge /= total;
        }
        int heatCount = (int) Math.round(count * heat);
        int soakCount = (int) Math.round(count * soak);
        int freezeCount = (int) Math.round(count * freeze);
        int chargeCount = (int) Math.round(count * charge);
        int white = Math.max(0, count - heatCount - soakCount - freezeCount - chargeCount);
        ZoneParticles.vortex(level(), random, box, white, ParticleTypes.CLOUD, swirl, 0.06);
        tintedVortex(box, heatCount, HEAT_SMOKE, swirl, 0.08);
        tintedVortex(box, soakCount, SOAK_SMOKE, swirl, 0.05);
        tintedVortex(box, freezeCount, FREEZE_SMOKE, swirl, 0.04);
        tintedVortex(box, chargeCount, CHARGE_SMOKE, swirl, 0.03);
        if (random.nextInt(3) == 0) {
            ZoneParticles.vortex(level(), random, box, 2, ParticleTypes.WHITE_SMOKE, swirl * 1.2, 0.04);
        }
    }

    private void tintedVortex(AABB box, int count, float[] color, double swirl, double rise) {
        if (count > 0) {
            ZoneParticles.vortex(level(), random, box, count,
                    ColorParticleOption.create(PsitweaksParticles.TINTED_CLOUD.get(), color[0], color[1], color[2]), swirl, rise);
        }
    }

    /**
     * クライアント側の演出用: 範囲 {@code box} のうち、乱流と重なって強まっている（{@link SpellGramZone#isStorm}）
     * {@code type} の領域と重なっている体積の割合（0〜1）。
     */
    private double stormShare(AABB box, Class<? extends SpellGramZone> type) {
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        if (volume <= 0.0) {
            return 0.0;
        }
        double overlapped = 0.0;
        for (SpellGramZone zone : level().getEntitiesOfClass(type, box.inflate(1.0))) {
            if (zone.isStorm()) {
                AABB overlap = ZoneAttacks.overlap(box, zone.getOutline());
                if (overlap != null) {
                    overlapped += overlap.getXsize() * overlap.getYsize() * overlap.getZsize();
                }
            }
        }
        return Math.min(1.0, overlapped / volume);
    }

    @Override
    protected void writeZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(power);
    }

    @Override
    protected void readZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        power = buffer.readFloat();
    }
}
