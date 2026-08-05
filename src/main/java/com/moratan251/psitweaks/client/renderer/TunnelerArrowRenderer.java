package com.moratan251.psitweaks.client.renderer;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.EntityTunnelerArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TunnelerArrowRenderer extends ArrowRenderer<EntityTunnelerArrow> {
    private static final ResourceLocation TEXTURE = Psitweaks.location("textures/entity/projectiles/tunneler.png");

    public TunnelerArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTunnelerArrow entity) {
        return TEXTURE;
    }
}
