package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.config.PsitweaksClientConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramAim;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;


/**
 * 魔法式オブジェクト（サークル・領域・流星群の夜）の核に照準を合わせると、その名前と残りの耐久値をクロスヘアのすぐ下に出す
 * （2026-11-12 ユーザー）。術式解体などで削るときの目安。2026-11-14 ユーザー: 水色の文字で「加重領域  50」のように名前と今の
 * 耐久値だけを出し（上限とバーは出さない）、核から {@link #RANGE} ブロック以内でだけ出す。
 * <p>
 * 狙いはクライアントの tick ごとに {@link SpellGramAim#aimedAt} で決める（ブロックに遮られていない、視線の先の核のうち最も近いもの）。
 * 覚えるのはエンティティの番号だけで、ログアウトで消す。
 */
public final class SpellGramIntegrityHud {
    /** 文字の色（水色、ChatFormatting.AQUA と同じ）。 */
    private static final int TEXT_COLOR = 0x55FFFF;
    /** 出す距離（ブロック、目から核まで）。 */
    public static final double RANGE = 5.0;

    /** 照準が合っている魔法式オブジェクトのエンティティの番号（なければ -1）。 */
    private static int targetId = -1;

    private SpellGramIntegrityHud() {
    }

    public static void registerEvents(IEventBus modEventBus) {
        modEventBus.addListener(SpellGramIntegrityHud::onRegisterGuiLayers);
        NeoForge.EVENT_BUS.addListener(SpellGramIntegrityHud::onClientTick);
        NeoForge.EVENT_BUS.addListener(SpellGramIntegrityHud::onLoggingOut);
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, Psitweaks.location("spellgram_integrity"), SpellGramIntegrityHud::render);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        targetId = -1;
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Entity camera = minecraft.getCameraEntity();
        if (level == null || camera == null || !PsitweaksClientConfig.CLIENT.spellGramIntegrityHud.get()) {
            targetId = -1;
            return;
        }
        SpellGramObject target = SpellGramAim.aimedAt(level, camera, 1.0F, RANGE);
        targetId = target == null ? -1 : target.getId();
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (targetId < 0 || minecraft.options.hideGui || minecraft.level == null
                || !(minecraft.level.getEntity(targetId) instanceof SpellGramObject object) || object.isRemoved()) {
            return;
        }
        Font font = minecraft.font;
        Component text = Component.translatable("psitweaks.hud.spellgram_integrity", object.getName(),
                format(object.getIntegrity(), object.getMaxIntegrity()));
        graphics.drawCenteredString(font, text, graphics.guiWidth() / 2, graphics.guiHeight() / 2 + 10, TEXT_COLOR);
    }

    /** 置いたときの耐久値が 10 以上なら整数（切り上げ）、それより小さければ小数 1 桁。 */
    static String format(float value, float max) {
        return max >= 10.0F ? Integer.toString(Mth.ceil(value)) : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

}
