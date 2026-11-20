package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityInterferenceZone;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 床に張る円の常駐魔法式（ファイアサークル・アイスサークル）の基底。
 * 半径 {@link #getDamageRadius()} の円の中（上下 {@link #VERTICAL_RANGE}）にいる相手に、一定の間隔でダメージを与える。
 * 円ごとの違いは、ダメージの種類・当てたときの効果・パーティクル（とアイスサークルの薄氷）だけ。
 * <ul>
 *   <li>重なり: 同じ術者の同じ種類の円が重なったところにいる相手には、一番強い円（{@link #outranks}）だけが当たる。</li>
 *   <li>領域干渉: 中心のブロックが、勝てない他人の領域干渉の中にある間は、何もしない（{@link #isSuppressed}）。時間は進む。</li>
 *   <li>追従（作動式: サークル追従）: 追従先の位置に毎 tick 移り、動いた距離のぶん耐久値を使う。追従を続けられなくなると
 *   その場に残る（{@link #shouldRelease}）。</li>
 *   <li>Psi の運動追加・瞬間移動・大移動: 術者自身の、追従していない円だけが動く（{@link #isMovableBy}）。耐久値は減らない。</li>
 * </ul>
 */
public abstract class SpellGramCircle extends SpellGramObject {

    @Override
    public boolean isResident() {
        return true;
    }

    /** 術者以外の追従先を、術者からこれより遠くへは連れていけない（ブロック、Psi の射程と同じ）。 */
    public static final double FOLLOW_LEASH = 32.0D;

    private static final String NBT_SAFE_TO_PLAYERS = "SafeToPlayers";
    private static final String NBT_VISUAL_SCALE = "VisualScale";
    private static final String NBT_POWER = "CirclePower";
    private static final String NBT_OWNER_SIDE = "CircleOwnerSide";
    private static final String NBT_PLACED_INTERFERENCE = "CirclePlacedInterference";

    private static final int RING_PARTICLE_COUNT = 12;
    private static final int INNER_PARTICLE_COUNT = 4;
    private static final double VERTICAL_RANGE = 1.0D;

    private static final EntityDataAccessor<Boolean> DATA_SAFE_TO_PLAYERS =
            SynchedEntityData.defineId(SpellGramCircle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_VISUAL_SCALE =
            SynchedEntityData.defineId(SpellGramCircle.class, EntityDataSerializers.FLOAT);

    /** 威力（重なりでどれが当たるかを決める）。サーバー側だけ。 */
    private float power = 1.0F;
    /** 置いた人の側（術者の {@code getUUID()}。自律詠唱機は所有者の UUID を名乗る）。領域干渉が「他人」かを見分ける。サーバー側だけ。 */
    @Nullable
    private UUID ownerSide;
    /** 置いた時点の術者の事象干渉力（重なりと領域干渉で比べる）。サーバー側だけ。 */
    private double placedInterference = 1.0D;

    /** この tick に動いた距離（ブロック、追従・Psi の運動追加・瞬間移動などすべて）。サーバー側だけ。 */
    private double lastMoved;
    /** 前の tick の終わりの位置（tick の合間の瞬間移動も数えるため）。サーバー側だけ。 */
    @Nullable
    private Vec3 lastTickPosition;
    /** 領域干渉に止められているかを確かめた tick と台帳の番号、その結果。サーバー側だけ。 */
    private long suppressedTick = Long.MIN_VALUE;
    private long suppressedVersion = -1L;
    private boolean suppressed;

    protected SpellGramCircle(EntityType<? extends SpellGramCircle> type, Level level) {
        super(type, level);
    }

    /** 円のダメージの種類（術者を攻撃者に持つ）。 */
    protected abstract ResourceKey<DamageType> damageType();

    /** 円周を回るパーティクル。 */
    protected abstract ParticleOptions ringParticle();

    /** 円の中に散るパーティクル。 */
    protected abstract ParticleOptions innerParticle();

    @Override
    protected void defineSpellGramSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SAFE_TO_PLAYERS, false);
        builder.define(DATA_VISUAL_SCALE, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide && !isRemoved()) {
            spawnClientParticles();
        }
    }

    /** 置くときに呼ぶ。置いた人の側と、置いた時点の事象干渉力もここで決まる。 */
    @Override
    public void setCaster(@Nullable LivingEntity caster) {
        super.setCaster(caster);
        if (caster != null) {
            this.ownerSide = caster.getUUID();
            this.placedInterference = interferenceOf(caster);
        }
    }

    /** 置く前に呼ぶ。威力（重なりでどれが当たるかを決める）。 */
    public void setPower(float power) {
        this.power = Math.max(0.0F, power);
    }

    public float getPower() {
        return power;
    }

    /** 置いた時点の事象干渉力を変える（{@link #setCaster} のあとで呼ぶ）。 */
    public void setPlacedInterference(double interference) {
        this.placedInterference = interference;
    }

    public double getPlacedInterference() {
        return placedInterference;
    }

    @Nullable
    public UUID getOwnerSide() {
        return ownerSide;
    }

    /** この tick に動いた距離（ブロック、サーバー側）。追従・Psi の運動追加・瞬間移動など、どの動きも数える。 */
    public double getLastMoved() {
        return lastMoved;
    }

    /** 動いたあとに、この tick に動いた距離を数える（前の tick の終わりからの距離なので、tick の合間の瞬間移動も入る）。 */
    @Override
    protected void tickMotion() {
        super.tickMotion();
        if (!level().isClientSide) {
            Vec3 now = position();
            lastMoved = lastTickPosition == null ? 0.0D : now.distanceTo(lastTickPosition);
            lastTickPosition = now;
        }
    }

    /**
     * {@code mover} の Psi の作動式（運動追加・瞬間移動・大移動）で動かしてよいか: 術者自身（自律詠唱機は機械ごと）のサークルで、
     * 追従していないときだけ。追従中は追従が優先。Psi の作動式で動いても耐久値は減らない。
     */
    public boolean isMovableBy(@Nullable Entity mover) {
        return !isEntityFollowMode() && mover != null && CasterIds.matches(mover, getCasterUuid());
    }

    // ---- 追従（作動式: サークル追従） ----

    /** 追従先 {@code target} の位置へ移り続ける（サーバー側）。次の tick から動く。 */
    public void startFollowing(Entity target) {
        setFollowTarget(target, true);
    }

    /** 追従をやめ、今の場所に残る。 */
    public void stopFollowing() {
        setEntityFollowMode(false);
        clearFollowTargetEntity();
    }

    @Override
    protected boolean tickEntityFollow() {
        if (level().isClientSide || !isEntityFollowMode()) {
            return super.tickEntityFollow();
        }
        Entity target = getFollowTargetEntity();
        if (shouldRelease(target)) {
            stopFollowing();
            return false;
        }
        Vec3 before = position();
        boolean following = super.tickEntityFollow();
        double moved = position().distanceTo(before);
        if (moved > 0.0D) {
            // 追従で 1 ブロック動くと、存続時間 1 tick ぶんの耐久値を使う（術式で削られた分として数える）。
            float perTick = getMaxIntegrity() * getTimeShare() / Math.max(1, getLifetimeTicks());
            reduceIntegrity((float) (moved * perTick));
        }
        return following;
    }

    /**
     * 追従をやめるか（サーバー側）: 追従先を引けない・消えた、別のディメンション、エンティティの動くチャンクにいない、術者以外の
     * 追従先が術者から {@link #FOLLOW_LEASH} より遠い、術者以外の追従先に術者以外の生き物が乗っている。
     */
    protected boolean shouldRelease(@Nullable Entity target) {
        if (target == null || target.isRemoved() || !target.isAlive() || target.level() != level()) {
            return true;
        }
        if (level() instanceof ServerLevel serverLevel && !serverLevel.isPositionEntityTicking(target.blockPosition())) {
            return true;
        }
        if (CasterIds.matches(target, getCasterUuid())) {
            return false;
        }
        LivingEntity caster = getCaster();
        if (caster == null || caster.position().distanceToSqr(target.position()) > FOLLOW_LEASH * FOLLOW_LEASH) {
            return true;
        }
        return isRiddenByOthers(target, getCasterUuid());
    }

    /** {@code vehicle} に、術者（{@code casterId}）以外の生き物が（何段でも）乗っているか。 */
    public static boolean isRiddenByOthers(Entity vehicle, @Nullable UUID casterId) {
        for (Entity passenger : vehicle.getIndirectPassengers()) {
            if (passenger instanceof LivingEntity && !CasterIds.matches(passenger, casterId)) {
                return true;
            }
        }
        return false;
    }

    // ---- 領域干渉 ----

    /**
     * 領域干渉に止められているか（サーバー側。クライアントではいつも偽）: 中心のブロックで効いている他人の領域干渉のうち、置いた時点の
     * 事象干渉力（{@link EntityInterferenceZone#getPlacedInterference}）がこの円の置いた時点の事象干渉力より高いもの、同じなら先に
     * 置いたものがあれば止められている。止められている間はダメージも薄氷も出さない。
     */
    public boolean isSuppressed() {
        if (level().isClientSide) {
            return false;
        }
        long now = level().getGameTime();
        long version = SpellGramIndex.version();
        if (suppressedTick != now || suppressedVersion != version) {
            suppressedTick = now;
            suppressedVersion = version;
            suppressed = computeSuppressed();
        }
        return suppressed;
    }

    private boolean computeSuppressed() {
        List<EntityInterferenceZone> zones = SpellGramIndex.all(level(), EntityInterferenceZone.class);
        if (zones.isEmpty()) {
            return false;
        }
        BlockPos center = blockPosition();
        for (EntityInterferenceZone zone : zones) {
            UUID zoneSide = zone.getOwnerSide();
            if (zone.isEnded() || zoneSide == null || zoneSide.equals(ownerSide)) {
                continue;
            }
            if (zone.isInsideAndActive(center) && beatenBy(zone)) {
                return true;
            }
        }
        return false;
    }

    private boolean beatenBy(EntityInterferenceZone zone) {
        int interference = Double.compare(zone.getPlacedInterference(), placedInterference);
        return interference != 0 ? interference > 0 : zone.getId() < getId();
    }

    // ---- 重なり ----

    /** この円が {@code other} より強いか: 威力が高い → 置いた時点の事象干渉力が高い → 先に置いた。 */
    public boolean outranks(SpellGramCircle other) {
        int byPower = Float.compare(power, other.power);
        if (byPower != 0) {
            return byPower > 0;
        }
        int byInterference = Double.compare(placedInterference, other.placedInterference);
        if (byInterference != 0) {
            return byInterference > 0;
        }
        return getId() < other.getId();
    }

    /** {@code target} が、この円の当たる範囲（半径の円、上下 {@link #VERTICAL_RANGE}）の中にいるか。 */
    public boolean covers(LivingEntity target) {
        float radius = getDamageRadius();
        if (radius <= 0.0F || !target.getBoundingBox().intersects(damageArea(radius))) {
            return false;
        }
        Vec3 center = target.getBoundingBox().getCenter();
        double dx = center.x - getX();
        double dz = center.z - getZ();
        return dx * dx + dz * dz <= (double) radius * radius;
    }

    private AABB damageArea(float radius) {
        return new AABB(getX() - radius, getY() - VERTICAL_RANGE, getZ() - radius,
                getX() + radius, getY() + VERTICAL_RANGE, getZ() + radius);
    }

    /** {@code target} を覆っている、同じ術者の同じ種類の円のうち、この円より強い（止められていない）ものがあるか。 */
    private boolean outrankedFor(List<SpellGramObject> sameCaster, LivingEntity target) {
        for (SpellGramObject object : sameCaster) {
            if (object != this && object.getClass() == getClass() && !object.isEnded()
                    && object instanceof SpellGramCircle other && other.outranks(this)
                    && !other.isSuppressed() && other.covers(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected DamageSource createAreaDamageSource(@Nullable LivingEntity caster) {
        // 円ごとのダメージの種類で、術者を攻撃者に持つ。バニラの on_fire・freeze は継続ダメージとして障壁を素通りし、
        // 攻撃者も付かなかった。
        return AttackRules.source(level(), damageType(), this, caster);
    }

    @Override
    protected AttackRules.Options attackOptions() {
        return AttackRules.Options.of(isSafeToPlayers());
    }

    @Override
    protected void applyAreaDamage() {
        LivingEntity caster = getCaster();
        float horizontalRadius = getDamageRadius();
        if (horizontalRadius <= 0.0F || getAoEDamage() <= 0.0F || isSuppressed()) {
            return;
        }

        List<SpellGramObject> sameCaster = SpellGramIndex.ofCaster(level(), getCasterUuid());
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, damageArea(horizontalRadius),
                target -> canDamageEntity(caster, target) && covers(target) && !outrankedFor(sameCaster, target));

        if (targets.isEmpty()) {
            return;
        }

        DamageSource source = createAreaDamageSource(caster);
        float damage = getAoEDamage();
        for (LivingEntity target : targets) {
            if (caster instanceof Player player) {
                target.setLastHurtByPlayer(player);
            } else if (caster != null) {
                target.setLastHurtByMob(caster);
            }

            if (AttackRules.harmWithoutCooldown(caster, target, attackOptions(), source, damage)) {
                onAreaDamageApplied(target, damage, source);
            }
        }
    }

    public boolean isSafeToPlayers() {
        return this.entityData.get(DATA_SAFE_TO_PLAYERS);
    }

    public void setSafeToPlayers(boolean safeToPlayers) {
        this.entityData.set(DATA_SAFE_TO_PLAYERS, safeToPlayers);
    }

    public float getVisualScale() {
        return this.entityData.get(DATA_VISUAL_SCALE);
    }

    public void setVisualScale(float scale) {
        this.entityData.set(DATA_VISUAL_SCALE, Math.max(0.1F, scale));
    }

    private void spawnClientParticles() {
        float radius = getDamageRadius();
        if (radius <= 0.0F) {
            return;
        }

        double centerX = getX();
        double centerY = getY() + 0.05D;
        double centerZ = getZ();
        double baseAngle = (tickCount * 0.15D) % (Math.PI * 2.0D);

        ParticleOptions ring = ringParticle();
        for (int i = 0; i < RING_PARTICLE_COUNT; i++) {
            double angle = baseAngle + (Math.PI * 2.0D * i) / RING_PARTICLE_COUNT;
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            double ySpeed = 0.005D + random.nextDouble() * 0.015D;
            level().addParticle(ring, x, centerY, z, 0.0D, ySpeed, 0.0D);
        }

        ParticleOptions inner = innerParticle();
        for (int i = 0; i < INNER_PARTICLE_COUNT; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = Math.sqrt(random.nextDouble()) * radius;
            double x = centerX + Math.cos(angle) * distance;
            double z = centerZ + Math.sin(angle) * distance;
            level().addParticle(inner, x, centerY, z, 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    protected void readSpellGramData(CompoundTag tag) {
        setSafeToPlayers(tag.getBoolean(NBT_SAFE_TO_PLAYERS));
        if (tag.contains(NBT_VISUAL_SCALE)) {
            setVisualScale(tag.getFloat(NBT_VISUAL_SCALE));
        }
        if (tag.contains(NBT_POWER)) {
            setPower(tag.getFloat(NBT_POWER));
        }
        ownerSide = tag.hasUUID(NBT_OWNER_SIDE) ? tag.getUUID(NBT_OWNER_SIDE) : null;
        if (tag.contains(NBT_PLACED_INTERFERENCE)) {
            placedInterference = tag.getDouble(NBT_PLACED_INTERFERENCE);
        }
    }

    @Override
    protected void addSpellGramData(CompoundTag tag) {
        tag.putBoolean(NBT_SAFE_TO_PLAYERS, isSafeToPlayers());
        tag.putFloat(NBT_VISUAL_SCALE, getVisualScale());
        tag.putFloat(NBT_POWER, power);
        if (ownerSide != null) {
            tag.putUUID(NBT_OWNER_SIDE, ownerSide);
        }
        tag.putDouble(NBT_PLACED_INTERFERENCE, placedInterference);
    }

    @Override
    protected void writeSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(isSafeToPlayers());
        buffer.writeFloat(getVisualScale());
    }

    @Override
    protected void readSpellGramSpawnData(RegistryFriendlyByteBuf buffer) {
        setSafeToPlayers(buffer.readBoolean());
        setVisualScale(buffer.readFloat());
    }
}
