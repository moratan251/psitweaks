package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.mixin.WitherBossAccessor;
import javax.annotation.Nullable;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Trick: Cocytus freezes a mob's mind: its AI is switched off and it is drawn encased in ice. If something else turns
 * the AI back on (a command, another mod), the ice melts away and the mob gets its voice back.
 * <p>
 * Bosses freeze too. Vanilla's two need a hand to stay beatable: the Wither is not frozen while it is still gathering
 * strength after being built (its countdown runs on its AI, and it takes no damage until it ends), and a frozen Ender
 * Dragon thaws once it is defeated (its death throes run on its AI). While they stay frozen their boss bars still follow
 * their health, and a frozen dragon's parts (which its AI places) are laid out around it when it is loaded again.
 */
public final class CocytusFreezes {
    /** A dragon part this far from the dragon was never placed (a frozen dragon just loaded). */
    private static final double DRAGON_PART_REACH = 12.0;

    private CocytusFreezes() {
    }

    /**
     * Whether Trick: Cocytus cast by {@code caster} freezes the entity: a mob an attack of the caster may hit
     * ({@link AttackRules#canHarm}, so never the caster's pets or allies), that does not opt out of spells and that
     * freezing would not leave stuck ({@link #wouldBeStuck}). Players have no AI to switch off and are never frozen.
     */
    public static boolean canFreeze(@Nullable LivingEntity caster, @Nullable Entity entity, AttackRules.Options options) {
        return entity instanceof Mob mob
                && AttackRules.canHarm(caster, mob, options)
                && !AttackRules.isSpellImmune(mob)
                && !wouldBeStuck(mob);
    }

    /**
     * Whether switching off the mob's AI now would leave it stuck for good: the Wither while it gathers strength after
     * being built (invulnerable until its AI counts down), and the Ender Dragon once it is dying (it can no longer be
     * hurt, and its AI plays out its death).
     */
    public static boolean wouldBeStuck(Mob mob) {
        if (mob instanceof WitherBoss wither) {
            return wither.getInvulnerableTicks() > 0;
        }
        return mob instanceof EnderDragon dragon && isDying(dragon);
    }

    private static boolean isDying(EnderDragon dragon) {
        return dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING;
    }

    /** Switches off the mob's AI and encases it in ice. Freezing an already frozen mob only replays the effects. */
    public static void freeze(ServerLevel level, Mob mob) {
        if (!mob.hasData(PsitweaksAttachments.COCYTUS_FREEZE)) {
            mob.setData(PsitweaksAttachments.COCYTUS_FREEZE, new CocytusFreeze(mob.isSilent()));
        }
        mob.setNoAi(true);
        mob.setSilent(true);
        mob.setDeltaMovement(Vec3.ZERO);
        if (mob instanceof WitherBoss wither) {
            // Its body and heads keep turning to these even without AI.
            for (int head = 0; head < 3; head++) {
                wither.setAlternativeTarget(head, 0);
            }
        }

        Vec3 center = mob.getBoundingBox().getCenter();
        double spreadX = mob.getBbWidth() * 0.5;
        double spreadY = mob.getBbHeight() * 0.5;
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, 40, spreadX, spreadY, spreadX, 0.05);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                center.x, center.y, center.z, 30, spreadX, spreadY, spreadX, 0.1);
        CombatEffects.sound(level, center, SoundEvents.PLAYER_HURT_FREEZE, 1.0F, 0.6F);
        CombatEffects.sound(level, center, SoundEvents.GLASS_PLACE, 1.0F, 0.7F);
    }

    /** Whether the entity should be drawn frozen: Cocytus froze it and its AI is still off. Works on both sides. */
    public static boolean isFrozen(Entity entity) {
        return entity instanceof Mob mob && mob.isNoAi() && mob.hasData(PsitweaksAttachments.COCYTUS_FREEZE);
    }

    /**
     * Server side, every tick of a mob: keeps a frozen boss's bar on its health and thaws a frozen Ender Dragon that has
     * been defeated; then melts the ice once the mob's AI has been turned back on.
     */
    public static void tick(ServerLevel level, Mob mob) {
        CocytusFreeze freeze = mob.getExistingDataOrNull(PsitweaksAttachments.COCYTUS_FREEZE);
        if (freeze == null) {
            return;
        }
        if (mob.isNoAi()) {
            if (mob instanceof EnderDragon dragon && isDying(dragon)) {
                mob.setNoAi(false);
            } else {
                syncBossBar(mob);
                return;
            }
        }
        mob.removeData(PsitweaksAttachments.COCYTUS_FREEZE);
        mob.setSilent(freeze.wasSilent());

        Vec3 center = mob.getBoundingBox().getCenter();
        double spreadX = mob.getBbWidth() * 0.5;
        double spreadY = mob.getBbHeight() * 0.5;
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                center.x, center.y, center.z, 40, spreadX, spreadY, spreadX, 0.15);
        CombatEffects.sound(level, center, SoundEvents.GLASS_BREAK, 1.0F, 1.2F);
    }

    /** The boss bar follows health only on the boss's AI tick, which a frozen boss no longer runs. */
    private static void syncBossBar(Mob mob) {
        if (mob instanceof WitherBoss wither) {
            ((WitherBossAccessor) wither).psitweaks$getBossEvent().setProgress(wither.getHealth() / wither.getMaxHealth());
        } else if (mob instanceof EnderDragon dragon && dragon.getDragonFight() != null) {
            dragon.getDragonFight().updateDragon(dragon);
        }
    }

    /**
     * Both sides, every tick of a frozen Ender Dragon: its parts (head, neck, body, tail, wings), which take hits and
     * which only its AI moves, are left where they were when it froze; a dragon loaded frozen has never placed them, so
     * they are laid out around it at rest, as its first AI tick would.
     */
    public static void layOutFrozenDragon(EnderDragon dragon) {
        EnderDragonPart[] parts = dragon.getSubEntities();
        // head, neck, body, tail1, tail2, tail3, wing1, wing2
        if (parts.length != 8 || parts[2].distanceToSqr(dragon) <= DRAGON_PART_REACH * DRAGON_PART_REACH) {
            return;
        }
        float yaw = dragon.getYRot() * Mth.DEG_TO_RAD;
        float sin = Mth.sin(yaw);
        float cos = Mth.cos(yaw);
        double headY = dragon.getPhaseManager().getCurrentPhase().isSitting() ? -1.0 : 0.0;
        place(dragon, parts[0], sin * 6.5, headY, -cos * 6.5);
        place(dragon, parts[1], sin * 5.5, headY, -cos * 5.5);
        place(dragon, parts[2], sin * 0.5, 0.0, -cos * 0.5);
        for (int tail = 0; tail < 3; tail++) {
            double reach = 1.5 + (tail + 1) * 2.0;
            place(dragon, parts[3 + tail], -sin * reach, 1.5, cos * reach);
        }
        place(dragon, parts[6], cos * 4.5, 2.0, sin * 4.5);
        place(dragon, parts[7], -cos * 4.5, 2.0, -sin * 4.5);
    }

    private static void place(EnderDragon dragon, EnderDragonPart part, double x, double y, double z) {
        part.setPos(dragon.getX() + x, dragon.getY() + y, dragon.getZ() + z);
        part.setOldPosAndRot();
    }
}
