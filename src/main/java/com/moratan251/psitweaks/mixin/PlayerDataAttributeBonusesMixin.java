package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.common.core.handler.PlayerDataHandler;

import java.lang.ref.WeakReference;

@Mixin(value = PlayerDataHandler.PlayerData.class, remap = false)
public abstract class PlayerDataAttributeBonusesMixin {
    @Shadow public int totalPsi;
    @Shadow public int regen;
    @Shadow @Final public WeakReference<Player> playerWR;

    @Inject(method = "getTotalPsi()I", at = @At("HEAD"), cancellable = true)
    private void psitweaks$applyMaxPsiBonus(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(0, totalPsi + psitweaks$getAttributeBonus(PsitweaksAttributes.MAX_PSI_BONUS)));
    }

    @Inject(method = "getRegenPerTick()I", at = @At("HEAD"), cancellable = true)
    private void psitweaks$applyPsiRegenBonus(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(0, regen + psitweaks$getAttributeBonus(PsitweaksAttributes.PSI_REGEN_BONUS)));
    }

    @Unique
    private int psitweaks$getAttributeBonus(RegistryObject<Attribute> attribute) {
        if (playerWR == null) {
            return 0;
        }

        Player player = playerWR.get();
        if (player == null || player.getAttribute(attribute.get()) == null) {
            return 0;
        }

        return (int) Math.round(player.getAttributeValue(attribute.get()));
    }
}
