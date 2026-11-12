package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.spells.combat.AttackCaster;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 魔法式オブジェクト（SpellGram Object）の基底: 耐久値を持つ魔法の実体すべて。2026-11-13 ユーザーの分類で、
 * 常駐魔法式（Resident SpellGram: 円 {@link SpellGramCircle} と領域 {@code SpellGramZone}、{@link #isResident} が真）と、
 * 流星群の夜の核（目印型 {@code SpellGramMarker}）に分かれる。
 * 存続時間、耐久値（{@link #getIntegrity}）、周囲へのダメージ、追従を共通にする。
 * <p>
 * 魔法式オブジェクトは物ではない: 剣や矢、爆発などふつうの攻撃では壊れず（{@link #hurt}）、押されず、チャンクと一緒に
 * 保存しない。存続時間が尽きると消える（{@link EndReason#FINISHED}）。耐久値は時間とともに一定の割合で減り（存続時間の
 * 間に減る分は耐久値の {@link #getTimeShare} 倍。既定の 1 なら存続時間の終わりにちょうど 0）、術式解体などの術式からは
 * 受け口 {@link #reduceIntegrity} で削られる。尽きると存続時間が残っていても消える（{@link EndReason#DESTROYED}）。
 * 時間で減る量は 1 tick あたり 置いたときの耐久値 × {@link #getTimeShare} ÷ 存続時間 で、置いたときの耐久値を存続時間に
 * 比例させれば（領域の「時間」の入力）、干渉力が同じなら減り方は存続時間によらず一定になり、長く置いたものほど解体に強い。
 * 作動式: 魔法式延長は {@link #restoreIntegrity} で耐久値を戻す（置いたときの耐久値を超えた分は、そのぶん長く残る）。
 * 解体される・術者がいなくなると {@link #end} で消える。Psi の運動追加・瞬間移動・大移動では、置いた術者のものも含めて動かない
 * （{@code PieceTrickAddMotionSpellGramMixin}・{@code PieceTrickBlinkSpellGramMixin}）。動かす仕組みは専用の作動式として
 * 後で作る予定（保留中）。
 */
public abstract class SpellGramObject extends Entity implements IEntityWithComplexSpawn {

    /**
     * 常駐魔法式か（2026-11-13 ユーザーの分類）: 置いたあとも残り続け、術者が手入れするもの（サークル・領域）。取得子: 自分の常駐魔法式・
     * 作動式: 魔法式回収・魔法式延長・置ける数の上限は、常駐魔法式だけを扱う。流星群の夜の核（目印型）は常駐魔法式ではない。
     * 新しい種類は、常駐魔法式なら上書きして真を返す。
     */
    public boolean isResident() {
        return false;
    }

    /** 魔法式オブジェクトが消えた理由。 */
    public enum EndReason {
        /** 存続時間が尽きた、または作動式の役目を終えた。 */
        FINISHED,
        /** ほかの術式で解体された（張り直しで古いものが解けたときも）。 */
        DISPELLED,
        /** 強度が尽きた。 */
        DESTROYED,
        /** 術者がいなくなった（ログアウト、別のディメンション、自律詠唱機が読み込まれていない）。 */
        CASTER_GONE,
        /** 術者が自分で回収した（作動式: 魔法式回収）。 */
        RECALLED
    }

    private static final String NBT_REMAINING_TICKS = "SpellGramRemainingTicks";
    private static final String NBT_DAMAGE_RADIUS = "SpellGramDamageRadius";
    private static final String NBT_AOE_DAMAGE = "SpellGramAoeDamage";
    private static final String NBT_DAMAGE_INTERVAL = "SpellGramDamageInterval";
    private static final String NBT_INTEGRITY = "SpellGramIntegrity";
    private static final String NBT_LIFETIME_TICKS = "SpellGramLifetimeTicks";
    private static final String NBT_MAX_INTEGRITY = "SpellGramMaxIntegrity";
    private static final String NBT_WORN = "SpellGramWorn";
    private static final String NBT_TIME_SHARE = "SpellGramTimeShare";
    private static final String NBT_MOTION_DAMPING = "SpellGramMotionDamping";
    private static final String NBT_CASTER_UUID = "SpellGramCaster";
    private static final String NBT_AOE_COUNTER = "SpellGramAoeCounter";
    private static final String NBT_ENTITY_FOLLOW_MODE = "SpellGramEntityFollowMode";
    private static final String NBT_FOLLOW_TARGET_UUID = "SpellGramFollowTarget";

    /** 残りの存続時間（tick）。 */
    private static final EntityDataAccessor<Float> DATA_REMAINING_TICKS =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_LIFETIME_TICKS =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE_RADIUS =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_AOE_DAMAGE =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_DAMAGE_INTERVAL =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_MAX_INTEGRITY =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    /** 術式で削られた耐久値の合計。 */
    private static final EntityDataAccessor<Float> DATA_WORN =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    /** 存続時間の間に時間で減る耐久値の、置いたときの耐久値に対する割合。 */
    private static final EntityDataAccessor<Float> DATA_TIME_SHARE =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MOTION_DAMPING =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_ENTITY_FOLLOW_MODE =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_FOLLOW_TARGET_ID =
            SynchedEntityData.defineId(SpellGramObject.class, EntityDataSerializers.INT);

    private static final int NO_FOLLOW_TARGET_ENTITY_ID = -1;
    /** 置いたときの耐久値に対して、これより少ない耐久値は 0 とみなす（丸めで 0 のすぐ上に残らないように）。 */
    private static final float INTEGRITY_EPSILON = 1.0E-5F;

    private int aoeTickCounter;
    /** 術者。保存した ID（CasterIds）だけで引き直す。攻撃の設定は {@link #attackOptions()}。 */
    private final AttackCaster attackCaster = AttackCaster.byId();
    @Nullable
    private UUID followTargetUuid;
    private boolean ended;

    protected SpellGramObject(EntityType<? extends SpellGramObject> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    @Override
    protected final void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_REMAINING_TICKS, 40.0F);
        builder.define(DATA_LIFETIME_TICKS, 40);
        builder.define(DATA_DAMAGE_RADIUS, 0.0F);
        builder.define(DATA_AOE_DAMAGE, 0.0F);
        builder.define(DATA_DAMAGE_INTERVAL, 20);
        builder.define(DATA_MAX_INTEGRITY, 1.0F);
        builder.define(DATA_WORN, 0.0F);
        builder.define(DATA_TIME_SHARE, 1.0F);
        builder.define(DATA_MOTION_DAMPING, 0.85F);
        builder.define(DATA_ENTITY_FOLLOW_MODE, false);
        builder.define(DATA_FOLLOW_TARGET_ID, NO_FOLLOW_TARGET_ENTITY_ID);
        defineSpellGramSynchedData(builder);
    }

    protected void defineSpellGramSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        tickMotion();

        if (level().isClientSide) {
            return;
        }

        if (getCasterUuid() != null && getCaster() == null) {
            // ダメージの帰属と持ち主の確かめに術者が要るので、術者を引き直せなくなったら消える。
            end(EndReason.CASTER_GONE);
            return;
        }
        onSpellGramTickServer();
        if (isEnded()) {
            return;
        }
        tickAreaDamage();
        tickLifetime();
    }

    protected void tickMotion() {
        if (tickEntityFollow()) {
            return;
        }

        Vec3 velocity = getDeltaMovement();
        if (velocity.lengthSqr() <= 1.0E-7) {
            return;
        }

        move(MoverType.SELF, velocity);

        float damping = getMotionDamping();
        if (damping <= 0.0F) {
            setDeltaMovement(Vec3.ZERO);
        } else if (damping < 1.0F) {
            setDeltaMovement(velocity.scale(damping));
        }
    }

    protected boolean tickEntityFollow() {
        if (!isEntityFollowMode()) {
            return false;
        }

        Entity followTarget = getFollowTargetEntity();
        if (!isValidFollowTarget(followTarget)) {
            return false;
        }

        setDeltaMovement(Vec3.ZERO);
        setPos(followTarget.getX(), followTarget.getY(), followTarget.getZ());
        return true;
    }

    protected boolean isValidFollowTarget(@Nullable Entity entity) {
        return entity != null
                && entity != this
                && !entity.isRemoved()
                && entity.isAlive()
                && entity.level() == level();
    }

    protected void tickAreaDamage() {
        if (getAoEDamage() <= 0.0F || getDamageRadius() <= 0.0F) {
            return;
        }

        aoeTickCounter++;
        if (aoeTickCounter < Math.max(1, getDamageIntervalTicks())) {
            return;
        }
        aoeTickCounter = 0;
        applyAreaDamage();
    }

    protected void applyAreaDamage() {
        LivingEntity caster = getCaster();
        AABB area = getBoundingBox().inflate(getDamageRadius());
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area, target -> canDamageEntity(caster, target));

        if (targets.isEmpty()) {
            return;
        }

        DamageSource source = createAreaDamageSource(caster);
        float damage = getAoEDamage();
        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            if (AttackRules.harm(caster, target, attackOptions(), source, damage)) {
                onAreaDamageApplied(target, damage, source);
            }
        }
    }

    protected DamageSource createAreaDamageSource(@Nullable LivingEntity caster) {
        if (caster != null) {
            return this.damageSources().indirectMagic(this, caster);
        }
        return this.damageSources().magic();
    }

    /** 攻撃術式で共通の命中判定（{@link AttackRules#canHarm}）。 */
    protected boolean canDamageEntity(@Nullable LivingEntity caster, LivingEntity target) {
        // 術者がいなくなっても、詠唱したときの術者の側（本人、ペット、チーム）には当てない。
        return attackCaster.canHarm(caster, target, attackOptions());
    }

    /** 攻撃の設定。プレイヤーに安全の扱いを持つ術式円は上書きする。 */
    protected AttackRules.Options attackOptions() {
        return AttackRules.Options.DEFAULT;
    }

    protected void onAreaDamageApplied(LivingEntity target, float damage, DamageSource source) {
    }

    /** 存続時間を 1 tick 進める（耐久値も時間の分だけ減る）。存続時間が尽きたら役目を終え、先に耐久値が尽きたら壊れる。 */
    protected void tickLifetime() {
        float remaining = getRemaining() - 1.0F;
        setRemaining(remaining);
        if (remaining <= 0.0F) {
            end(EndReason.FINISHED);
        } else if (isIntegrityDepleted()) {
            onIntegrityDepleted();
        }
    }

    protected void onSpellGramTickServer() {
    }

    public boolean isEnded() {
        return ended || isRemoved();
    }

    /** 魔法式オブジェクトを消す（サーバー側）。何度呼んでも {@link #onEnd} は一度だけ。 */
    public void end(EndReason reason) {
        if (isEnded() || level().isClientSide) {
            return;
        }
        ended = true;
        onEnd(reason);
        discard();
    }

    /** 消える直前（サーバー側）。 */
    protected void onEnd(EndReason reason) {
    }

    /** ふつうの攻撃（剣、矢、爆発など）では壊れない。耐久値は {@link #reduceIntegrity} でだけ減る。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    /**
     * 魔法式オブジェクト用の受け口: 耐久値を {@code amount} だけ減らし、尽きたら {@link #onIntegrityDepleted}。
     * 剣や矢などのダメージ（{@link #hurt}）とは別の入口。術式で削るときは {@link SpellGramIntegrity#reduce}
     * （減らす側の事象干渉力とイベントを通す）を使う。
     *
     * @return 耐久値が減ったか
     */
    public boolean reduceIntegrity(float amount) {
        if (isEnded() || level().isClientSide || amount <= 0.0F) {
            return false;
        }
        this.entityData.set(DATA_WORN, getWorn() + amount);
        if (isIntegrityDepleted()) {
            onIntegrityDepleted();
        }
        return true;
    }

    private boolean isIntegrityDepleted() {
        return getIntegrity() <= getMaxIntegrity() * INTEGRITY_EPSILON;
    }

    /** 受け口で耐久値が尽きたとき。 */
    protected void onIntegrityDepleted() {
        end(EndReason.DESTROYED);
    }

    /** ほかの術式で解体する（{@link EndReason#DISPELLED}）。 */
    public void destroyBySpell() {
        end(EndReason.DISPELLED);
    }

    public void moveBySpell(Vec3 offset) {
        setPos(getX() + offset.x, getY() + offset.y, getZ() + offset.z);
    }

    public void addSpellVelocity(Vec3 velocity) {
        setDeltaMovement(getDeltaMovement().add(velocity));
    }

    /** 残りの存続時間（tick）。 */
    public int getRemainingTicks() {
        return (int) Math.ceil(getRemaining());
    }

    /** 置いてから自然に消えるまでの時間（tick）。 */
    public int getLifetimeTicks() {
        return this.entityData.get(DATA_LIFETIME_TICKS);
    }

    /** 置いたときに払ったコスト（作動式: 魔法式延長が、どれだけ回復できるかを決める）。サーバー側だけ。 */
    private float placementCost = 1.0F;

    /** 置く前に呼ぶ。置いたときに払ったコスト（その作動式の Psi のコスト）。 */
    public void setPlacementCost(float cost) {
        this.placementCost = Math.max(1.0F, cost);
    }

    /** 置いたときに払ったコスト（サーバー側）。 */
    public float getPlacementCost() {
        return placementCost;
    }

    /**
     * 耐久値を {@code amount} だけ戻す（作動式: 魔法式延長、2026-11-11 ユーザー）。耐久値は 置いたときの耐久値 ×
     * {@code maxFactor} までで、それを超える分は捨てる。術式で削られた分から先に戻し、残りは時間で減った分として残りの存続時間を
     * 延ばす（耐久値は毎 tick 同じ量ずつ減るので、置いたときの耐久値を超えた分は、そのぶん長く残る）。時間で減らない魔法式
     * （{@link #getTimeShare} が 0）は、削られた分しか戻らない。実際に戻った耐久値を返す。
     */
    public float restoreIntegrity(float amount, float maxFactor) {
        if (isEnded() || amount <= 0.0F) {
            return 0.0F;
        }
        float max = getMaxIntegrity();
        float before = getIntegrity();
        float target = Math.min(max * Math.max(1.0F, maxFactor), before + amount);
        if (target <= before) {
            return 0.0F;
        }
        float need = target - before;
        float fromWorn = Math.min(getWorn(), need);
        this.entityData.set(DATA_WORN, getWorn() - fromWorn);
        float rest = need - fromWorn;
        float share = getTimeShare();
        if (rest > 0.0F && share > 0.0F && max > 0.0F) {
            setRemaining(getRemaining() + rest * getLifetimeTicks() / (max * share));
        }
        return Math.max(0.0F, getIntegrity() - before);
    }

    /** 置く前に呼ぶ。存続時間を {@code ticks} にし、耐久値を満たす。 */
    public void setLifetimeTicks(int ticks) {
        int lifetime = Math.max(1, ticks);
        this.entityData.set(DATA_LIFETIME_TICKS, lifetime);
        setRemaining(lifetime);
    }

    /** 存続時間の残りの割合（0〜1）。 */
    public float getRemainingFraction() {
        return Math.min(1.0F, getRemaining() / getLifetimeTicks());
    }

    private float getRemaining() {
        return this.entityData.get(DATA_REMAINING_TICKS);
    }

    private void setRemaining(float ticks) {
        this.entityData.set(DATA_REMAINING_TICKS, Math.max(0.0F, ticks));
    }

    public float getDamageRadius() {
        return this.entityData.get(DATA_DAMAGE_RADIUS);
    }

    public void setDamageRadius(float radius) {
        this.entityData.set(DATA_DAMAGE_RADIUS, Math.max(0.0F, radius));
    }

    public float getAoEDamage() {
        return this.entityData.get(DATA_AOE_DAMAGE);
    }

    public void setAoEDamage(float damage) {
        this.entityData.set(DATA_AOE_DAMAGE, Math.max(0.0F, damage));
    }

    public int getDamageIntervalTicks() {
        return this.entityData.get(DATA_DAMAGE_INTERVAL);
    }

    public void setDamageIntervalTicks(int intervalTicks) {
        this.entityData.set(DATA_DAMAGE_INTERVAL, Math.max(1, intervalTicks));
    }

    public void configureAreaDamage(float radius, float damage, int intervalTicks) {
        setDamageRadius(radius);
        setAoEDamage(damage);
        setDamageIntervalTicks(intervalTicks);
    }

    /**
     * 耐久値。置いたときの耐久値（{@link #getMaxIntegrity}）から、時間とともに一定の割合で減り（存続時間の間に減る分は
     * {@link #getTimeShare} 倍）、術式で削った分（{@link #reduceIntegrity}）も減る。作動式: 魔法式延長で残りの存続時間が
     * 元より長くなっていれば、置いたときの耐久値を超える（{@link #restoreIntegrity}）。
     */
    public float getIntegrity() {
        float max = getMaxIntegrity();
        float elapsed = 1.0F - getRemaining() / getLifetimeTicks();
        return Math.max(0.0F, max * (1.0F - getTimeShare() * elapsed) - getWorn());
    }

    /** 耐久値を {@code integrity} にする（術式で削った分を変える）。 */
    public void setIntegrity(float integrity) {
        float untouched = getIntegrity() + getWorn();
        this.entityData.set(DATA_WORN, Math.max(0.0F, untouched - Math.max(0.0F, integrity)));
    }

    /** 術式で削られた耐久値の合計。 */
    public float getWorn() {
        return this.entityData.get(DATA_WORN);
    }

    /**
     * 存続時間の間に時間で減る耐久値の、置いたときの耐久値に対する割合（0〜1）。1 なら存続時間の終わりにちょうど 0、
     * 0.2 なら置いたときの耐久値は時間で減る分の 5 倍。
     */
    public float getTimeShare() {
        return this.entityData.get(DATA_TIME_SHARE);
    }

    /** 置く前に呼ぶ。{@link #getTimeShare}。 */
    public void setTimeShare(float share) {
        this.entityData.set(DATA_TIME_SHARE, Math.max(0.0F, Math.min(1.0F, share)));
    }

    /** 置いたときの耐久値。 */
    public float getMaxIntegrity() {
        return this.entityData.get(DATA_MAX_INTEGRITY);
    }

    /** 置く前に呼ぶ。置いたときの耐久値（{@link SpellGramIntegrity#initial}）。 */
    public void setMaxIntegrity(float integrity) {
        this.entityData.set(DATA_MAX_INTEGRITY, Math.max(0.0F, integrity));
    }

    /** 術者の事象干渉力（置いた時点の値を使う）。 */
    public static double interferenceOf(@Nullable LivingEntity caster) {
        return caster != null ? caster.getAttributeValue(PsitweaksAttributes.SPELL_DAMAGE_FACTOR) : 1.0D;
    }

    public float getMotionDamping() {
        return this.entityData.get(DATA_MOTION_DAMPING);
    }

    public void setMotionDamping(float damping) {
        this.entityData.set(DATA_MOTION_DAMPING, Math.max(0.0F, Math.min(1.0F, damping)));
    }

    public boolean isEntityFollowMode() {
        return this.entityData.get(DATA_ENTITY_FOLLOW_MODE);
    }

    public void setEntityFollowMode(boolean enabled) {
        this.entityData.set(DATA_ENTITY_FOLLOW_MODE, enabled);
    }

    public void setFollowTarget(@Nullable Entity target, boolean enableFollowMode) {
        setFollowTargetEntity(target);
        setEntityFollowMode(enableFollowMode);
    }

    public void setFollowTargetEntity(@Nullable Entity target) {
        if (!isValidFollowTarget(target)) {
            clearFollowTargetEntity();
            return;
        }

        followTargetUuid = CasterIds.idOf(target);
        setFollowTargetEntityId(target.getId());
    }

    public void clearFollowTargetEntity() {
        followTargetUuid = null;
        setFollowTargetEntityId(NO_FOLLOW_TARGET_ENTITY_ID);
    }

    public boolean hasFollowTargetEntity() {
        return getFollowTargetEntityId() != NO_FOLLOW_TARGET_ENTITY_ID || followTargetUuid != null;
    }

    @Nullable
    public UUID getFollowTargetUuid() {
        return followTargetUuid;
    }

    @Nullable
    public Entity getFollowTargetEntity() {
        Entity resolvedById = resolveFollowTargetById();
        if (isValidFollowTarget(resolvedById)) {
            return resolvedById;
        }

        if (followTargetUuid == null || !(level() instanceof ServerLevel)) {
            return null;
        }

        // 自律詠唱機の詠唱主体はワールドに入らないため、ID では引けず内部キーで引き直す。
        Entity resolvedByUuid = CasterIds.find(level(), followTargetUuid);
        if (!isValidFollowTarget(resolvedByUuid)) {
            return null;
        }

        setFollowTargetEntityId(resolvedByUuid.getId());
        return resolvedByUuid;
    }

    private int getFollowTargetEntityId() {
        return this.entityData.get(DATA_FOLLOW_TARGET_ID);
    }

    private void setFollowTargetEntityId(int targetEntityId) {
        this.entityData.set(DATA_FOLLOW_TARGET_ID, targetEntityId);
    }

    @Nullable
    private Entity resolveFollowTargetById() {
        int targetEntityId = getFollowTargetEntityId();
        if (targetEntityId == NO_FOLLOW_TARGET_ENTITY_ID) {
            return null;
        }
        return level().getEntity(targetEntityId);
    }

    public void setCaster(@Nullable LivingEntity caster) {
        this.attackCaster.set(caster);
    }

    @Nullable
    public UUID getCasterUuid() {
        return attackCaster.id();
    }

    @Nullable
    public LivingEntity getCaster() {
        // 自律詠唱機の詠唱主体はワールドに入らないため、保存した ID で引き直す（AttackCaster）。
        return attackCaster.get(level());
    }

    @Override
    protected final void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(NBT_REMAINING_TICKS)) {
            // 存続時間のない古い保存では、残りの時間を存続時間とみなす。
            float remaining = tag.getFloat(NBT_REMAINING_TICKS);
            int lifetime = tag.contains(NBT_LIFETIME_TICKS) ? tag.getInt(NBT_LIFETIME_TICKS) : (int) Math.ceil(remaining);
            setLifetimeTicks(lifetime);
            setRemaining(remaining);
        }
        if (tag.contains(NBT_DAMAGE_RADIUS)) {
            setDamageRadius(tag.getFloat(NBT_DAMAGE_RADIUS));
        }
        if (tag.contains(NBT_AOE_DAMAGE)) {
            setAoEDamage(tag.getFloat(NBT_AOE_DAMAGE));
        }
        if (tag.contains(NBT_DAMAGE_INTERVAL)) {
            setDamageIntervalTicks(tag.getInt(NBT_DAMAGE_INTERVAL));
        }
        if (tag.contains(NBT_MAX_INTEGRITY)) {
            setMaxIntegrity(tag.getFloat(NBT_MAX_INTEGRITY));
        } else if (tag.contains(NBT_INTEGRITY)) {
            setMaxIntegrity(tag.getFloat(NBT_INTEGRITY));
        }
        this.entityData.set(DATA_WORN, Math.max(0.0F, tag.getFloat(NBT_WORN)));
        if (tag.contains(NBT_TIME_SHARE)) {
            setTimeShare(tag.getFloat(NBT_TIME_SHARE));
        }
        if (tag.contains(NBT_MOTION_DAMPING)) {
            setMotionDamping(tag.getFloat(NBT_MOTION_DAMPING));
        }

        aoeTickCounter = Math.max(0, tag.getInt(NBT_AOE_COUNTER));

        attackCaster.loadId(tag, NBT_CASTER_UUID);

        setEntityFollowMode(tag.getBoolean(NBT_ENTITY_FOLLOW_MODE));
        if (tag.hasUUID(NBT_FOLLOW_TARGET_UUID)) {
            followTargetUuid = tag.getUUID(NBT_FOLLOW_TARGET_UUID);
            if (level() instanceof ServerLevel) {
                Entity followTarget = CasterIds.find(level(), followTargetUuid);
                setFollowTargetEntityId(followTarget != null ? followTarget.getId() : NO_FOLLOW_TARGET_ENTITY_ID);
            } else {
                setFollowTargetEntityId(NO_FOLLOW_TARGET_ENTITY_ID);
            }
        } else {
            clearFollowTargetEntity();
        }

        readSpellGramData(tag);
    }

    @Override
    protected final void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat(NBT_REMAINING_TICKS, getRemaining());
        tag.putInt(NBT_LIFETIME_TICKS, getLifetimeTicks());
        tag.putFloat(NBT_DAMAGE_RADIUS, getDamageRadius());
        tag.putFloat(NBT_AOE_DAMAGE, getAoEDamage());
        tag.putInt(NBT_DAMAGE_INTERVAL, getDamageIntervalTicks());
        tag.putFloat(NBT_MAX_INTEGRITY, getMaxIntegrity());
        tag.putFloat(NBT_WORN, getWorn());
        tag.putFloat(NBT_TIME_SHARE, getTimeShare());
        tag.putFloat(NBT_MOTION_DAMPING, getMotionDamping());
        tag.putInt(NBT_AOE_COUNTER, aoeTickCounter);
        tag.putBoolean(NBT_ENTITY_FOLLOW_MODE, isEntityFollowMode());
        attackCaster.saveId(tag, NBT_CASTER_UUID);
        if (followTargetUuid != null) {
            tag.putUUID(NBT_FOLLOW_TARGET_UUID, followTargetUuid);
        }

        addSpellGramData(tag);
    }

    protected void readSpellGramData(CompoundTag tag) {
    }

    protected void addSpellGramData(CompoundTag tag) {
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(getRemaining());
        buffer.writeInt(getLifetimeTicks());
        buffer.writeFloat(getDamageRadius());
        buffer.writeFloat(getAoEDamage());
        buffer.writeInt(getDamageIntervalTicks());
        buffer.writeFloat(getMaxIntegrity());
        buffer.writeFloat(getWorn());
        buffer.writeFloat(getTimeShare());
        buffer.writeFloat(getMotionDamping());
        buffer.writeBoolean(isEntityFollowMode());
        buffer.writeInt(getFollowTargetEntityId());
        writeSpellGramSpawnData(buffer);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        float remaining = additionalData.readFloat();
        setLifetimeTicks(additionalData.readInt());
        setRemaining(remaining);
        setDamageRadius(additionalData.readFloat());
        setAoEDamage(additionalData.readFloat());
        setDamageIntervalTicks(additionalData.readInt());
        setMaxIntegrity(additionalData.readFloat());
        this.entityData.set(DATA_WORN, additionalData.readFloat());
        setTimeShare(additionalData.readFloat());
        setMotionDamping(additionalData.readFloat());
        setEntityFollowMode(additionalData.readBoolean());
        setFollowTargetEntityId(additionalData.readInt());
        readSpellGramSpawnData(additionalData);
    }

    protected void writeSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
    }

    protected void readSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!level().isClientSide) {
            SpellGramIndex.add(this);
        }
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (!level().isClientSide) {
            SpellGramIndex.remove(this);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    /** チャンクと一緒には保存しない（術者がいないと続けられないため）。 */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}
