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
 * リソースを扱う領域（加熱・凍結・荷電・乱流）は、その下にもう 1 行、熱量・冷却量・充電能力・風量（WU）を出す
 * （2026-11-15 ユーザー）。量はサーバーが {@code SpellGramZone} の同期データで送る。
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
        if (object instanceof com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone zone) {
            String key = zone.resourceKey();
            float amount = zone.getResourceAmount();
            if (key != null && amount >= 0.0F) {
                String value = zone instanceof com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone
                        ? String.format(java.util.Locale.ROOT, "%.1f", amount) : withPrefix(amount);
                graphics.drawCenteredString(font, Component.translatable(key, value), graphics.guiWidth() / 2,
                        graphics.guiHeight() / 2 + 20, TEXT_COLOR);
            }
        }
    }

    /** 単位の前に付ける接頭辞つきの数（「337 」「12.5 k」「1.00 M」）。翻訳の側で単位（J/t・FE/t）を続ける。 */
    static String withPrefix(double value) {
        if (value >= 1.0E6) {
            return String.format(java.util.Locale.ROOT, "%.2f M", value / 1.0E6);
        }
        if (value >= 1.0E4) {
            return String.format(java.util.Locale.ROOT, "%.1f k", value / 1.0E3);
        }
        if (value >= 100.0) {
            return String.format(java.util.Locale.ROOT, "%.0f ", value);
        }
        return String.format(java.util.Locale.ROOT, "%.1f ", value);
    }

    /** 置いたときの耐久値が 10 以上なら整数（切り上げ）、それより小さければ小数 1 桁。 */
    static String format(float value, float max) {
        return max >= 10.0F ? Integer.toString(Mth.ceil(value)) : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

}
