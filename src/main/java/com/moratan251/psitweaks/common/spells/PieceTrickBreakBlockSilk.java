package com.moratan251.psitweaks.common.spells;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.spell.trick.block.PieceTrickBreakBlock;

import java.util.function.Predicate;

public class PieceTrickBreakBlockSilk extends PieceTrick {

    private static final int POTENCY = 250;
    private static final int COST = 250;

    SpellParam<Vector3> position;

    public PieceTrickBreakBlockSilk(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = this.getParamValue(context, position);

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        BlockPos pos = positionVal.toBlockPos();
        Level world = context.focalPoint.getCommandSenderWorld();

        ItemStack silkTool = createSilkTool(context);

        Predicate<BlockState> filter = state -> silkTool.isCorrectToolForDrops(state)
                || PieceTrickBreakBlock.canHarvest(ConfigHandler.COMMON.cadHarvestLevel.get(), state);

        PieceTrickBreakBlock.removeBlockWithDrops(context, context.caster, world, silkTool, pos, filter);

        return null;
    }

    private ItemStack createSilkTool(SpellContext context) throws SpellRuntimeException {
        ItemStack tool = context.getHarvestTool().copy();
        tool.enchant(Enchantments.SILK_TOUCH, 1);
        return tool;
    }
}
