package com.moratan251.psitweaks.common.items;

/*
 * This class is based on code from Psipherals (https://github.com/Dudblockman/Psipherals)
 * Licensed under the MIT License.
 *
 * Original author: Dudblockman
 */

import com.moratan251.psitweaks.common.entities.EntityTunnelerArrow;
import com.moratan251.psitweaks.common.handler.BowSpellProjectileHandler;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.api.cad.ISocketable;

public class ItemGravstringer extends BowItem {
    public static final int SLOT_COUNT = 11;
    public static final int FULL_DRAW_TICKS = 4;
    private static final double ARROW_BASE_DAMAGE = 12.0D;
    private static final String TAG_ARROW_MODE = "arrow_mode";
    private static final float FAN_ANGLE_DEGREES = 10.0F;
    private static final double FAN_LATERAL_OFFSET = 0.75D;

    public ItemGravstringer(Item.Properties properties) {
        super(properties);
    }

    public static int getArrowMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int mode = tag == null ? EntityTunnelerArrow.MODE_NORMAL : tag.getInt(TAG_ARROW_MODE);
        return Mth.clamp(mode, EntityTunnelerArrow.MODE_NORMAL, EntityTunnelerArrow.MODE_HYBRID);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player && player.isShiftKeyDown() && !player.level().isClientSide
                && player.pick(5.0D, 0.0F, false).getType() != HitResult.Type.BLOCK) {
            cycleArrowMode(stack, player);
        }
        return false;
    }

    public static void cycleArrowMode(ItemStack stack, Player player) {
        int mode = switch (getArrowMode(stack)) {
            case EntityTunnelerArrow.MODE_NORMAL -> EntityTunnelerArrow.MODE_SLOW;
            case EntityTunnelerArrow.MODE_SLOW -> EntityTunnelerArrow.MODE_HYBRID;
            default -> EntityTunnelerArrow.MODE_NORMAL;
        };
        stack.getOrCreateTag().putInt(TAG_ARROW_MODE, mode);
        player.displayClientMessage(Component.translatable("item.psitweaks.gravstringer.mode." + modeKey(mode)), true);
    }

    private static String modeKey(int mode) {
        return switch (mode) {
            case EntityTunnelerArrow.MODE_SLOW -> "slow";
            case EntityTunnelerArrow.MODE_INERTIALESS -> "inertialess";
            case EntityTunnelerArrow.MODE_HYBRID -> "hybrid";
            default -> "normal";
        };
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return player.isShiftKeyDown();
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isShiftKeyDown();
    }

    @Override
    public void releaseUsing(@NotNull ItemStack bow, @NotNull Level level, @NotNull LivingEntity living, int timeLeft) {
        if (!(living instanceof Player player)) {
            return;
        }

        boolean infinite = player.getAbilities().instabuild
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
        ItemStack ammo = player.getProjectile(bow);
        int charge = ForgeEventFactory.onArrowLoose(bow, level, player, getUseDuration(bow) - timeLeft,
                !ammo.isEmpty() || infinite);
        if (charge < 0 || (ammo.isEmpty() && !infinite)) {
            return;
        }
        if (ammo.isEmpty()) {
            ammo = new ItemStack(Items.ARROW);
        }

        float power = powerForCharge(charge);
        if (power < 0.1F) {
            return;
        }

        boolean infiniteAmmo = player.getAbilities().instabuild
                || ammo.getItem() instanceof ArrowItem arrowItem && arrowItem.isInfinite(ammo, bow, player);
        if (!level.isClientSide) {
            ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;
            if (ammo.is(PsitweaksItems.TUNNELER.get())) {
                shootTunnelers(level, player, bow, ammo, arrowItem, power, infiniteAmmo);
            } else {
                AbstractArrow arrow = createArrow(level, player, bow, ammo, arrowItem, power, infiniteAmmo);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                BowSpellProjectileHandler.attachSpell(level, player, bow, arrow);
                level.addFreshEntity(arrow);
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        if (!infiniteAmmo) {
            ammo.shrink(1);
            if (ammo.isEmpty()) {
                player.getInventory().removeItem(ammo);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private void shootTunnelers(Level level, Player player, ItemStack bow, ItemStack ammo, ArrowItem arrowItem,
                                float power, boolean infiniteAmmo) {
        int selectedMode = getArrowMode(bow);
        float yawRadians = player.getYRot() * Mth.DEG_TO_RAD;
        for (int index = -1; index <= 1; index++) {
            AbstractArrow arrow = createArrow(level, player, bow, ammo, arrowItem, power, infiniteAmmo);
            if (arrow instanceof EntityTunnelerArrow tunneler) {
                int mode = selectedMode == EntityTunnelerArrow.MODE_HYBRID
                        ? (index == 0 ? EntityTunnelerArrow.MODE_NORMAL : EntityTunnelerArrow.MODE_SLOW)
                        : selectedMode;
                tunneler.setMode(mode);
            }
            if (index != 0) {
                double offset = index * FAN_LATERAL_OFFSET;
                arrow.setPos(
                        arrow.getX() - Mth.cos(yawRadians) * offset,
                        arrow.getY(),
                        arrow.getZ() - Mth.sin(yawRadians) * offset
                );
            }
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + index * FAN_ANGLE_DEGREES,
                    0.0F, power * 3.0F, 1.0F);
            if (index == 0) {
                BowSpellProjectileHandler.attachSpell(level, player, bow, arrow);
            }
            level.addFreshEntity(arrow);
        }
    }

    private AbstractArrow createArrow(Level level, Player player, ItemStack bow, ItemStack ammo,
                                      ArrowItem arrowItem, float power, boolean infiniteAmmo) {
        AbstractArrow arrow = arrowItem.createArrow(level, ammo, player);
        arrow = customArrow(arrow);
        arrow.setBaseDamage(ARROW_BASE_DAMAGE);
        arrow.setCritArrow(power == 1.0F);

        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5D + 0.5D);
        }
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
        if (punchLevel > 0) {
            arrow.setKnockback(punchLevel);
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) {
            arrow.setSecondsOnFire(100);
        }
        if (infiniteAmmo || player.getAbilities().instabuild
                && (ammo.is(Items.SPECTRAL_ARROW) || ammo.is(Items.TIPPED_ARROW))) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
        return arrow;
    }

    private static float powerForCharge(int charge) {
        float power = (float) charge / FULL_DRAW_TICKS;
        power = (power * power + power * 2.0F) / 3.0F;
        return Math.min(power, 1.0F);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY.or(stack -> stack.is(PsitweaksItems.TUNNELER.get()));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        Component bulletName = ISocketable.getSocketedItemName(stack, "psimisc.none");
        tooltip.add(Component.translatable("psimisc.spell_selected", bulletName));
        tooltip.add(Component.translatable("item.psitweaks.gravstringer.current_mode",
                Component.translatable("item.psitweaks.gravstringer.mode." + modeKey(getArrowMode(stack)))));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ItemInlineCasterBase.InlineCasterSocketable(stack, SLOT_COUNT);
    }
}
