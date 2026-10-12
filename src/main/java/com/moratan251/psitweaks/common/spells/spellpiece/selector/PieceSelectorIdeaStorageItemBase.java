package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Item queries take either an ID String or an Item value; the sole input keeps its wire when the mode changes. */
public abstract class PieceSelectorIdeaStorageItemBase extends PieceSelectorIdeaStorageAmountBase
        implements ModeConfigurableSpellPiece {
    private static final String TAG_MODE = "psitweaksMode";
    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.STRING, PsitweaksModeOptions.ITEM);
    private PsitweaksModeOption mode = PsitweaksModeOptions.STRING;
    private SpellParam<SpellItemValue> item;

    protected PieceSelectorIdeaStorageItemBase(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        if (itemMode()) {
            addParam(item = new ParamSpellItemValue(PsitweaksSpellParams.ITEM,
                    PsitweaksSpellParams.ITEM_COLOR, false, false));
        } else {
            super.initParams();
        }
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return MODES;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    @Override
    public void setModeOption(PsitweaksModeOption option) {
        PsitweaksModeOption next = normalizeModeOption(option);
        if (next.id().equals(getModeOption().id())) {
            return;
        }
        // The sole input keeps its connection even when its name/type changes.
        SpellParam.Side side = paramSides.get(params.values().iterator().next());
        mode = next;
        rebuildParams();
        if (side != null) {
            paramSides.put(params.values().iterator().next(), side);
        }
    }

    private boolean itemMode() {
        return getModeOption().id().equals(PsitweaksModeOptions.ITEM.id());
    }

    private void rebuildParams() {
        params.clear();
        paramSides.clear();
        initParams();
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        mode = normalizeModeOption(PsitweaksModeOptions.byId(tag.getString(TAG_MODE)).orElse(null));
        rebuildParams();
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    @Override
    protected StorageQuery prepareQuery(SpellContext context) throws SpellRuntimeException {
        if (!itemMode()) {
            return super.prepareQuery(context);
        }
        SpellItemValue value = getParamValue(context, item);
        if (value == null || value.isEmpty()) {
            return null;
        }
        ItemStack stack = value.copyStack();
        return storage -> itemValue(storage, stack);
    }

    /** The value for an Item input, which still carries its data components. */
    protected abstract double itemValue(PlayerIdeaStorage storage, ItemStack stack);
}
