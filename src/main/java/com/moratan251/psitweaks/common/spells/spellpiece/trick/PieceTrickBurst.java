package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.particles.PsitweaksParticles;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Vaporises the liquid inside the target so it bursts from within: armour-piercing damage, more against a wet creature
 * ({@link LivingEntity#isInWaterOrRain}). A creature killed by it bursts and hits hostile creatures nearby. Bosses and
 * riders are fair targets ({@link AttackRules#verifyTarget}); given a part of a multipart creature such as the Ender
 * Dragon's head, it bursts that part.
 */
public class PieceTrickBurst extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 36.0F;
    private static final float DAMAGE_PER_POWER = 30.0F;
    /** Damage factor against a wet creature (in water or rain, or soaked by Trick: Aqua Cutter), as Chain Lightning's. */
    private static final float WET_FACTOR = 1.25F;
    private static final double SPLASH_RADIUS = 3.0;
    private static final float SPLASH_FACTOR = 0.4F;
    private static final double POTENCY_BASE = 1180.0;
    private static final double POTENCY_PER_POWER = 60.0;
    private static final double COST_BASE = 400.0;
    private static final double COST_PER_POWER = 300.0;
    /** The smoke of a killed creature: dark red and near black, for the tinted cloud particle. */
    private static final float[] BLOOD_SMOKE = {0.45F, 0.03F, 0.03F};
    private static final float[] BLACK_SMOKE = {0.08F, 0.02F, 0.02F};

    private SpellParam<Entity> target;
    private SpellParam<Number> power;

    public PieceTrickBurst(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        double powerVal = Math.max(MIN_POWER, CombatTrickStats.positive(getParamEvaluation(power), x, y));
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + powerVal * POTENCY_PER_POWER));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(COST_BASE + powerVal * COST_PER_POWER));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = getParamValue(context, target);
        Entity creature = AttackRules.wholeEntity(targetVal);
        if (AttackRules.skipFallenTarget(context, creature)) {
            return null;
        }
        AttackRules.verifyTarget(context, creature);
        if (!(creature instanceof LivingEntity living)) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (!context.isInRadius(targetVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        LivingEntity caster = context.caster;
        AttackRules.Options options = AttackRules.Options.from(context);
        if (!AttackRules.canHarm(caster, living, options)) {
            return null;
        }

        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        float damage = AttackRules.scale(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.burstDamageMultiplier.get(), caster);
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.BURST, caster, caster);
        Vec3 center = CombatSpellHelper.center(targetVal);
        float size = Math.max(targetVal.getBbWidth(), targetVal.getBbHeight());
        int color = CombatSpellHelper.spellColor(caster);

        boolean wasAlive = living.isAlive();
        // A part takes the hit itself, so the Ender Dragon's head takes it in full as from any other attack.
        AttackRules.harm(caster, targetVal, options, source, damage * damageFactor(living));
        burstEffects(level, center, size, color, false);

        if (wasAlive && !living.isAlive()) {
            splash(level, caster, living, center, damage * SPLASH_FACTOR, options, source);
            burstEffects(level, center, size, color, true);
        }
        return null;
    }

    private static ColorParticleOption smoke(float[] color) {
        return ColorParticleOption.create(PsitweaksParticles.TINTED_CLOUD.get(), color[0], color[1], color[2]);
    }

    private static float damageFactor(LivingEntity living) {
        return living.isInWaterOrRain() ? WET_FACTOR : 1.0F;
    }

    private static void splash(ServerLevel level, LivingEntity caster, LivingEntity origin, Vec3 center, float damage,
                               AttackRules.Options options, DamageSource source) {
        AABB area = new AABB(center, center).inflate(SPLASH_RADIUS);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (nearby == origin || !AttackRules.isHostileTarget(caster, nearby, options)) {
                continue;
            }
            double distance = CombatSpellHelper.center(nearby).distanceTo(center);
            if (distance <= SPLASH_RADIUS) {
                AttackRules.harm(caster, nearby, options, source, CombatSpellHelper.falloff(damage * damageFactor(nearby), distance, SPLASH_RADIUS, 0.5));
            }
        }
    }

    private static void burstEffects(ServerLevel level, Vec3 center, float size, int color, boolean killed) {
        double spread = size * 0.35;
        if (killed) {
            level.sendParticles(smoke(BLOOD_SMOKE), center.x, center.y, center.z, 20, spread, spread, spread, 0.15);
            level.sendParticles(smoke(BLACK_SMOKE), center.x, center.y, center.z, 12, spread, spread, spread, 0.1);
            CombatEffects.sphereFlash(level, center, (float) SPLASH_RADIUS, color, MessageSphereFlash.Style.SHOCKWAVE, 1.2F);
            CombatEffects.sound(level, center, SoundEvents.GENERIC_EXPLODE, 0.7F, 1.4F);
        } else {
            level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 14, spread, spread, spread, 0.08);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, center.x, center.y, center.z, 6, spread, spread, spread, 0.2);
            CombatEffects.sphereFlash(level, center, size * 0.9F + 0.4F, color, MessageSphereFlash.Style.SHOCKWAVE, 0.8F);
            CombatEffects.sound(level, center, SoundEvents.FIREWORK_ROCKET_BLAST, 1.0F, 0.7F);
            CombatEffects.sound(level, center, SoundEvents.FIRE_EXTINGUISH, 0.6F, 1.3F);
        }
    }
}
