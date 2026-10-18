package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Trick: Cocytus freezes a mob's mind: its AI is switched off and it is drawn encased in ice. If something else turns
 * the AI back on (a command, another mod), the ice melts away and the mob gets its voice back.
 */
public final class CocytusFreezes {
    private CocytusFreezes() {
    }

    /** Switches off the mob's AI and encases it in ice. Freezing an already frozen mob only replays the effects. */
    public static void freeze(ServerLevel level, Mob mob) {
        if (!mob.hasData(PsitweaksAttachments.COCYTUS_FREEZE)) {
            mob.setData(PsitweaksAttachments.COCYTUS_FREEZE, new CocytusFreeze(mob.isSilent()));
        }
        mob.setNoAi(true);
        mob.setSilent(true);
        mob.setDeltaMovement(Vec3.ZERO);

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

    /** Melts the ice once the mob's AI has been turned back on by something else. Server side. */
    public static void tick(ServerLevel level, Mob mob) {
        CocytusFreeze freeze = mob.getExistingDataOrNull(PsitweaksAttachments.COCYTUS_FREEZE);
        if (freeze == null || mob.isNoAi()) {
            return;
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
}
