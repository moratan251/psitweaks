package com.moratan251.psitweaks.common.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 領域受電器。持ち物のどこか（防具・オフハンドも含む）に「入」のものがあるプレイヤーだけが、荷電領域の受け手になる。
 * 右クリックで入と切を切り替える。入のときはエンチャントの光を出す。
 */
public class ItemZoneReceiver extends Item {
    public ItemZoneReceiver(Properties properties) {
        super(properties);
    }

    /** {@code stack} が入か。切り替えたことのないものは入。 */
    public static boolean isEnabled(ItemStack stack) {
        return stack.getOrDefault(PsitweaksDataComponents.ZONE_RECEIVER_ENABLED.get(), true);
    }

    public static void setEnabled(ItemStack stack, boolean enabled) {
        stack.set(PsitweaksDataComponents.ZONE_RECEIVER_ENABLED.get(), enabled);
    }

    /** {@code player} の持ち物のどこかに、入の領域受電器があるか。 */
    public static boolean isCarriedBy(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof ItemZoneReceiver && isEnabled(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            boolean enabled = !isEnabled(stack);
            setEnabled(stack, enabled);
            player.displayClientMessage(Component.translatable(enabled
                    ? "message.psitweaks.zone_receiver.on" : "message.psitweaks.zone_receiver.off")
                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS,
                    0.4F, enabled ? 0.6F : 0.5F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isEnabled(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.psitweaks.zone_receiver.effect").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.psitweaks.zone_receiver.toggle").withStyle(ChatFormatting.GRAY));
        boolean enabled = isEnabled(stack);
        tooltip.add(Component.translatable(enabled
                ? "tooltip.psitweaks.zone_receiver.on" : "tooltip.psitweaks.zone_receiver.off")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
    }
}
