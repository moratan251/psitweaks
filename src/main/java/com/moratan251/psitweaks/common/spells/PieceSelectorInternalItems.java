package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.util.ItemListValueHelper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
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
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorInternalItems extends PieceSelector {
    private SpellParam<Vector3> position;

    public PieceSelectorInternalItems(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockPos pos = SpellHelpers.getBlockPos(this, context, position, true, false);
        Level level = context.focalPoint.level();
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        IItemHandler handler = Capabilities.ItemHandler.BLOCK.getCapability(level, pos, state, blockEntity, null);
        if (handler != null) {
            return ItemListValueHelper.fromBlockItemHandler(level, pos, handler);
        }

        if (blockEntity instanceof Container container) {
            return ItemListValueHelper.fromBlockContainer(level, pos, container);
        }

        return SpellItemListWrapper.EMPTY;
    }

    @Override
    public Class<?> getEvaluationType() {
        return SpellItemListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.item_list");
    }
}
