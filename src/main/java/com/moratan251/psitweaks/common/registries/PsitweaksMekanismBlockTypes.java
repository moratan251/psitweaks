package com.moratan251.psitweaks.common.registries;

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

import java.util.EnumSet;

public class PsitweaksMekanismBlockTypes {

    private static final FloatingLong SCULK_ERODER_STORAGE = FloatingLong.createConst(20000);
    private static final ILangEntry DESCRIPTION_SCULK_ERODER = () -> "description.psitweaks.sculk_eroder";

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
