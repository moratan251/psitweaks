package com.moratan251.psitweaks.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.ProgramResearcherBlockEntity;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.block.attribute.Attribute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;

/**
 * 稼働中のプログラム研究台の上に、研究中のプログラムのカードを浮かべる。カードの枠はブロックのテクスチャ
 * {@code hologram_card}、中の記号は完成品のアイテムテクスチャから {@link ProgramHologramGlyphs} で取り出す。
 * 完成品がプログラムでないときや記号を取り出せないときは何も描かない。
 * <p>
 * 寸法はモデルの画素単位（1 ブロック = 16）で、北向き（モニターが北を向く）の研究台を基準にする。
 */
public class RenderProgramResearcher extends MekanismTileEntityRenderer<ProgramResearcherBlockEntity> {
    private static final ResourceLocation CARD_FRAME = Psitweaks.location("block/program_researcher/hologram_card");

    /** カードは天板（y = 10）の上の 8 × 8 の板。テクスチャの上端がモニター側、左端が東になる。 */
    private static final float CARD_EAST = 12.0F;
    private static final float CARD_SIZE = 8.0F;
    private static final float CARD_FAR = 10.5F;
    private static final float CARD_Y = 12.0F;
    /** カードの中心を通る東西の軸で、モニター側を持ち上げる角度。 */
    private static final float CARD_TILT = 22.5F;
    private static final float CARD_PIVOT_Z = CARD_FAR - CARD_SIZE / 2.0F;
    /** 記号を枠と走査線より手前に置き、重なりのちらつきを防ぐ。 */
    private static final float GLYPH_LIFT = 0.05F;
    private static final float BOB_AMPLITUDE = 0.25F;
    private static final int BOB_PERIOD_TICKS = 60;

    public RenderProgramResearcher(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(ProgramResearcherBlockEntity researcher, float partialTick, PoseStack matrix, MultiBufferSource renderer,
                          int light, int overlayLight, ProfilerFiller profiler) {
        Level level = researcher.getLevel();
        if (level == null || !Attribute.isActive(researcher.getBlockState())) {
            return;
        }
        ProgramHologramGlyphs.Glyph glyph = ProgramHologramGlyphs.get(researcher.getResearchingItem());
        if (glyph == null) {
            return;
        }
        TextureAtlasSprite frame = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CARD_FRAME);
        float time = (level.getGameTime() % BOB_PERIOD_TICKS) + partialTick;
        float bob = Mth.sin(time / BOB_PERIOD_TICKS * Mth.TWO_PI) * BOB_AMPLITUDE;

        matrix.pushPose();
        matrix.translate(0.5F, 0.0F, 0.5F);
        matrix.mulPose(Axis.YP.rotationDegrees(180.0F - researcher.getDirection().toYRot()));
        matrix.translate(-0.5F, 0.0F, -0.5F);
        matrix.scale(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);
        matrix.translate(0.0F, CARD_Y + bob, CARD_PIVOT_Z);
        matrix.mulPose(Axis.XP.rotationDegrees(-CARD_TILT));
        matrix.translate(0.0F, 0.0F, -CARD_PIVOT_Z);

        VertexConsumer buffer = renderer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose pose = matrix.last();
        quad(buffer, pose, CARD_EAST, CARD_FAR, CARD_EAST - CARD_SIZE, CARD_FAR - CARD_SIZE, 0.0F,
                frame.getU0(), frame.getV0(), frame.getU1(), frame.getV1(), 0xFFFFFFFF);

        TextureAtlasSprite icon = glyph.sprite();
        int size = glyph.size();
        float cell = CARD_SIZE / size;
        int[] pixels = glyph.pixels();
        int[] tints = glyph.tints();
        for (int i = 0; i < pixels.length; i++) {
            int x = pixels[i] & 0xFF;
            int y = pixels[i] >> 8;
            // 1 画素の四角形には、その画素の中心だけを貼る。
            float u = icon.getU((x + 0.5F) / size);
            float v = icon.getV((y + 0.5F) / size);
            float east = CARD_EAST - x * cell;
            float far = CARD_FAR - y * cell;
            quad(buffer, pose, east, far, east - cell, far - cell, GLYPH_LIFT, u, v, u, v, tints[i]);
        }
        matrix.popPose();
    }

    /** y を高さとする水平な四角形。(east, far) に (u0, v0)、(west, near) に (u1, v1) を貼る。 */
    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, float east, float far, float west, float near, float y,
                             float u0, float v0, float u1, float v1, int color) {
        vertex(buffer, pose, east, y, far, u0, v0, color);
        vertex(buffer, pose, west, y, far, u1, v0, color);
        vertex(buffer, pose, west, y, near, u1, v1, color);
        vertex(buffer, pose, east, y, near, u0, v1, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    protected String getProfilerSection() {
        return "psitweaks_program_researcher";
    }
}
