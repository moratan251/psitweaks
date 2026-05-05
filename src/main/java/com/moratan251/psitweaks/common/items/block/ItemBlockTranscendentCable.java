package com.moratan251.psitweaks.common.items.block;

import com.moratan251.psitweaks.common.blocks.transmitter.BlockTranscendentCable;
import com.moratan251.psitweaks.common.content.network.transmitter.TranscendentCable;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemBlockTranscendentCable extends ItemBlockTooltip<BlockTranscendentCable> {
    public ItemBlockTranscendentCable(BlockTranscendentCable block, Item.Properties properties) {
        super(block, true, properties.rarity(Rarity.EPIC));
    }

    @Override
    protected void addDetails(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.addDetails(stack, context, tooltip, flag);
        tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        tooltip.add(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.PURPLE, MekanismLang.ENERGY_FORGE_SHORT, MekanismLang.FORGE));
        tooltip.add(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.PURPLE, MekanismLang.ENERGY_JOULES_PLURAL, MekanismLang.MEKANISM));
    }

    @Override
    protected void addStats(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.addStats(stack, context, tooltip, flag);
        tooltip.add(MekanismLang.CAPACITY_PER_TICK.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                EnergyDisplay.of(TranscendentCable.CAPACITY)));
    }
}
