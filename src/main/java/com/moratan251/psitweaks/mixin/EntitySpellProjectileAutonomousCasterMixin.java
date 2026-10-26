package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.AutonomousCasterRegistry;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.common.entity.EntitySpellProjectile;

/**
 * 発射・チャージ・グレネード・鉱山の術式エンティティは、術者の UUID を覚え、保存後に術者をプレイヤー一覧から探し直す。
 * 自律詠唱機の Caster は所有者の UUID を名乗るため、所有者本人と取り違えないよう機械ごとの内部キーを覚えさせ、
 * 見つからない時にそのキーで Caster を引く。バニラ側の所有者は {@link ProjectileAutonomousCasterMixin} が扱う。
 */
@Mixin(value = EntitySpellProjectile.class, remap = false)
public abstract class EntitySpellProjectileAutonomousCasterMixin {
    @Shadow
    @Final
    private static EntityDataAccessor<Optional<UUID>> CASTER_UUID;

    @Inject(method = "setInfo", at = @At("TAIL"))
    private void psitweaks$rememberAutonomousCasterKey(Player player, ItemStack colorizer, ItemStack bullet,
                                                       CallbackInfoReturnable<EntitySpellProjectile> cir) {
        rememberKey(player);
    }

    /** 読み込み時は Psi が所有者から術者の UUID を取り直すので、専用 Caster なら内部キーに戻す。 */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void psitweaks$keepAutonomousCasterKey(CompoundTag tag, CallbackInfo ci) {
        rememberKey(((Projectile) (Object) this).getOwner());
    }

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

    private void rememberKey(Entity caster) {
        if (caster instanceof AutonomousCasterPlayer autonomous) {
            ((Entity) (Object) this).getEntityData().set(CASTER_UUID, Optional.of(autonomous.casterKey()));
        }
    }
}
