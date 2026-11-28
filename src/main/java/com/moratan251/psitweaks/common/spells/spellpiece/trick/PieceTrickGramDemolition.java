package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramAim;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 術式解体（Gram Demolition）。入力なし。術者の目から視線の方向へ {@link #RANGE} ブロック、
 * 半角 {@link #HALF_ANGLE_DEGREES} 度の円錐（ブロックは貫通する）の中にある魔法式オブジェクトの核すべての耐久値を
 * {@link #INTEGRITY_DAMAGE} 減らし（× 術者の事象干渉力）、同じ範囲の生物に {@link #DAMAGE} のダメージ（全体倍率・個別倍率・
 * 事象干渉力を掛ける）を与える。術者自身の魔法式オブジェクト（自律詠唱機は所有者の側のものも）は削らない
 * （{@link SpellGramIntegrity#isSparedBy}）。ダメージを受けた生物は、術者の目から見た水平の向きへ、パンチ II の矢と
 * 同じだけ押し出す（{@link #KNOCKBACK}、ノックバック耐性で弱まる）。
 */
public class PieceTrickGramDemolition extends PieceTrick {
    private static final int POTENCY = 750;
    private static final int COST = 7500;
    public static final double RANGE = 16.0;
    public static final double HALF_ANGLE_DEGREES = 30.0;
    public static final float INTEGRITY_DAMAGE = 300.0F;
    public static final float DAMAGE = 6.0F;
    /**
     * ダメージを受けた生物を水平に押し出す速さ。パンチ II の矢の押し出し（エンチャントのノックバック 2 × 0.6）と同じ。
     * ダメージ自体の通常のノックバックに上乗せする。
     */
    public static final double KNOCKBACK = 1.2;
    /** 押し出すときに加える上向きの速さ。矢のノックバックと同じ。 */
    public static final double KNOCKBACK_LIFT = 0.1;

    public PieceTrickGramDemolition(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        LivingEntity caster = context.caster;
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();

        for (SpellGramObject object : targets(level, caster, eye, look)) {
            Vec3 core = object.coreBox().getCenter();
            SpellGramIntegrity.reduce(object, INTEGRITY_DAMAGE, caster);
            CombatEffects.sphereFlash(level, core, 1.6F, CombatSpellHelper.spellColor(caster),
                    MessageSphereFlash.Style.SHOCKWAVE, 0.8F);
            level.sendParticles(ParticleTypes.ENCHANT, core.x, core.y, core.z, 30, 0.6, 0.6, 0.6, 1.0);
        }

        AttackRules.Options options = AttackRules.Options.from(context);
        float damage = AttackRules.scale(DAMAGE, PsitweaksConfig.COMMON.gramDemolitionDamageMultiplier.get(), caster);
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.GRAM_DEMOLITION, caster, caster);
        for (LivingEntity living : creaturesInCone(level, caster, eye, look)) {
            if (!AttackRules.harm(caster, living, options, source, damage)) {
                continue;
            }
            Vec3 away = CombatSpellHelper.center(living).subtract(eye);
            Vec3 horizontal = new Vec3(away.x, 0.0, away.z);
            if (horizontal.lengthSqr() < 1.0E-6) {
                horizontal = new Vec3(look.x, 0.0, look.z);
            }
            if (horizontal.lengthSqr() < 1.0E-6) {
                continue;
            }
            // ノックバック耐性で弱め、プレイヤーにも速度を送る（CombatSpellHelper.push）。
            CombatSpellHelper.push(living, horizontal.normalize().scale(KNOCKBACK).add(0.0, KNOCKBACK_LIFT, 0.0));
        }

        CombatEffects.coneWave(level, eye, look, (float) RANGE, CombatSpellHelper.spellColor(caster), 1.0F);
        CombatEffects.sound(level, eye, SoundEvents.EVOKER_CAST_SPELL, 1.2F, 1.3F);
        CombatEffects.sound(level, eye, SoundEvents.BEACON_DEACTIVATE, 1.0F, 1.8F);
        return null;
    }

    /**
     * 円錐の中にいる、術者以外の生物（胴の中心で比べ、幅の半分だけ広げて見る）。誰に効かせるかは呼ぶ側が決める。
     * 作動式: コキュートスも同じ範囲を使う。
     */
    public static List<LivingEntity> creaturesInCone(ServerLevel level, @Nullable LivingEntity caster, Vec3 eye, Vec3 look) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, eye).inflate(RANGE + 2.0),
                living -> living != caster && inCone(eye, look, CombatSpellHelper.center(living), living.getBbWidth() * 0.5));
    }

    /**
     * 円錐の中にある、術者がよけない（{@link SpellGramIntegrity#isSparedBy}）魔法式オブジェクト（核の当たりの箱
     * {@link SpellGramObject#coreBox} の中心で比べる）。
     */
    public static List<SpellGramObject> targets(ServerLevel level, @Nullable LivingEntity caster, Vec3 eye, Vec3 look) {
        List<SpellGramObject> result = new ArrayList<>();
        for (SpellGramObject object : level.getEntitiesOfClass(SpellGramObject.class,
                new AABB(eye, eye).inflate(RANGE + SpellGramAim.CORE_HALF))) {
            if (object.isEnded() || SpellGramIntegrity.isSparedBy(object, caster)) {
                continue;
            }
            if (inCone(eye, look, object.coreBox().getCenter(), SpellGramAim.CORE_HALF)) {
                result.add(object);
            }
        }
        return result;
    }

    /**
     * 点 {@code point}（大きさ {@code margin} の分だけ広げて見る）が、目から視線の方向への円錐（{@link #RANGE} ブロック、
     * 半角 {@link #HALF_ANGLE_DEGREES} 度）の中か。目のすぐそば（{@code margin} 以内）なら中とみなす。
     */
    public static boolean inCone(Vec3 eye, Vec3 look, Vec3 point, double margin) {
        Vec3 offset = point.subtract(eye);
        double distance = offset.length();
        if (distance <= margin) {
            return true;
        }
        if (distance > RANGE + margin) {
            return false;
        }
        // 大きさの分だけ角度を甘くする（遠いほど少し、近いほど多く）。
        double slack = Math.asin(Math.min(1.0, margin / distance));
        double cos = offset.dot(look) / distance;
        return cos >= Math.cos(Math.min(Math.PI, Math.toRadians(HALF_ANGLE_DEGREES) + slack));
    }
}
