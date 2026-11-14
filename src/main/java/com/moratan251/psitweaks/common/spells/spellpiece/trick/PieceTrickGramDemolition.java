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
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
 * 作動式: 術式解体（Gram Demolition、2026-11-14 ユーザー）。入力なし。術者の目から視線の方向へ {@link #RANGE} ブロック、
 * 半角 {@link #HALF_ANGLE_DEGREES} 度の円錐（ブロックは貫通する）の中にある魔法式オブジェクトの核すべての耐久値を
 * {@link #INTEGRITY_DAMAGE} 減らし（× 術者の事象干渉力）、同じ範囲の生物に {@link #DAMAGE} のダメージ（全体倍率・個別倍率・
 * 事象干渉力を掛ける）を与える。術者自身の魔法式オブジェクトは削らない。
 */
public class PieceTrickGramDemolition extends PieceTrick {
    private static final int POTENCY = 750;
    private static final int COST = 7500;
    public static final double RANGE = 16.0;
    public static final double HALF_ANGLE_DEGREES = 30.0;
    public static final float INTEGRITY_DAMAGE = 300.0F;
    public static final float DAMAGE = 6.0F;
    /** 円錐の中に散らす光の粒の数。 */
    private static final int WAVE_PARTICLES = 48;

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
            Vec3 core = object.position();
            SpellGramIntegrity.reduce(object, INTEGRITY_DAMAGE, caster);
            CombatEffects.sphereFlash(level, core, 1.6F, CombatSpellHelper.spellColor(caster),
                    MessageSphereFlash.Style.SHOCKWAVE, 0.8F);
            level.sendParticles(ParticleTypes.ENCHANT, core.x, core.y, core.z, 30, 0.6, 0.6, 0.6, 1.0);
        }

        AttackRules.Options options = AttackRules.Options.from(context);
        float damage = AttackRules.scale(DAMAGE, PsitweaksConfig.COMMON.gramDemolitionDamageMultiplier.get(), caster);
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.GRAM_DEMOLITION, caster, caster);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, eye).inflate(RANGE + 2.0),
                living -> living != caster && inCone(eye, look, CombatSpellHelper.center(living), living.getBbWidth() * 0.5))) {
            AttackRules.harm(caster, living, options, source, damage);
        }

        wave(level, eye, look);
        CombatEffects.sound(level, eye, SoundEvents.EVOKER_CAST_SPELL, 1.2F, 1.3F);
        CombatEffects.sound(level, eye, SoundEvents.BEACON_DEACTIVATE, 1.0F, 1.8F);
        return null;
    }

    /** 円錐の中にある、術者自身のものでない魔法式オブジェクト（核の位置で比べる）。 */
    public static List<SpellGramObject> targets(ServerLevel level, @Nullable LivingEntity caster, Vec3 eye, Vec3 look) {
        UUID own = caster != null ? caster.getUUID() : null;
        List<SpellGramObject> result = new ArrayList<>();
        for (SpellGramObject object : level.getEntitiesOfClass(SpellGramObject.class,
                new AABB(eye, eye).inflate(RANGE + SpellGramAim.CORE_HALF))) {
            if (object.isEnded() || (own != null && own.equals(object.getCasterUuid()))) {
                continue;
            }
            if (inCone(eye, look, object.position(), SpellGramAim.CORE_HALF)) {
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

    /** 円錐の中に、前へ流れる光の粒を散らす。 */
    private static void wave(ServerLevel level, Vec3 eye, Vec3 look) {
        RandomSource random = level.getRandom();
        Vec3 helper = Math.abs(look.y) < 0.95 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 right = look.cross(helper).normalize();
        Vec3 up = right.cross(look).normalize();
        double tan = Math.tan(Math.toRadians(HALF_ANGLE_DEGREES));
        for (int i = 0; i < WAVE_PARTICLES; i++) {
            double along = 1.0 + random.nextDouble() * (RANGE - 1.0);
            double radius = Math.sqrt(random.nextDouble()) * along * tan;
            double angle = random.nextDouble() * Mth.TWO_PI;
            Vec3 at = eye.add(look.scale(along)).add(right.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius));
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 0, look.x, look.y, look.z, 0.15);
        }
    }
}
