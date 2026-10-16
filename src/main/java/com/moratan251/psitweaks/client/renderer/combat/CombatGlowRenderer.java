package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Base of the combat spell renderers. They draw glowing geometry with {@code GlowDrawing}, whose camera-facing
 * shapes expect the camera at the origin, so {@link #cameraPose} moves the origin from the entity to the camera.
 */
abstract class CombatGlowRenderer<T extends Entity> extends EntityRenderer<T> {
    protected static final Vector3f UP = new Vector3f(0.0F, 1.0F, 0.0F);

    protected CombatGlowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    /** The camera position in the world. */
    protected Vec3 camera() {
        return entityRenderDispatcher.camera.getPosition();
    }

    /**
     * Returns a pose whose origin is the camera and stores in {@code origin} where the entity is drawn, relative to
     * the camera (the same smoothed position the render dispatcher moved the pose to).
     */
    protected Matrix4f cameraPose(T entity, float partialTick, PoseStack poseStack, Vector3f origin) {
        Vec3 camera = camera();
        origin.set((float) (Mth.lerp(partialTick, entity.xOld, entity.getX()) - camera.x),
                (float) (Mth.lerp(partialTick, entity.yOld, entity.getY()) - camera.y),
                (float) (Mth.lerp(partialTick, entity.zOld, entity.getZ()) - camera.z));
        return new Matrix4f(poseStack.last().pose()).translate(-origin.x, -origin.y, -origin.z);
    }

    /** {@link #cameraPose(Entity, float, PoseStack, Vector3f)} for renderers that place everything themselves. */
    protected Matrix4f cameraPose(T entity, float partialTick, PoseStack poseStack) {
        return cameraPose(entity, partialTick, poseStack, new Vector3f());
    }

    /** A world position relative to the camera. */
    protected Vector3f relative(Vec3 point) {
        Vec3 camera = camera();
        return new Vector3f((float) (point.x - camera.x), (float) (point.y - camera.y), (float) (point.z - camera.z));
    }

    protected static Vector3f offset(Vector3f base, Vec3 direction, double distance) {
        return new Vector3f(base).add((float) (direction.x * distance), (float) (direction.y * distance), (float) (direction.z * distance));
    }
}
