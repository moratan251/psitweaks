package com.moratan251.psitweaks.client.combat;

import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.spells.combat.ParadeSight;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Trick: Parade hides its caster: while their lookalike is around, other players do not see the caster's body, gear or
 * name at all, and the caster counts as invisible to them ({@link ParadeSight}), which other mods' overlays respect.
 * Team mates still see them. The set of hidden players is rebuilt from the loaded lookalikes every tick, so nothing
 * outlives the lookalike or the connection.
 */
public final class ParadeHiddenPlayers {
    private static final Set<UUID> HIDDEN = new HashSet<>();

    private ParadeHiddenPlayers() {
    }

    public static void registerEvents() {
        ParadeSight.install(ParadeHiddenPlayers::isHiddenFrom);
        NeoForge.EVENT_BUS.addListener(ParadeHiddenPlayers::onClientTick);
        NeoForge.EVENT_BUS.addListener(ParadeHiddenPlayers::onRenderPlayer);
        NeoForge.EVENT_BUS.addListener(ParadeHiddenPlayers::onLoggingOut);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        HIDDEN.clear();
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof EntityParadeDecoy decoy && decoy.isAlive() && decoy.getOwnerUUID() != null) {
                HIDDEN.add(decoy.getOwnerUUID());
            }
        }
    }

    private static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer != null && isHiddenFrom(event.getEntity(), viewer)) {
            event.setCanceled(true);
        }
    }

    /** Whether {@code entity} is a Parade caster hidden from {@code viewer}: not the viewer and not a team mate. */
    public static boolean isHiddenFrom(Entity entity, Player viewer) {
        return entity instanceof Player player && player != viewer && HIDDEN.contains(player.getUUID())
                && !viewer.isAlliedTo(player);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        HIDDEN.clear();
    }
}
