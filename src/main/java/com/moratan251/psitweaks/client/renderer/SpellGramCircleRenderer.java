package com.moratan251.psitweaks.client.renderer;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramCircle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.client.render.entity.RenderSpellCircle;

/** 円の魔法式オブジェクト（{@link SpellGramCircle}）を Psi の魔法陣で、円ごとの色で描く。 */
public class SpellGramCircleRenderer<T extends SpellGramCircle> extends EntityRenderer<T> {

    /** ファイアサークルの色。 */
    public static final int FLARE_COLOR = 0xFF0000;
    /** アイスサークルの色。 */
    public static final int ICE_COLOR = 0x00CCFF;

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("psi", "textures/misc/spell_circle0.png");

    private final int color;

    public SpellGramCircleRenderer(EntityRendererProvider.Context context, int color) {
        super(context);
        this.color = color;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        float horizontalScale = entity.getVisualScale();
        if (horizontalScale <= 0.0F) {
            return;
        }

        float alive = entity.tickCount + partialTicks;
        poseStack.pushPose();
        Vec3 offset = followOffset(entity, partialTicks);
        if (offset != null) {
            poseStack.translate(offset.x, offset.y, offset.z);
        }
        RenderSpellCircle.renderSpellCircle(
                alive,
                1.0F,
                horizontalScale,
                0.0F,
                1.0F,
                0.0F,
                color,
                poseStack,
                buffer
        );
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * 追従中の円を、追従先の補間した位置に描くためのずれ。円は自分の tick で追従先の位置へ移るので、追従先が後から動いた tick は
     * 1 tick 遅れて見える。追従先がクライアントで見つからなければ null（円の位置のまま描く）。
     */
    private static Vec3 followOffset(SpellGramCircle circle, float partialTicks) {
        if (!circle.isEntityFollowMode()) {
            return null;
        }
        Entity target = circle.getFollowTargetEntity();
        if (target == null) {
            return null;
        }
        return new Vec3(
                Mth.lerp(partialTicks, target.xo, target.getX()) - Mth.lerp(partialTicks, circle.xo, circle.getX()),
                Mth.lerp(partialTicks, target.yo, target.getY()) - Mth.lerp(partialTicks, circle.yo, circle.getY()),
                Mth.lerp(partialTicks, target.zo, target.getZ()) - Mth.lerp(partialTicks, circle.zo, circle.getZ()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return TEXTURE;
    }
}
