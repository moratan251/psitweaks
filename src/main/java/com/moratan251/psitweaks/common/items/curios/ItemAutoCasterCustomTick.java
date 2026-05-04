package com.moratan251.psitweaks.common.items.curios;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import vazkii.psi.api.exosuit.PsiArmorEvent;

public class ItemAutoCasterCustomTick extends ItemCuriosCompat {
    public static final String TAG_CAST_INTERVAL = "CustomTickInterval";
    public static final int DEFAULT_CAST_INTERVAL = 20;
    public static final int MIN_CAST_INTERVAL = 1;
    public static final int MAX_CAST_INTERVAL = 1200;

    public ItemAutoCasterCustomTick(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public String getEvent(ItemStack stack) {
        return "psitweaks.event.custom_tick";
    }

    @Override
    public void onEvent(ItemStack stack, PsiArmorEvent event) {
        if (!event.type.equals(getTrueEvent(stack)) || event.getEntity() == null) {
            return;
        }

        int interval = getCastInterval(stack);
        if (event.getEntity().level().getGameTime() % interval == 0L) {
            cast(stack, event);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) {
            openCustomTickScreen(held, hand);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.psitweaks.auto_caster_custom_tick.interval", getCastInterval(stack)));
    }

    public static int getCastInterval(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int interval = tag.contains(TAG_CAST_INTERVAL, Tag.TAG_INT) ? tag.getInt(TAG_CAST_INTERVAL) : DEFAULT_CAST_INTERVAL;
        return Mth.clamp(interval, MIN_CAST_INTERVAL, MAX_CAST_INTERVAL);
    }

    public static void setCastInterval(ItemStack stack, int interval) {
        int clamped = Mth.clamp(interval, MIN_CAST_INTERVAL, MAX_CAST_INTERVAL);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(TAG_CAST_INTERVAL, clamped));
    }

    private static void openCustomTickScreen(ItemStack stack, InteractionHand hand) {
        try {
            Class<?> screenClass = Class.forName("com.moratan251.psitweaks.client.gui.machine.GuiAutoCasterCustomTick");
            screenClass.getMethod("open", ItemStack.class, InteractionHand.class).invoke(null, stack, hand);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }
}
