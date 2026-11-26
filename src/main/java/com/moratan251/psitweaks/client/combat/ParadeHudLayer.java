package com.moratan251.psitweaks.client.combat;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.config.PsitweaksClientConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityParadeCore;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A bar above the armour row (above Trick: Barrier's bar when both show) for the local player's own Trick: Parade:
 * fill = the core's integrity / its highest, text = integrity / highest and the seconds left, with the Parade piece's
 * icon to the left so it is not taken for the barrier. It blinks during the last three seconds, like the barrier's.
 */
final class ParadeHudLayer {
    private static final ResourceLocation ICON = Psitweaks.location("textures/spell/trick_parade.png");
    private static final int WIDTH = 81;
    private static final int HEIGHT = 9;
    private static final int ICON_SIZE = 9;
    private static final int ICON_TEXTURE_SIZE = 16;
    private static final int EXPIRY_WARNING_TICKS = 60;

    private ParadeHudLayer() {
    }

    static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()
                || minecraft.player == null || minecraft.getCameraEntity() != minecraft.player
                || !PsitweaksClientConfig.CLIENT.paradeHud.get()) {
            return;
        }
        EntityParadeCore core = ParadeHiddenPlayers.ownCore();
        if (core == null || core.isEnded()) {
            return;
        }

        int left = graphics.guiWidth() / 2 - 91;
        int top = graphics.guiHeight() - minecraft.gui.leftHeight;
        minecraft.gui.leftHeight += HEIGHT + 1;

        float integrity = core.getIntegrity();
        float highest = Math.max(core.getMaxIntegrity(), integrity);
        int remaining = core.getRemainingTicks();
        float alpha = 1.0F;
        if (remaining < EXPIRY_WARNING_TICKS) {
            float time = minecraft.player.level().getGameTime() + deltaTracker.getGameTimeDeltaPartialTick(false);
            alpha = 0.55F + 0.45F * Mth.cos(time * 0.6F);
        }
        EntityParadeDecoy image = core.getImage();
        int color = GlowDrawing.glowColor(image != null ? image.getColor() : CombatSpellHelper.DEFAULT_SPELL_COLOR);
        int frame = argb(alpha * 0.85F, 0x101418);
        int fill = argb(alpha * 0.9F, color);
        int shine = argb(alpha * 0.9F, GlowDrawing.whiten(color, 0.5F));

        graphics.blit(ICON, left - ICON_SIZE - 1, top, ICON_SIZE, ICON_SIZE, 0.0F, 0.0F,
                ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, frame);
        float fraction = highest > 0.0F ? Mth.clamp(integrity / highest, 0.0F, 1.0F) : 0.0F;
        int filled = Mth.ceil((WIDTH - 2) * fraction);
        if (filled > 0) {
            graphics.fill(left + 1, top + 1, left + 1 + filled, top + HEIGHT - 1, fill);
            graphics.fill(left + 1, top + 1, left + 1 + filled, top + 2, shine);
        }
        int secondsLeft = (remaining + 19) / 20;
        String text = Mth.ceil(integrity) + " / " + Mth.ceil(highest) + "  " + secondsLeft + "s";
        int textWidth = minecraft.font.width(text);
        int textColor = argb(alpha, 0xFFFFFF);
        if (textWidth <= WIDTH - 4) {
            graphics.drawString(minecraft.font, text, left + (WIDTH - textWidth) / 2, top + 1, textColor, true);
        } else {
            // Very high integrity: shrink the text to fit inside the bar.
            float scale = (WIDTH - 4) / (float) textWidth;
            graphics.pose().pushPose();
            graphics.pose().translate(left + WIDTH / 2.0F, top + HEIGHT / 2.0F, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(minecraft.font, text, -textWidth / 2, -4, textColor, true);
            graphics.pose().popPose();
        }
    }

    private static int argb(float alpha, int rgb) {
        return Mth.clamp((int) (alpha * 255.0F), 0, 255) << 24 | rgb & 0xFFFFFF;
    }
}
