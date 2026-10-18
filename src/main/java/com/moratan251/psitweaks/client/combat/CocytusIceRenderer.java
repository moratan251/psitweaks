package com.moratan251.psitweaks.client.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.common.spells.combat.CocytusFreezes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Draws mobs frozen by Trick: Cocytus encased in a block of ice that fits their hitbox, with frost drifting off it.
 * The ice is the translucent vanilla ice texture, tiled one block per tile so it does not stretch on tall mobs.
 */
public final class CocytusIceRenderer {
    /** How far the ice sticks out of the hitbox. */
    private static final float MARGIN = 0.1F;
    private static final int ALPHA = 210;
    private static final int FROST_CHANCE = 6;

    private CocytusIceRenderer() {
    }

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(CocytusIceRenderer::onRenderLiving);
        NeoForge.EVENT_BUS.addListener(CocytusIceRenderer::onEntityTick);
    }

    private static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (!CocytusFreezes.isFrozen(entity)) {
            return;
        }
        TextureAtlasSprite ice = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getParticleIcon(Blocks.ICE.defaultBlockState());
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(Sheets.translucentCullBlockSheet());
        float halfWidth = entity.getBbWidth() / 2.0F + MARGIN;
        float height = entity.getBbHeight() + MARGIN;
        drawBox(consumer, event.getPoseStack().last(), ice, -halfWidth, -MARGIN * 0.5F, -halfWidth,
                halfWidth, height, halfWidth, event.getPackedLight());
    }

    /** Frost drifting off the ice now and then. */
    private static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide || !CocytusFreezes.isFrozen(entity)
                || entity.level().getRandom().nextInt(FROST_CHANCE) != 0) {
            return;
        }
        var random = entity.level().getRandom();
        Vec3 center = entity.getBoundingBox().getCenter();
        double x = center.x + (random.nextDouble() - 0.5) * (entity.getBbWidth() + MARGIN * 2.0);
        double y = entity.getY() + random.nextDouble() * entity.getBbHeight();
        double z = center.z + (random.nextDouble() - 0.5) * (entity.getBbWidth() + MARGIN * 2.0);
        entity.level().addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0, -0.01, 0.0);
    }

    private static void drawBox(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                                float x0, float y0, float z0, float x1, float y1, float z1, int light) {
        // Each face is split into one-block tiles, so the texture keeps its scale on large mobs.
        face(consumer, pose, sprite, light, x0, y0, z1, x1 - x0, 0, 0, 0, y1 - y0, 0, 0, 0, 1);   // south
        face(consumer, pose, sprite, light, x1, y0, z0, x0 - x1, 0, 0, 0, y1 - y0, 0, 0, 0, -1);  // north
        face(consumer, pose, sprite, light, x1, y0, z1, 0, 0, z0 - z1, 0, y1 - y0, 0, 1, 0, 0);   // east
        face(consumer, pose, sprite, light, x0, y0, z0, 0, 0, z1 - z0, 0, y1 - y0, 0, -1, 0, 0);  // west
        face(consumer, pose, sprite, light, x0, y1, z1, x1 - x0, 0, 0, 0, 0, z0 - z1, 0, 1, 0);   // up
        face(consumer, pose, sprite, light, x0, y0, z0, x1 - x0, 0, 0, 0, 0, z1 - z0, 0, -1, 0);  // down
    }

    /**
     * A face spanned from {@code origin} by the edge vectors {@code u} and {@code v}, wound counter-clockwise when seen
     * from the side the normal points to.
     */
    private static void face(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite, int light,
                             float ox, float oy, float oz, float ux, float uy, float uz, float vx, float vy, float vz,
                             float nx, float ny, float nz) {
        float uLength = Mth.sqrt(ux * ux + uy * uy + uz * uz);
        float vLength = Mth.sqrt(vx * vx + vy * vy + vz * vz);
        int uTiles = Math.max(1, Mth.ceil(uLength - 1.0E-3F));
        int vTiles = Math.max(1, Mth.ceil(vLength - 1.0E-3F));
        for (int i = 0; i < uTiles; i++) {
            float a0 = i / uLength;
            float a1 = Math.min(1.0F, (i + 1) / uLength);
            for (int j = 0; j < vTiles; j++) {
                float b0 = j / vLength;
                float b1 = Math.min(1.0F, (j + 1) / vLength);
                // Texture coordinates of this tile: a whole tile, or the part that fits at the far edge.
                float su = (a1 - a0) * uLength;
                float sv = (b1 - b0) * vLength;
                float u0 = sprite.getU0();
                float u1 = sprite.getU(su);
                float v0 = sprite.getV1();
                float v1 = sprite.getV(1.0F - sv);
                vertex(consumer, pose, ox + ux * a0 + vx * b0, oy + uy * a0 + vy * b0, oz + uz * a0 + vz * b0, u0, v0, light, nx, ny, nz);
                vertex(consumer, pose, ox + ux * a1 + vx * b0, oy + uy * a1 + vy * b0, oz + uz * a1 + vz * b0, u1, v0, light, nx, ny, nz);
                vertex(consumer, pose, ox + ux * a1 + vx * b1, oy + uy * a1 + vy * b1, oz + uz * a1 + vz * b1, u1, v1, light, nx, ny, nz);
                vertex(consumer, pose, ox + ux * a0 + vx * b1, oy + uy * a0 + vy * b1, oz + uz * a0 + vz * b1, u0, v1, light, nx, ny, nz);
            }
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v,
                               int light, float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, ALPHA)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
