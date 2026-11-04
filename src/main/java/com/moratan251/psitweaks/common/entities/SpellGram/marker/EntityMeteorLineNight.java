package com.moratan251.psitweaks.common.entities.SpellGram.marker;

import com.moratan251.psitweaks.common.entities.EntityMeteorLineBeam;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.MeteorLineTuning;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 作動式: 流星群の夜。中心に置く目印型の魔法式オブジェクトで、半径 R の半球の夜を広げて内側の上のほうに星を灯し、
 * 星から光条（{@link EntityMeteorLineBeam}）を降らせてから晴れる。流れと数は {@link MeteorLineTuning}。
 * <p>
 * 光条は斉射ごとに、夜の中にいる攻撃対象の生物（{@link AttackRules#canHarm}）それぞれへ最低1本を、その tick の体の中心へ
 * 向けて通し、残りを夜の中の地面へ降らせる。狙いと線の端点はサーバーで決め、光条のエンティティで送る。
 * 強度が尽きると夜がつぶれ、まだ降っていない光条も、残っている光条も消える。
 */
public class EntityMeteorLineNight extends SpellGramMarker {
    private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));
    /** 狙う相手ごとに、星をいくつ比べて一番まっすぐ降らせられるものを選ぶか。 */
    private static final int STAR_CANDIDATES = 3;
    /** 地面を探すとき、中心より上の坂を何ブロックまで登るか。 */
    private static final int MAX_CLIMB = 4;

    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(EntityMeteorLineNight.class, EntityDataSerializers.FLOAT);
    /** 星の並びの種。 */
    private static final EntityDataAccessor<Long> SEED =
            SynchedEntityData.defineId(EntityMeteorLineNight.class, EntityDataSerializers.LONG);
    /** 術者の CAD の色。星と光条にアクセントとして混ぜる。 */
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(EntityMeteorLineNight.class, EntityDataSerializers.INT);
    /** 置いたときのゲーム時刻。見た目は世界の時刻で進めるので、後から見えたクライアントも足並みがそろう。 */
    private static final EntityDataAccessor<Long> CAST_TIME =
            SynchedEntityData.defineId(EntityMeteorLineNight.class, EntityDataSerializers.LONG);

    /**
     * 星1つ。{@code offset} は中心からの位置、{@code phase}・{@code speed} は瞬き、{@code appear} は灯る時刻（tick）。
     */
    public record Star(Vec3 offset, float phase, float speed, float appear) {
    }

    private final List<EntityMeteorLineBeam> beams = new ArrayList<>();
    /** 狙った相手と、最後に狙った経過 tick。倒れなかった相手は、その光条が消えてから狙い直す。 */
    private final Map<UUID, Integer> aimed = new HashMap<>();
    private int age;
    private int fired;
    @Nullable
    private Star[] stars;
    private long starsSeed;
    private float starsRadius;

    public EntityMeteorLineNight(EntityType<? extends EntityMeteorLineNight> type, Level level) {
        super(type, level);
    }

    public EntityMeteorLineNight(Level level, Vec3 center, double radius) {
        this(PsitweaksEntities.METEOR_LINE_NIGHT.get(), level);
        setPos(center.x, center.y, center.z);
        entityData.set(RADIUS, (float) radius);
        entityData.set(SEED, level.getRandom().nextLong());
        entityData.set(CAST_TIME, level.getGameTime());
        setLifetimeTicks(MeteorLineTuning.TOTAL_TICKS);
    }

    /**
     * 置く前に呼ぶ。強度は {@link MeteorLineTuning#BASE_INTEGRITY} × 置いた時点の術者の事象干渉力、色は術者の CAD の色。
     */
    public void configure(LivingEntity caster, AttackRules.Options options) {
        configureMarker(caster, options, (float) (MeteorLineTuning.BASE_INTEGRITY * interferenceOf(caster)));
        entityData.set(COLOR, CombatSpellHelper.spellColor(caster) & 0xFFFFFF);
    }

    @Override
    protected void defineSpellGramSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, (float) MeteorLineTuning.DEFAULT_RADIUS);
        builder.define(SEED, 0L);
        builder.define(COLOR, CombatSpellHelper.DEFAULT_SPELL_COLOR);
        builder.define(CAST_TIME, 0L);
    }

    public double getRadius() {
        return entityData.get(RADIUS);
    }

    /** 術者の CAD の色（CAD がなければ Psi の既定の色）。 */
    public int getColor() {
        return entityData.get(COLOR);
    }

    /** 置いてからの tick（tick の間はなめらかに）。 */
    public float getAge(float partialTick) {
        return (float) (level().getGameTime() - entityData.get(CAST_TIME)) + partialTick;
    }

    /** 夜の濃さ。広がる間に 0 から 1 へ、晴れる間に 1 から 0 へ。 */
    public float getStrength(float partialTick) {
        float age = getAge(partialTick);
        float spread = Mth.clamp(age / MeteorLineTuning.SPREAD_TICKS, 0.0F, 1.0F);
        float clear = Mth.clamp((age - MeteorLineTuning.CLEAR_START) / MeteorLineTuning.CLEAR_TICKS, 0.0F, 1.0F);
        return spread * (1.0F - clear);
    }

    /** 今の半球の半径。広がる間は小さく、はじめ速く後でゆっくり広がる。 */
    public double getCurrentRadius(float partialTick) {
        float spread = Mth.clamp(getAge(partialTick) / MeteorLineTuning.SPREAD_TICKS, 0.0F, 1.0F);
        float rest = 1.0F - spread;
        return getRadius() * (1.0F - rest * rest * rest);
    }

    /** 点が夜の中か。中心からの距離が半径以内で、中心より下は {@link MeteorLineTuning#BELOW_CENTER} まで。 */
    public boolean contains(Vec3 point) {
        return contains(point, getRadius());
    }

    public boolean contains(Vec3 point, double radius) {
        Vec3 center = position();
        return point.y >= center.y - MeteorLineTuning.BELOW_CENTER && point.distanceToSqr(center) <= radius * radius;
    }

    /** 星の並び。種と半径だけで決まるので、サーバーとクライアントで同じになる。 */
    public Star[] getStars() {
        long seed = entityData.get(SEED);
        float radius = entityData.get(RADIUS);
        if (stars == null || starsSeed != seed || starsRadius != radius) {
            stars = makeStars(seed, radius);
            starsSeed = seed;
            starsRadius = radius;
        }
        return stars;
    }

    public Vec3 starPosition(Star star) {
        return position().add(star.offset());
    }

    private static Star[] makeStars(long seed, double radius) {
        RandomSource random = RandomSource.create(seed);
        int count = MeteorLineTuning.stars(radius);
        double minSin = Math.sin(Math.toRadians(MeteorLineTuning.STAR_MIN_ELEVATION));
        double maxSin = Math.sin(Math.toRadians(MeteorLineTuning.STAR_MAX_ELEVATION));
        double azimuth = random.nextDouble() * Mth.TWO_PI;
        Star[] result = new Star[count];
        for (int i = 0; i < count; i++) {
            // 仰角の正弦を等分すると、星は半球の面に均等に散る。方位は黄金角ずつ回して重ならないようにする。
            double sin = Mth.lerp((i + random.nextDouble()) / count, minSin, maxSin);
            double cos = Math.sqrt(1.0 - sin * sin);
            azimuth += GOLDEN_ANGLE;
            double distance = radius * MeteorLineTuning.STAR_DISTANCE * (0.94 + 0.06 * random.nextDouble());
            Vec3 offset = new Vec3(Math.cos(azimuth) * cos * distance, sin * distance, Math.sin(azimuth) * cos * distance);
            result[i] = new Star(offset, random.nextFloat() * Mth.TWO_PI, 0.15F + random.nextFloat() * 0.25F,
                    random.nextFloat() * MeteorLineTuning.SPREAD_TICKS * 0.75F);
        }
        return result;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return new AABB(position(), position()).inflate(getRadius() * (1.0 + MeteorLineTuning.STAR_SCATTER) + 1.0);
    }

    // ---- サーバー ----

    @Override
    protected void onSpellGramTickServer() {
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity caster = getCaster();
        if (caster == null) {
            // ダメージの帰属に術者が要るので、術者がいなくなったら止める（目印型としての扱いは未定）。
            end(EndReason.CASTER_GONE);
            return;
        }
        if (age == 0) {
            CombatEffects.sound(level, position(), MeteorLineTuning.SPREAD_SOUND, 1.5F, MeteorLineTuning.SPREAD_PITCH);
        }
        int sinceFall = age - MeteorLineTuning.FALL_START;
        if (sinceFall >= 0 && age < MeteorLineTuning.CLEAR_START && sinceFall % MeteorLineTuning.VOLLEY_INTERVAL == 0) {
            volley(level, caster, sinceFall / MeteorLineTuning.VOLLEY_INTERVAL);
        }
        if (age == MeteorLineTuning.CLEAR_START) {
            CombatEffects.sound(level, position(), MeteorLineTuning.CLEAR_SOUND, 1.2F, MeteorLineTuning.CLEAR_PITCH);
        }
        age++;
    }

    /**
     * 斉射1回。狙う相手それぞれに1本（多くて {@link MeteorLineTuning#AIMED_PER_VOLLEY} 本。まだ狙っていない相手から）、
     * 残りは予定の本数まで地面へ。音は斉射ごとに数回だけ鳴らす。
     */
    private void volley(ServerLevel level, LivingEntity caster, int volley) {
        Star[] stars = getStars();
        if (stars.length == 0) {
            return;
        }
        List<Vec3> landed = new ArrayList<>();
        List<LivingEntity> targets = new ArrayList<>(targets(caster));
        // 大勢いても全員に一度は降るよう、狙い直しは後に回す。
        targets.sort(Comparator.comparing(living -> aimed.containsKey(living.getUUID())));
        int aimedNow = 0;
        for (LivingEntity target : targets) {
            if (fired >= MeteorLineTuning.MAX_BEAMS || aimedNow >= MeteorLineTuning.AIMED_PER_VOLLEY) {
                break;
            }
            aimedNow++;
            Vec3 through = CombatSpellHelper.center(target);
            fire(level, caster, starFor(stars, through), through);
            landed.add(through);
            aimed.put(target.getUUID(), age);
        }
        int due = MeteorLineTuning.baseBeams(getRadius()) * (volley + 1) / MeteorLineTuning.volleys();
        while (fired < due && fired < MeteorLineTuning.MAX_BEAMS) {
            Vec3 through = groundPoint(level);
            fire(level, caster, starPosition(stars[random.nextInt(stars.length)]), through);
            landed.add(through);
        }
        for (int i = 0; i < Math.min(MeteorLineTuning.BEAM_SOUNDS_PER_VOLLEY, landed.size()); i++) {
            float pitch = Mth.lerp(random.nextFloat(), MeteorLineTuning.BEAM_MIN_PITCH, MeteorLineTuning.BEAM_MAX_PITCH);
            CombatEffects.sound(level, landed.get(i), MeteorLineTuning.BEAM_SOUND, MeteorLineTuning.BEAM_VOLUME, pitch);
        }
    }

    /** 夜の中にいて、まだ狙っていない（狙った光条が消えても倒れていない）攻撃対象。 */
    private List<LivingEntity> targets(LivingEntity caster) {
        AABB box = new AABB(position(), position()).inflate(getRadius());
        return level().getEntitiesOfClass(LivingEntity.class, box, living -> canDamageEntity(caster, living)
                && contains(CombatSpellHelper.center(living)) && !recentlyAimed(living));
    }

    private boolean recentlyAimed(LivingEntity living) {
        Integer last = aimed.get(living.getUUID());
        return last != null && age - last < MeteorLineTuning.BEAM_TICKS;
    }

    /** 星をいくつか比べ、狙う点へ一番まっすぐ降らせられるものを選ぶ。 */
    private Vec3 starFor(Star[] stars, Vec3 target) {
        Vec3 best = null;
        double steepest = Double.MAX_VALUE;
        for (int i = 0; i < STAR_CANDIDATES; i++) {
            Vec3 star = starPosition(stars[random.nextInt(stars.length)]);
            Vec3 down = target.subtract(star);
            double slope = down.lengthSqr() < 1.0E-6 ? -1.0 : down.normalize().y;
            if (best == null || slope < steepest) {
                best = star;
                steepest = slope;
            }
        }
        return best;
    }

    /** 夜の中の地面のどこか（演出用の光条が落ちる所）。 */
    private Vec3 groundPoint(ServerLevel level) {
        double angle = random.nextDouble() * Mth.TWO_PI;
        double distance = Math.sqrt(random.nextDouble()) * getRadius() * MeteorLineTuning.GROUND_SPREAD;
        double x = getX() + Math.cos(angle) * distance;
        double z = getZ() + Math.sin(angle) * distance;
        return new Vec3(x, groundY(level, x, getY(), z), z);
    }

    /**
     * その列で中心の高さに近い地面。中心より上の坂は {@link #MAX_CLIMB} ブロックまで登り、ほかは下を探すので、
     * 中心の上に屋根があっても屋根の下の床に落ちる。
     */
    private double groundY(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(x, y + 0.5, z).mutable();
        for (int i = 0; i < MAX_CLIMB && !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty(); i++) {
            pos.move(Direction.UP);
        }
        Double ground = CombatSpellHelper.findGroundY(level, x, pos.getY() + 0.5, z, Mth.ceil(getRadius()));
        return ground != null ? ground : y;
    }

    /** 星から {@code through} を通り、夜の球を出るまでの光条を1本降らせる。 */
    private void fire(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 through) {
        Vec3 direction = through.subtract(start);
        direction = direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, -1.0, 0.0) : direction.normalize();
        Vec3 end = start.add(direction.scale(exitDistance(start, direction)));
        EntityMeteorLineBeam beam = new EntityMeteorLineBeam(level, start, end);
        beam.configure(caster, attackOptions(), MeteorLineTuning.DAMAGE, getColor());
        level.addFreshEntity(beam);
        beams.add(beam);
        fired++;
    }

    /** {@code start}（球の中）から {@code direction}（長さ1）へ進んで、夜の球（半径 R）を出るまでの距離。 */
    private double exitDistance(Vec3 start, Vec3 direction) {
        Vec3 relative = start.subtract(position());
        double radius = getRadius();
        double b = relative.dot(direction);
        double discriminant = b * b - (relative.lengthSqr() - radius * radius);
        return discriminant <= 0.0 ? 0.0 : -b + Math.sqrt(discriminant);
    }

    /** 降らせた光条のうち、まだ残っているもの。 */
    public List<EntityMeteorLineBeam> getBeams() {
        return beams.stream().filter(beam -> !beam.isRemoved()).toList();
    }

    public int getFiredCount() {
        return fired;
    }

    @Override
    protected void onEnd(EndReason reason) {
        if (reason != EndReason.FINISHED) {
            // 夜がつぶれたら残っている光条も消え、まだ降っていない分はもう降らない。
            for (EntityMeteorLineBeam beam : beams) {
                if (!beam.isRemoved()) {
                    beam.discard();
                }
            }
        }
        beams.clear();
        if (reason == EndReason.DESTROYED && level() instanceof ServerLevel level) {
            CombatEffects.sound(level, position(), MeteorLineTuning.COLLAPSE_SOUND, 1.2F, 0.6F);
            double spread = getRadius() * 0.4;
            level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + getRadius() * 0.5, getZ(), 40, spread, spread * 0.5,
                    spread, 0.05);
        }
    }
}
