package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.EntityAquaCutterProjectile;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public class AquaCutterProjectileHandler {

    public static final String TAG_AQUA_CUTTER = "PsitweaksAquaCutter";
    public static final String TAG_AQUA_CUTTER_DAMAGE = "PsitweaksAquaCutterDamage";

    /** How far the drops of the burst fly. */
    private static final float SPLASH_SIZE = 1.1F;

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
            if (target instanceof Player && data.getBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS)) {
                projectile.discard();
                event.setCanceled(true);
                return;
            }
            float damage = Math.max(0.0F, data.getFloat(TAG_AQUA_CUTTER_DAMAGE));
            if (target.isSensitiveToWater()) {
                damage *= 2.0F;
            }

            Entity owner = projectile.getOwner();
            DamageSource source = projectile.damageSources().thrown(projectile, owner);
            AttackRules.harmByProjectile(projectile, target,
                    AttackRules.Options.of(data.getBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS)), source, damage);

            // The water breaks on the side of the creature it struck and splashes back off it.
            Vec3 flight = flightDirection(projectile);
            Vec3 surface = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ())
                    .subtract(flight.scale(target.getBbWidth() * 0.5));
            playSplashEffect(projectile, surface, flight.scale(-1.0));
        } else if (hitResult instanceof BlockHitResult blockHitResult) {
            Vec3 face = Vec3.atLowerCornerOf(blockHitResult.getDirection().getNormal());
            playSplashEffect(projectile, blockHitResult.getLocation(), face);
        }

        projectile.discard();
        event.setCanceled(true);
    }

    /** Where the blade was heading when it hit. */
    private static Vec3 flightDirection(Projectile projectile) {
        Vec3 motion = projectile.getDeltaMovement();
        return motion.lengthSqr() > 1.0E-6 ? motion.normalize() : new Vec3(0.0, 0.0, 1.0);
    }

    /**
     * The blade bursts: drops of water fly off the struck surface, mostly along {@code spray} (the side the blade came
     * from), and vanilla splashes and drips follow.
     */
    private static void playSplashEffect(Projectile projectile, Vec3 at, Vec3 spray) {
        double x = at.x;
        double y = at.y;
        double z = at.z;
        if (projectile.level() instanceof ServerLevel serverLevel) {
            int casterColor = projectile instanceof EntityAquaCutterProjectile cutter
                    ? cutter.getColor() : CombatSpellHelper.DEFAULT_SPELL_COLOR;
            CombatEffects.sphereFlash(serverLevel, at, SPLASH_SIZE, EntityAquaCutterProjectile.tint(casterColor),
                    MessageSphereFlash.Style.WATER_SPLASH, spray, 1.0F);
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    x, y, z,
                    16, 0.3, 0.25, 0.3, 0.1);
            serverLevel.sendParticles(ParticleTypes.FALLING_WATER,
                    x, y, z,
                    6, 0.35, 0.2, 0.35, 0.0);
        }

        projectile.level().playSound(null, x, y, z,
                SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.8F, 1.1F);
    }
}
