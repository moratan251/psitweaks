package com.moratan251.psitweaks.client.render.tile;

import com.moratan251.psitweaks.common.tile.machine.AutonomousCasterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import vazkii.psi.api.spell.SpellContext;

/**
 * 自律詠唱機の視線。GUI のタブで表示をオンにした機械だけ、詠唱主体の目の位置から視線の先へ線を引き、
 * 視線が当たったブロックを枠で囲む。
 */
public class RenderAutonomousCaster extends MekanismTileEntityRenderer<AutonomousCasterBlockEntity> {
    /** 線を引く長さ。術式が届く距離に合わせる。 */
    private static final double RANGE = SpellContext.MAX_DISTANCE;
    private static final float RED = 0.37F;
    private static final float GREEN = 0.72F;
    private static final float BLUE = 1.0F;

    public RenderAutonomousCaster(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(AutonomousCasterBlockEntity caster, float partialTick, PoseStack matrix, MultiBufferSource renderer,
                          int light, int overlayLight, ProfilerFiller profiler) {
        Level level = caster.getLevel();
        if (level == null) {
            return;
        }
        Vec3 eye = caster.eyePosition();
        Vec3 end = eye.add(caster.lookVector().scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                CollisionContext.empty()));
        boolean hitBlock = hit.getType() == HitResult.Type.BLOCK;
        // 描画の原点は機械のブロックの角。
        Vec3 origin = Vec3.atLowerCornerOf(caster.getBlockPos());
        Vec3 from = eye.subtract(origin);
        Vec3 to = (hitBlock ? hit.getLocation() : end).subtract(origin);
        Vec3 normal = to.subtract(from).normalize();

        VertexConsumer lines = renderer.getBuffer(RenderType.lines());
        PoseStack.Pose pose = matrix.last();
        lines.addVertex(pose, (float) from.x, (float) from.y, (float) from.z).setColor(RED, GREEN, BLUE, 1.0F)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        lines.addVertex(pose, (float) to.x, (float) to.y, (float) to.z).setColor(RED, GREEN, BLUE, 1.0F)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        if (hitBlock) {
            BlockPos target = hit.getBlockPos();
            AABB box = new AABB(target).move(-origin.x, -origin.y, -origin.z).inflate(0.002D);
            LevelRenderer.renderLineBox(matrix, lines, box, RED, GREEN, BLUE, 1.0F);
        }
    }

    @Override
    protected String getProfilerSection() {
        return "psitweaks_autonomous_caster";
    }

    @Override
    public boolean shouldRenderOffScreen(AutonomousCasterBlockEntity caster) {
        return true;
    }

    @Override
    public boolean shouldRender(AutonomousCasterBlockEntity caster, Vec3 camera) {
        return caster.isClientRendering() && super.shouldRender(caster, camera);
    }

    @Override
    public AABB getRenderBoundingBox(AutonomousCasterBlockEntity caster) {
        if (caster.isClientRendering()) {
            // 視線は機械から最大 RANGE 先まで伸びる。
            return new AABB(caster.getBlockPos()).inflate(RANGE + 1.0D);
        }
        return super.getRenderBoundingBox(caster);
    }
}
