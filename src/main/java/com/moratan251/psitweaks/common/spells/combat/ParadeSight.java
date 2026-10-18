package com.moratan251.psitweaks.common.spells.combat;

import java.util.function.BiPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Client-side hook for Trick: Parade: whether a player is hidden from the local viewer. Common code (the
 * {@code isInvisibleTo} mixin) asks here; the client installs the real check, so dedicated servers never load
 * client classes and always answer "not hidden".
 */
public final class ParadeSight {
    private static volatile BiPredicate<Entity, Player> hiddenFrom = (entity, viewer) -> false;

    private ParadeSight() {
    }

    public static void install(BiPredicate<Entity, Player> check) {
        hiddenFrom = check;
    }

    public static boolean isHiddenFrom(Entity entity, Player viewer) {
        return hiddenFrom.test(entity, viewer);
    }
}
