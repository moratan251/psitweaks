package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.AutonomousCasterRegistry;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.common.entity.EntitySpellCircle;

/**
 * 術式円は術者の UUID を保存し、ワールドのプレイヤー一覧から探し直す。
 * 自律詠唱機の Caster は所有者の UUID を名乗るため、所有者本人と取り違えないよう機械ごとの内部キーを覚えさせ、
 * プレイヤー一覧で見つからない時にそのキーで Caster を引く。
 */
@Mixin(value = EntitySpellCircle.class, remap = false)
public abstract class EntitySpellCircleAutonomousCasterMixin {
    @Shadow
    @Final
    private static EntityDataAccessor<Optional<UUID>> CASTER_UUID;

    @Inject(method = "setInfo", at = @At("TAIL"))
    private void psitweaks$rememberAutonomousCasterKey(Player player, ItemStack colorizer, ItemStack bullet,
                                                       CallbackInfoReturnable<EntitySpellCircle> cir) {
        if (player instanceof AutonomousCasterPlayer caster) {
            ((Entity) (Object) this).getEntityData().set(CASTER_UUID, Optional.of(caster.casterKey()));
        }
    }

    @Inject(method = "getCaster", at = @At("RETURN"), cancellable = true)
    private void psitweaks$findAutonomousCaster(CallbackInfoReturnable<LivingEntity> cir) {
        if (cir.getReturnValue() != null) {
            return;
        }
        Entity self = (Entity) (Object) this;
        Optional<UUID> casterId = self.getEntityData().get(CASTER_UUID);
        if (casterId.isPresent()) {
            Player caster = AutonomousCasterRegistry.find(self.level(), casterId.get());
            if (caster != null) {
                cir.setReturnValue(caster);
            }
        }
    }
}
