package com.moratan251.psitweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.common.entities.SpellGram.region.EntitySpellGramRegion;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.client.render.entity.RenderSpellCircle;

import java.util.ArrayList;
import java.util.List;

/** 領域型の魔法式オブジェクト（試作）。Psi の術式円を半径に合わせて描き、性質が複数あれば色を順に切り替える。 */
public class SpellGramRegionRenderer extends EntityRenderer<EntitySpellGramRegion> {
    private static final int COLOR_PERIOD_TICKS = 40;

    public SpellGramRegionRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntitySpellGramRegion entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        float radius = entity.getDamageRadius();
        if (radius <= 0.0F) {
            return;
        }
        float alive = entity.tickCount + partialTicks;
        RenderSpellCircle.renderSpellCircle(alive, 1.0F, radius * 0.5F, 0.0F, 1.0F, 0.0F, color(entity), poseStack, buffer);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static int color(EntitySpellGramRegion entity) {
        List<SpellGramProperty> shown = new ArrayList<>();
        for (SpellGramProperty property : SpellGramProperty.values()) {
            if (entity.hasProperty(property)) {
                shown.add(property);
            }
        }
        if (shown.isEmpty()) {
            return entity.getColor();
        }
        return shown.get((entity.tickCount / COLOR_PERIOD_TICKS) % shown.size()).color();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntitySpellGramRegion entity) {
        return ResourceLocation.fromNamespaceAndPath("psi", "textures/misc/spell_circle0.png");
    }
}
