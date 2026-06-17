package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.item.EntityHandSource;
import com.moratan251.psitweaks.common.spells.item.EntityInventorySlotSource;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorHeldItem extends PieceSelector {
    private SpellParam<Entity> target;

    public PieceSelectorHeldItem(Spell spell) {
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
            int slot = player.getInventory().selected;
            return SpellItemValue.sourced(
                    player.getMainHandItem(),
                    new EntityInventorySlotSource(player.getUUID(), player.getId(), slot));
        }

        if (entity instanceof LivingEntity living) {
            return SpellItemValue.sourced(
                    living.getMainHandItem(),
                    new EntityHandSource(living.getUUID(), InteractionHand.MAIN_HAND));
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
