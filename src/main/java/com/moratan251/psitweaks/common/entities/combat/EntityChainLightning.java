package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The branches of Trick: Chain Lightning. Each stage of branches reaches its targets a little after the one before, in
 * step with the arcs the client draws, so this invisible entity deals those hits on schedule. It is never sent to
 * clients.
 */
public class EntityChainLightning extends CombatSpellEntity {
    /** Ticks between one stage of the bolt and the next. The client arcs light up at the same pace. */
    public static final int STAGGER = 2;
    private static final float WET_DAMAGE_FACTOR = 1.5F;

    private final List<Hit> hits = new ArrayList<>();
    /** Game time of the cast; counting from it keeps the pace whether the entity first ticks this tick or the next. */
    private long castTime;

    public EntityChainLightning(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityChainLightning(Level level, Vec3 position) {
        this(PsitweaksEntities.CHAIN_LIGHTNING.get(), level);
        setPos(position.x, position.y, position.z);
        castTime = level.getGameTime();
    }

    /**
     * Schedules a hit at stage {@code stage} of the bolt (the first target is stage 0), {@code stage × STAGGER} ticks
     * from now.
     */
    public void schedule(LivingEntity target, int stage, float damage) {
        hits.add(new Hit(target, stage, damage));
    }

    /**
     * Strikes one target of the bolt: extra damage in water or rain, and for a branch a crackle that rises in pitch
     * with the stage.
     */
    public static void strike(ServerLevel level, @Nullable LivingEntity caster, AttackRules.Options options, LivingEntity target,
                              DamageSource source, float damage, int stage) {
        AttackRules.harm(caster, target, options, source, target.isInWaterOrRain() ? damage * WET_DAMAGE_FACTOR : damage);
        if (stage > 0) {
            CombatEffects.sound(level, CombatSpellHelper.center(target), SoundEvents.LIGHTNING_BOLT_IMPACT, 0.45F,
                    Math.min(2.0F, 1.6F + 0.15F * stage));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        long elapsed = level.getGameTime() - castTime;
        LivingEntity caster = null;
        boolean casterLooked = false;
        for (Hit hit : hits) {
            if (hit.done || elapsed < (long) hit.stage * STAGGER) {
                continue;
            }
            hit.done = true;
            if (!casterLooked) {
                caster = getCaster();
                casterLooked = true;
            }
            LivingEntity target = hit.target;
            if (target.isRemoved() || target.level() != level || !canAffect(caster, target)) {
                continue;
            }
            strike(level, caster, options(), target, source(level, caster), hit.damage, hit.stage);
        }
        if (hits.stream().allMatch(hit -> hit.done)) {
            discard();
        }
    }

    private static DamageSource source(ServerLevel level, @Nullable LivingEntity caster) {
        return AttackRules.source(level, PsitweaksDamageTypes.CHAIN_LIGHTNING, caster, caster);
    }

    @Override
    public boolean broadcastToPlayer(ServerPlayer player) {
        return false;
    }

    private static final class Hit {
        final LivingEntity target;
        final int stage;
        final float damage;
        boolean done;

        Hit(LivingEntity target, int stage, float damage) {
            this.target = target;
            this.stage = stage;
            this.damage = damage;
        }
    }
}
