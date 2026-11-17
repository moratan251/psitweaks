package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.param.SpellParamContextualValue;
import com.moratan251.psitweaks.common.spells.util.NbtPathHelper;
import com.moratan251.psitweaks.common.spells.util.NbtTargetReader;
import java.util.List;
import net.minecraft.nbt.Tag;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

/** The NBT selectors: a target (Entity or Contextual Value) and an NBT path, read through {@link NbtPathHelper}. */
abstract class PieceSelectorNbtBase extends PieceSelector {
    private SpellParam<Object> target;
    private SpellParam<String> path;

    protected PieceSelectorNbtBase(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new SpellParamContextualValue(SpellParam.GENERIC_NAME_TARGET,
                PsitweaksSpellParams.CONTEXTUAL_VALUE_COLOR, false));
        addParam(path = new ParamString(PsitweaksSpellParams.NBT_PATH, PsitweaksSpellParams.STRING_COLOR,
                pathOptional(), false));
        addAdditionalParams();
    }

    /** Whether the path input may be left unconnected, which reads the whole root. */
    protected boolean pathOptional() {
        return true;
    }

    protected void addAdditionalParams() {
    }

    /** Every tag the path reaches in the target's NBT; empty when the target has no NBT or nothing matches. */
    protected final List<Tag> find(SpellContext context) throws SpellRuntimeException {
        Object targetValue = getNotNullParamValue(context, target);
        String pathValue = getParamValue(context, path);
        return NbtPathHelper.find(NbtTargetReader.read(context, targetValue).orElse(null), pathValue);
    }
}
