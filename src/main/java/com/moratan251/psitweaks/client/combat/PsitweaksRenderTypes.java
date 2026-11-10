package com.moratan251.psitweaks.client.combat;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Render types for the glowing combat effects. None of them write depth, so overlapping glows add up cleanly. */
public final class PsitweaksRenderTypes {
    /** Additive, untextured. Colour channels are the light added; alpha is ignored. */
    public static final RenderType ADDITIVE_COLOR = RenderType.create(
            "psitweaks_additive_color",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            4096,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    /** Alpha-blended, untextured: used for the dark core of Micro Black Hole. */
    public static final RenderType TRANSLUCENT_COLOR = RenderType.create(
            "psitweaks_translucent_color",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            4096,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private static final Function<ResourceLocation, RenderType> ADDITIVE_TEXTURED = Util.memoize(texture -> RenderType.create(
            "psitweaks_additive_textured",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            8192,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)));

    private static final RenderStateShard.ShaderStateShard POSITION_TEX_COLOR_SHADER =
            new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader);

    /**
     * Additive, textured, unlit, linear filtering: the texture's colour times the vertex colour is added to the scene. One
     * per texture; the textures are a fixed set, so the memo stays bounded.
     */
    private static final Function<ResourceLocation, RenderType> ADDITIVE_LINEAR_TEXTURE = Util.memoize(texture -> RenderType.create(
            "psitweaks_additive_linear_texture",
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_TEX_COLOR_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, true, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)));

    private PsitweaksRenderTypes() {
    }

    /**
     * Additive and textured without lightmap (POSITION_TEX_COLOR), with linear filtering and no mipmaps: the caller picks
     * a texture of about the size it appears on screen.
     */
    public static RenderType additiveLinearTexture(ResourceLocation texture) {
        return ADDITIVE_LINEAR_TEXTURE.apply(texture);
    }

    /** Additive and textured: the texture's colour times the vertex colour is added to the scene. */
    public static RenderType additiveTextured(ResourceLocation texture) {
        return ADDITIVE_TEXTURED.apply(texture);
    }
}
