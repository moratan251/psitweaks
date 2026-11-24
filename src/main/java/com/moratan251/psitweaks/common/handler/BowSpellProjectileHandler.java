package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerData;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemProjectileSpellBullet;

@EventBusSubscriber(modid = Psitweaks.MOD_ID)
public final class BowSpellProjectileHandler {
    private static final String TAG_ATTACHED_SPELL = "PsitweaksAttachedSpell";

    private BowSpellProjectileHandler() {
    }

    public static void attachSpell(ServerLevel level, LivingEntity shooter, ItemStack bowStack, Projectile projectile) {
        if (!(shooter instanceof Player player) || !ISocketable.isSocketable(bowStack)) {
            return;
        }

        PlayerData data = PlayerDataHandler.get(player);
        ItemStack playerCad = PsiAPI.getPlayerCAD(player);
        ItemStack bullet = ISocketable.socketable(bowStack).getSelectedBullet();
        if (playerCad.isEmpty() || bullet.isEmpty() || !ISpellAcceptor.hasSpell(bullet)) {
            return;
        }
        // 同乗中の EntitySpellProjectile は矢の命中では onHit が呼ばれないため、発射型術式弾のときだけ
        // マーカーを付けて onProjectileImpact で発火させる。EntitySpellGrenade / EntitySpellCharge も
        // EntitySpellProjectile 派生なので、エンティティ型で判定すると各弾種のタイマー・起爆処理を無視して即時発火する。
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

    // 運搬側の命中を Psi 本体の EntitySpellProjectile#onHit と同じ分岐（生物なら attackedEntity を設定）で
    // 一度だけ cast する。生物の部位（エンダードラゴンの頭など）に当たったときは、その生物を attackedEntity にする。
    // マーカーは発火前に外し、多重命中する投射物でも再発火させない。
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
            LivingEntity target = attackedEntity(hitResult);
            if (target != null) {
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

    /**
     * 命中した生物。複数部位の生き物の部位（エンダードラゴンの頭・胴体・翼など）なら、その生き物
     * （{@link AttackRules#wholeEntity}）。生物でなければ null。
     */
    @Nullable
    public static LivingEntity attackedEntity(HitResult hitResult) {
        return hitResult instanceof EntityHitResult entityHit
                && AttackRules.wholeEntity(entityHit.getEntity()) instanceof LivingEntity target ? target : null;
    }
}
