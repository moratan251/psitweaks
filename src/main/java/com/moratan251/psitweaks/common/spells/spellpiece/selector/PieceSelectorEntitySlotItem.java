package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.util.ItemListValueHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorEntitySlotItem extends PieceSelector {
    private SpellParam<Entity> target;
    private SpellParam<Number> number;

    public PieceSelectorEntitySlotItem(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psi.spellparam.target", SpellParam.GRAY, false, false));
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity entity = getNotNullParamValue(context, target);
        context.verifyEntity(entity);

        int slot = getNotNullParamValue(context, number).intValue();
        if (entity instanceof Player player) {
            return ItemListValueHelper.fromEntityContainerSlot(player, player.getInventory(), slot);
        }

        IItemHandler handler = Capabilities.ItemHandler.ENTITY.getCapability(entity, null);
        if (handler != null) {
            return ItemListValueHelper.fromEntityItemHandlerSlot(entity, handler, slot);
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
