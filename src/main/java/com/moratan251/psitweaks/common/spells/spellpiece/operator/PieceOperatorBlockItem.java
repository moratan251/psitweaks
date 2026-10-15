package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamBlockValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

/** The pick-block item of a Block value, without a source. Air and blocks without an item give an empty Item. */
public class PieceOperatorBlockItem extends PieceOperator {
    private SpellParam<?> target;

    public PieceOperatorBlockItem(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamBlockValue(SpellParam.GENERIC_NAME_TARGET, PsitweaksSpellParams.BLOCK_COLOR,
                false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockValue block = BlockValueHelper.getBlockValue(this, context, target);
        if (block.state().isAir()) {
            return SpellItemValue.EMPTY;
        }
        ItemStack stack = pickStack(context, block);
        return stack.isEmpty() ? SpellItemValue.EMPTY : SpellItemValue.snapshot(stack);
    }

    /**
     * Asks the block for its pick-block stack while the world still holds the snapshot's state. Otherwise, or when the
     * block fails, the block's own item stands in, so another block's data never leaks into the result.
     */
    static ItemStack pickStack(SpellContext context, BlockValue block) {
        BlockState state = block.state();
        Level level = context.focalPoint == null ? null : context.focalPoint.level();
        BlockPos pos = block.blockPos();
        if (level != null && level.dimension().equals(block.dimension()) && level.isLoaded(pos)
                && level.getBlockState(pos) == state) {
            try {
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                return state.getCloneItemStack(hit, level, pos, context.caster).copy();
            } catch (RuntimeException ignored) {
                // Fall back to the plain block item below.
            }
        }
        return new ItemStack(state.getBlock());
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
