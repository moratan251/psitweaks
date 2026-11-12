package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * 領域を組み合わせたときのダメージ（氷炎地獄・熱乱流・吹雪・暴風雨）。攻撃術式の共通ルール {@link AttackRules} に乗せる: 術者・
 * 術者のペット・同じチームには当てず、ダメージは 基本値 × 設定の倍率 × 全体倍率 × 術者の事象干渉力。
 */
final class ZoneAttacks {
    private ZoneAttacks() {
    }

    /**
     * 領域 {@code zone} の中で、{@code where} が真のブロックにいる生物（当たり判定の中心で判定）に、1 回ぶんのダメージを
     * 与える。当たった生物に {@code afterHit} を呼ぶ。
     */
    static void harm(ServerLevel level, SpellGramZone zone, ResourceKey<DamageType> type, float baseDamage,
                     double perSpellMultiplier, Predicate<BlockPos> where, Consumer<LivingEntity> afterHit) {
        harm(level, zone, type, baseDamage, perSpellMultiplier, where, target -> true, afterHit);
    }

    /** {@link #harm} のうち、{@code who} が真の生物だけに当てる。 */
    static void harm(ServerLevel level, SpellGramZone zone, ResourceKey<DamageType> type, float baseDamage,
                     double perSpellMultiplier, Predicate<BlockPos> where, Predicate<LivingEntity> who,
                     Consumer<LivingEntity> afterHit) {
        harmScaled(level, zone, type, perSpellMultiplier, pos -> where.test(pos) ? baseDamage : 0.0, who, afterHit);
    }

    /**
     * 生物のいるブロックごとに 1 回ぶんの基本のダメージ（{@code baseDamageAt}、倍率を掛ける前）を決めて当てる。0 以下の
     * ブロックの生物には当てない。乱流の威力で強さの決まる組み合わせ（熱乱流・吹雪・暴風雨）に使う。
     */
    static void harmScaled(ServerLevel level, SpellGramZone zone, ResourceKey<DamageType> type, double perSpellMultiplier,
                           ToDoubleFunction<BlockPos> baseDamageAt, Predicate<LivingEntity> who,
                           Consumer<LivingEntity> afterHit) {
        LivingEntity caster = zone.getCaster();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, zone.getArea().bounds(), target -> {
            if (!target.isAlive() || !who.test(target)) {
                return false;
            }
            return zone.isInsideAndActive(BlockPos.containing(target.getBoundingBox().getCenter()));
        });
        if (targets.isEmpty()) {
            return;
        }
        DamageSource source = AttackRules.source(level, type, zone, caster);
        for (LivingEntity target : targets) {
            BlockPos at = BlockPos.containing(target.getBoundingBox().getCenter());
            // 同じ種類の領域が重なっていれば、そこで一番強い領域だけが当てる（重ねてもダメージは増えない）。
            if (!zone.isStrongestAt(at)) {
                continue;
            }
            double base = baseDamageAt.applyAsDouble(at);
            if (base <= 0.0) {
                continue;
            }
            float damage = AttackRules.scale(base, perSpellMultiplier, caster);
            target.invulnerableTime = 0;
            if (AttackRules.harm(caster, target, AttackRules.Options.DEFAULT, source, damage)) {
                afterHit.accept(target);
            }
        }
    }

    /** 2 つの箱の重なり（重ならなければ null）。 */
    static AABB overlap(AABB a, AABB b) {
        if (!a.intersects(b)) {
            return null;
        }
        return a.intersect(b);
    }

    /** 箱 {@code box} の中のランダムな点。 */
    static Vec3 randomPoint(AABB box, net.minecraft.util.RandomSource random) {
        return new Vec3(box.minX + random.nextDouble() * box.getXsize(), box.minY + random.nextDouble() * box.getYsize(),
                box.minZ + random.nextDouble() * box.getZsize());
    }
}
