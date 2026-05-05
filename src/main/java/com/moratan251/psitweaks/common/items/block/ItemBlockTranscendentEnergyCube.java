package com.moratan251.psitweaks.common.items.block;

import com.moratan251.psitweaks.common.tier.TranscendentEnergyCubeTier;
import com.moratan251.psitweaks.common.tile.machine.TileEntityTranscendentEnergyCube;
import mekanism.api.tier.ITier;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.containers.energy.EnergyContainersBuilder;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemBlockTranscendentEnergyCube extends ItemBlockTooltip<BlockTile.BlockTileModel<TileEntityTranscendentEnergyCube, BlockTypeTile<TileEntityTranscendentEnergyCube>>> {
    public ItemBlockTranscendentEnergyCube(BlockTile.BlockTileModel<TileEntityTranscendentEnergyCube, BlockTypeTile<TileEntityTranscendentEnergyCube>> block,
                                          Item.Properties properties) {
        super(block, true, properties
                .rarity(Rarity.EPIC)
                .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
                .component(MekanismDataComponents.SIDE_CONFIG, ItemBlockEnergyCube.SIDE_CONFIG));
    }

    @Override
    public ITier getTier() {
        return TranscendentEnergyCubeTier.TRANSCENDENT;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                EnergyDisplay.of(TileEntityTranscendentEnergyCube.getStorage())));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    protected void addTypeDetails(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    }

    @Override
    protected void addStats(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(MekanismLang.MAX_OUTPUT.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                EnergyDisplay.of(TileEntityTranscendentEnergyCube.getOutput())));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    protected EnergyContainersBuilder addDefaultEnergyContainers(EnergyContainersBuilder builder) {
        return builder.addBasic(
                BasicEnergyContainer.manualOnly,
                getEnergyCapInsertPredicate(),
                TileEntityTranscendentEnergyCube::getOutput,
                TileEntityTranscendentEnergyCube::getStorage
        );
    }
}
