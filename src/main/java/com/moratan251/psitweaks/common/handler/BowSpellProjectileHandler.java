package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemProjectileSpellBullet;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BowSpellProjectileHandler {
    private static final String TAG_ATTACHED_SPELL = "PsitweaksAttachedSpell";

    private BowSpellProjectileHandler() {
    }

    public static void attachSpell(Level level, LivingEntity shooter, ItemStack bowStack, Projectile projectile) {
        if (!(shooter instanceof Player player) || !ISocketable.isSocketable(bowStack)) {
            return;
        }

        PlayerDataHandler.PlayerData data = PlayerDataHandler.get(player);
        ItemStack playerCad = PsiAPI.getPlayerCAD(player);
        ItemStack bullet = ISocketable.socketable(bowStack).getSelectedBullet();
        if (playerCad.isEmpty() || bullet.isEmpty() || !ISpellAcceptor.hasSpell(bullet)) {
            return;
        }
        boolean firesOnImpact = bullet.getItem() instanceof ItemProjectileSpellBullet;

        ItemCAD.cast(level, player, data, bullet, playerCad, 5, 10, 0.05F, (SpellContext context) -> {
            context.tool = bowStack;
        });

        float radius = 0.2F;
        AABB region = new AABB(
                player.getX() - radius,
                player.getY() + player.getEyeHeight() - radius,
                player.getZ() - radius,
                player.getX() + radius,
                player.getY() + player.getEyeHeight() + radius,
                player.getZ() + radius
        );

        List<EntitySpellProjectile> spells = level.getEntitiesOfClass(EntitySpellProjectile.class, region,
                spell -> spell.context != null && spell.context.caster == player && spell.tickCount <= 1);
        boolean attached = false;
        for (EntitySpellProjectile spell : spells) {
            attached |= spell.startRiding(projectile, true);
        }
        if (attached && firesOnImpact) {
            projectile.getPersistentData().putBoolean(TAG_ATTACHED_SPELL, true);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide || !projectile.getPersistentData().getBoolean(TAG_ATTACHED_SPELL)) {
            return;
        }

        List<EntitySpellProjectile> spells = projectile.getPassengers().stream()
                .filter(EntitySpellProjectile.class::isInstance)
                .map(EntitySpellProjectile.class::cast)
                .toList();
        if (spells.isEmpty()) {
            return;
        }

        projectile.getPersistentData().remove(TAG_ATTACHED_SPELL);
        HitResult hitResult = event.getRayTraceResult();
        Vec3 impactPosition = hitResult.getLocation();
        for (EntitySpellProjectile spell : spells) {
            spell.stopRiding();
            spell.setPos(impactPosition.x, impactPosition.y, impactPosition.z);
            if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
                spell.cast(context -> {
                    if (context != null) {
                        context.attackedEntity = target;
                    }
                });
            } else {
                spell.cast();
            }
        }
    }
}
