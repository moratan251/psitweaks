package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.common.tile.machine.TileEntityMaterialMutator;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import vazkii.psi.client.fx.SparkleParticleData;

import java.util.EnumSet;

public class PsitweaksMekanismBlockTypes {

    private static final FloatingLong SCULK_ERODER_STORAGE = FloatingLong.createConst(20000);
    private static final FloatingLong MATERIAL_MUTATOR_STORAGE = FloatingLong.createConst(4_000_000);
    private static final ILangEntry DESCRIPTION_SCULK_ERODER = () -> "description.psitweaks.sculk_eroder";
    private static final ILangEntry DESCRIPTION_MATERIAL_MUTATOR = () -> "description.psitweaks.material_mutator";

    public static final BlockTypeTile<TileEntitySculkEroder> SCULK_ERODER =
            BlockTileBuilder.createBlock(() -> PsitweaksMekanismTileEntityTypes.SCULK_ERODER, DESCRIPTION_SCULK_ERODER)
                    .with(
                            Attributes.ACTIVE_LIGHT,
                            new AttributeStateFacing(),
                            Attributes.INVENTORY,
                            Attributes.SECURITY,
                            Attributes.REDSTONE,
                            Attributes.COMPARATOR,
                            new AttributeUpgradeSupport(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING))
                    )
                    .with(new AttributeParticleFX()
                            .add(ParticleTypes.SCULK_SOUL, PsitweaksMekanismBlockTypes::machineFrontParticleOffset)
                            .addDense(ParticleTypes.SCULK_CHARGE_POP, 2, PsitweaksMekanismBlockTypes::machineFrontParticleOffset))
                    .withGui(() -> PsitweaksMekanismContainerTypes.SCULK_ERODER)
                    .withSound(MekanismSounds.ENRICHMENT_CHAMBER)
                    .withEnergyConfig(MekanismConfig.usage.enrichmentChamber, () -> SCULK_ERODER_STORAGE)
                    .withComputerSupport("sculkEroder")
                    .build();

    public static final BlockTypeTile<TileEntityMaterialMutator> MATERIAL_MUTATOR =
            BlockTileBuilder.createBlock(() -> PsitweaksMekanismTileEntityTypes.MATERIAL_MUTATOR, DESCRIPTION_MATERIAL_MUTATOR)
                    .with(
                            Attributes.ACTIVE_LIGHT,
                            new AttributeStateFacing(),
                            Attributes.INVENTORY,
                            Attributes.SECURITY,
                            Attributes.REDSTONE,
                            Attributes.COMPARATOR,
                            new AttributeUpgradeSupport(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.GAS, Upgrade.MUFFLING))
                    )
                    .with(new AttributeParticleFX()
                            .add(SparkleParticleData.sparkle(0.9F, 0.55F, 0.85F, 1.0F, 10, 0.0D, -0.01D, 0.0D), PsitweaksMekanismBlockTypes::machineFrontParticleOffset)
                            .addDense(SparkleParticleData.sparkle(0.7F, 0.35F, 0.75F, 1.0F, 8, 0.0D, -0.02D, 0.0D), 2, PsitweaksMekanismBlockTypes::machineFrontParticleOffset))
                    .withGui(() -> PsitweaksMekanismContainerTypes.MATERIAL_MUTATOR)
                    .withSound(MekanismSounds.CHEMICAL_INJECTION_CHAMBER)
                    .withEnergyConfig(() -> MekanismConfig.usage.chemicalInjectionChamber.get().multiply(100), () -> MATERIAL_MUTATOR_STORAGE)
                    .withComputerSupport("materialMutator")
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
