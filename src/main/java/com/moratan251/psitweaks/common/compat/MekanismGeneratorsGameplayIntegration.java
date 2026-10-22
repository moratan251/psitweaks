package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.handler.PsitweaksAdvancements;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.tile.fusion.TileEntityLaserFocusMatrix;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Mekanism Generators classes; only reached when Mekanism Generators is loaded. */
final class MekanismGeneratorsGameplayIntegration {
    private MekanismGeneratorsGameplayIntegration() {
    }

    /**
     * Called before a laser heats {@code blockEntity}. If it is the Laser Focus Matrix of a formed, unlit Fusion Reactor,
     * checks once this tick is over (the reactor ignites in its own tick, after entities) and awards the caster.
     */
    static void watchFusionIgnition(BlockEntity blockEntity, ServerPlayer caster) {
        if (!(blockEntity instanceof TileEntityLaserFocusMatrix matrix)) {
            return;
        }
        FusionReactorMultiblockData reactor = matrix.getMultiblock();
        if (!reactor.isFormed() || reactor.isBurning()) {
            return;
        }
        MinecraftServer server = caster.server;
        server.tell(new TickTask(server.getTickCount(), () -> {
            if (reactor.isFormed() && reactor.isBurning() && !caster.hasDisconnected()) {
                PsitweaksAdvancements.award(caster, PsitweaksAdvancements.PHONON_MASER_FUSION_IGNITION);
            }
        }));
    }
}
