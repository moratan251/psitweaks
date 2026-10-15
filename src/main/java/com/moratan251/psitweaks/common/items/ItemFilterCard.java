package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.menu.FilterCardMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemFilterCard extends Item implements ICurioItem {
    public ItemFilterCard(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new FilterCardMenu.Provider(hand, stack.getHoverName()),
                    buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /**
     * Curios lists every slot the card is valid for, which depends on the slots other mods add. The card fits any
     * tag-validated slot through {@code curios:curio}, so show that instead of the list.
     */
    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        if (tooltips.isEmpty()) {
            return tooltips;
        }
        return List.of(Component.translatable("curios.tooltip.slot").append(" ").withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("tooltip.psitweaks.filter_card.any_slot")
                        .withStyle(ChatFormatting.YELLOW)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<ItemStack> templates = FilterCards.templates(stack);
        boolean empty = true;
        for (int slot = 0; slot < templates.size(); slot++) {
            ItemStack template = templates.get(slot);
            if (template.isEmpty()) {
                continue;
            }
            empty = false;
            tooltip.add(Component.translatable("tooltip.psitweaks.filter_card.slot", slot + 1, template.getHoverName())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (empty) {
            tooltip.add(Component.translatable("tooltip.psitweaks.filter_card.empty").withStyle(ChatFormatting.GRAY));
        }
    }
}
