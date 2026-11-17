package com.moratan251.psitweaksqol.client.gui;

import com.moratan251.psitweaksqol.client.snippet.SpellPieceLayout;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.render.spell.SpellPieceRenderer;

/**
 * スニペットのピースを Psi と同じ絵で描く。描くためだけの術式にピースを並べ（矢印の位置を隣のピースで決めるため）、
 * 座標は並びの左上を (0, 0) にする。知らないピースは「?」の四角で描く。
 * 今のプレイヤーが使えないピース（貼り付けで飛ばすもの）は、作ったときに調べて覚えておく。
 */
final class SpellSnippetPreview {
    static final int CELL_SIZE = 18;
    private static final int PIECE_SIZE = 16;
    private static final int UNKNOWN_FILL_COLOR = 0xFF5A1E1E;
    private static final int UNKNOWN_TEXT_COLOR = 0xFFFF8080;
    private static final int UNUSABLE_SHADE_COLOR = 0xB0000000;
    private static final int UNUSABLE_MARK_COLOR = 0xFFFF6060;

    private final SpellPieceLayout layout;
    /** {@code layout.entries()} と同じ順。知らないピースは null。 */
    private final List<SpellPiece> pieces;
    private final List<SpellPieceLayoutPlacer.Usability> usability;

    private SpellSnippetPreview(SpellPieceLayout layout, List<SpellPiece> pieces, List<SpellPieceLayoutPlacer.Usability> usability) {
        this.layout = layout;
        this.pieces = pieces;
        this.usability = usability;
    }

    static SpellSnippetPreview of(SpellPieceLayout layout) {
        Spell spell = new Spell();
        List<SpellPiece> pieces = new ArrayList<>(layout.pieceCount());
        for (SpellPieceLayout.Entry entry : layout.entries()) {
            SpellPiece piece;
            try {
                piece = SpellPiece.createFromNBT(spell, entry.pieceData().copy());
            } catch (RuntimeException e) {
                piece = null;
            }
            if (piece != null) {
                piece.x = entry.offsetX();
                piece.y = entry.offsetY();
                piece.isInGrid = true;
                spell.grid.gridData[piece.x][piece.y] = piece;
            }
            pieces.add(piece);
        }
        return new SpellSnippetPreview(layout, pieces, SpellPieceLayoutPlacer.usability(layout));
    }

    SpellPieceLayout layout() {
        return layout;
    }

    /** {@code layout().entries()} の {@code index} 番目のピースを貼り付けで置けるか。 */
    boolean isUsable(int index) {
        return usability.get(index) == SpellPieceLayoutPlacer.Usability.USABLE;
    }

    int unusableCount() {
        return (int) usability.stream().filter(value -> value != SpellPieceLayoutPlacer.Usability.USABLE).count();
    }

    /**
     * 並びの左上のマスを ({@code left}, {@code top}) に合わせ、{@code scale} 倍で描く。{@code alpha} はピースの絵の濃さ。
     * {@code showUnusable} が true なら使えないピースも描いて暗くし、false なら描かない。
     * 描く前に、それまでに積んだ描画を先に流す（色の掛け合わせがほかの描画に移らないように）。
     */
    void draw(GuiGraphics guiGraphics, float left, float top, float scale, float alpha, boolean showUnusable) {
        guiGraphics.flush();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(left, top, 0.0F);
        pose.scale(scale, scale, 1.0F);

        List<SpellPieceLayout.Entry> entries = layout.entries();
        if (showUnusable) {
            for (int i = 0; i < entries.size(); i++) {
                if (pieces.get(i) == null) {
                    int x = entries.get(i).offsetX() * CELL_SIZE;
                    int y = entries.get(i).offsetY() * CELL_SIZE;
                    guiGraphics.fill(x, y, x + PIECE_SIZE, y + PIECE_SIZE, UNKNOWN_FILL_COLOR);
                    guiGraphics.drawCenteredString(Minecraft.getInstance().font, "?", x + PIECE_SIZE / 2, y + 4, UNKNOWN_TEXT_COLOR);
                }
            }
            guiGraphics.flush();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        MultiBufferSource.BufferSource buffers = guiGraphics.bufferSource();
        for (int i = 0; i < entries.size(); i++) {
            SpellPiece piece = pieces.get(i);
            if (piece == null || (!showUnusable && !isUsable(i))) {
                continue;
            }
            pose.pushPose();
            pose.translate((float) (piece.x * CELL_SIZE), (float) (piece.y * CELL_SIZE), 0.0F);
            SpellPieceRenderer.draw(piece, pose, buffers, LightTexture.FULL_BRIGHT);
            pose.popPose();
        }
        buffers.endBatch();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (showUnusable) {
            pose.pushPose();
            pose.translate(0.0F, 0.0F, 1.0F);
            for (int i = 0; i < entries.size(); i++) {
                if (pieces.get(i) != null && !isUsable(i)) {
                    drawUnusableMark(guiGraphics, entries.get(i).offsetX() * CELL_SIZE, entries.get(i).offsetY() * CELL_SIZE);
                }
            }
            pose.popPose();
        }
        pose.popPose();
    }

    /** 使えないピースのマスを暗くし、「×」を付ける。座標は左上の画素。 */
    static void drawUnusableMark(GuiGraphics guiGraphics, int left, int top) {
        guiGraphics.fill(left, top, left + PIECE_SIZE, top + PIECE_SIZE, UNUSABLE_SHADE_COLOR);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "×", left + PIECE_SIZE / 2, top + 4, UNUSABLE_MARK_COLOR);
    }
}
