package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.util.ItemListValueHelper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.Optional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
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
        Entity entity = getRequired(getParamValue(context, target));
        context.verifyEntity(entity);

        if (entity instanceof Player player) {
            return ItemListValueHelper.fromEntityContainer(player, player.getInventory());
        }

        Optional<IItemHandler> handler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handler.isPresent()) {
            return ItemListValueHelper.fromEntityItemHandler(entity, handler.get());
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

    private static <T> T getRequired(T value) throws SpellRuntimeException {
        if (value == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        return value;
    }
}

