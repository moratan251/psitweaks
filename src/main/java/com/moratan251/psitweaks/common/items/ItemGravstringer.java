package com.moratan251.psitweaks.common.items;

/*
 * This class is based on code from Psipherals (https://github.com/Dudblockman/Psipherals)
 * Licensed under the MIT License.
 *
 * Original author: Dudblockman
 */

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemCAD;

public class ItemGravstringer extends BowItem {
    public static final int SLOT_COUNT = 11;
    public static final int FULL_DRAW_TICKS = 4;
    private static final double ARROW_BASE_DAMAGE = 12.0;

    public ItemGravstringer(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) {
            return;
        }

        ItemStack projectile = player.getProjectile(stack);
        if (projectile.isEmpty()) {
            return;
        }

        int charge = this.getUseDuration(stack, entityLiving) - timeLeft;
        charge = EventHooks.onArrowLoose(stack, level, player, charge, true);
        if (charge < 0) {
            return;
        }

        float power = powerForCharge(charge);
        if ((double) power < 0.1) {
            return;
        }

        List<ItemStack> projectiles = draw(stack, projectile, player);
        if (level instanceof ServerLevel serverLevel && !projectiles.isEmpty()) {
            this.shoot(serverLevel, player, player.getUsedItemHand(), stack, projectiles, power * 3.0F, 1.0F, power == 1.0F, null);
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F
        );
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private static float powerForCharge(int charge) {
        float power = (float) charge / FULL_DRAW_TICKS;
        power = (power * power + power * 2.0F) / 3.0F;
        return Math.min(power, 1.0F);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        arrow.setBaseDamage(ARROW_BASE_DAMAGE);
        return super.customArrow(arrow, projectileStack, weaponStack);
    }

    @Override
    protected void shoot(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon,
                         List<ItemStack> projectileItems, float velocity, float inaccuracy, boolean isCrit,
                         @Nullable LivingEntity target) {
        float spread = EnchantmentHelper.processProjectileSpread(level, weapon, shooter, 0.0F);
        float projectileCount = projectileItems.size();
        float centeredIndex = (projectileCount - 1.0F) / 2.0F;

        for (int index = 0; index < projectileItems.size(); index++) {
            ItemStack projectileItem = projectileItems.get(index);
            if (projectileItem.isEmpty()) {
                continue;
            }

            float offset = index - centeredIndex;
            Projectile projectile = createProjectile(level, shooter, weapon, projectileItem, isCrit);
            shootProjectile(shooter, projectile, index, velocity, inaccuracy, offset * spread, target);
            attachSpellToProjectile(level, shooter, weapon, projectile);
            level.addFreshEntity(projectile);
            weapon.hurtAndBreak(getDurabilityUse(projectileItem), shooter, LivingEntity.getSlotForHand(hand));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Component componentName = ISocketable.getSocketedItemName(stack, "psimisc.none");
        tooltip.add(Component.translatable("psimisc.spell_selected", componentName));
    }

    private static void attachSpellToProjectile(ServerLevel level, LivingEntity shooter, ItemStack bowStack, Projectile projectile) {
        if (!(shooter instanceof Player player) || !ISocketable.isSocketable(bowStack)) {
            return;
        }

        PlayerDataHandler.PlayerData data = PlayerDataHandler.get(player);
        ItemStack playerCad = PsiAPI.getPlayerCAD(player);
        ItemStack bullet = ISocketable.socketable(bowStack).getSelectedBullet();
        if (playerCad.isEmpty() || bullet.isEmpty() || !ISpellAcceptor.hasSpell(bullet)) {
            return;
        }

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
                spell -> spell != null && spell.context != null && spell.context.caster == player && spell.tickCount <= 1);
        for (EntitySpellProjectile spell : spells) {
            spell.startRiding(projectile, true);
        }
    }
}
