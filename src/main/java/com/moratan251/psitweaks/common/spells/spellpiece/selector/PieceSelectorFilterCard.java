package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.items.FilterCards;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * Reads one template of the caster's Filter Card in use, as an Item value without a source.
 */
public class PieceSelectorFilterCard extends PieceSelector {
    public static final String ERROR_NO_CARD = "psitweaks.spellerror.no_filter_card";
    public static final String ERROR_EMPTY_SLOT = "psitweaks.spellerror.filter_card_slot_empty";
    public static final String ERROR_SLOT_RANGE = "psitweaks.spellerror.filter_card_slot_range";

    private SpellParam<Number> number;

    public PieceSelectorFilterCard(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.GREEN, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Number constant = getParamEvaluation(number);
        if (constant != null && !isValidSlot(constant)) {
            throw new SpellCompilationException(ERROR_SLOT_RANGE, x, y);
        }
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        return resolve(context.caster, getNotNullParamValue(context, number));
    }

    /** The piece's result for a caster and a 1-based slot number. */
    public static SpellItemValue resolve(Player caster, Number slotNumber) throws SpellRuntimeException {
        if (!isValidSlot(slotNumber)) {
            throw new SpellRuntimeException(ERROR_SLOT_RANGE);
        }
        if (caster == null) {
            throw new SpellRuntimeException(ERROR_NO_CARD);
        }
        ItemStack card = FilterCards.findActive(caster)
                .orElseThrow(() -> new SpellRuntimeException(ERROR_NO_CARD));
        int slot = slotNumber.intValue();
        ItemStack template = FilterCards.template(card, slot - 1);
        if (template.isEmpty()) {
            throw new SpellRuntimeException(ERROR_EMPTY_SLOT, slot);
        }
        return SpellItemValue.snapshot(template);
    }

    private static boolean isValidSlot(Number slotNumber) {
        double value = slotNumber.doubleValue();
        return value == Math.floor(value) && value >= 1 && value <= FilterCards.SLOT_COUNT;
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
