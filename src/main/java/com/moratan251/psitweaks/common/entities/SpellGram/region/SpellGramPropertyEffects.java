package com.moratan251.psitweaks.common.entities.SpellGram.region;

import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 性質ごとの効き目（試作）。領域型が {@link EntitySpellGramRegion#PULSE_INTERVAL_TICKS} ごとに呼ぶ。
 * ダメージは {@link AttackRules#harm} だけで与え、ブロックへの作用は時間で元に戻るものにとどめる。
 */
final class SpellGramPropertyEffects {
    // 炎: 威力1で 0.5 秒ごとに 3.5（全体倍率・個別倍率・事象干渉力は付けた時点の倍率に入っている）
    static final double FIRE_BASE_DAMAGE = 2.0D;
    static final double FIRE_DAMAGE_PER_POWER = 1.5D;
    private static final int FIRE_IGNITE_TICKS = 40;

    // 氷: 威力1で 0.5 秒ごとに 2.5。凍える時間を足し、濡れた相手は2倍凍える
    static final double ICE_BASE_DAMAGE = 1.5D;
    static final double ICE_DAMAGE_PER_POWER = 1.0D;
    private static final int ICE_FREEZE_TICKS = 40;
    private static final int ICE_FREEZE_OVER_LIMIT = 60;
    private static final int ICE_SLOWNESS_TICKS = 30;

    // 湿潤: 火を消し、領域を出てから5秒は濡れたままにする
    static final int WET_SOAK_TICKS = 100;

    private SpellGramPropertyEffects() {
    }

    static void pulse(EntitySpellGramRegion region, ServerLevel level, @Nullable LivingEntity caster,
                      List<LivingEntity> inside, SpellGramPropertyInstance instance, boolean blockPulse) {
        switch (instance.property()) {
            case FIRE -> fire(region, level, caster, inside, instance);
            case ICE -> {
                ice(region, level, caster, inside, instance);
                if (blockPulse) {
                    freezeWater(region, level, caster);
                }
            }
            case WET -> {
                wet(inside);
                if (blockPulse) {
                    putOutFire(region, level, caster);
                }
            }
        }
    }

    private static void fire(EntitySpellGramRegion region, ServerLevel level, @Nullable LivingEntity caster,
                             List<LivingEntity> inside, SpellGramPropertyInstance instance) {
        float damage = (float) ((FIRE_BASE_DAMAGE + FIRE_DAMAGE_PER_POWER * instance.power()) * instance.scale());
        if (damage <= 0.0F) {
            return;
        }
        DamageSource source = AttackRules.source(level, DamageTypes.IN_FIRE, region, caster);
        AttackRules.Options options = region.attackOptions();
        for (LivingEntity target : inside) {
            if (!AttackRules.canHarm(caster, target, options)) {
                continue;
            }
            target.invulnerableTime = 0;
            if (AttackRules.harm(caster, target, options, source, damage) && !SpellGramWetness.isWet(target)) {
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), FIRE_IGNITE_TICKS));
            }
        }
    }

    private static void ice(EntitySpellGramRegion region, ServerLevel level, @Nullable LivingEntity caster,
                            List<LivingEntity> inside, SpellGramPropertyInstance instance) {
        float damage = (float) ((ICE_BASE_DAMAGE + ICE_DAMAGE_PER_POWER * instance.power()) * instance.scale());
        DamageSource source = AttackRules.source(level, DamageTypes.FREEZE, region, caster);
        AttackRules.Options options = region.attackOptions();
        for (LivingEntity target : inside) {
            if (!AttackRules.canHarm(caster, target, options)) {
                continue;
            }
            target.invulnerableTime = 0;
            if (damage > 0.0F) {
                AttackRules.harm(caster, target, options, source, damage);
            }
            if (!target.isAlive() || !target.canFreeze()) {
                continue;
            }
            int added = SpellGramWetness.isWet(target) ? ICE_FREEZE_TICKS * 2 : ICE_FREEZE_TICKS;
            int limit = target.getTicksRequiredToFreeze() + ICE_FREEZE_OVER_LIMIT;
            target.setTicksFrozen(Math.min(limit, target.getTicksFrozen() + added));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ICE_SLOWNESS_TICKS, 0, false, true), caster);
        }
    }

    private static void wet(List<LivingEntity> inside) {
        for (LivingEntity target : inside) {
            if (target.isOnFire()) {
                target.clearFire();
            }
            SpellGramWetness.soak(target, WET_SOAK_TICKS);
        }
    }

    /** 水面を薄氷（時間で溶ける氷）にする。足場になるので、領域が続くあいだは溶けても張り直す。 */
    private static void freezeWater(EntitySpellGramRegion region, ServerLevel level, @Nullable LivingEntity caster) {
        int minY = Mth.floor(region.getY()) - 1;
        int maxY = Mth.floor(region.getY()) + 1;
        forEachColumn(region, (x, z) -> {
            for (int y = maxY; y >= minY; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(pos);
                if (!state.is(Blocks.WATER) || !state.getFluidState().isSourceOfType(Fluids.WATER)) {
                    continue;
                }
                if (!level.getBlockState(pos.above()).isAir() || !mayChange(level, caster, pos)) {
                    break;
                }
                level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                level.scheduleTick(pos, Blocks.FROSTED_ICE, Mth.nextInt(level.getRandom(), 60, 120));
                break;
            }
        });
    }

    private static void putOutFire(EntitySpellGramRegion region, ServerLevel level, @Nullable LivingEntity caster) {
        int minY = Mth.floor(region.getY()) - EntitySpellGramRegion.DEPTH;
        int maxY = Mth.floor(region.getY()) + EntitySpellGramRegion.HEIGHT;
        boolean[] putOut = {false};
        forEachColumn(region, (x, z) -> {
            for (int y = minY; y <= maxY; y++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockState(pos).getBlock() instanceof BaseFireBlock && mayChange(level, caster, pos)) {
                    level.removeBlock(pos, false);
                    putOut[0] = true;
                }
            }
        });
        if (putOut[0]) {
            level.playSound(null, region.getX(), region.getY(), region.getZ(), SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS, 0.6F, 1.4F);
        }
    }

    private static boolean mayChange(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos) {
        return caster instanceof Player player ? level.mayInteract(player, pos) : caster != null;
    }

    private interface ColumnVisitor {
        void visit(int x, int z);
    }

    private static void forEachColumn(EntitySpellGramRegion region, ColumnVisitor visitor) {
        double radius = region.getDamageRadius();
        double radiusSq = radius * radius;
        int minX = Mth.floor(region.getX() - radius);
        int maxX = Mth.floor(region.getX() + radius);
        int minZ = Mth.floor(region.getZ() - radius);
        int maxZ = Mth.floor(region.getZ() + radius);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dx = x + 0.5D - region.getX();
                double dz = z + 0.5D - region.getZ();
                if (dx * dx + dz * dz <= radiusSq) {
                    visitor.visit(x, z);
                }
            }
        }
    }
}
