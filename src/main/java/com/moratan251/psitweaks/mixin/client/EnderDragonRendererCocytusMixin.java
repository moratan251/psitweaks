package com.moratan251.psitweaks.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.client.combat.CocytusIceRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Trick: Cocytus: the Ender Dragon's renderer is not a living entity renderer, so the render event that draws the ice
 * of frozen mobs never fires for it; the ice is drawn here instead.
 */
@Mixin(EnderDragonRenderer.class)
public abstract class EnderDragonRendererCocytusMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("TAIL"))
    private void psitweaks$drawCocytusIce(EnderDragon dragon, float entityYaw, float partialTick, PoseStack poseStack,
                                          MultiBufferSource buffers, int light, CallbackInfo ci) {
        CocytusIceRenderer.drawDragonIce(dragon, partialTick, poseStack, buffers, light);
    }
}
