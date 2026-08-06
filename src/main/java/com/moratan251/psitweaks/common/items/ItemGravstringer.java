package com.moratan251.psitweaks.common.items;

/*
 * This class is based on code from Psipherals (https://github.com/Dudblockman/Psipherals)
 * Licensed under the MIT License.
 *
 * Original author: Dudblockman
 */

import java.util.List;
import java.util.function.Predicate;
import com.moratan251.psitweaks.common.entities.EntityTunnelerArrow;
import com.moratan251.psitweaks.common.handler.BowSpellProjectileHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.cad.ISocketable;

public class ItemGravstringer extends BowItem {
    public static final int SLOT_COUNT = 11;
    public static final int FULL_DRAW_TICKS = 4;
    private static final double ARROW_BASE_DAMAGE = 12.0;
    private static final String TAG_ARROW_MODE = "arrow_mode";

    /** 扇状に発射する左右の矢の角度オフセット（度） */
    private static final float FAN_ANGLE_DEGREES = 10.0F;

    /** 扇状に発射する左右の矢の、射線に垂直なスポーン位置オフセット(ブロック) */
    private static final double FAN_LATERAL_OFFSET = 0.75;

    public ItemGravstringer(Item.Properties properties) {
        super(properties);
    }

    /** このグラヴストリンガーから発射されるトンネラーの飛翔モード（{@link EntityTunnelerArrow} の MODE_* 定数） */
    public static int getArrowMode(ItemStack stack) {
        int mode = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(TAG_ARROW_MODE);
        return Mth.clamp(mode, EntityTunnelerArrow.MODE_NORMAL, EntityTunnelerArrow.MODE_HYBRID);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        // ブロックへのクリックは GravstringerModeHandler が開始時に一度だけ処理する。
        if (entity instanceof Player player && player.isShiftKeyDown() && !player.level().isClientSide
                && player.pick(5.0, 0.0F, false).getType() != HitResult.Type.BLOCK) {
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
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(TAG_ARROW_MODE, mode));
        player.displayClientMessage(Component.translatable("item.psitweaks.gravstringer.mode." + modeKey(mode)), true);
    }

    /** モードに対応する翻訳キー suffix（normal / slow / inertialess / hybrid） */
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
        // スニーク左クリックはモード切替専用とし、mobへの攻撃をキャンセルする
        return player.isShiftKeyDown();
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        // スニーク左クリックはモード切替専用とし、ブロック破壊をキャンセルする
        return !player.isShiftKeyDown();
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
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY.or(stack -> stack.is(PsitweaksItems.TUNNELER.get()));
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
        int arrowMode = getArrowMode(weapon);
        float yawRad = shooter.getYRot() * (float) (Math.PI / 180.0);

        // トンネラーは1回の射撃で3本を扇状に発射する（弾薬消費は1本分）。
        // 通常/低速モードは3本ともそのモード、ハイブリッドモードは中央=通常・左右=低速
        for (ItemStack projectileItem : projectileItems) {
            if (projectileItem.isEmpty()) {
                continue;
            }

            // 扇状3本発射はトンネラーのみ。それ以外の矢は通常通り1本だけ発射する
            if (!projectileItem.is(PsitweaksItems.TUNNELER.get())) {
                Projectile projectile = createProjectile(level, shooter, weapon, projectileItem, isCrit);
                shootProjectile(shooter, projectile, 0, velocity, inaccuracy, 0.0F, target);
                BowSpellProjectileHandler.attachSpell(level, shooter, weapon, projectile);
                level.addFreshEntity(projectile);
                weapon.hurtAndBreak(getDurabilityUse(projectileItem), shooter, LivingEntity.getSlotForHand(hand));
                continue;
            }

            for (int i = -1; i <= 1; i++) {
                Projectile projectile = createProjectile(level, shooter, weapon, projectileItem, isCrit);
                if (projectile instanceof EntityTunnelerArrow tunneler) {
                    int mode = arrowMode == EntityTunnelerArrow.MODE_HYBRID
                            ? (i == 0 ? EntityTunnelerArrow.MODE_NORMAL : EntityTunnelerArrow.MODE_SLOW)
                            : arrowMode;
                    tunneler.setMode(mode);
                }
                // 低速の矢でも左右が見た目で分かれるよう、左右の矢は射線の垂直方向にずらして発射する
                if (i != 0) {
                    double offset = i * FAN_LATERAL_OFFSET;
                    projectile.setPos(
                            projectile.getX() - Mth.cos(yawRad) * offset,
                            projectile.getY(),
                            projectile.getZ() - Mth.sin(yawRad) * offset
                    );
                }
                shootProjectile(shooter, projectile, i + 1, velocity, inaccuracy,
                        i * FAN_ANGLE_DEGREES, target);
                if (i == 0) {
                    BowSpellProjectileHandler.attachSpell(level, shooter, weapon, projectile);
                }
                level.addFreshEntity(projectile);
                weapon.hurtAndBreak(getDurabilityUse(projectileItem), shooter, LivingEntity.getSlotForHand(hand));
            }
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
        tooltip.add(Component.translatable("item.psitweaks.gravstringer.current_mode",
                Component.translatable("item.psitweaks.gravstringer.mode." + modeKey(getArrowMode(stack)))));
    }

}
