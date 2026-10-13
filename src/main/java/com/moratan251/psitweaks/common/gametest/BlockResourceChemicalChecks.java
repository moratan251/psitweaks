package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorBlockFreeCapacity;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorBlockResourceAmount;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;

/** Chemical reads of the block resource operators; run only when Mekanism is installed. */
final class BlockResourceChemicalChecks {
    private BlockResourceChemicalChecks() {
    }

    static void run(GameTestHelper helper, SpellContext context, IdeaspaceConnectorBlockEntity connector) throws Exception {
        var hydrogen = ResourceLocation.parse("mekanism:hydrogen");
        long old = PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(1000L);
            connector.setResource(3, ConnectorResource.chemical(hydrogen));
            connector.storage().insertChemical(hydrogen, 700);
            var pos = connector.getBlockPos();
            Object[] side = {new Vector3(0, 1, 0)};
            Object[] filter = {"hydro*"};
            var amount = new PieceOperatorBlockResourceAmount(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return param.name.equals(SpellParam.GENERIC_NAME_TARGET) ? new Vector3(pos.getX(), pos.getY(), pos.getZ())
                            : param.name.equals(SpellParam.GENERIC_NAME_DIRECTION) ? side[0] : filter[0];
                }
            };
            var free = new PieceOperatorBlockFreeCapacity(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                    return param.name.equals(SpellParam.GENERIC_NAME_TARGET) ? new Vector3(pos.getX(), pos.getY(), pos.getZ())
                            : param.name.equals(SpellParam.GENERIC_NAME_DIRECTION) ? side[0] : filter[0];
                }
            };
            amount.setModeOption(BlockResourceModes.CHEMICAL);
            free.setModeOption(BlockResourceModes.CHEMICAL);
            for (Object face : new Object[]{new Vector3(0, 1, 0), null}) {
                side[0] = face;
                filter[0] = "hydro*";
                helper.assertTrue(amount.execute(context).equals(700.0D), "Connector hydrogen amount, face " + face);
                filter[0] = "mekanism:oxygen";
                helper.assertTrue(amount.execute(context).equals(0.0D), "The chemical filter must apply, face " + face);
                filter[0] = "mekanism:hydrogen";
                helper.assertTrue(free.execute(context).equals(300.0D),
                        "The connector's tanks share one storage, face " + face + ": " + free.execute(context));
                filter[0] = "missing:chemical";
                helper.assertTrue(free.execute(context).equals(0.0D), "Unknown chemicals must read 0, face " + face);
            }
            connector.setSideMode(Direction.DOWN, ConnectorSideMode.DISABLED);
            side[0] = null;
            filter[0] = "hydro*";
            helper.assertTrue(amount.execute(context).equals(700.0D), "Without Direction the fullest face counts");
            filter[0] = "mekanism:hydrogen";
            helper.assertTrue(free.execute(context).equals(300.0D), "Free hydrogen without Direction, bottom disabled");
        } finally {
            connector.setSideMode(Direction.DOWN, ConnectorSideMode.BOTH);
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(old);
        }
    }
}
