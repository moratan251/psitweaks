package com.moratan251.psitweaks.mixin.client;

import com.moratan251.psitweaks.client.combat.ParadeHiddenPlayers;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Trick: Parade: a caster hidden from the local player is left out of entity rendering altogether, so nothing that the
 * level renderer draws for an entity shows them: the body, the shadow, flames, and what other mods draw alongside the
 * entity (Neat's health bar, which it shows for invisible players wearing armour). SpellGram objects not shown to the
 * local player (the image's core, {@link SpellGramObject#isShownTo}) are left out the same way, so the hitboxes of F3+B
 * do not show the core in the image's chest.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherParadeMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void psitweaks$skipParadeHiddenPlayer(E entity, Frustum frustum, double camX, double camY,
                                                                     double camZ, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer != null && (ParadeHiddenPlayers.isHiddenFrom(entity, viewer)
                || entity instanceof SpellGramObject object && !object.isShownTo(viewer))) {
            cir.setReturnValue(false);
        }
    }
}
