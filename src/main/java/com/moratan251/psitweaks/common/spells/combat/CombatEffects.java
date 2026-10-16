package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.network.MessageLightningArcs;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-side triggers for the client effects of the combat tricks. */
public final class CombatEffects {
    /** Players within this distance of an effect receive it. */
    public static final double EFFECT_RANGE = 96.0;

    private CombatEffects() {
    }

    public static void sphereFlash(ServerLevel level, Vec3 position, float radius, int color,
                                   MessageSphereFlash.Style style, float strength) {
        PacketDistributor.sendToPlayersNear(level, null, position.x, position.y, position.z, EFFECT_RANGE,
                new MessageSphereFlash(MessageSphereFlash.NO_ENTITY, position, radius, color, style, Vec3.ZERO, strength));
    }

    public static void lightningArcs(ServerLevel level, List<Vec3> points, int color, long seed) {
        if (points.size() < 2) {
            return;
        }
        Vec3 anchor = points.get(1);
        PacketDistributor.sendToPlayersNear(level, null, anchor.x, anchor.y, anchor.z, EFFECT_RANGE,
                new MessageLightningArcs(points, color, seed));
    }

    public static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    public static void sound(ServerLevel level, Vec3 at, Holder<SoundEvent> sound, float volume, float pitch) {
        sound(level, at, sound.value(), volume, pitch);
    }
}
