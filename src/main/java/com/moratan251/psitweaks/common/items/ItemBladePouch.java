package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.menu.BladePouchMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Blade Pouch: holds up to eight swords for Trick: Dancing Blades. Using it opens its slots. */
public class ItemBladePouch extends Item implements ICurioItem {
    public ItemBladePouch(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new BladePouchMenu.Provider(hand, stack.getHoverName()), buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** A pouch burnt or destroyed as an item entity drops its swords, like a shulker box. */
    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        ItemContainerContents contents = itemEntity.getItem().set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (contents != null) {
            ItemUtils.onContainerDestroyed(itemEntity, contents.nonEmptyItemsCopy());
        }
    }

    /** Like the Filter Card it fits any tag-validated Curios slot, so say so instead of listing the slots. */
    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        if (tooltips.isEmpty()) {
            return tooltips;
        }
        return List.of(Component.translatable("curios.tooltip.slot").append(" ").withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("tooltip.psitweaks.filter_card.any_slot").withStyle(ChatFormatting.YELLOW)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<ItemStack> swords = BladePouches.swords(stack);
        if (swords.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.psitweaks.blade_pouch.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("tooltip.psitweaks.blade_pouch.count", swords.size(), BladePouches.SLOT_COUNT)
                .withStyle(ChatFormatting.GRAY));
        for (ItemStack sword : swords) {
            Component line = Component.literal("- ").append(sword.getHoverName()).withStyle(ChatFormatting.GRAY);
            if (BladePouches.isBroken(sword)) {
                line = line.copy().append(" ").append(Component.translatable("tooltip.psitweaks.blade_pouch.broken")
                        .withStyle(ChatFormatting.RED));
            }
            tooltip.add(line);
        }
    }
}
