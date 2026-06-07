package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.util.ItemListValueHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellHelpers;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorInternalSlotItem extends PieceSelector {
    private SpellParam<Vector3> position;
    private SpellParam<Number> number;

    public PieceSelectorInternalSlotItem(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockPos pos = SpellHelpers.getBlockPos(this, context, position, true, false);
        int slot = getNotNullParamValue(context, number).intValue();
        Level level = context.focalPoint.level();
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        IItemHandler handler = Capabilities.ItemHandler.BLOCK.getCapability(level, pos, state, blockEntity, null);
        if (handler != null) {
            return ItemListValueHelper.fromBlockItemHandlerSlot(level, pos, handler, slot);
        }

        if (blockEntity instanceof Container container) {
            return ItemListValueHelper.fromBlockContainerSlot(level, pos, container, slot);
        }

        return SpellItemValue.EMPTY;
    }

    @Override
    public Class<?> getEvaluationType() {
        return SpellItemValue.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.item");
    }
}
