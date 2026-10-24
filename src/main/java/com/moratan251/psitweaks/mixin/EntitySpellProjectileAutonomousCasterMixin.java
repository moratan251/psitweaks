package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.caster.AutonomousCasterRegistry;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.common.entity.EntitySpellProjectile;

/**
 * 発射・チャージ・グレネード・鉱山の術式エンティティは、保存後に術者をプレイヤー一覧から探し直す。
 * 見つからない時だけ自律詠唱機の Caster を引く。
 */
@Mixin(value = EntitySpellProjectile.class, remap = false)
public abstract class EntitySpellProjectileAutonomousCasterMixin {
    @Shadow
    @Final
    private static EntityDataAccessor<Optional<UUID>> CASTER_UUID;

    @Inject(method = "getOwner", at = @At("RETURN"), cancellable = true)
    private void psitweaks$findAutonomousCaster(CallbackInfoReturnable<Entity> cir) {
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
