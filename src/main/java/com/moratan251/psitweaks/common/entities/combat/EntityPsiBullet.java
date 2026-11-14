package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramAim;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 作動式: サイ・ブリット の弾（2026-11-14 ユーザー）。エア・ブリットと同じ速さと射程でまっすぐ飛び、最初に当たったもので消える:
 * 生物ならダメージ（かすりはない）、魔法式オブジェクトの核（{@link SpellGramAim#coreBox}、耐久値の表示と同じ箱）なら耐久値を減らす
 * （{@link SpellGramIntegrity#reduce}、量 × 術者の事象干渉力）、ブロックならそこで弾ける。術者自身の魔法式オブジェクトはすり抜ける。
 */
public class EntityPsiBullet extends CombatSpellEntity {
    public static final float SPEED = EntityAirBullet.SPEED;
    public static final int MAX_LIFETIME = EntityAirBullet.MAX_LIFETIME;
    /** 弾の半径。生物の体の中心からの距離（＋体の幅・高さの半分）がこれ以内なら当たる。 */
    public static final float REACH = 0.3F;

    private static final String NBT_INTEGRITY_DAMAGE = "IntegrityDamage";

    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(EntityPsiBullet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(EntityPsiBullet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(EntityPsiBullet.class, EntityDataSerializers.FLOAT);

    /** 魔法式オブジェクトに当たったときに減らす耐久値（事象干渉力を掛ける前、サーバー側）。 */
    private float integrityDamage;

    public EntityPsiBullet(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityPsiBullet(Level level, Vec3 start, Vec3 direction) {
        this(PsitweaksEntities.PSI_BULLET.get(), level);
        Vec3 dir = direction.normalize();
        setPos(start.x, start.y, start.z);
        entityData.set(DIR_X, (float) dir.x);
        entityData.set(DIR_Y, (float) dir.y);
        entityData.set(DIR_Z, (float) dir.z);
        setDeltaMovement(dir.scale(SPEED));
    }

    /** 魔法式オブジェクトに当たったときに減らす耐久値（事象干渉力を掛ける前）。{@link #configure} と一緒に、置く前に呼ぶ。 */
    public void setIntegrityDamage(float integrityDamage) {
        this.integrityDamage = Math.max(0.0F, integrityDamage);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DIR_X, 0.0F);
        builder.define(DIR_Y, 0.0F);
        builder.define(DIR_Z, 1.0F);
    }

    public Vec3 getFlightDirection() {
        Vec3 dir = new Vec3(entityData.get(DIR_X), entityData.get(DIR_Y), entityData.get(DIR_Z));
        return dir.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : dir.normalize();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(1.5);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 start = position();
        Vec3 direction = getFlightDirection();
        Vec3 end = start.add(direction.scale(SPEED));
        if (level() instanceof ServerLevel level) {
            if (tickCount > MAX_LIFETIME) {
                discard();
                return;
            }
            BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this));
            boolean hitsBlock = blockHit.getType() != HitResult.Type.MISS;
            double reach = hitsBlock ? blockHit.getLocation().distanceTo(start) : SPEED;
            LivingEntity caster = getCaster();

            // この tick に進む間で、いちばん手前に当たるもの（生物・魔法式オブジェクトの核・ブロック）で止まる。
            LivingEntity creature = null;
            SpellGramObject object = null;
            double nearest = reach;
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(REACH + 2.0))) {
                if (!canAffect(caster, living)) {
                    continue;
                }
                double along = reachOf(living, start, direction);
                if (along >= 0.0 && along < nearest) {
                    nearest = along;
                    creature = living;
                }
            }
            for (SpellGramObject candidate : level.getEntitiesOfClass(SpellGramObject.class,
                    new AABB(start, end).inflate(SpellGramAim.CORE_HALF + 1.0))) {
                if (candidate.isEnded() || isOwn(caster, candidate)) {
                    continue;
                }
                AABB core = SpellGramAim.coreBox(candidate).inflate(REACH);
                Optional<Vec3> hit = core.contains(start) ? Optional.of(start) : core.clip(start, end);
                double along = hit.map(point -> point.distanceTo(start)).orElse(-1.0);
                if (along >= 0.0 && along < nearest) {
                    nearest = along;
                    object = candidate;
                    creature = null;
                }
            }

            if (object != null) {
                SpellGramIntegrity.reduce(object, integrityDamage, caster);
                impact(level, start.add(direction.scale(nearest)), true);
                discard();
                return;
            }
            if (creature != null) {
                if (harm(caster, creature, AttackRules.source(level, PsitweaksDamageTypes.PSI_BULLET, this, caster), getDamage())) {
                    Vec3 center = CombatSpellHelper.center(creature);
                    CombatEffects.sound(level, center, SoundEvents.AMETHYST_BLOCK_HIT, 0.8F, 1.6F);
                }
                impact(level, start.add(direction.scale(nearest)), false);
                discard();
                return;
            }
            if (hitsBlock) {
                impact(level, blockHit.getLocation(), false);
                discard();
                return;
            }
        }
        setPos(end.x, end.y, end.z);
    }

    /**
     * 弾の線（{@code from} から {@code direction} へ）が生物の体に届く距離。体の中心から線までの横と縦の隔たりが、
     * {@link #REACH} ＋ 体の幅・高さの半分を超えれば届かない（-1）。
     */
    private static double reachOf(LivingEntity living, Vec3 from, Vec3 direction) {
        Vec3 offset = CombatSpellHelper.center(living).subtract(from);
        double along = offset.dot(direction);
        double halfWidth = living.getBbWidth() * 0.5;
        double halfHeight = living.getBbHeight() * 0.5;
        if (along < -halfWidth || along > SPEED + halfWidth) {
            return -1.0;
        }
        Vec3 apart = offset.subtract(direction.scale(along));
        double horizontal = Math.sqrt(apart.x * apart.x + apart.z * apart.z);
        if (horizontal > REACH + halfWidth || Math.abs(apart.y) > REACH + halfHeight) {
            return -1.0;
        }
        return Mth.clamp(along - halfWidth, 0.0, SPEED);
    }

    /** 術者自身の魔法式オブジェクトか（弾はすり抜ける）。 */
    private static boolean isOwn(@Nullable LivingEntity caster, SpellGramObject object) {
        UUID owner = object.getCasterUuid();
        return caster != null && owner != null && owner.equals(caster.getUUID());
    }

    private void impact(ServerLevel level, Vec3 at, boolean spellGram) {
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, spellGram ? 10 : 5, 0.15, 0.15, 0.15, 0.06);
        if (spellGram) {
            CombatEffects.sound(level, at, SoundEvents.GLASS_HIT, 1.0F, 1.4F);
            CombatEffects.sound(level, at, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.8F, 1.8F);
        } else {
            CombatEffects.sound(level, at, SoundEvents.AMETHYST_CLUSTER_HIT, 0.6F, 1.6F);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        integrityDamage = tag.getFloat(NBT_INTEGRITY_DAMAGE);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(NBT_INTEGRITY_DAMAGE, integrityDamage);
    }
}
