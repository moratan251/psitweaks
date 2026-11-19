package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.marker.SpellGramMarker;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The singularity of Trick: Micro Black Hole. For ten seconds it drags creatures within its radius and swings them
 * around its centre, then swallows them and collapses: everything close to the centre takes armour-piercing damage.
 * <p>
 * 吸引は重力に似せ、端ほど弱く、中心に近づくほど急に強い（中心のすぐそばだけはなだらか）。範囲内の生物は無重力になり、
 * 端からゆっくり引き寄せられて中心のそばで速さが乗り、回り込んで離れていく（スイングバイ）。振り回す分の速度と勢い（エネルギー）は
 * 生物ごとに覚えておき、速さは勢いから決める（1 tick ずつの計算のずれで中心を通るたびに勢いが減らないように）。中心のそばを
 * 回り込むたびに速さと向きがランダムに少し変わり、端から落ちてきて勢いの付いた生物は、そのまま範囲の外へ投げ飛ばされることがある。
 * 勢いの小さい生物には、届く距離の {@link #SWING_FLOOR} まで振れるまで少しずつ勢いを足す。最後の {@link #SWALLOW_TICKS} tick で
 * 中心に呑み込んでから崩壊する。
 * <p>
 * 魔法式オブジェクト（目印型、常駐魔法式ではない）。耐久値を持ち（仮に固有値 {@link #BASE_INTEGRITY}、
 * ほかの魔法式オブジェクトと同じく置いた時点の事象干渉力と設定の倍率を掛ける）、時間では減らない。崩壊する前に耐久値が尽きると
 * （術式解体など）、何も押し潰さずに消える。ふつうの攻撃では壊れない・保存しない・術者がいなくなると消える・Psi の作動式では
 * 動かせないのは、魔法式オブジェクト共通。
 */
public class EntityMicroBlackHole extends SpellGramMarker {
    public static final int CHARGE_TICKS = 200;
    private static final int LINGER_TICKS = 4;
    /** 耐久値の固有値（仮）。 */
    public static final float BASE_INTEGRITY = 150.0F;
    /** 崩壊の直前に中心へ呑み込む時間（tick）。この間は振り回さず、吸引を {@link #SWALLOW_PULL} 倍にして速度を落とす。 */
    public static final int SWALLOW_TICKS = 30;
    private static final double SWALLOW_PULL = 3.0;
    private static final double SWALLOW_KEEP = 0.85;
    /** 端で止まっていた生物が中心まで落ちてきたときの速さ（ブロック/tick）。吸引の強さはここから決める。 */
    private static final double CENTRE_SPEED = 2.3;
    /** 吸引が中心のすぐそばでなだらかになる距離（ブロック）。この内側では引きが弱まる。 */
    private static final double SOFTENING = 2.0;
    /** 中心を回り込むときに最も近づく距離（ブロック）。 */
    private static final double PERICENTRE = 1.5;
    /** 回り込みの勢い（角運動量）を目標へ寄せる速さと、横向きの力の上限（吸引に対する割合）。 */
    private static final double STEER_RATE = 0.15;
    private static final double MAX_STEER = 0.6;
    /** 勢いの下限。どの生物も、中心から届く距離のこの割合までは振れる勢いで振り回す。 */
    public static final double SWING_FLOOR = 0.3;
    /** 勢いが下限に足りないとき、1 tick ごとに足す量（中心での深さに対する割合）。 */
    private static final double PUMP_RATE = 0.02;
    /** 中心からこの距離（ブロック）より内側を回り込んだとき、スイングバイで速さと向きが変わる。 */
    private static final double SLINGSHOT_RANGE = 4.0;
    /** スイングバイで速さに掛かる倍率の、1 からのずれの幅。2 回目からは {@link #SLINGSHOT_FADE} 倍ずつ狭まる。 */
    public static final double SLINGSHOT_MIN = -0.25;
    public static final double SLINGSHOT_MAX = 0.4;
    private static final double SLINGSHOT_FADE = 0.3;
    /** スイングバイで向きがずれる最大の角度（ラジアン）。 */
    private static final double SLINGSHOT_TURN = 0.6;
    /** 範囲の外へ投げ飛ばすときの跳ね上げ（水平の速さに対する割合と上限）。{@link #MIN_FLING_SPEED} より遅ければ跳ね上げない。 */
    private static final double FLING_LIFT = 0.35;
    private static final double MAX_FLING_LIFT = 0.6;
    private static final double MIN_FLING_SPEED = 0.3;
    /** 折り返すところでも、振り回す分の速さをこれより下げない（向きを失わないように）。 */
    private static final double MIN_SWING_SPEED = 0.02;
    /** バニラの空中の空気抵抗（1 tick 後に残る速度の割合）。振り回す分の速度ではこれを打ち消す。 */
    private static final double VANILLA_AIR_KEEP = 0.91;
    /** 縦の揺れを毎 tick この割合だけ抑え、ほぼ水平な輪で回らせる。 */
    private static final double VERTICAL_DAMPING = 0.15;
    /** 範囲内の生物の最高速（ブロック/tick）。 */
    private static final double MAX_SPEED = 2.6;
    /** 地面にいる生物を浮かせる上向きの速度。地面の摩擦で止まらないように、空中を引きずる。 */
    private static final double GROUND_LIFT = 0.12;
    /** 穴の下にある地面を探す深さ（ブロック）。地面に近い穴では、生物の体を地面から {@link #HOVER} だけ浮いた高さで回す。 */
    private static final double FLOOR_SCAN = 4.0;
    private static final double HOVER = 0.3;
    private static final double MIN_COLLAPSE_RADIUS = 3.0;
    private static final double COLLAPSE_RADIUS_FACTOR = 0.3;
    private static final double EDGE_DAMAGE_FACTOR = 0.5;
    /** 耐久値が尽きて消えるときのガラスの破壊音。 */
    private static final float SHATTER_VOLUME = 1.5F;
    private static final float SHATTER_PITCH = 0.7F;

    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(EntityMicroBlackHole.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(EntityMicroBlackHole.class, EntityDataSerializers.INT);

    /** 崩壊のダメージ（サーバー側）。 */
    private float damage;
    /** 中心を回り込む向き（1 か -1）。穴ごとにランダム。 */
    private final int spin = random.nextBoolean() ? 1 : -1;
    /** 振り回す分の水平の速度と勢い（サーバー側、エンティティ ID ごと）。生物が自分で歩く分やほかの攻撃で飛ばされた分とは分けて持つ。 */
    private final Int2ObjectOpenHashMap<Swing> swings = new Int2ObjectOpenHashMap<>();

    public EntityMicroBlackHole(EntityType<? extends EntityMicroBlackHole> type, Level level) {
        super(type, level);
    }

    public EntityMicroBlackHole(Level level, Vec3 center, float radius) {
        this(PsitweaksEntities.MICRO_BLACK_HOLE.get(), level);
        setPos(center.x, center.y, center.z);
        entityData.set(RADIUS, radius);
        setLifetimeTicks(CHARGE_TICKS + LINGER_TICKS + 1);
    }

    /**
     * 置く前に呼ぶ。術者と攻撃の設定、崩壊のダメージ、光の色。耐久値は {@link #BASE_INTEGRITY}（{@link SpellGramIntegrity#initial}）
     * で、時間では減らない。
     */
    public void configure(LivingEntity caster, AttackRules.Options options, float damage, int color) {
        configureMarker(caster, options, SpellGramIntegrity.initial(BASE_INTEGRITY, caster, 1.0D));
        setTimeShare(0.0F);
        this.damage = damage;
        entityData.set(COLOR, color & 0xFFFFFF);
    }

    @Override
    protected void defineSpellGramSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 6.0F);
        builder.define(COLOR, CombatSpellHelper.DEFAULT_SPELL_COLOR);
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    /** 崩壊の直前の、中心へ呑み込んでいる間か。 */
    public static boolean isSwallowing(int tick) {
        return tick >= CHARGE_TICKS - SWALLOW_TICKS;
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    protected float getDamage() {
        return damage;
    }

    /** 0..1 over the pull phase. */
    public float getCharge(float partialTick) {
        return Math.min(1.0F, (tickCount + partialTick) / CHARGE_TICKS);
    }

    public double getCollapseRadius() {
        return Math.max(MIN_COLLAPSE_RADIUS, getRadius() * COLLAPSE_RADIUS_FACTOR);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(getRadius());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && tickCount < CHARGE_TICKS) {
            inflowParticles();
        }
    }

    @Override
    protected void onSpellGramTickServer() {
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity caster = getCaster();
        if (tickCount < CHARGE_TICKS) {
            pull(level, caster);
            if (tickCount % 20 == 1) {
                CombatEffects.sound(level, position(), SoundEvents.PORTAL_AMBIENT, 1.0F, 0.6F + getCharge(0.0F));
            }
        } else if (tickCount == CHARGE_TICKS) {
            collapse(level, caster);
        }
    }

    private void pull(ServerLevel level, LivingEntity caster) {
        double reach = getRadius();
        Vec3 center = position();
        int tick = tickCount;
        boolean swallowing = isSwallowing(tick);
        // 呑み込むときは地面の上に浮かせず、本当の中心へ引く。
        double floor = swallowing ? Double.NEGATIVE_INFINITY : floorBelow(level, center);
        Int2ObjectOpenHashMap<Swing> pulled = new Int2ObjectOpenHashMap<>();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(reach))) {
            if (!canDamageEntity(caster, living) || AttackRules.isSpellImmune(living)) {
                continue;
            }
            Vec3 body = CombatSpellHelper.center(living);
            double give = Mth.clamp(1.0 - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            if (body.distanceToSqr(center) > reach * reach || give <= 0.0) {
                continue;
            }
            double aimY = Math.max(center.y, floor + living.getBbHeight() * 0.5 + HOVER);
            Vec3 offset = new Vec3(center.x - body.x, aimY - body.y, center.z - body.z);
            Vec3 velocity = living.getDeltaMovement();
            double distance = offset.length();
            // 振り回す分の速度と勢い。初めてのときや、地面にいるとき・壁に当たったときは、今の速度に合わせ直す。
            Swing swing = swings.get(living.getId());
            if (swing == null || living.onGround() || living.horizontalCollision) {
                double scale = living.onGround() ? 1.0 : 1.0 / VANILLA_AIR_KEEP;
                swing = new Swing(velocity.x * scale, velocity.z * scale);
                swing.energy = energy(swing, distance, reach);
            }
            double lastX = swing.x;
            double lastZ = swing.z;
            advance(swing, offset, reach, tick, spin, random);
            double vertical = distance < 1.0E-4 ? 0.0 : offset.y * pullAcceleration(distance, reach, tick) / distance;
            double speed = Math.sqrt(swing.x * swing.x + swing.z * swing.z);
            boolean leaving = !swallowing && speed >= MIN_FLING_SPEED
                    && body.add(swing.x, 0.0, swing.z).distanceToSqr(center) > reach * reach;
            if (leaving) {
                // 範囲の外へ飛び出す生物は、少し跳ね上げて放り出す（次の tick からは重力が戻る）。
                vertical = Math.max(0.0, Math.min(MAX_FLING_LIFT, FLING_LIFT * speed) - velocity.y);
            } else if (living.onGround()) {
                vertical += GROUND_LIFT;
            } else if (!living.isInWater() && !living.isInLava()) {
                // 重力を打ち消し（次の tick の移動から効く）、縦の揺れは抑える。
                vertical += gravityToCancel(living) - velocity.y * VERTICAL_DAMPING;
            }
            // 振り回す分の速度は、空気抵抗で減る分を足し戻してから次の値にする。
            Vec3 change = new Vec3(swing.x - VANILLA_AIR_KEEP * lastX, vertical, swing.z - VANILLA_AIR_KEEP * lastZ).scale(give);
            Vec3 moved = velocity.add(change);
            swing.x = VANILLA_AIR_KEEP * lastX + change.x;
            swing.z = VANILLA_AIR_KEEP * lastZ + change.z;
            if (moved.lengthSqr() > MAX_SPEED * MAX_SPEED) {
                double slow = MAX_SPEED / moved.length();
                moved = moved.scale(slow);
                swing.x *= slow;
                swing.z *= slow;
            }
            // 勢いは、実際に動かす速度と次の tick の位置から決め直す（ノックバック耐性や最高速で減った分、スイングバイで変わった分）。
            swing.energy = energy(swing, nextDistance(offset, swing.x, swing.z), reach);
            living.setDeltaMovement(moved);
            living.hasImpulse = true;
            living.hurtMarked = true;
            living.fallDistance = 0.0F;
            pulled.put(living.getId(), swing);
        }
        swings.clear();
        swings.putAll(pulled);
    }

    /**
     * 振り回す分の水平の速度を 1 tick 進める。{@code offset} は体の中心から狙う点（穴の中心）まで。吸引と、中心のまわりの勢い
     * （角運動量）を {@link #PERICENTRE} の近さで回り込む値に寄せる横向きの力で向きを変え、速さは勢いと次の tick の位置から決める。
     * 中心から {@link #SLINGSHOT_RANGE} より内側でいちばん近づいたら、スイングバイで速さと向きをランダムに変える。
     * 呑み込むときは勢いを使わず、吸引を強めて速度を落とす。
     */
    private static void advance(Swing swing, Vec3 offset, double reach, int tick, int spin, RandomSource random) {
        double distance = offset.length();
        double pull = pullAcceleration(distance, reach, tick);
        double ax = 0.0;
        double az = 0.0;
        if (distance > 1.0E-4) {
            ax = offset.x * pull / distance;
            az = offset.z * pull / distance;
        }
        if (isSwallowing(tick)) {
            swing.x = swing.x * SWALLOW_KEEP + ax;
            swing.z = swing.z * SWALLOW_KEEP + az;
            return;
        }
        double x = swing.x + ax;
        double z = swing.z + az;
        double rx = -offset.x;
        double rz = -offset.z;
        double horizontal = Math.sqrt(rx * rx + rz * rz);
        if (horizontal > 1.0E-4) {
            double angular = rx * swing.z - rz * swing.x;
            double target = spin * PERICENTRE
                    * Math.sqrt(Math.max(0.0, 2.0 * (depth(PERICENTRE, reach) + Math.min(swing.energy, 0.0))));
            double steer = Mth.clamp(STEER_RATE * (target - angular) / horizontal, -MAX_STEER * pull, MAX_STEER * pull);
            x -= rz / horizontal * steer;
            z += rx / horizontal * steer;
        }
        double lowest = -depth(SWING_FLOOR * reach, reach);
        if (swing.energy < lowest) {
            swing.energy = Math.min(lowest, swing.energy + PUMP_RATE * depth(0.0, reach));
        }
        double speed = Math.sqrt(x * x + z * z);
        double wanted = Math.max(MIN_SWING_SPEED,
                Math.sqrt(Math.max(0.0, 2.0 * (swing.energy + depth(nextDistance(offset, x, z), reach)))));
        if (speed > 1.0E-6) {
            x *= wanted / speed;
            z *= wanted / speed;
        } else {
            double angle = random.nextDouble() * Math.PI * 2.0;
            x = Math.cos(angle) * wanted;
            z = Math.sin(angle) * wanted;
        }
        boolean approaching = (offset.x - x) * x + (offset.z - z) * z > 0.0;
        if (swing.approaching && !approaching && nextDistance(offset, x, z) < SLINGSHOT_RANGE) {
            double factor = 1.0 + Math.pow(SLINGSHOT_FADE, swing.passes++)
                    * Mth.lerp(random.nextDouble(), SLINGSHOT_MIN, SLINGSHOT_MAX);
            double turn = (random.nextDouble() * 2.0 - 1.0) * SLINGSHOT_TURN;
            double cos = Math.cos(turn) * factor;
            double sin = Math.sin(turn) * factor;
            double turnedX = x * cos - z * sin;
            z = x * sin + z * cos;
            x = turnedX;
        }
        swing.approaching = approaching;
        swing.x = x;
        swing.z = z;
    }

    /** 振り回す分の速度で 1 tick 動いたあとの、体の中心から狙う点までの距離。 */
    private static double nextDistance(Vec3 offset, double x, double z) {
        double dx = offset.x - x;
        double dz = offset.z - z;
        return Math.sqrt(dx * dx + offset.y * offset.y + dz * dz);
    }

    /** 中心から {@code distance} の位置で振り回す分の速度を持つ生物の勢い。0 以上なら、届く距離の外まで出ていく。 */
    private static double energy(Swing swing, double distance, double reach) {
        return 0.5 * (swing.x * swing.x + swing.z * swing.z) - depth(distance, reach);
    }

    /**
     * 中心から {@code distance} ブロックでの吸引の加速度（ブロック/tick²）。{@code reach} は届く距離、{@code tick} は置いてからの tick。
     * 遠くでは距離の 1.5 乗に反比例して端ほど弱く（端は届く距離の半分のところの 4 割弱）、中心のすぐそばではなだらかに 0 へ下がる。
     * 呑み込むときは {@link #SWALLOW_PULL} 倍。
     */
    public static double pullAcceleration(double distance, double reach, int tick) {
        double pull = strength(reach) * distance / Math.pow(distance * distance + SOFTENING * SOFTENING, 1.25);
        return isSwallowing(tick) ? pull * SWALLOW_PULL : pull;
    }

    /** 吸引の強さの係数。端で止まっていた生物が中心まで落ちると {@link #CENTRE_SPEED} になるように決める。 */
    private static double strength(double reach) {
        return CENTRE_SPEED * CENTRE_SPEED
                / (4.0 * (1.0 / Math.sqrt(SOFTENING) - Math.pow(reach * reach + SOFTENING * SOFTENING, -0.25)));
    }

    /**
     * 中心から {@code distance} の位置から届く距離 {@code reach} の端まで、吸引に逆らって登るのに要る仕事（質量あたり）。
     * 端で止まっていた生物がここまで落ちてきたときの速さは、この 2 倍の平方根。
     */
    public static double depth(double distance, double reach) {
        double clamped = Math.min(distance, reach);
        return 2.0 * strength(reach) * (Math.pow(clamped * clamped + SOFTENING * SOFTENING, -0.25)
                - Math.pow(reach * reach + SOFTENING * SOFTENING, -0.25));
    }

    /** 範囲内を無重力にするために、次の tick に効く重力の分だけ上へ足す速度。浮いている生物や浮遊中の生物には足さない。 */
    private static double gravityToCancel(LivingEntity living) {
        if (living instanceof FlyingMob || living.hasEffect(MobEffects.LEVITATION)) {
            return 0.0;
        }
        double gravity = living.getGravity();
        if (living.getDeltaMovement().y <= 0.0 && living.hasEffect(MobEffects.SLOW_FALLING)) {
            gravity = Math.min(gravity, 0.01);
        }
        return gravity * 0.98;
    }

    /** 穴の真下 {@link #FLOOR_SCAN} ブロック以内にある地面の高さ。なければ負の無限大。 */
    private double floorBelow(Level level, Vec3 center) {
        HitResult hit = level.clip(new ClipContext(center, center.subtract(0.0, FLOOR_SCAN, 0.0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.MISS ? Double.NEGATIVE_INFINITY : hit.getLocation().y;
    }

    private void collapse(ServerLevel level, LivingEntity caster) {
        double collapseRadius = getCollapseRadius();
        Vec3 center = position();
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.MICRO_BLACK_HOLE, this, caster);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(collapseRadius + 1.0))) {
            if (!canDamageEntity(caster, living)) {
                continue;
            }
            double distance = Math.max(0.0, CombatSpellHelper.center(living).distanceTo(center) - living.getBbWidth() * 0.5);
            if (distance <= collapseRadius) {
                AttackRules.harm(caster, living, attackOptions(), source,
                        CombatSpellHelper.falloff(getDamage(), distance, collapseRadius, EDGE_DAMAGE_FACTOR));
            }
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 80, 0.3, 0.3, 0.3, 0.6);
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 2, 0.4, 0.4, 0.4, 0.0);
        level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        CombatEffects.sphereFlash(level, center, getRadius(), getColor(), MessageSphereFlash.Style.IMPLOSION, 1.0F);
        CombatEffects.sound(level, center, SoundEvents.GENERIC_EXPLODE, 1.3F, 0.6F);
        CombatEffects.sound(level, center, SoundEvents.RESPAWN_ANCHOR_DEPLETE, 1.0F, 0.7F);
    }

    /** 耐久値が尽きて消えるとき、ガラスが割れる音を鳴らす。押し潰しはしない。 */
    @Override
    protected void onEnd(EndReason reason) {
        if (reason == EndReason.DESTROYED && level() instanceof ServerLevel level) {
            CombatEffects.sound(level, position(), SoundEvents.GLASS_BREAK, SHATTER_VOLUME, SHATTER_PITCH);
        }
    }

    private void inflowParticles() {
        double radius = getRadius();
        // More particles for wider wells, so a large one does not look empty.
        int count = 4 + (int) (radius / 3.0);
        for (int i = 0; i < count; i++) {
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos(random.nextDouble() * 2.0 - 1.0);
            double distance = radius * (0.5 + random.nextDouble() * 0.5);
            double dx = Math.sin(phi) * Math.cos(theta) * distance;
            double dy = Math.cos(phi) * distance * 0.6;
            double dz = Math.sin(phi) * Math.sin(theta) * distance;
            // Portal particles drift from the given offset back to their origin: here, into the centre.
            level().addParticle(ParticleTypes.PORTAL, getX(), getY(), getZ(), dx, dy, dz);
        }
    }

    /** 振り回している生物 1 体分の、振り回す分の水平の速度と勢い。 */
    private static final class Swing {
        private double x;
        private double z;
        /** 勢い（質量あたり）。速さの 2 乗の半分から、今の位置から届く距離の端まで登るのに要る仕事を引いたもの。 */
        private double energy;
        /** 中心へ近づいているところか。近づくのをやめた tick に、いちばん近づいたとみなす。 */
        private boolean approaching = true;
        /** スイングバイした回数。 */
        private int passes;

        private Swing(double x, double z) {
            this.x = x;
            this.z = z;
        }
    }
}
