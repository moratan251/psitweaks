package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.client.compat.MekanismClientIntegration;
import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import com.moratan251.psitweaks.common.handler.PsitweaksMekanismGeneratorTweaks;
import com.moratan251.psitweaks.common.handler.MekanismMaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.items.PsitweaksMekanismItems;
import com.moratan251.psitweaks.common.items.armor.ArmorSpellDamageAttributeHandler;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageChemicalTransfer;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageTransferDirection;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismTileEntityTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import mekanism.api.MekanismIMC;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.StorageUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

final class MekanismIntegration {
    private MekanismIntegration() {
    }

    static void register(IEventBus modEventBus) {
        PsitweaksMekanismItems.register(modEventBus);
        PsitweaksChemicals.register(modEventBus);
        PsitweaksMekanismBlocks.register(modEventBus);
        PsitweaksMekanismTileEntityTypes.register(modEventBus);
        PsitweaksMekanismContainerTypes.register(modEventBus);
        PsitweaksModules.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(ArmorSpellDamageAttributeHandler::onItemAttributeModifier);
        NeoForge.EVENT_BUS.addListener(MekanismMaterialMutationRecipeHandler::onAddReloadListener);
    }

    static void commonSetup(FMLCommonSetupEvent event) {
        if (MekanismCompat.isGeneratorsLoaded()) {
            event.enqueueWork(PsitweaksMekanismGeneratorTweaks::registerGeneratorTweaks);
        }
        event.enqueueWork(MekanismIntegration::enqueueIMC);
    }

    static void registerClient(IEventBus modEventBus) {
        MekanismClientIntegration.register(modEventBus);
    }

    static void addCreativeTabContents(CreativeModeTab.Output output) {
        output.accept(PsitweaksMekanismItems.MODULE_PSYON_SUPPLYING.get());
        output.accept(PsitweaksMekanismItems.MODULE_PSYON_CAPACITY.get());
        output.accept(PsitweaksMekanismItems.MODULE_PHENOMENON_INTERFERENCE_ENHANCEMENT.get());
        output.accept(PsitweaksMekanismBlocks.SCULK_ERODER.get());
        output.accept(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get());
        output.accept(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get());
        output.accept(PsitweaksMekanismBlocks.PSIONIC_GENERATOR.get());
        output.accept(PsitweaksMekanismBlocks.TRANSCENDENT_CABLE.get());
        output.accept(PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.get());
        output.accept(StorageUtils.getFilledEnergyVariant(
                PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.getItemHolder()
        ));
    }

    private static void enqueueIMC() {
        MekanismIMC.addMekaSuitBodyarmorModules(
                PsitweaksModules.PSYON_SUPPLYING_UNIT,
                PsitweaksModules.PSYON_CAPACITY_UNIT
        );
        MekanismIMC.addMekaSuitHelmetModules(
                PsitweaksModules.PHENOMENON_INTERFERENCE_ENHANCEMENT_UNIT
        );
    }

    @Nullable
    static IdeaStorageChemicalTransfer planIdeaStorageChemicalTransfer(
            PlayerIdeaStorage storage, ItemStack container, @Nullable ResourceLocation targetChemicalId,
            IdeaStorageTransferDirection direction) {
        ItemStack working = container.copyWithCount(1);
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(working);
        if (handler == null) {
            return null;
        }

        if (direction.allowsIntoContainer() && targetChemicalId != null) {
            var target = MekanismAPI.CHEMICAL_REGISTRY.getHolder(targetChemicalId)
                    .filter(holder -> !holder.is(MekanismAPI.EMPTY_CHEMICAL_KEY));
            long available = storage.simulateExtractChemical(targetChemicalId, Long.MAX_VALUE);
            if (target.isPresent() && available > 0) {
                ChemicalStack offered = new ChemicalStack(target.get(), available);
                ChemicalStack simulatedRemainder = handler.insertChemical(offered, Action.SIMULATE);
                long accepted = available - simulatedRemainder.getAmount();
                if (accepted > 0) {
                    ChemicalStack remainder = handler.insertChemical(offered.copyWithAmount(accepted), Action.EXECUTE);
                    long moved = accepted - remainder.getAmount();
                    if (moved > 0) {
                        return new IdeaStorageChemicalTransfer(working, targetChemicalId, moved, false);
                    }
                }
            }
        }

        if (!direction.allowsIntoStorage()) {
            return null;
        }
        for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
            ChemicalStack contained = handler.getChemicalInTank(tank);
            if (contained.isEmpty()) {
                continue;
            }
            ChemicalStack simulated = handler.extractChemical(tank, contained.getAmount(), Action.SIMULATE);
            if (simulated.isEmpty()) {
                continue;
            }
            ResourceLocation chemicalId = simulated.getChemicalHolder().unwrapKey()
                    .map(key -> key.location())
                    .orElse(null);
            if (chemicalId == null) {
                continue;
            }
            long accepted = storage.simulateInsertChemical(chemicalId, simulated.getAmount());
            if (accepted <= 0) {
                continue;
            }
            ChemicalStack extracted = handler.extractChemical(tank, accepted, Action.EXECUTE);
            if (!extracted.isEmpty()) {
                return new IdeaStorageChemicalTransfer(working, chemicalId, extracted.getAmount(), true);
            }
        }
        return null;
    }
}
