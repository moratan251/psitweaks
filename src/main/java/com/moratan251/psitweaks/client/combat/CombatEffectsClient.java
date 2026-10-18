package com.moratan251.psitweaks.client.combat;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.combat.EntityGravityWell;
import com.moratan251.psitweaks.common.entities.combat.EntityGroundShockwave;
import com.moratan251.psitweaks.common.network.MessageLightningArcs;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;

/**
 * Client side of the combat spell effects that are not entities: sphere flashes (barrier honeycomb, shock spheres),
 * chain lightning arcs, camera shake and the barrier HUD bar.
 */
public final class CombatEffectsClient {
    private static final int MAX_FLASHES = 64;
    private static final int MAX_ARCS = 32;
    private static final double RUMBLE_SEARCH = 24.0;
    private static final double SHOCKWAVE_RUMBLE_RANGE = 10.0;

    private static final List<SphereFlashEffect> FLASHES = new ArrayList<>();
    private static final List<LightningArcEffect> ARCS = new ArrayList<>();

    private CombatEffectsClient() {
    }

    public static void registerEvents(IEventBus modEventBus) {
        modEventBus.addListener(CombatEffectsClient::onRegisterGuiLayers);
        NeoForge.EVENT_BUS.addListener(CombatEffectsClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(CombatEffectsClient::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(CameraShake::onComputeCameraAngles);
        NeoForge.EVENT_BUS.addListener(CombatEffectsClient::onLoggingOut);
        CocytusIceRenderer.registerEvents();
        ParadeHiddenPlayers.registerEvents();
    }

    public static void handleSphereFlash(MessageSphereFlash message) {
        if (FLASHES.size() >= MAX_FLASHES) {
            FLASHES.remove(0);
        }
        FLASHES.add(new SphereFlashEffect(message));
        switch (message.style()) {
            case SHOCKWAVE -> CameraShake.kickFrom(message.position(), 12.0 + message.radius() * 2.0, 0.35F * message.strength());
            case IMPLOSION -> CameraShake.kickFrom(message.position(), 16.0 + message.radius() * 2.0, 0.55F * message.strength());
            default -> {
            }
        }
    }

    public static void handleLightningArcs(MessageLightningArcs message) {
        if (message.points().size() < 2) {
            return;
        }
        if (ARCS.size() >= MAX_ARCS) {
            ARCS.remove(0);
        }
        ARCS.add(new LightningArcEffect(message));
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, Psitweaks.location("barrier_shield"), BarrierHudLayer::render);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }
        // Like particles, the effects stand still while the game is paused or frozen with /tick freeze.
        if (minecraft.isPaused() || !level.tickRateManager().runsNormally()) {
            return;
        }
        CameraShake.tick();
        if (minecraft.player != null) {
            rumble(level, minecraft.player);
        }
        FLASHES.removeIf(flash -> !flash.tick());
        for (Iterator<LightningArcEffect> iterator = ARCS.iterator(); iterator.hasNext(); ) {
            if (!iterator.next().tick(level)) {
                iterator.remove();
            }
        }
    }

    /** Keeps the camera trembling while a ground shockwave front or a charging gravity well is close by. */
    private static void rumble(ClientLevel level, Player player) {
        AABB area = player.getBoundingBox().inflate(RUMBLE_SEARCH);
        for (EntityGroundShockwave wave : level.getEntitiesOfClass(EntityGroundShockwave.class, area)) {
            if (wave.tickCount > wave.getExpandTicks()) {
                continue;
            }
            double dx = player.getX() - wave.getX();
            double dz = player.getZ() - wave.getZ();
            double fromFront = Math.abs(Math.sqrt(dx * dx + dz * dz) - wave.getRadius(0.0F));
            if (fromFront < SHOCKWAVE_RUMBLE_RANGE && Math.abs(player.getY() - wave.getY()) < 8.0) {
                CameraShake.sustain((float) (0.55 * (1.0 - fromFront / SHOCKWAVE_RUMBLE_RANGE)));
            }
        }
        for (EntityGravityWell well : level.getEntitiesOfClass(EntityGravityWell.class, area)) {
            if (well.tickCount >= EntityGravityWell.CHARGE_TICKS) {
                continue;
            }
            double range = well.getRadius() + 6.0;
            double distance = player.position().distanceTo(well.position());
            if (distance < range) {
                CameraShake.sustain((0.15F + 0.3F * well.getCharge(0.0F)) * (float) (1.0 - distance / range));
            }
        }
    }

    private static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || (FLASHES.isEmpty() && ARCS.isEmpty())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Matrix4f pose = new Matrix4f(event.getPoseStack().last().pose());
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        for (SphereFlashEffect flash : FLASHES) {
            flash.render(level, buffers, pose, camera, partialTick);
        }
        for (LightningArcEffect arc : ARCS) {
            arc.render(buffers, pose, camera, partialTick);
        }
        buffers.endBatch(PsitweaksRenderTypes.TRANSLUCENT_COLOR);
        buffers.endBatch(PsitweaksRenderTypes.ADDITIVE_COLOR);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static void clear() {
        FLASHES.clear();
        ARCS.clear();
        CameraShake.clear();
    }
}
