package com.moratan251.psitweaks.common.items;

/*
 * This class is based on code from Psipherals (https://github.com/Dudblockman/Psipherals)
 * Licensed under the MIT License.
 *
 * Original author: Dudblockman
 */

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.tool.IPsimetalTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class ItemPsimetalBow extends BowItem implements IPsimetalTool {
    public ItemPsimetalBow(Item.Properties props) {
        super(props.durability(768));
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity living, int timeLeft) {
        if (living instanceof Player player) {
            boolean flag = player.getAbilities().instabuild || stack.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0;
            ItemStack ammo = player.getProjectile(stack);
            int i = this.getUseDuration(stack) - timeLeft;
            i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, level, player, i, !ammo.isEmpty() || flag);
            if (i < 0) return;

            if (!ammo.isEmpty() || flag) {
                if (ammo.isEmpty()) {
                    ammo = new ItemStack(Items.ARROW);
                }

                float f = getPowerForTime(i);
                if (!(f < 0.1D)) {
                    boolean flag1 = player.getAbilities().instabuild || (ammo.getItem() instanceof ArrowItem arrowItem && arrowItem.isInfinite(ammo, stack, player));

                    if (!level.isClientSide) {
                        ArrowItem arrowitem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
                        AbstractArrow arrow = arrowitem.createArrow(level, ammo, player);
                        arrow = this.customArrow(arrow);
                        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 1.0F);

                        if (f == 1.0F) {
                            arrow.setCritArrow(true);
                        }

                        //int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                        int j = stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                        if (j > 0) {
                            arrow.setBaseDamage(arrow.getBaseDamage() + (double) j * 0.5D + 0.5D);
                        }

                        int k = stack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
                        if (k > 0) {
                            arrow.setKnockback(k);
                        }

                        if (stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                            arrow.setSecondsOnFire(100);
                        }

                        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

                        if (flag1 || player.getAbilities().instabuild && (ammo.is(Items.SPECTRAL_ARROW) || ammo.is(Items.TIPPED_ARROW))) {
                            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        // --- Psi の SpellCasting 部分ここに移植 ---
                        PlayerDataHandler.PlayerData data = PlayerDataHandler.get(player);
                        ItemStack playerCad = PsiAPI.getPlayerCAD(player);

                        if (!playerCad.isEmpty()) {
                            ISocketable sockets = ISocketable.socketable(stack);
                            ItemStack bullet = sockets.getSelectedBullet();
                            ItemCAD.cast(player.level(), player, data, bullet, playerCad, 5, 10, 0.05F, (SpellContext context) -> {
                                context.tool = stack;
                            });

                            float radiusVal = 0.2f;
                            AABB region = new AABB(player.getX() - radiusVal, player.getY() + player.getEyeHeight() - radiusVal, player.getZ() - radiusVal, player.getX() + radiusVal, player.getY() + player.getEyeHeight() + radiusVal, player.getZ() + radiusVal);

                            List<EntitySpellProjectile> spells = player.level().getEntitiesOfClass(EntitySpellProjectile.class, region, (e) -> ((e != null) && (e.context.caster == player) && (e.tickCount <= 1)));
                            for (EntitySpellProjectile spell : spells) {
                                spell.startRiding(arrow, true);
                            }

                        }


                        level.addFreshEntity(arrow);
                    }

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                            1.0F, 1.0F / (RandomSource.create().nextFloat() * 0.4F + 1.2F) + f * 0.5F);

                    if (!flag1 && !player.getAbilities().instabuild) {
                        ammo.shrink(1);
                        if (ammo.isEmpty()) {
                            player.getInventory().removeItem(ammo);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        if (damage > stack.getMaxDamage()) {
            damage = stack.getDamageValue();
        }
        super.setDamage(stack, damage);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, net.minecraft.world.entity.@NotNull Entity entity, int itemSlot, boolean isSelected) {
        IPsimetalTool.regen(stack, entity);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return IPsimetalTool.super.initCapabilities(stack, nbt);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level playerIn, List<Component> tooltip, @NotNull TooltipFlag advanced) {
        Component componentName = ISocketable.getSocketedItemName(stack, "psimisc.none");
        tooltip.add(Component.translatable("psimisc.spell_selected", new Object[]{componentName}));
    }


    public boolean isRepairable(ItemStack thisStack, @Nonnull ItemStack material) {
        return IPsimetalTool.isRepairableBy(material) || super.isRepairable(thisStack);
    }


}