package com.moratan251.psitweaks.common.spells.spellpiece.trick;


import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

import java.util.List;

public class PieceTrickActiveAirMine extends PieceTrick {

    private static final double MIN_POWER = 0.1;
    private static final double MIN_RADIUS = 2.0;
    private static final double MAX_RADIUS = 10.0;
    private static final double RADIUS_PER_POWER = 1.0 ;
    private static final float BASE_DAMAGE = 7.0F;
    private static final float DAMAGE_PER_POWER = 14.0F;
    private static final double POTENCY_BASE = 200;
    private static final double POTENCY_PER_POWER = 50;
    private static final double COST_BASE = 100;
    private static final double COST_PER_POWER = 270;
    private static final double EDGE_DAMAGE_FACTOR = 0.35;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;

    public PieceTrickActiveAirMine(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY,
                new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST,
                new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = getParamEvaluation(power);
        if (powerVal == null || powerVal <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        powerVal = Math.max(MIN_POWER, powerVal);
        meta.addStat(EnumSpellStat.POTENCY, (int) (POTENCY_BASE + powerVal * POTENCY_PER_POWER));
        meta.addStat(EnumSpellStat.COST, (int) (COST_BASE + powerVal * COST_PER_POWER));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        double powerVal = getParamValue(context, power).doubleValue();

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (powerVal <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        double clampedPower = Math.max(MIN_POWER, powerVal);
        double radius = calculateRadius(clampedPower);
        float damage = calculateDamage(clampedPower);

        float finalDamage = AttackRules.scale(damage, PsitweaksConfig.COMMON.activeAirMineDamageMultiplier.get(), context.caster);

        Vec3 center = SableRangeCompat.projectForEffect(level, positionVal);
        double radiusSq = radius * radius;
        AABB searchArea = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        AttackRules.Options options = AttackRules.Options.from(context);
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                // 攻撃術式で共通の命中判定（術者側・防具立て・プレイヤーに安全・受ける側の PvP）。
                target -> AttackRules.canHarm(context.caster, target, options)
                        && target.getBoundingBox().distanceToSqr(center) <= radiusSq
        );

        for (LivingEntity target : targets) {
            double distance = target.getBoundingBox().getCenter().distanceTo(center);
            if (distance > radius) {
                continue;
            }

            float distanceDamage = calculateDistanceAdjustedDamage(finalDamage, distance, radius);
            if (distanceDamage <= 0.0F) {
                continue;
            }

            AttackRules.harm(context.caster, target, options,
                    AttackRules.source(level, PsitweaksDamageTypes.ACTIVE_AIR_MINE, context.caster, context.caster), distanceDamage);
        }

        if (level instanceof ServerLevel serverLevel) {
            burstEffects(serverLevel, center, radius, CombatSpellHelper.spellColor(context.caster));
        }

        return null;
    }

    private static double calculateRadius(double powerVal) {
        return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, MIN_RADIUS + powerVal * RADIUS_PER_POWER));
    }

    private static float calculateDamage(double powerVal) {
        return BASE_DAMAGE + (float) (powerVal * DAMAGE_PER_POWER);
    }

    private static float calculateDistanceAdjustedDamage(float baseDamage, double distance, double radius) {
        if (radius <= 0.0) {
            return baseDamage;
        }

        double normalized = Math.min(1.0, Math.max(0.0, distance / radius));
        double multiplier = EDGE_DAMAGE_FACTOR + (1.0 - EDGE_DAMAGE_FACTOR) * (1.0 - normalized);
        return (float) (baseDamage * multiplier);
    }

    /**
     * The compressed air bursts: a spherical shock wave the size of the blast, a quicker inner one, a gust at the centre
     * and a shell of air rushing outward.
     */
    private static void burstEffects(ServerLevel level, Vec3 center, double radius, int color) {
        CombatEffects.sphereFlash(level, center, (float) radius, color, MessageSphereFlash.Style.SHOCKWAVE, 1.2F);
        CombatEffects.sphereFlash(level, center, (float) radius * 0.55F, color,
                MessageSphereFlash.Style.SHOCKWAVE, 0.9F);
        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);

        int pointCount = Math.max(24, (int) Math.ceil(radius * 10.0));
        for (int i = 0; i < pointCount; i++) {
            double y = 1.0 - (2.0 * (i + 0.5)) / pointCount;
            double horizontal = Math.sqrt(Math.max(0.0, 1.0 - y * y));
            double theta = GOLDEN_ANGLE * i;
            double x = Math.cos(theta) * horizontal;
            double z = Math.sin(theta) * horizontal;
            // Count 0 makes the offset the direction and the speed its scale: each puff flies straight outward.
            level.sendParticles(ParticleTypes.CLOUD, center.x + x * 0.3, center.y + y * 0.3, center.z + z * 0.3,
                    0, x, y, z, 0.12 + radius * 0.04);
        }

        CombatEffects.sound(level, center, SoundEvents.WIND_CHARGE_BURST, 1.2F, 0.6F);
        CombatEffects.sound(level, center, SoundEvents.GENERIC_EXPLODE, 0.8F, 1.3F);
    }
}
