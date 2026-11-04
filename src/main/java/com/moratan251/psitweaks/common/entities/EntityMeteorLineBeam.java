package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.entities.combat.CombatSpellEntity;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.MeteorLineTuning;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * 作動式: 流星群の光条1本。星（エンティティの位置）から狙う点を通り、夜の球を出るまでの直線で、ブロックは素通りして壊さない。
 * 流星の頭が走るのは見た目だけで、当たりは線ができた tick から線の全体に {@link MeteorLineTuning#BEAM_TICKS} tick 残り、
 * その間に線に触れた相手に一度ずつ当たる。そのあとは、地面や壁に入った点の印のためだけに
 * {@link MeteorLineTuning#MARK_TICKS} tick まで残る。
 */
public class EntityMeteorLineBeam extends CombatSpellEntity {
    /** 穴の印を探すときに線をたどる刻み（ブロック）。 */
    private static final double MARK_STEP = 0.25;

    // 終点は始点（エンティティの位置。正確に同期される）からの相対で送る。絶対座標の float はワールドの原点から遠いとずれる。
    // 当たりはサーバーが持つ double の終点で判定する。
    private static final EntityDataAccessor<Float> END_X = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Y = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> END_Z = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.FLOAT);
    /** 降らせたときのゲーム時刻。見た目は世界の時刻で進める。 */
    private static final EntityDataAccessor<Long> CAST_TIME = SynchedEntityData.defineId(EntityMeteorLineBeam.class, EntityDataSerializers.LONG);

    @Nullable
    private Vec3 end;
    /**
     * 当てた相手。同じ光条は同じ相手に一度だけ当たる。ダメージは無敵時間を無視するので、毎 tick 当てると倒れない相手
     * （ターゲットダミーなど）が大勢いるとき被ダメージ音があふれる。
     */
    private final Set<UUID> struck = new HashSet<>();
    /** クライアントで一度だけ求める、線が地面や壁に入る点。 */
    @Nullable
    private List<Vec3> marks;

    public EntityMeteorLineBeam(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityMeteorLineBeam(Level level, Vec3 start, Vec3 end) {
        this(PsitweaksEntities.METEOR_LINE_BEAM.get(), level);
        setPos(start.x, start.y, start.z);
        this.end = end;
        entityData.set(END_X, (float) (end.x - start.x));
        entityData.set(END_Y, (float) (end.y - start.y));
        entityData.set(END_Z, (float) (end.z - start.z));
        entityData.set(CAST_TIME, level.getGameTime());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(END_X, 0.0F);
        builder.define(END_Y, 0.0F);
        builder.define(END_Z, 0.0F);
        builder.define(CAST_TIME, 0L);
    }

    /** 星の位置。エンティティはここにとどまる。 */
    public Vec3 getStartPos() {
        return position();
    }

    public Vec3 getEndPos() {
        if (end != null) {
            return end;
        }
        return position().add(entityData.get(END_X), entityData.get(END_Y), entityData.get(END_Z));
    }

    /** 降らせてからの tick（tick の間はなめらかに）。 */
    public float getAge(float partialTick) {
        return (float) (level().getGameTime() - entityData.get(CAST_TIME)) + partialTick;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return new AABB(getStartPos(), getEndPos()).inflate(1.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity caster = getCaster();
        if (caster == null) {
            discard();
            return;
        }
        if (tickCount <= MeteorLineTuning.BEAM_TICKS) {
            hitAlongPath(level, caster);
        }
        if (tickCount >= MeteorLineTuning.MARK_TICKS) {
            discard();
        }
    }

    private void hitAlongPath(ServerLevel level, LivingEntity caster) {
        Vec3 start = getStartPos();
        Vec3 finish = getEndPos();
        AABB bounds = new AABB(start, finish).inflate(1.0);
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.METEOR_LINE, this, caster);
        // 攻撃術式で共通の命中判定（術者側・防具立て・プレイヤーに安全・受ける側の PvP）。
        List<Entity> entities = level.getEntities(this, bounds, entity -> !struck.contains(entity.getUUID())
                && canAffect(caster, entity)
                && entity.getBoundingBox().inflate(MeteorLineTuning.HIT_RADIUS).clip(start, finish).isPresent());
        for (Entity entity : entities) {
            struck.add(entity.getUUID());
            // 光は体力の多さに関係なく通り抜ける: 当てる相手（canAffect 済み）の体力を直前に 1 にしてから、攻撃として与える。
            // ダメージそのものを打ち消す相手（無敵の段階など）は体力 1 で残る。
            if (entity instanceof LivingEntity living && living.getHealth() > 1.0F) {
                living.setHealth(1.0F);
            }
            harm(caster, entity, source, getDamage());
        }
    }

    /** 線が地面や壁に入る点（多くて {@link MeteorLineTuning#MAX_MARKS_PER_BEAM} 個）。ブロックの中から外へ出る点は数えない。 */
    public List<Vec3> getMarks() {
        if (marks == null) {
            marks = findMarks(level(), getStartPos(), getEndPos());
        }
        return marks;
    }

    private static List<Vec3> findMarks(Level level, Vec3 start, Vec3 end) {
        List<Vec3> found = new ArrayList<>();
        Vec3 line = end.subtract(start);
        double length = line.length();
        if (length < 1.0E-4) {
            return found;
        }
        int steps = Mth.ceil(length / MARK_STEP);
        Vec3 previous = start;
        boolean inside = isSolidAt(level, start);
        for (int i = 1; i <= steps && found.size() < MeteorLineTuning.MAX_MARKS_PER_BEAM; i++) {
            Vec3 point = i == steps ? end : start.add(line.scale((double) i / steps));
            boolean solid = isSolidAt(level, point);
            if (solid && !inside) {
                BlockHitResult hit = level.clip(new ClipContext(previous, point, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, CollisionContext.empty()));
                found.add(hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : point);
            }
            inside = solid;
            previous = point;
        }
        return found;
    }

    private static boolean isSolidAt(Level level, Vec3 point) {
        BlockPos pos = BlockPos.containing(point);
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return false;
        }
        Vec3 local = point.subtract(pos.getX(), pos.getY(), pos.getZ());
        for (AABB box : shape.toAabbs()) {
            if (box.contains(local)) {
                return true;
            }
        }
        return false;
    }
}
