package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.client.PsitweaksMekanismClient;
import com.moratan251.psitweaks.client.gui.machine.MessagePsiLinkGeneratorSettingsSync;
import com.moratan251.psitweaks.common.chemicals.PsitweaksGases;
import com.moratan251.psitweaks.common.chemicals.PsitweaksInfuseTypes;
import com.moratan251.psitweaks.common.chemicals.PsitweaksSlurries;
import com.moratan251.psitweaks.common.handler.NetworkHandler;
import com.moratan251.psitweaks.common.handler.MekanismMaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.handler.PsitweaksMekanismGeneratorTweaks;
import com.moratan251.psitweaks.common.items.PsitweaksMekanismItems;
import com.moratan251.psitweaks.common.items.armor.ArmorSpellDamageAttributeHandler;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismRecipeSerializers;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismRecipeTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismTileEntityTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import com.moratan251.psitweaks.common.tile.machine.TileEntityTranscendentEnergyCube;
import mekanism.api.MekanismIMC;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.FloatingLong;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.StorageUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

final class MekanismIntegration {
    private MekanismIntegration() {
    }

    static void register(IEventBus modEventBus) {
        PsitweaksMekanismItems.register(modEventBus);
        PsitweaksInfuseTypes.register(modEventBus);
        PsitweaksGases.register(modEventBus);
        PsitweaksSlurries.register(modEventBus);
        PsitweaksMekanismBlocks.register(modEventBus);
        PsitweaksMekanismTileEntityTypes.register(modEventBus);
        PsitweaksMekanismContainerTypes.register(modEventBus);
        PsitweaksMekanismRecipeTypes.register(modEventBus);
        PsitweaksMekanismRecipeSerializers.register(modEventBus);
        PsitweaksModules.MODULES.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(ArmorSpellDamageAttributeHandler::onItemAttributeModifier);
        MinecraftForge.EVENT_BUS.addListener(MekanismMaterialMutationRecipeHandler::onAddReloadListener);
    }

    static void commonSetup(FMLCommonSetupEvent event) {
        if (MekanismCompat.isGeneratorsLoaded()) {
            event.enqueueWork(PsitweaksMekanismGeneratorTweaks::registerGeneratorTweaks);
        }
    }

    static void enqueueIMC() {
        MekanismIMC.addMekaSuitBodyarmorModules(
                PsitweaksModules.PSYON_SUPPLYING_UNIT,
                PsitweaksModules.PSYON_CAPACITY_UNIT
        );
        MekanismIMC.addMekaSuitHelmetModules(PsitweaksModules.PHENOMENON_INTERFERENCE_ENHANCEMENT_UNIT);
    }

    static void registerClient(IEventBus modEventBus) {
        PsitweaksMekanismClient.register(modEventBus);
        modEventBus.addListener(MekanismMaterialMutationRecipeHandler::onRegisterClientReloadListeners);
    }

    static void registerNetworkMessages(int messageId) {
        NetworkHandler.CHANNEL.registerMessage(
                messageId,
                MessagePsiLinkGeneratorSettingsSync.class,
                MessagePsiLinkGeneratorSettingsSync::encode,
                MessagePsiLinkGeneratorSettingsSync::decode,
                MessagePsiLinkGeneratorSettingsSync::handle
        );
    }

    static void addCreativeTabContents(CreativeModeTab.Output output) {
        output.accept(PsitweaksMekanismItems.MODULE_PSYON_SUPPLYING.get());
        output.accept(PsitweaksMekanismItems.MODULE_PSYON_CAPACITY.get());
        output.accept(PsitweaksMekanismItems.MODULE_PHENOMENON_INTERFERENCE_ENHANCEMENT.get());
        output.accept(PsitweaksMekanismBlocks.SCULK_ERODER.getBlock());
        output.accept(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.getBlock());
        output.accept(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.getBlock());
        output.accept(PsitweaksMekanismBlocks.PSIONIC_GENERATOR.getBlock());
        output.accept(PsitweaksMekanismBlocks.TRANSCENDENT_CABLE.getBlock());
        ItemStack emptyCube = new ItemStack(PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.getBlock());
        output.accept(emptyCube);
        output.accept(StorageUtils.getFilledEnergyVariant(emptyCube.copy(), TileEntityTranscendentEnergyCube.getStorage()));
    }

    static SoundEvent laserSound() {
        return MekanismSounds.LASER.get();
    }

    static ResourceKey<DamageType> laserDamageType() {
        return MekanismDamageTypes.LASER.key();
    }

    static void supplyLaserEnergy(BlockEntity blockEntity, long energy) {
        if (blockEntity instanceof ILaserReceptor receptor) {
            receptor.receiveLaserEnergy(FloatingLong.create(energy));
        }
    }
}
