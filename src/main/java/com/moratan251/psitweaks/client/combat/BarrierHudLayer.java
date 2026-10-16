package com.moratan251.psitweaks.client.combat;

import com.moratan251.psitweaks.client.config.PsitweaksClientConfig;
import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.common.spells.combat.BarrierShield;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * A bar above the armour row showing the shield of Trick: Barrier: fill = remaining / raised amount,
 * text = remaining points. It blinks during the last three seconds.
 */
final class BarrierHudLayer {
    private static final int WIDTH = 81;
    private static final int HEIGHT = 9;
    private static final int EXPIRY_WARNING_TICKS = 60;

    private BarrierHudLayer() {
    }

    static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()
                || !PsitweaksClientConfig.CLIENT.barrierHud.get()) {
            return;
        }
        if (!(minecraft.getCameraEntity() instanceof Player player)) {
            return;
        }
        BarrierShield shield = player.getExistingDataOrNull(PsitweaksAttachments.BARRIER_SHIELD);
        long now = player.level().getGameTime();
        if (shield == null || !shield.isActive(now)) {
            return;
        }

        int left = graphics.guiWidth() / 2 - 91;
        int top = graphics.guiHeight() - minecraft.gui.leftHeight;
        minecraft.gui.leftHeight += HEIGHT + 1;

        float alpha = 1.0F;
        long remaining = shield.expiresAt() - now;
        if (remaining < EXPIRY_WARNING_TICKS) {
            float time = now + deltaTracker.getGameTimeDeltaPartialTick(false);
            alpha = 0.55F + 0.45F * Mth.cos(time * 0.6F);
        }
        int color = GlowDrawing.glowColor(shield.color());
        int frame = argb(alpha * 0.85F, 0x101418);
        int fill = argb(alpha * 0.9F, color);
        int shine = argb(alpha * 0.9F, GlowDrawing.whiten(color, 0.5F));

        graphics.fill(left, top, left + WIDTH, top + HEIGHT, frame);
        int filled = Mth.ceil((WIDTH - 2) * shield.fraction());
        if (filled > 0) {
            graphics.fill(left + 1, top + 1, left + 1 + filled, top + HEIGHT - 1, fill);
            graphics.fill(left + 1, top + 1, left + 1 + filled, top + 2, shine);
        }
        String text = Mth.ceil(shield.amount()) + " / " + Mth.ceil(shield.maxAmount());
        int textWidth = minecraft.font.width(text);
        graphics.drawString(minecraft.font, text, left + (WIDTH - textWidth) / 2, top + 1, argb(alpha, 0xFFFFFF), true);
    }

    private static int argb(float alpha, int rgb) {
        return Mth.clamp((int) (alpha * 255.0F), 0, 255) << 24 | rgb & 0xFFFFFF;
    }
}
