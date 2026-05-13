package com.moratan251.psitweaks.common.spells;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.EntityMolecularDivider;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

import java.util.List;

/**
 * 分子ディバイダー - 三点で三角形領域を形成し、内部のエンティティにダメージを与える
 */
public class PieceTrickMolecularDivider extends PieceTrick {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double DEGENERATE_EPSILON = 1.0E-7;
    private static final double AXIS_EPSILON = 1.0E-12;

    SpellParam<Vector3> position1;
    SpellParam<Vector3> position2;
    SpellParam<Vector3> position3;
    SpellParam<Number> power;

    public PieceTrickMolecularDivider(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY,
                (new StatLabel("psi.spellparam.power", true)).max(0.1).mul(200.0).add(800.0).floor());
        this.setStatLabel(EnumSpellStat.COST,
                (new StatLabel("psi.spellparam.power", true)).floor().add(0.0).add(1200.0));
    }

    @Override
    public void initParams() {
        this.addParam(this.position1 = new ParamVector("psi.spellparam.vector1", SpellParam.BLUE, false, false));
        this.addParam(this.position2 = new ParamVector("psi.spellparam.vector2", SpellParam.CYAN, false, false));
        this.addParam(this.position3 = new ParamVector("psi.spellparam.vector3", SpellParam.PURPLE, false, false));
        this.addParam(this.power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = (Double) this.getParamEvaluation(this.power);
        if (powerVal == null || powerVal <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, this.x, this.y);
        }

        powerVal = Math.max(0.1, powerVal);
        meta.addStat(EnumSpellStat.POTENCY, (int) (800 + powerVal * 200));
        meta.addStat(EnumSpellStat.COST, (int) (1200 * powerVal));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 pos1 = (Vector3) this.getParamValue(context, this.position1);
        Vector3 pos2 = (Vector3) this.getParamValue(context, this.position2);
        Vector3 pos3 = (Vector3) this.getParamValue(context, this.position3);
        double powerVal = ((Number) this.getParamValue(context, this.power)).doubleValue();

        if (pos1 == null || pos2 == null || pos3 == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        if (!context.isInRadius(pos1) || !context.isInRadius(pos2) || !context.isInRadius(pos3)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Level world = context.caster.level();
        if (world.isClientSide) {
            return null;
        }

        Vec3 v1 = SableRangeCompat.projectForEffect(world, pos1);
        Vec3 v2 = SableRangeCompat.projectForEffect(world, pos2);
        Vec3 v3 = SableRangeCompat.projectForEffect(world, pos3);

        double minX = Math.min(Math.min(v1.x, v2.x), v3.x) - 0.5;
        double minY = Math.min(Math.min(v1.y, v2.y), v3.y) - 0.5;
        double minZ = Math.min(Math.min(v1.z, v2.z), v3.z) - 0.5;
        double maxX = Math.max(Math.max(v1.x, v2.x), v3.x) + 0.5;
        double maxY = Math.max(Math.max(v1.y, v2.y), v3.y) + 0.5;
        double maxZ = Math.max(Math.max(v1.z, v2.z), v3.z) + 0.5;

        AABB boundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        List<Entity> entities = world.getEntities(context.caster, boundingBox, entity -> entity instanceof LivingEntity);

        // Configからダメージ倍率を取得（個別倍率 x 全体倍率）
        double perSpellDamageMul = PsitweaksConfig.COMMON.molecularDividerDamageMultiplier.get();
        double globalDamageMul = PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get();
        double spellDamageFactor = context.caster.getAttributeValue(PsitweaksAttributes.SPELL_DAMAGE_FACTOR);
        double damageMul = perSpellDamageMul * globalDamageMul * spellDamageFactor;
        float damage = (float) (60.0 * powerVal * damageMul);
     //   LOGGER.debug("Molecular Divider Damage: {} (Power: {}, PerSpell: {}, Global: {})",
       //         damage, powerVal, perSpellDamageMul, globalDamageMul);

        boolean safeToPlayers = SpellSafetyUtils.hasSafeToPlayers(context);
        for (Entity entity : entities) {
            if (entity instanceof Player && safeToPlayers) {
                continue;
            }

            if (doesHitboxIntersectTriangle(entity.getBoundingBox(), v1, v2, v3)) {
                entity.hurt(context.caster.damageSources().indirectMagic(context.caster, context.caster), damage);

                // ヒット時のパーティクル
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.CRIT,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            15,
                            0.3, 0.3, 0.3,
                            0.1
                    );
                }

                // ヒット時のサウンド（エンティティの位置から発生）
                world.playSound(
                        null,
                        entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.2F
                );
            }
        }

        // ========== エンティティをスポーンして三角形を描画 ==========
        EntityMolecularDivider visualEntity = new EntityMolecularDivider(world, v1, v2, v3, 15); // 15tick = 0.75秒
        world.addFreshEntity(visualEntity);
        // =========================================================

        // 発動時のサウンド（三角形の中心から）
        double centerX = (v1.x + v2.x + v3.x) / 3;
        double centerY = (v1.y + v2.y + v3.y) / 3;
        double centerZ = (v1.z + v2.z + v3.z) / 3;

        world.playSound(
                null,
                centerX, centerY, centerZ,
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS,
                0.8F,
                1.5F
        );

        return null;
    }

    private boolean doesHitboxIntersectTriangle(AABB box, Vec3 v1, Vec3 v2, Vec3 v3) {
        Vec3 center = box.getCenter();
        Vec3 halfSize = new Vec3(
                (box.maxX - box.minX) * 0.5,
                (box.maxY - box.minY) * 0.5,
                (box.maxZ - box.minZ) * 0.5
        );

        Vec3 tv1 = v1.subtract(center);
        Vec3 tv2 = v2.subtract(center);
        Vec3 tv3 = v3.subtract(center);

        Vec3 edge1 = tv2.subtract(tv1);
        Vec3 edge2 = tv3.subtract(tv2);
        Vec3 edge3 = tv1.subtract(tv3);
        Vec3 normal = edge1.cross(edge2);

        if (normal.lengthSqr() < DEGENERATE_EPSILON) {
            return doesDegenerateTriangleIntersectBox(box, v1, v2, v3);
        }

        if (!overlapsOnAxis(new Vec3(1.0, 0.0, 0.0), halfSize, tv1, tv2, tv3)
                || !overlapsOnAxis(new Vec3(0.0, 1.0, 0.0), halfSize, tv1, tv2, tv3)
                || !overlapsOnAxis(new Vec3(0.0, 0.0, 1.0), halfSize, tv1, tv2, tv3)
                || !overlapsOnAxis(normal, halfSize, tv1, tv2, tv3)) {
            return false;
        }

        Vec3[] boxAxes = {
                new Vec3(1.0, 0.0, 0.0),
                new Vec3(0.0, 1.0, 0.0),
                new Vec3(0.0, 0.0, 1.0)
        };
        Vec3[] triangleEdges = {edge1, edge2, edge3};
        for (Vec3 edge : triangleEdges) {
            for (Vec3 boxAxis : boxAxes) {
                if (!overlapsOnAxis(edge.cross(boxAxis), halfSize, tv1, tv2, tv3)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean overlapsOnAxis(Vec3 axis, Vec3 halfSize, Vec3 v1, Vec3 v2, Vec3 v3) {
        if (axis.lengthSqr() < AXIS_EPSILON) {
            return true;
        }

        double p1 = v1.dot(axis);
        double p2 = v2.dot(axis);
        double p3 = v3.dot(axis);
        double min = Math.min(Math.min(p1, p2), p3);
        double max = Math.max(Math.max(p1, p2), p3);
        double radius = halfSize.x * Math.abs(axis.x)
                + halfSize.y * Math.abs(axis.y)
                + halfSize.z * Math.abs(axis.z);

        return min <= radius && max >= -radius;
    }

    private boolean doesDegenerateTriangleIntersectBox(AABB box, Vec3 v1, Vec3 v2, Vec3 v3) {
        return isPointInsideBox(box, v1)
                || isPointInsideBox(box, v2)
                || isPointInsideBox(box, v3)
                || doesSegmentIntersectBox(box, v1, v2)
                || doesSegmentIntersectBox(box, v2, v3)
                || doesSegmentIntersectBox(box, v3, v1);
    }

    private boolean isPointInsideBox(AABB box, Vec3 point) {
        return point.x >= box.minX && point.x <= box.maxX
                && point.y >= box.minY && point.y <= box.maxY
                && point.z >= box.minZ && point.z <= box.maxZ;
    }

    private boolean doesSegmentIntersectBox(AABB box, Vec3 start, Vec3 end) {
        double tMin = 0.0;
        double tMax = 1.0;
        double[] startCoords = {start.x, start.y, start.z};
        double[] endCoords = {end.x, end.y, end.z};
        double[] minCoords = {box.minX, box.minY, box.minZ};
        double[] maxCoords = {box.maxX, box.maxY, box.maxZ};

        for (int i = 0; i < 3; i++) {
            double direction = endCoords[i] - startCoords[i];
            if (Math.abs(direction) < DEGENERATE_EPSILON) {
                if (startCoords[i] < minCoords[i] || startCoords[i] > maxCoords[i]) {
                    return false;
                }
                continue;
            }

            double inverseDirection = 1.0 / direction;
            double axisMin = (minCoords[i] - startCoords[i]) * inverseDirection;
            double axisMax = (maxCoords[i] - startCoords[i]) * inverseDirection;
            if (axisMin > axisMax) {
                double swap = axisMin;
                axisMin = axisMax;
                axisMax = swap;
            }
            tMin = Math.max(tMin, axisMin);
            tMax = Math.min(tMax, axisMax);
            if (tMin > tMax) {
                return false;
            }
        }

        return true;
    }
}
