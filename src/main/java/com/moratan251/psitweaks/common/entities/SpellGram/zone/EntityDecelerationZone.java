package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.mixin.AbstractArrowAccessor;
import com.moratan251.psitweaks.mixin.FireworkRocketEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Locale;

/**
 * 作動式: 減速領域（Deceleration Zone）の範囲。
 * <ul>
 *   <li>中の生き物（攻撃術式の共通ルールで選ぶ、{@link #followsAttackRules}）に鈍化（{@code EffectTorpor}）をかけ続ける。
 *   移動速度と飛ぶ速さは 中の威力 × 5%、プレイヤーの攻撃速度は 中の威力 × 3% 下がる。出ると {@link #EFFECT_TICKS} で切れる。</li>
 *   <li>プレイヤー以外の生き物は、毎 tick 速さも (1 − 中の威力 × 0.025) 倍にする（属性を見ずに動く mob のため）。飛ぶもの以外は
 *   横の速さだけ（跳ぶ・落ちるのは変えない）。</li>
 *   <li>飛び道具は、毎 tick 速さを (1 − 中の威力 × 0.04) 倍にする（{@link #slowsProjectile}）。</li>
 *   <li>タグ {@code #c:bosses} には効かない。</li>
 * </ul>
 * 同じ種類が重なったところでは、威力の一番高いものだけが効く。加速領域とは打ち消し合わない。
 */
public class EntityDecelerationZone extends SpellGramZone {
    /** 辺の色: 藍に寄せた深い青。 */
    public static final int OUTLINE_COLOR = 0x3A5FCD;
    /** 粒の 3 つに 1 つを明るい青（水色のコンクリートパウダーの砂ぼこり）にする。残りは青。 */
    private static final int LIGHT_DUST_EVERY = 3;
    /** 威力の既定値（置く前に設定しなかったとき、中の威力）。 */
    public static final float DEFAULT_POWER = 5.0F;
    /** 鈍化をかけ直す間隔の目安（tick）: 残りがこれより短くなったらかけ直す。 */
    public static final int REFRESH_INTERVAL = 10;
    /** 鈍化をかける長さ（tick）。範囲を出てから切れるまで。 */
    public static final int EFFECT_TICKS = 15;
    /** 中の威力 1 あたりの、移動速度・飛ぶ速さの下げ幅。 */
    public static final double MOVEMENT_SLOW_PER_POWER = 0.05;
    /** 中の威力 1 あたりの、プレイヤーの攻撃速度の下げ幅。 */
    public static final double ATTACK_SLOW_PER_POWER = 0.03;
    /** 中の威力 1 あたりの、プレイヤー以外の生き物の速さの 1 tick あたりの下げ幅。 */
    public static final double MOB_DRAG_PER_POWER = 0.025;
    /** 中の威力 1 あたりの、飛び道具の速さの 1 tick あたりの下げ幅。 */
    public static final double PROJECTILE_DRAG_PER_POWER = 0.04;
    private static final int MAX_DUST_PER_TICK = 4;

    /** 中の威力（作動式に入れた威力 + 4、5〜14）。 */
    private float power = DEFAULT_POWER;

    public EntityDecelerationZone(EntityType<? extends EntityDecelerationZone> type, Level level) {
        super(type, level);
    }

    public EntityDecelerationZone(Level level) {
        this(PsitweaksEntities.DECELERATION_ZONE.get(), level);
    }

    /** 置く前に呼ぶ。{@code power} は中の威力（作動式に入れた威力 + 4）。 */
    public void setPower(float power) {
        this.power = Math.max(0.0F, Math.min(power, MAX_EFFECT_POWER));
        SpellGramIndex.touch();
    }

    public float getPower() {
        return power;
    }

    /** 中の威力 {@code power} でかける鈍化の増幅値: レベル（増幅値 + 1）= 中の威力 × 5（移動速度はレベル 1 で 1% 下がる）。 */
    public static int amplifierFor(float power) {
        return Math.max(0, Math.round(Math.max(0.0F, power) * 5.0F) - 1);
    }

    /** プレイヤー以外の生き物の速さに毎 tick 掛ける数: 1 − 中の威力 × 0.025。 */
    public static double mobDragFor(float power) {
        return Math.max(0.0, 1.0 - Math.max(0.0F, power) * MOB_DRAG_PER_POWER);
    }

    /** 飛び道具の速さに毎 tick 掛ける数: 1 − 中の威力 × 0.04。 */
    public static double projectileDragFor(float power) {
        return Math.max(0.0, 1.0 - Math.max(0.0F, power) * PROJECTILE_DRAG_PER_POWER);
    }

    /** ボス（タグ {@code #c:bosses}）には、鈍化も速さの引き下げもかけない。 */
    public static boolean isExempt(Entity entity) {
        return entity.getType().is(Tags.EntityTypes.BOSSES);
    }

    /** 同じ種類が重なったところでは、威力の一番高いものだけが効く。 */
    @Override
    protected double stackStrength() {
        return power;
    }

    @Override
    protected boolean followsAttackRules() {
        return true;
    }

    /** 毎 tick（鈍化はかけ直しの間隔ごと、速さの引き下げは毎 tick）。 */
    @Override
    protected int effectInterval() {
        return 1;
    }

    @Override
    protected boolean affects(Entity entity) {
        return entity instanceof LivingEntity living && living.isAlive() && !isExempt(living);
    }

    @Override
    protected void affect(Entity target) {
        if (!(target instanceof LivingEntity living)) {
            return;
        }
        int amplifier = amplifierFor(power);
        MobEffectInstance current = living.getEffect(PsitweaksEffects.TORPOR);
        if (current == null || current.getAmplifier() < amplifier
                || (current.getAmplifier() == amplifier && current.getDuration() <= EFFECT_TICKS - REFRESH_INTERVAL)) {
            living.addEffect(new MobEffectInstance(PsitweaksEffects.TORPOR, EFFECT_TICKS, amplifier, true, false, true));
        }
        if (!(living instanceof Player)) {
            double drag = mobDragFor(power);
            Vec3 velocity = living.getDeltaMovement();
            living.setDeltaMovement(velocity.x * drag, fliesFreely(living) ? velocity.y * drag : velocity.y, velocity.z * drag);
        }
    }

    /** 重さに引かれずに動く生き物か（縦の速さも下げる）: 重力のないもの・空を飛ぶ mob・飛ぶ道案内を持つもの。 */
    private static boolean fliesFreely(LivingEntity living) {
        return living.isNoGravity() || living instanceof FlyingMob || living instanceof FlyingAnimal
                || (living instanceof Mob mob && mob.getNavigation() instanceof FlyingPathNavigation);
    }

    @Override
    protected void onSpellGramTickServer() {
        super.onSpellGramTickServer();
        if (isEnded() || getArea().isEmpty() || !(level() instanceof ServerLevel level)) {
            return;
        }
        double drag = projectileDragFor(power);
        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, getArea().bounds(), projectile -> {
            if (!projectile.isAlive() || !slowsProjectile(projectile)) {
                return false;
            }
            BlockPos pos = BlockPos.containing(projectile.getBoundingBox().getCenter());
            return isInsideAndActive(pos) && isStrongestAt(pos);
        });
        for (Projectile projectile : projectiles) {
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(drag));
            projectile.hasImpulse = true;
        }
    }

    /**
     * 飛び道具 {@code projectile} を遅くするか。遅くしないもの: 魔法の弾（{@link ZoneProjectiles}）、持ち主が攻撃術式の共通ルールで
     * 外れるもの（術者の側と、PvP が切ってあるときのほかのプレイヤーのもの）、地面に刺さった矢、生き物についているロケット花火、
     * 釣りの浮き。落ちているアイテム・経験値・トロッコ・ボート・機体は飛び道具ではないので触らない。
     */
    public boolean slowsProjectile(Projectile projectile) {
        if (ZoneProjectiles.isSpellProjectile(projectile) || projectile instanceof FishingHook) {
            return false;
        }
        if (projectile instanceof AbstractArrow arrow && ((AbstractArrowAccessor) arrow).psitweaks$isInGround()) {
            return false;
        }
        if (projectile instanceof FireworkRocketEntity firework
                && ((FireworkRocketEntityAccessor) firework).psitweaks$isAttachedToEntity()) {
            return false;
        }
        return !(projectile.getOwner() instanceof LivingEntity owner) || canDamageEntity(getCaster(), owner);
    }

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
    }

    /** 照準の表示: 移動速度の下げ幅と、飛び道具の速さに毎 tick 掛ける数。 */
    @Override
    public List<Component> hudLines() {
        return List.of(Component.translatable("psitweaks.hud.zone_deceleration",
                hudNumber(power * MOVEMENT_SLOW_PER_POWER * 100.0),
                String.format(Locale.ROOT, "%.2f", projectileDragFor(power))));
    }

    @Override
    protected void writeZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(power);
    }

    @Override
    protected void readZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        power = buffer.readFloat();
    }

    /**
     * 青い砂ぼこりが、範囲の中をゆっくり落ちる。色はコンクリートパウダーの地図の色: 青（0x334CB2）を主に、3 つに 1 つを
     * 明るい青（0x6699D8）にする。
     */
    @Override
    protected void spawnClientParticles() {
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int dust = particleCount(Math.min(MAX_DUST_PER_TICK, Math.max(1.0, volume / 300.0)));
        BlockParticleOption blue = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.BLUE_CONCRETE_POWDER.defaultBlockState());
        BlockParticleOption lightBlue = new BlockParticleOption(ParticleTypes.FALLING_DUST,
                Blocks.LIGHT_BLUE_CONCRETE_POWDER.defaultBlockState());
        for (int i = 0; i < dust; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(random.nextInt(LIGHT_DUST_EVERY) == 0 ? lightBlue : blue, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
