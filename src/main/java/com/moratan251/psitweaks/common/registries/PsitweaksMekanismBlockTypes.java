package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.common.tile.machine.MaterialMutatorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.ProgramResearcherBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.SculkEroderBlockEntity;
import com.moratan251.psitweaks.common.tile.machine.TileEntityTranscendentEnergyCube;
import com.moratan251.psitweaks.common.tile.transmitter.TileEntityTranscendentCable;
import com.moratan251.psitweaks.common.tier.TranscendentEnergyCubeTier;
import java.util.EnumSet;
import mekanism.api.Upgrade;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tier.CableTier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import vazkii.psi.client.fx.SparkleParticleData;

public final class PsitweaksMekanismBlockTypes {
    private static final long SCULK_ERODER_STORAGE = 20_000L;
    private static final long PROGRAM_RESEARCHER_STORAGE = 1_000_000L;
    private static final long MATERIAL_MUTATOR_STORAGE = 4_000_000L;
    private static final long PSIONIC_GENERATOR_MAX_OUTPUT = 6_250L;
    private static final ILangEntry DESCRIPTION_SCULK_ERODER = () -> "description.psitweaks.sculk_eroder";
    private static final ILangEntry DESCRIPTION_PROGRAM_RESEARCHER = () -> "description.psitweaks.program_researcher";
    private static final ILangEntry DESCRIPTION_MATERIAL_MUTATOR = () -> "description.psitweaks.material_mutator";
    private static final ILangEntry DESCRIPTION_PSIONIC_GENERATOR = () -> "description.psitweaks.psionic_generator";
    private static final ILangEntry DESCRIPTION_TRANSCENDENT_CABLE = () -> "description.psitweaks.transcendent_universal_cable";
    private static final ILangEntry DESCRIPTION_TRANSCENDENT_ENERGY_CUBE = () -> "description.psitweaks.transcendent_energy_cube";

    public static final BlockTypeTile<SculkEroderBlockEntity> SCULK_ERODER =
            BlockTileBuilder.createBlock(() -> PsitweaksMekanismTileEntityTypes.SCULK_ERODER, DESCRIPTION_SCULK_ERODER)
                    .with(
                            Attributes.ACTIVE_LIGHT,
                            new AttributeStateFacing(),
                            Attributes.INVENTORY,
                            Attributes.SECURITY,
                            Attributes.REDSTONE,
                            Attributes.COMPARATOR,
                            AttributeSideConfig.ELECTRIC_MACHINE,
                            new AttributeUpgradeSupport(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING))
                    )
                    .with(new AttributeParticleFX()
                            .add(ParticleTypes.SCULK_SOUL, PsitweaksMekanismBlockTypes::machineFrontParticleOffset)
                            .addDense(ParticleTypes.SCULK_CHARGE_POP, 2, PsitweaksMekanismBlockTypes::machineFrontParticleOffset))
                    .withGui(() -> PsitweaksMekanismContainerTypes.SCULK_ERODER)
                    .withSound(MekanismSounds.ENRICHMENT_CHAMBER)
                    .withEnergyConfig(() -> 50L, () -> SCULK_ERODER_STORAGE)
                    .withComputerSupport("sculkEroder")
                    .build();

    public static final BlockTypeTile<ProgramResearcherBlockEntity> PROGRAM_RESEARCHER =
            BlockTileBuilder.createBlock(() -> PsitweaksMekanismTileEntityTypes.PROGRAM_RESEARCHER, DESCRIPTION_PROGRAM_RESEARCHER)
                    .with(
                            Attributes.ACTIVE_LIGHT,
                            new AttributeStateFacing(),
                            Attributes.INVENTORY,
                            Attributes.SECURITY,
                            Attributes.REDSTONE,
                            Attributes.COMPARATOR,
                            AttributeSideConfig.ELECTRIC_MACHINE,
                            new AttributeUpgradeSupport(EnumSet.of(Upgrade.MUFFLING))
                    )
                    .withGui(() -> PsitweaksMekanismContainerTypes.PROGRAM_RESEARCHER)
                    .withSound(MekanismSounds.ENRICHMENT_CHAMBER)
                    .withEnergyConfig(() -> PROGRAM_RESEARCHER_STORAGE, () -> PROGRAM_RESEARCHER_STORAGE)
                    .withComputerSupport("programResearcher")
                    .build();

    public static final BlockTypeTile<MaterialMutatorBlockEntity> MATERIAL_MUTATOR =
            BlockTileBuilder.createBlock(() -> PsitweaksMekanismTileEntityTypes.MATERIAL_MUTATOR, DESCRIPTION_MATERIAL_MUTATOR)
                    .with(
                            Attributes.ACTIVE_LIGHT,
                            new AttributeStateFacing(),
                            Attributes.INVENTORY,
                            Attributes.SECURITY,
                            Attributes.REDSTONE,
                            Attributes.COMPARATOR,
                            AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE,
                            new AttributeUpgradeSupport(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.CHEMICAL, Upgrade.MUFFLING))
                    )
                    .with(new AttributeParticleFX()
                            .add(new SparkleParticleData(0.9F, 0.55F, 0.85F, 1.0F, 10, 0.0D, -0.01D, 0.0D), PsitweaksMekanismBlockTypes::machineFrontParticleOffset)
                            .addDense(new SparkleParticleData(0.7F, 0.35F, 0.75F, 1.0F, 8, 0.0D, -0.02D, 0.0D), 2, PsitweaksMekanismBlockTypes::machineFrontParticleOffset))
                    .withGui(() -> PsitweaksMekanismContainerTypes.MATERIAL_MUTATOR)
                    .withSound(MekanismSounds.CHEMICAL_INJECTION_CHAMBER)
                    .withEnergyConfig(() -> 200L, () -> MATERIAL_MUTATOR_STORAGE)
                    .withComputerSupport("materialMutator")
                    .build();

    public static final BlockTypeTile<PsionicGeneratorBlockEntity> PSIONIC_GENERATOR =
            BlockTileBuilder.createBlock(() -> PsitweaksMekanismTileEntityTypes.PSIONIC_GENERATOR, DESCRIPTION_PSIONIC_GENERATOR)
                    .with(
                            Attributes.ACTIVE_LIGHT,
                            new AttributeStateFacing(),
                            Attributes.INVENTORY,
                            Attributes.SECURITY,
                            Attributes.REDSTONE,
                            Attributes.COMPARATOR,
                            AttributeSideConfig.create(mekanism.common.lib.transmitter.TransmissionType.ENERGY),
                            new AttributeUpgradeSupport(EnumSet.of(Upgrade.MUFFLING))
                    )
                    .with(new AttributeParticleFX()
                            .add(new SparkleParticleData(0.45F, 0.95F, 1.0F, 1.0F, 10, 0.0D, -0.01D, 0.0D), PsitweaksMekanismBlockTypes::machineFrontParticleOffset)
                            .addDense(new SparkleParticleData(0.75F, 0.45F, 1.0F, 1.0F, 8, 0.0D, -0.02D, 0.0D), 2, PsitweaksMekanismBlockTypes::machineFrontParticleOffset))
                    .withGui(() -> PsitweaksMekanismContainerTypes.PSIONIC_GENERATOR)
                    .withSound(MekanismSounds.LASER)
                    .withEnergyConfig(() -> PSIONIC_GENERATOR_MAX_OUTPUT, PsionicGeneratorBlockEntity::getEnergyCapacityJoules)
                    .build();

    public static final BlockTypeTile<TileEntityTranscendentCable> TRANSCENDENT_CABLE =
            BlockTileBuilder.createBlock(() -> PsitweaksMekanismTileEntityTypes.TRANSCENDENT_CABLE, DESCRIPTION_TRANSCENDENT_CABLE)
                    .with(new AttributeTier<>(CableTier.ULTIMATE))
                    .withComputerSupport("transcendentCable")
                    .build();

    public static final BlockTypeTile<TileEntityTranscendentEnergyCube> TRANSCENDENT_ENERGY_CUBE =
            BlockTileBuilder.createBlock(() -> PsitweaksMekanismTileEntityTypes.TRANSCENDENT_ENERGY_CUBE, DESCRIPTION_TRANSCENDENT_ENERGY_CUBE)
                    .with(
                            new AttributeStateFacing(BlockStateProperties.FACING),
                            Attributes.INVENTORY,
                            Attributes.SECURITY,
                            Attributes.REDSTONE,
                            Attributes.COMPARATOR,
                            AttributeSideConfig.create(TransmissionType.ENERGY, TransmissionType.ITEM),
                            new AttributeTier<>(TranscendentEnergyCubeTier.TRANSCENDENT)
                    )
                    .withGui(() -> PsitweaksMekanismContainerTypes.TRANSCENDENT_ENERGY_CUBE)
                    .withEnergyConfig(TileEntityTranscendentEnergyCube::getOutput, TileEntityTranscendentEnergyCube::getStorage)
                    .withComputerSupport("transcendentEnergyCube")
                    .build();

    private PsitweaksMekanismBlockTypes() {
    }

    private static Pos3D machineFrontParticleOffset(RandomSource random) {
        return new Pos3D(
                random.nextFloat() * 0.6F - 0.3F,
                random.nextFloat() * 6.0F / 16.0F,
                0.52D
        );
    }

}
