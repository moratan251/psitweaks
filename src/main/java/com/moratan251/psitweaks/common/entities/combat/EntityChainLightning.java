package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramWetness;
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
 * The later jumps of Trick: Chain Lightning. The bolt reaches each target a little after the previous one, in step
 * with the arcs the client draws, so this invisible entity deals those hits on schedule. It is never sent to clients.
 */
public class EntityChainLightning extends CombatSpellEntity {
    /** Ticks between one jump and the next. The client arcs light up at the same pace. */
    public static final int STAGGER = 2;
    private static final float WET_DAMAGE_FACTOR = 1.5F;

    private final List<Jump> jumps = new ArrayList<>();
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

    /** Schedules the hit of jump number {@code jump} (the first target is jump 0), {@code jump × STAGGER} ticks from now. */
    public void schedule(LivingEntity target, int jump, float damage) {
        jumps.add(new Jump(target, jump, damage));
    }

    /**
     * Strikes one target of the chain: extra damage in water or rain, and a crackle that rises in pitch along the chain.
     */
    public static void strike(ServerLevel level, @Nullable LivingEntity caster, AttackRules.Options options, LivingEntity target,
                              DamageSource source, float damage, int jump) {
        AttackRules.harm(caster, target, options, source, SpellGramWetness.isWet(target) ? damage * WET_DAMAGE_FACTOR : damage);
        if (jump > 0) {
            CombatEffects.sound(level, CombatSpellHelper.center(target), SoundEvents.LIGHTNING_BOLT_IMPACT, 0.45F,
                    Math.min(2.0F, 1.6F + 0.06F * jump));
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
        for (Jump jump : jumps) {
            if (jump.done || elapsed < (long) jump.jump * STAGGER) {
                continue;
            }
            jump.done = true;
            if (!casterLooked) {
                caster = getCaster();
                casterLooked = true;
            }
            LivingEntity target = jump.target;
            if (target.isRemoved() || target.level() != level || !canAffect(caster, target)) {
                continue;
            }
            strike(level, caster, options(), target, source(level, caster), jump.damage, jump.jump);
        }
        if (jumps.stream().allMatch(jump -> jump.done)) {
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

    private static final class Jump {
        final LivingEntity target;
        final int jump;
        final float damage;
        boolean done;

        Jump(LivingEntity target, int jump, float damage) {
            this.target = target;
            this.jump = jump;
            this.damage = damage;
        }
    }
}
