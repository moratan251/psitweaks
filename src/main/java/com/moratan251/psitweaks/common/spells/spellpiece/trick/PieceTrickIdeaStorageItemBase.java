package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Only item logistics has multiple filter modes. */
public abstract class PieceTrickIdeaStorageItemBase extends PieceTrickIdeaStorageResourceBase implements ModeConfigurableSpellPiece {
    private PsitweaksModeOption mode = PsitweaksModeOptions.STRING;
    private SpellParam<SpellItemValue> item;

    protected PieceTrickIdeaStorageItemBase(Spell spell) {
        super(spell);
    }

    @Override public List<PsitweaksModeOption> getAvailableModeOptions() {
        return List.of(PsitweaksModeOptions.STRING, PsitweaksModeOptions.ITEM, PsitweaksModeOptions.ITEM_STRICT);
    }

    @Override public PsitweaksModeOption getModeOption() { return normalizeModeOption(mode); }

    @Override public void setModeOption(PsitweaksModeOption option) {
        var next = normalizeModeOption(option);
        if (next.id().equals(getModeOption().id())) return;
        var posSide = paramSides.get(position);
        var directionSide = paramSides.get(direction);
        var quantitySide = paramSides.get(quantity);
        var filterSide = paramSides.get(itemMode() ? item : string);
        mode = next;
        rebuildParams();
        if (posSide != null) paramSides.put(position, posSide);
        if (directionSide != null) paramSides.put(direction, directionSide);
        if (quantitySide != null) paramSides.put(quantity, quantitySide);
        if (filterSide != null) paramSides.put(itemMode() ? item : string, filterSide);
    }

    private boolean itemMode() { return !getModeOption().id().equals(PsitweaksModeOptions.STRING.id()); }
    private void rebuildParams() { params.clear(); paramSides.clear(); initParams(); }

    @Override protected void initFilter() {
        if (itemMode()) addParam(item = new ParamSpellItemValue(PsitweaksSpellParams.ITEM, PsitweaksSpellParams.ITEM_COLOR, true, false));
        else super.initFilter();
    }

    @Override public void readFromNBT(CompoundTag tag) {
        mode = normalizeModeOption(PsitweaksModeOptions.byId(tag.getString("psitweaksMode")).orElse(null));
        rebuildParams();
        super.readFromNBT(tag);
        // Psi saves standard parameter names as "_name"; retain pre-rename wiring.
        var savedParams = tag.getCompound("params");
        if (!savedParams.contains("_number") && !savedParams.contains("psi.spellparam.number")) {
            String oldKey = savedParams.contains("psi.spellparam.power", Tag.TAG_ANY_NUMERIC) ? "psi.spellparam.power" : "_power";
            if (savedParams.contains(oldKey, Tag.TAG_ANY_NUMERIC))
                paramSides.put(quantity, SpellParam.Side.fromInt(savedParams.getInt(oldKey)));
        }
    }

    @Override public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString("psitweaksMode", getModeOption().serializedId());
    }

    @Override protected Predicate<ItemStack> itemFilter(SpellContext context) throws SpellRuntimeException {
        if (!itemMode()) return super.itemFilter(context);
        SpellItemValue value = getParamValue(context, item);
        if (value == null) return stack -> true;
        if (value.isEmpty()) throw new SpellRuntimeException("psitweaks.spellerror.nullitem");
        ItemStack template = value.snapshot();
        return getModeOption().id().equals(PsitweaksModeOptions.ITEM_STRICT.id())
                ? stack -> ItemStack.isSameItemSameComponents(template, stack) : stack -> stack.is(template.getItem());
    }
}
