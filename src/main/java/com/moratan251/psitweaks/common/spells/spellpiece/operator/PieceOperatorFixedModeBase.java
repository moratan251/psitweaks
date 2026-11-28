package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.piece.PieceOperator;

/** An operator whose mode menu offers a fixed list of value types; the inputs are rebuilt on each mode change. */
public abstract class PieceOperatorFixedModeBase extends PieceOperator implements ModeConfigurableSpellPiece {
    private static final String TAG_MODE = "psitweaksMode";

    private PsitweaksModeOption mode;

    protected PieceOperatorFixedModeBase(Spell spell) {
        super(spell);
    }

    /** The modes in menu order. */
    protected abstract List<PsitweaksModeOption> modes();

    /** The mode of a newly placed piece. */
    protected PsitweaksModeOption defaultMode() {
        return modes().get(0);
    }

    /** Adds the inputs for the current mode; params are empty when this runs. */
    protected abstract void addModeParams();

    @Override
    public final void initParams() {
        rebuildParams(null);
    }

    @Override
    public final List<PsitweaksModeOption> getAvailableModeOptions() {
        return modes();
    }

    @Override
    public final PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode == null ? defaultMode() : mode);
    }

    @Override
    public final void setModeOption(PsitweaksModeOption modeOption) {
        PsitweaksModeOption nextMode = normalizeModeOption(modeOption);
        if (nextMode.id().equals(getModeOption().id())) {
            return;
        }

        Map<String, SpellParam.Side> savedSides = new LinkedHashMap<>();
        paramSides.forEach((param, side) -> savedSides.put(param.name, side));
        mode = nextMode;
        rebuildParams(savedSides);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        mode = PsitweaksModeOptions.byId(tag.getString(TAG_MODE)).orElse(null);
        mode = getModeOption();
        rebuildParams(null);
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    protected final boolean isMode(PsitweaksModeOption option) {
        return getModeOption().id().equals(option.id());
    }

    @SuppressWarnings("unchecked")
    protected static <T> SpellParam<T> typed(SpellParam<?> param) {
        return (SpellParam<T>) param;
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        addModeParams();
        if (savedSides == null) {
            return;
        }
        for (SpellParam<?> param : params.values()) {
            SpellParam.Side side = savedSides.get(param.name);
            if (side != null) {
                paramSides.put(param, side);
            }
        }
    }
}
