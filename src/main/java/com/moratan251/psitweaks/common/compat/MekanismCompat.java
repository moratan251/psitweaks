package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.storage.idea.IdeaStorageChemicalTransfer;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageTransferDirection;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.Nullable;

public final class MekanismCompat {
    private static final String MEKANISM_MOD_ID = "mekanism";
    private static final String GENERATORS_MOD_ID = "mekanismgenerators";

    private MekanismCompat() {
    }

    public static boolean isIdeaStorageChemicalContainer(net.minecraft.world.item.ItemStack stack) {
        return isMekanismLoaded() && MekanismIntegration.isIdeaStorageChemicalContainer(stack);
    }

    public static boolean isMekanismLoaded() {
        return ModList.get().isLoaded(MEKANISM_MOD_ID);
    }

    public static boolean isGeneratorsLoaded() {
        return isMekanismLoaded() && ModList.get().isLoaded(GENERATORS_MOD_ID);
    }

    public static void register(IEventBus modEventBus) {
        if (isMekanismLoaded()) {
            MekanismIntegration.register(modEventBus);
        }
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        if (isMekanismLoaded()) {
            MekanismIntegration.commonSetup(event);
        }
    }

    public static void registerClient(IEventBus modEventBus) {
        if (isMekanismLoaded()) {
            MekanismIntegration.registerClient(modEventBus);
        }
    }

    public static void addCreativeTabContents(CreativeModeTab.Output output) {
        if (isMekanismLoaded()) {
            MekanismIntegration.addCreativeTabContents(output);
        }
    }

    @Nullable
    public static IdeaStorageChemicalTransfer planIdeaStorageChemicalTransfer(
            PlayerIdeaStorage storage, ItemStack container, @Nullable ResourceLocation targetChemicalId,
            IdeaStorageTransferDirection direction) {
        return isMekanismLoaded()
                ? MekanismIntegration.planIdeaStorageChemicalTransfer(
                        storage, container, targetChemicalId, direction)
                : null;
    }
}
