package com.moratan251.psitweaksqol.client.gui;

import com.moratan251.psitweaksqol.client.snippet.SpellPieceLayout;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellPieceGroup;
import vazkii.psi.client.gui.GuiProgrammer;
import vazkii.psi.common.core.handler.PlayerData;
import vazkii.psi.common.core.handler.PlayerDataHandler;

/**
 * ピースの並びを盤面へ置く。今のプレイヤーが使えないピース（見つからない・無効・未解禁・未習得）はそのマスだけ飛ばし、
 * 飛ばしたピースをチャットに出す。置けたときは元に戻す 1 回分として記録し、Psi の通常の経路（{@code onSpellChanged}）で
 * サーバーへ送る。
 */
public final class SpellPieceLayoutPlacer {
    /** ピースを置けるか。置けない理由は Psi の貼り付け・読み込みが確かめる順。 */
    public enum Usability {
        USABLE(null),
        UNKNOWN("psitweaks_qol.paste.reason.unknown"),
        DISABLED("psitweaks_qol.paste.reason.disabled"),
        LOCKED("psitweaks_qol.paste.reason.locked"),
        NOT_LEARNED("psitweaks_qol.paste.reason.not_learned");

        private final String translationKey;

        Usability(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    private SpellPieceLayoutPlacer() {
    }

    /** 並びの左上を ({@code originX}, {@code originY}) に合わせたとき、全部が盤面に収まるか。 */
    public static boolean fits(SpellPieceLayout layout, int originX, int originY) {
        return !layout.isEmpty()
                && SpellGrid.exists(originX, originY)
                && SpellGrid.exists(originX + layout.width() - 1, originY + layout.height() - 1);
    }

    /** 並びの各ピースを今のプレイヤーが置けるか。{@link SpellPieceLayout#entries()} と同じ順に返す。 */
    public static List<Usability> usability(SpellPieceLayout layout) {
        LocalPlayer player = Minecraft.getInstance().player;
        Spell spell = new Spell();
        List<Usability> result = new ArrayList<>(layout.pieceCount());
        for (SpellPieceLayout.Entry entry : layout.entries()) {
            result.add(usability(player, createPiece(spell, entry)));
        }
        return List.copyOf(result);
    }

    /**
     * 並びの左上を ({@code originX}, {@code originY}) に合わせて置く。並びの空きマスと、使えないピースのマスは盤面を変えない。
     * 盤面からはみ出すとき、置けるピースが 1 つもないときは何もせずエラー音を鳴らす。
     */
    public static boolean place(GuiProgrammer screen, SpellPieceLayout layout, int originX, int originY) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (screen.spell == null || layout.isEmpty() || player == null) {
            return false;
        }
        if (!fits(layout, originX, originY)) {
            ClientGuiSounds.playError();
            return false;
        }

        List<SpellPiece> pieces = new ArrayList<>(layout.pieceCount());
        Map<Usability, Set<String>> skipped = new EnumMap<>(Usability.class);
        int skippedCount = 0;
        for (SpellPieceLayout.Entry entry : layout.entries()) {
            SpellPiece piece = createPiece(screen.spell, entry);
            Usability usability = usability(player, piece);
            if (usability != Usability.USABLE) {
                String name = piece == null
                        ? SpellPieceLayout.pieceKey(entry.pieceData())
                        : Component.translatable(piece.getUnlocalizedName()).getString();
                skipped.computeIfAbsent(usability, ignored -> new LinkedHashSet<>()).add(name);
                skippedCount++;
                continue;
            }
            piece.x = originX + entry.offsetX();
            piece.y = originY + entry.offsetY();
            piece.isInGrid = true;
            pieces.add(piece);
        }

        if (pieces.isEmpty()) {
            sendSkipped(player, "psitweaks_qol.paste.nothing", skippedCount, skipped, ChatFormatting.RED);
            ClientGuiSounds.playError();
            return false;
        }

        screen.pushState(true);
        for (SpellPiece piece : pieces) {
            screen.spell.grid.gridData[piece.x][piece.y] = piece;
        }
        screen.onSpellChanged(false);
        if (skippedCount > 0) {
            sendSkipped(player, "psitweaks_qol.paste.skipped", skippedCount, skipped, ChatFormatting.GOLD);
        }
        return true;
    }

    private static SpellPiece createPiece(Spell spell, SpellPieceLayout.Entry entry) {
        try {
            return SpellPiece.createFromNBT(spell, entry.pieceData().copy());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Psi 本体の貼り付けと読み込みと同じ確かめ方。プレイヤーがいなければ使えないものとする。 */
    private static Usability usability(LocalPlayer player, SpellPiece piece) {
        if (piece == null) {
            return Usability.UNKNOWN;
        }
        if (player == null) {
            return Usability.LOCKED;
        }
        RegistryAccess registryAccess = player.registryAccess();
        ResourceLocation key = piece.getRegistryKey();
        if (!PsiAPI.isPieceEnabled(registryAccess, key)) {
            return Usability.DISABLED;
        }
        if (PsiAPI.isPieceLocked(player, registryAccess, key)) {
            return Usability.LOCKED;
        }
        Optional<Holder.Reference<SpellPieceGroup>> group = PsiAPI.getPieceGroup(registryAccess, key);
        PlayerData data = PlayerDataHandler.get(player);
        if (group.isPresent() && !player.isCreative() && !data.isPieceGroupUnlocked(group.get().key().location(), key)) {
            return Usability.NOT_LEARNED;
        }
        return Usability.USABLE;
    }

    /** 1 行目に件数、続けて理由ごとに 1 行ずつピースの名前を並べる。 */
    private static void sendSkipped(
            LocalPlayer player,
            String headerKey,
            int skippedCount,
            Map<Usability, Set<String>> skipped,
            ChatFormatting color
    ) {
        player.sendSystemMessage(Component.translatable(headerKey, skippedCount).withStyle(color));
        for (Map.Entry<Usability, Set<String>> reason : skipped.entrySet()) {
            MutableComponent line = Component.literal(" - ")
                    .append(Component.translatable(reason.getKey().translationKey))
                    .append(": ")
                    .append(String.join(", ", reason.getValue()));
            player.sendSystemMessage(line.withStyle(ChatFormatting.GRAY));
        }
    }
}
