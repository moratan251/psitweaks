package com.moratan251.psitweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.client.renderer.circle.CircleDesign;
import com.moratan251.psitweaks.client.renderer.circle.VectorCircleRenderer;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * 領域型の魔法式オブジェクトの見た目（試作）: 範囲を囲む箱の 12 本の辺を領域の種類ごとの色の線で、床に数式で描いた魔法陣
 * （{@link CircleDesign}）を術者の CAD の色で描く。中心には CAD の色の核の魔法陣（{@link SpellGramCoreRenderer}）。
 * 耐久値が減るほど薄くなり、残りが 4 分の 1 を切るとちらつく。
 * <p>
 * 床の魔法陣は、範囲の中心のブロックから下へたどって最初に当たる足場の上面に敷く（範囲は術者のまわりの立方体のことが多く、
 * 箱の底は地面の下に埋まりやすいため）。足場がなければ中心のブロックの底の高さに浮かべる。
 */
public class SpellGramZoneRenderer<T extends SpellGramZone> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("psi", "textures/misc/spell_circle0.png");
    /** 床の魔法陣を足場から浮かせる高さ（ブロックの面とのちらつき防止）。 */
    private static final float FLOOR_LIFT = 0.02F;
    /** 床の魔法陣の明るさ。 */
    private static final float FLOOR_BRIGHTNESS = 0.9F;
    /** 置いた直後に、床の魔法陣が広がりながら明るくなる時間（tick）。 */
    private static final float APPEAR_TICKS = 12.0F;

    private final CircleDesign floorDesign;

    public SpellGramZoneRenderer(EntityRendererProvider.Context context, CircleDesign floorDesign) {
        super(context);
        this.floorDesign = floorDesign;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        AABB world = entity.getOutline();
        AABB outline = world.move(-entity.getX(), -entity.getY(), -entity.getZ());
        float max = entity.getMaxIntegrity();
        float fraction = max > 0.0F ? Mth.clamp(entity.getIntegrity() / max, 0.0F, 1.0F) : 1.0F;
        float time = entity.tickCount + partialTicks;
        float alpha = 0.35F + 0.65F * fraction;
        if (fraction < 0.25F) {
            alpha *= 0.6F + 0.4F * Mth.sin(time * 1.3F);
        }
        int edge = entity.getOutlineColor();
        LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(RenderType.lines()), outline,
                ((edge >> 16) & 0xFF) / 255.0F, ((edge >> 8) & 0xFF) / 255.0F, (edge & 0xFF) / 255.0F, alpha);

        renderFloor(entity, world, outline, time, alpha, poseStack, buffer);
        SpellGramCoreRenderer.render(entity, entity.getColor(), 0.0F, partialTicks, entityRenderDispatcher.camera, poseStack, buffer);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderFloor(T entity, AABB world, AABB outline, float time, float alpha, PoseStack poseStack,
                             MultiBufferSource buffer) {
        float half = (float) Math.min(outline.getXsize(), outline.getZsize()) * 0.5F;
        if (half <= 0.0F) {
            return;
        }
        float appear = Mth.clamp(time / APPEAR_TICKS, 0.0F, 1.0F);
        float grow = 1.0F - (1.0F - appear) * (1.0F - appear);
        float radius = half * (floorDesign.square() ? 1.0F : 0.98F) * (0.75F + 0.25F * grow);
        double floor = floorHeight(entity.level(), entity.getArea().center(), Mth.floor(world.minY));

        poseStack.pushPose();
        poseStack.translate((outline.minX + outline.maxX) * 0.5, floor - entity.getY() + FLOOR_LIFT,
                (outline.minZ + outline.maxZ) * 0.5);
        VectorCircleRenderer.render(floorDesign, 1.0F, poseStack, buffer, radius, time, entity.getColor(),
                FLOOR_BRIGHTNESS * alpha * appear);
        poseStack.popPose();
    }

    /** 中心のブロックから範囲の底の 1 つ下まで下へたどり、最初に当たった足場の上面の高さ。なければ中心のブロックの底。 */
    private static double floorHeight(Level level, BlockPos center, int minY) {
        BlockPos.MutableBlockPos pos = center.mutable();
        for (int y = center.getY(); y >= minY - 1; y--) {
            pos.setY(y);
            VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
            if (!shape.isEmpty()) {
                return y + Math.min(1.0, shape.max(Direction.Axis.Y));
            }
        }
        return center.getY();
    }

    @Override
    public boolean shouldRender(@NotNull T entity, @NotNull net.minecraft.client.renderer.culling.Frustum frustum,
                                double camX, double camY, double camZ) {
        return frustum.isVisible(entity.getOutline());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return TEXTURE;
    }
}
