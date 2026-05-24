package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.util.ItemListValueHelper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorHeldItems extends PieceSelector {
    private SpellParam<Entity> target;

    public PieceSelectorHeldItems(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psi.spellparam.target", SpellParam.GRAY, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity entity = getNotNullParamValue(context, target);
        context.verifyEntity(entity);

        if (entity instanceof Player player) {
            return ItemListValueHelper.fromEntityContainer(player, player.getInventory());
        }

        IItemHandler handler = Capabilities.ItemHandler.ENTITY.getCapability(entity, null);
        if (handler != null) {
            return ItemListValueHelper.fromEntityItemHandler(entity, handler);
        }

        if (entity instanceof LivingEntity living) {
            return ItemListValueHelper.fromLivingEquipment(living);
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
