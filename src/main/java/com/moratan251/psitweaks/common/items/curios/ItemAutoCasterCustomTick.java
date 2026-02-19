package com.moratan251.psitweaks.common.items.curios;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.exosuit.PsiArmorEvent;

import java.util.List;

public class ItemAutoCasterCustomTick extends ItemCuriosCompat {
    public static final String TAG_CAST_INTERVAL = "CustomTickInterval";
    public static final int DEFAULT_CAST_INTERVAL = 20;
    public static final int MIN_CAST_INTERVAL = 1;
    public static final int MAX_CAST_INTERVAL = 1200;

    public ItemAutoCasterCustomTick(Properties pProperties) {
        super(pProperties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public String getEvent(ItemStack stack) {
        return "psitweaks.event.custom_tick";
    }

    @Override
    public void onEvent(ItemStack stack, PsiArmorEvent event) {
        if (!event.type.equals(this.getTrueEvent(stack)) || event.getEntity() == null) {
            return;
        }

        int interval = getCastInterval(stack);
        if (event.getEntity().level().getGameTime() % interval == 0L) {
            this.cast(stack, event);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) {
            Psitweaks.proxyPsitweaks.openAutoCasterCustomTickGUI(held, hand);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level playerIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced) {
        super.appendHoverText(stack, playerIn, tooltip, advanced);
        tooltip.add(Component.translatable("tooltip.psitweaks.auto_caster_custom_tick.interval", getCastInterval(stack)));
    }

    public static int getCastInterval(ItemStack stack) {
        int interval = DEFAULT_CAST_INTERVAL;
        if (stack.getOrCreateTag().contains(TAG_CAST_INTERVAL, Tag.TAG_INT)) {
            interval = stack.getOrCreateTag().getInt(TAG_CAST_INTERVAL);
        }
        return Mth.clamp(interval, MIN_CAST_INTERVAL, MAX_CAST_INTERVAL);
    }

    public static void setCastInterval(ItemStack stack, int interval) {
        stack.getOrCreateTag().putInt(TAG_CAST_INTERVAL, Mth.clamp(interval, MIN_CAST_INTERVAL, MAX_CAST_INTERVAL));
    }
}
