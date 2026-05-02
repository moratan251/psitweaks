package com.moratan251.psitweaks.common.items.block;

import com.moratan251.psitweaks.client.render.item.block.RenderTranscendentEnergyCubeItem;
import com.moratan251.psitweaks.common.tier.TranscendentEnergyCubeTier;
import com.moratan251.psitweaks.common.tile.machine.TileEntityTranscendentEnergyCube;
import mekanism.api.tier.ITier;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.ItemStackEnergyHandler;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import mekanism.common.block.prefab.BlockTile;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ItemBlockTranscendentEnergyCube extends ItemBlockMachine {

    public ItemBlockTranscendentEnergyCube(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return RenderTranscendentEnergyCubeItem.RENDERER;
            }
        });
    }

    @Override
    public ITier getTier() {
        return TranscendentEnergyCubeTier.TRANSCENDENT;
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                EnergyDisplay.of(TileEntityTranscendentEnergyCube.getStorage())));
        tooltip.add(MekanismLang.MAX_OUTPUT.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                EnergyDisplay.of(TileEntityTranscendentEnergyCube.getOutput())));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        RateLimitEnergyHandler energyHandler = RateLimitEnergyHandler.create(
                TileEntityTranscendentEnergyCube::getOutput,
                TileEntityTranscendentEnergyCube::getStorage,
                BasicEnergyContainer.alwaysTrue,
                BasicEnergyContainer.alwaysTrue
        );
        for (int i = 0; i < capabilities.size(); i++) {
            if (capabilities.get(i) instanceof ItemStackEnergyHandler) {
                capabilities.set(i, energyHandler);
                return;
            }
        }
        capabilities.add(energyHandler);
    }
}
