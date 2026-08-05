package com.moratan251.psitweaks.common.items;

/*
 * This class is based on code from Psipherals (https://github.com/Dudblockman/Psipherals)
 * Licensed under the MIT License.
 *
 * Original author: Dudblockman
 */

import com.moratan251.psitweaks.common.handler.BowSpellProjectileHandler;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.common.item.tool.IPsimetalTool;

public class ItemPsimetalBow extends BowItem implements IPsimetalTool {
    public static final int SLOT_COUNT = 3;

    public ItemPsimetalBow(Item.Properties properties) {
        super(properties.durability(768));
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
            BowSpellProjectileHandler.attachSpell(level, shooter, weapon, projectile);
            level.addFreshEntity(projectile);
            weapon.hurtAndBreak(getDurabilityUse(projectileItem), shooter, LivingEntity.getSlotForHand(hand));
        }
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, damage > stack.getMaxDamage() ? stack.getDamageValue() : damage);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        IPsimetalTool.regen(stack, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Component componentName = ISocketable.getSocketedItemName(stack, "psimisc.none");
        tooltip.add(Component.translatable("psimisc.spell_selected", componentName));
    }

}
