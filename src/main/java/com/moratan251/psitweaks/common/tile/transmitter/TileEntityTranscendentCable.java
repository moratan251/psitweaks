package com.moratan251.psitweaks.common.tile.transmitter;

import com.moratan251.psitweaks.common.content.network.transmitter.TranscendentCable;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityTranscendentCable extends TileEntityUniversalCable {

    public TileEntityTranscendentCable(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.TRANSCENDENT_CABLE, pos, state);
    }

    @Override
    protected UniversalCable createTransmitter(IBlockProvider blockProvider) {
        return new TranscendentCable(blockProvider, this);
    }

    @Override
    public TranscendentCable getTransmitter() {
        return (TranscendentCable) super.getTransmitter();
    }

    @Override
    public void onAlloyInteraction(Player player, ItemStack stack, AlloyTier tier) {
    }

    @Override
    protected BlockState upgradeResult(BlockState current, BaseTier tier) {
        return current;
    }

    @Override
    public String getComputerName() {
        return "transcendentCable";
    }
}
