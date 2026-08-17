package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.tile.ConjuredPulsarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.spell.trick.block.PieceTrickConjureBlock;

final class PulsarConjurationHelper {
    private PulsarConjurationHelper() {
    }

    static boolean conjure(SpellContext context, Level level, BlockPos pos, Number time, BlockState state) {
        if (level.isClientSide || level.getBlockState(pos).is(state.getBlock())) {
            return false;
        }
        if (!PieceTrickConjureBlock.conjure(level, pos, context.caster, state)) {
            return false;
        }

        if (time != null && time.intValue() > 0) {
            level.scheduleTick(pos, state.getBlock(), time.intValue());
        }
        if (level.getBlockEntity(pos) instanceof ConjuredPulsarBlockEntity pulsar) {
            pulsar.setColorizer(getColorizer(context));
        }
        return true;
    }

    private static ItemStack getColorizer(SpellContext context) {
        ItemStack cad = PsiAPI.getPlayerCAD(context.caster);
        if (!cad.isEmpty() && cad.getItem() instanceof ICAD cadItem) {
            return cadItem.getComponentInSlot(cad, EnumCADComponent.DYE);
        }
        return ItemStack.EMPTY;
    }
}

