package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.common.core.handler.PlayerDataHandler;

import java.lang.ref.WeakReference;

@Mixin(value = PlayerDataHandler.PlayerData.class, remap = false)
public abstract class PlayerDataAttributeBonusesMixin {
    @Unique private static final String PSITWEAKS_TAG_TOTAL_PSI = "psitweaksSavedTotalPsi";
    @Unique private static final int PSITWEAKS_RESTORE_GRACE_TICKS = 100;

    @Shadow public int totalPsi;
    @Shadow public int regen;
    @Shadow public int availablePsi;
    @Shadow @Final public WeakReference<Player> playerWR;

    @Unique private int psitweaks$savedTotalPsi;
    @Unique private int psitweaks$restoreGraceTicks;

    @Inject(method = "getTotalPsi()I", at = @At("HEAD"), cancellable = true)
    private void psitweaks$applyMaxPsiBonus(CallbackInfoReturnable<Integer> cir) {
        int attributeAdjustedTotal = psitweaks$getAttributeAdjustedTotalPsi();
        if (psitweaks$restoreGraceTicks > 0) {
            attributeAdjustedTotal = Math.max(attributeAdjustedTotal, psitweaks$savedTotalPsi);
        }

        cir.setReturnValue(attributeAdjustedTotal);
    }

    @Inject(method = "getRegenPerTick()I", at = @At("HEAD"), cancellable = true)
    private void psitweaks$applyPsiRegenBonus(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(0, regen + psitweaks$getAttributeBonus(PsitweaksAttributes.PSI_REGEN_BONUS)));
    }

    @Inject(method = "writeToNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void psitweaks$writeSavedTotalPsi(CompoundTag tag, CallbackInfo ci) {
        tag.putInt(PSITWEAKS_TAG_TOTAL_PSI, psitweaks$getAttributeAdjustedTotalPsi());
    }

    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void psitweaks$readSavedTotalPsi(CompoundTag tag, CallbackInfo ci) {
        int savedTotalPsi = tag.contains(PSITWEAKS_TAG_TOTAL_PSI) ? tag.getInt(PSITWEAKS_TAG_TOTAL_PSI) : 0;
        psitweaks$savedTotalPsi = Math.max(savedTotalPsi, availablePsi);
        psitweaks$restoreGraceTicks = psitweaks$savedTotalPsi > psitweaks$getAttributeAdjustedTotalPsi()
                ? PSITWEAKS_RESTORE_GRACE_TICKS
                : 0;
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void psitweaks$tickSavedTotalPsiRestore(CallbackInfo ci) {
        if (psitweaks$restoreGraceTicks <= 0) {
            return;
        }

        if (psitweaks$getAttributeAdjustedTotalPsi() >= psitweaks$savedTotalPsi || --psitweaks$restoreGraceTicks <= 0) {
            psitweaks$savedTotalPsi = 0;
            psitweaks$restoreGraceTicks = 0;
        }
    }

    @Unique
    private int psitweaks$getAttributeAdjustedTotalPsi() {
        return Math.max(0, totalPsi + psitweaks$getAttributeBonus(PsitweaksAttributes.MAX_PSI_BONUS));
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
