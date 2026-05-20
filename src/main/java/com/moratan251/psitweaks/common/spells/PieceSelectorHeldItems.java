package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.util.ItemListStringHelper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
            return ItemListStringHelper.fromContainer(player.getInventory());
        }

        IItemHandler handler = Capabilities.ItemHandler.ENTITY.getCapability(entity, null);
        if (handler != null) {
            return ItemListStringHelper.fromItemHandler(handler);
        }

        if (entity instanceof LivingEntity living) {
            List<ItemStack> stacks = new ArrayList<>();
            living.getHandSlots().forEach(stacks::add);
            living.getArmorSlots().forEach(stacks::add);
            return ItemListStringHelper.fromStacks(stacks);
        }

        return StringListWrapper.EMPTY;
    }

    @Override
    public Class<?> getEvaluationType() {
        return StringListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string_list");
    }
}
