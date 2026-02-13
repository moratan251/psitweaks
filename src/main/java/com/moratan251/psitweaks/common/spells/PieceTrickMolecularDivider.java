package com.moratan251.psitweaks.common.spells;

import com.mojang.logging.LogUtils;
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
    SpellParam<Vector3> position1;
    SpellParam<Vector3> position2;
    SpellParam<Vector3> position3;
    SpellParam<Number> power;

    public PieceTrickMolecularDivider(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY,
                (new StatLabel("psi.spellparam.power", true)).floor().add(1100).add(300.0));
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
        meta.addStat(EnumSpellStat.POTENCY, (int) (1100 + powerVal * 300));
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

        Vec3 v1 = new Vec3(pos1.x, pos1.y, pos1.z);
        Vec3 v2 = new Vec3(pos2.x, pos2.y, pos2.z);
        Vec3 v3 = new Vec3(pos3.x, pos3.y, pos3.z);

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
        double damageMul = perSpellDamageMul * globalDamageMul;
        float damage = (float) (60.0 * powerVal * damageMul);
     //   LOGGER.debug("Molecular Divider Damage: {} (Power: {}, PerSpell: {}, Global: {})",
       //         damage, powerVal, perSpellDamageMul, globalDamageMul);

        for (Entity entity : entities) {
            Vec3 entityPos = entity.position();

            if (isPointInTrianglePrism(entityPos, v1, v2, v3)) {
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

    private boolean isPointInTrianglePrism(Vec3 point, Vec3 v1, Vec3 v2, Vec3 v3) {
        double minY = Math.min(Math.min(v1.y, v2.y), v3.y) - 1.0;
        double maxY = Math.max(Math.max(v1.y, v2.y), v3.y) + 1.0;

        if (point.y < minY || point.y > maxY) {
            return false;
        }

        return isPointInTriangle2D(point.x, point.z, v1.x, v1.z, v2.x, v2.z, v3.x, v3.z);
    }

    private boolean isPointInTriangle2D(double px, double pz, double x1, double z1, double x2, double z2, double x3, double z3) {
        double d1 = sign(px, pz, x1, z1, x2, z2);
        double d2 = sign(px, pz, x2, z2, x3, z3);
        double d3 = sign(px, pz, x3, z3, x1, z1);

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    private double sign(double px, double pz, double x1, double z1, double x2, double z2) {
        return (px - x2) * (z1 - z2) - (x1 - x2) * (pz - z2);
    }
}
