package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksTags;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
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
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Vaporises the liquid inside the creature under the caster's crosshair (see {@link CombatSpellHelper#aimedCreature})
 * so it bursts from within: armour-piercing damage, reduced against creatures with little liquid in them. A creature
 * killed by it bursts and hits hostile creatures nearby.
 */
public class PieceTrickBurst extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 36.0F;
    private static final float DAMAGE_PER_POWER = 30.0F;
    /** Damage factor against {@code #psitweaks:burst_resistant} creatures (skeletons, golems, blazes, ...). */
    private static final float RESISTANT_FACTOR = 0.25F;
    private static final double SPLASH_RADIUS = 3.0;
    private static final float SPLASH_FACTOR = 0.4F;
    private static final double POTENCY_BASE = 1180.0;
    private static final double POTENCY_PER_POWER = 60.0;
    private static final double COST_BASE = 600.0;
    private static final double COST_PER_POWER = 450.0;

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
        // Spells saved with the old Target input still load: Psi only reads the inputs a piece has now.
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
        LivingEntity living = CombatSpellHelper.aimedCreature(context);
        if (CombatSpellHelper.skipFallenTarget(context, living)) {
            return null;
        }
        CombatSpellHelper.verifyTarget(context, living);
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        LivingEntity caster = context.caster;
        boolean safeToPlayers = SpellSafetyUtils.hasSafeToPlayers(context);
        if (!CombatSpellHelper.canHarm(caster, living, safeToPlayers)) {
            return null;
        }

        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        float damage = CombatSpellHelper.scaleDamage(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.burstDamageMultiplier.get(), caster);
        DamageSource source = CombatSpellHelper.damageSource(level, PsitweaksDamageTypes.BURST, caster, caster);
        Vec3 center = CombatSpellHelper.center(living);
        float size = Math.max(living.getBbWidth(), living.getBbHeight());
        int color = CombatSpellHelper.spellColor(caster);

        boolean wasAlive = living.isAlive();
        living.hurt(source, damage * damageFactor(living));
        burstEffects(level, center, size, color, false);

        if (wasAlive && !living.isAlive()) {
            splash(level, caster, living, center, damage * SPLASH_FACTOR, safeToPlayers, source);
            burstEffects(level, center, size, color, true);
        }
        return null;
    }

    private static float damageFactor(LivingEntity living) {
        return living.getType().is(PsitweaksTags.EntityTypes.BURST_RESISTANT) ? RESISTANT_FACTOR : 1.0F;
    }

    private static void splash(ServerLevel level, LivingEntity caster, LivingEntity origin, Vec3 center, float damage,
                               boolean safeToPlayers, DamageSource source) {
        AABB area = new AABB(center, center).inflate(SPLASH_RADIUS);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (nearby == origin || !CombatSpellHelper.isHostileTarget(caster, nearby, safeToPlayers)) {
                continue;
            }
            double distance = CombatSpellHelper.center(nearby).distanceTo(center);
            if (distance <= SPLASH_RADIUS) {
                nearby.hurt(source, CombatSpellHelper.falloff(damage * damageFactor(nearby), distance, SPLASH_RADIUS, 0.5));
            }
        }
    }

    private static void burstEffects(ServerLevel level, Vec3 center, float size, int color, boolean killed) {
        double spread = size * 0.35;
        if (killed) {
            level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 2, spread, spread, spread, 0.0);
            level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 24, spread, spread, spread, 0.15);
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
