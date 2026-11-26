package com.moratan251.psitweaks.common.spells.combat;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Client-side hooks for Trick: Parade: whether a player is hidden from a viewer or from the local player, and whether a
 * UUID is the local player's. Common code (the {@code isInvisibleTo} and particle mixins, the image's picking) asks
 * here; the client installs the real checks, so dedicated servers never load client classes and always answer "no".
 */
public final class ParadeSight {
    private static volatile BiPredicate<Entity, Player> hiddenFrom = (entity, viewer) -> false;
    private static volatile Predicate<UUID> localPlayer = id -> false;
    private static volatile Predicate<Entity> hiddenFromLocal = entity -> false;

    private ParadeSight() {
    }

    public static void install(BiPredicate<Entity, Player> hiddenCheck, Predicate<UUID> localPlayerCheck,
                               Predicate<Entity> hiddenFromLocalCheck) {
        hiddenFrom = hiddenCheck;
        localPlayer = localPlayerCheck;
        hiddenFromLocal = hiddenFromLocalCheck;
    }

    public static boolean isHiddenFrom(Entity entity, Player viewer) {
        return hiddenFrom.test(entity, viewer);
    }

    /** Client side: whether {@code entity} is a Parade caster hidden from the local player. */
    public static boolean isHiddenFromLocalPlayer(Entity entity) {
        return hiddenFromLocal.test(entity);
    }

    /** Client side: whether {@code id} is the local player's UUID. */
    public static boolean isLocalPlayer(@Nullable UUID id) {
        return id != null && localPlayer.test(id);
    }
}
