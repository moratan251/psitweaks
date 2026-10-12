package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageTransferModes;
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

/**
 * Only item logistics has multiple filter modes. The mode menu combines them with the transfer mode, but the filter
 * type is stored under its original key and only it decides the parameters.
 */
public abstract class PieceTrickIdeaStorageItemBase extends PieceTrickIdeaStorageResourceBase {
    private PsitweaksModeOption mode = PsitweaksModeOptions.STRING;
    private SpellParam<SpellItemValue> item;

    protected PieceTrickIdeaStorageItemBase(Spell spell) {
        super(spell);
    }

    @Override public List<PsitweaksModeOption> getAvailableModeOptions() {
        return supportsExactTransfer() ? IdeaStorageTransferModes.ITEM_FILTERS_WITH_EXACT : IdeaStorageTransferModes.ITEM_FILTERS;
    }

    @Override public PsitweaksModeOption getModeOption() {
        return IdeaStorageTransferModes.withExact(filterMode(), isExactTransfer());
    }

    @Override public PsitweaksModeOption overlayModeOption() { return getModeOption(); }

    /** Runs during construction too, before {@link #mode} is assigned. */
    private PsitweaksModeOption filterMode() { return IdeaStorageTransferModes.filterOf(mode); }

    @Override public void setModeOption(PsitweaksModeOption option) {
        var next = normalizeModeOption(option);
        setExactTransfer(IdeaStorageTransferModes.isExact(next));
        var nextFilter = IdeaStorageTransferModes.filterOf(next);
        if (nextFilter.id().equals(filterMode().id())) return;
        var posSide = paramSides.get(position);
        var directionSide = paramSides.get(direction);
        var quantitySide = paramSides.get(quantity);
        var filterSide = paramSides.get(itemMode() ? item : string);
        mode = nextFilter;
        rebuildParams();
        if (posSide != null) paramSides.put(position, posSide);
        if (directionSide != null) paramSides.put(direction, directionSide);
        if (quantitySide != null) paramSides.put(quantity, quantitySide);
        if (filterSide != null) paramSides.put(itemMode() ? item : string, filterSide);
    }

    private boolean itemMode() { return !filterMode().id().equals(PsitweaksModeOptions.STRING.id()); }
    private void rebuildParams() { params.clear(); paramSides.clear(); initParams(); }

    @Override protected void initFilter() {
        if (itemMode()) addParam(item = new ParamSpellItemValue(PsitweaksSpellParams.ITEM, PsitweaksSpellParams.ITEM_COLOR, true, false));
        else super.initFilter();
    }

    @Override public void readFromNBT(CompoundTag tag) {
        mode = IdeaStorageTransferModes.filterOf(PsitweaksModeOptions.byId(tag.getString("psitweaksMode")).orElse(null));
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
        tag.putString("psitweaksMode", filterMode().serializedId());
    }

    /** False when the filter is unconnected or an empty String, i.e. when {@link #itemFilter} accepts everything. */
    protected boolean hasFilter(SpellContext context) throws SpellRuntimeException {
        if (itemMode()) return getParamValue(context, item) != null;
        String pattern = getParamValue(context, string);
        return pattern != null && !pattern.isEmpty();
    }

    @Override protected Predicate<ItemStack> itemFilter(SpellContext context) throws SpellRuntimeException {
        if (!itemMode()) return super.itemFilter(context);
        SpellItemValue value = getParamValue(context, item);
        if (value == null) return stack -> true;
        if (value.isEmpty()) throw new SpellRuntimeException("psitweaks.spellerror.nullitem");
        ItemStack template = value.snapshot();
        return filterMode().id().equals(PsitweaksModeOptions.ITEM_STRICT.id())
                ? stack -> ItemStack.isSameItemSameComponents(template, stack) : stack -> stack.is(template.getItem());
    }
}
