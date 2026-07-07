package com.moratan251.psitweaks.client.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import net.minecraft.client.renderer.MultiBufferSource;

public final class ModeOverlayRenderer {
    private ModeOverlayRenderer() {
    }

    public static void drawModeOverlay(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            PsitweaksModeOption mode
    ) {
        // Psi 1.20.1 does not expose the 1.21.1 spell-piece material registry used for mode badges.
    }
}