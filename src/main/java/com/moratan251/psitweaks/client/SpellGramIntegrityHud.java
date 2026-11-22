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
 * 魔法式オブジェクト（サークル・領域・流星群の夜）の核に照準を合わせると、その名前と残りの耐久値をクロスヘアのすぐ下に出す。
 * 術式解体などで削るときの目安。水色の文字で「加重領域  50」のように名前と今の耐久値だけを出し（上限とバーは出さない）、
 * 核から {@link #RANGE} ブロック以内でだけ出す。
 * <p>
 * リソースを扱う領域（加熱・凍結・荷電・乱流）は、その下にもう 1 行、熱量・冷却量・充電能力・風量（WU）を出す。
 * 量はサーバーが {@code SpellGramZone} の同期データで送る。そのほかの領域は、自分の強さの行（{@code SpellGramZone#hudLines}）を
 * その下に出す（魔法式オブジェクトはどれも {@code SpellGramObject#hudLines} の行を出せる）。
 * <p>
 * 狙いはクライアントの tick ごとに {@link SpellGramAim#aimedAt} で決める（ブロックに遮られていない、視線の先の核のうち最も近いもの）。
 * 覚えるのはエンティティの番号だけで、ログアウトで消す。
 */
public final class SpellGramIntegrityHud {
    /** 文字の色（水色、ChatFormatting.AQUA と同じ）。 */
    private static final int TEXT_COLOR = 0x55FFFF;
    /** 出す距離（ブロック、目から核まで）。 */
    public static final double RANGE = 5.0;
    /** 行の間隔（GUI の座標）。 */
    private static final int LINE_HEIGHT = 10;

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
        int y = graphics.guiHeight() / 2 + 20;
        if (object instanceof com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone zone) {
            String key = zone.resourceKey();
            float amount = zone.getResourceAmount();
            if (key != null && amount >= 0.0F) {
                float used = Math.max(0.0F, zone.getResourceUsed());
                String[] values = zone instanceof com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone
                        ? new String[]{String.format(java.util.Locale.ROOT, "%.1f", used), String.format(java.util.Locale.ROOT, "%.1f", amount)}
                        : withPrefix(used, amount);
                graphics.drawCenteredString(font, Component.translatable(key, values[0], values[1]), graphics.guiWidth() / 2,
                        y, TEXT_COLOR);
                y += LINE_HEIGHT;
                // 熱の受け渡しの運搬・受け取り（0 なら出さない）。桁は総量にそろえる。
                String movedKey = zone.resourceMovedKey();
                float moved = zone.getResourceMoved();
                if (movedKey != null && moved > 0.0F) {
                    graphics.drawCenteredString(font, Component.translatable(movedKey, scaledLike(moved, amount)),
                            graphics.guiWidth() / 2, y, TEXT_COLOR);
                    y += LINE_HEIGHT;
                }
            }
        }
        for (Component line : object.hudLines()) {
            graphics.drawCenteredString(font, line, graphics.guiWidth() / 2, y, TEXT_COLOR);
            y += LINE_HEIGHT;
        }
    }

    /**
     * 使っている量と総量を、総量に合わせた桁でそろえる（「120」と「337 」、「0.25」と「1.00 M」）。総量のほうは単位の接頭辞まで
     * 含み、翻訳の側で単位（J/t・FE/t）を続ける。
     */
    static String[] withPrefix(double used, double total) {
        double scale;
        String prefix;
        String digits;
        if (total >= 1.0E6) {
            scale = 1.0E6;
            prefix = "M";
            digits = "%.2f";
        } else if (total >= 1.0E4) {
            scale = 1.0E3;
            prefix = "k";
            digits = "%.1f";
        } else if (total >= 100.0) {
            scale = 1.0;
            prefix = "";
            digits = "%.0f";
        } else {
            scale = 1.0;
            prefix = "";
            digits = "%.1f";
        }
        return new String[]{String.format(java.util.Locale.ROOT, digits, used / scale),
                String.format(java.util.Locale.ROOT, digits, total / scale) + " " + prefix};
    }

    /** {@code value} を、総量 {@code total} と同じ桁・単位の接頭辞で書く（{@link #withPrefix} の総量の書き方）。 */
    static String scaledLike(double value, double total) {
        String[] values = withPrefix(value, total);
        String totalText = values[1];
        // 総量の文字列の最後は「 」か「 k」「 M」（接頭辞）。それをそのまま使う。
        return values[0] + totalText.substring(totalText.lastIndexOf(' '));
    }

    /** 置いたときの耐久値が 10 以上なら整数（切り上げ）、それより小さければ小数 1 桁。 */
    static String format(float value, float max) {
        return max >= 10.0F ? Integer.toString(Mth.ceil(value)) : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

}
