package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AquaCutterProjectileHandler {

    public static final String TAG_AQUA_CUTTER = "PsitweaksAquaCutter";
    public static final String TAG_AQUA_CUTTER_DAMAGE = "PsitweaksAquaCutterDamage";

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide) {
            return;
        }

        CompoundTag data = projectile.getPersistentData();
        if (!data.getBoolean(TAG_AQUA_CUTTER)) {
            return;
        }

        HitResult hitResult = event.getRayTraceResult();
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity target) {
            float damage = Math.max(0.0F, data.getFloat(TAG_AQUA_CUTTER_DAMAGE));
            if (target.isSensitiveToWater()) {
                damage *= 2.0F;
            }

            Entity owner = projectile.getOwner();
            DamageSource source = owner instanceof LivingEntity livingOwner
                    ? livingOwner.damageSources().indirectMagic(projectile, livingOwner)
                    : projectile.damageSources().magic();
            target.hurt(source, damage);

            if (projectile.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SPLASH,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        12, 0.25, 0.25, 0.25, 0.05);
            }

            projectile.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.8F, 1.1F);
        }

        projectile.discard();
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.DEFAULT);
    }
}
