package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.NbtPathHelper;
import com.moratan251.psitweaks.common.spells.util.WildcardStringMatcher;
import net.minecraft.nbt.Tag;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Filters by the text of the NBT values at the path. Each value is turned into text as in Selector: NBT Value's
 * String mode and compared with the pattern as a whole (* ? [abc], case kept). One matching value is enough; an
 * element where the path reaches nothing never matches.
 */
public abstract class PieceOperatorListNbtFilterBase extends PieceOperatorListNbtBase {
    private SpellParam<String> pattern;

    protected PieceOperatorListNbtFilterBase(Spell spell) {
        super(spell);
    }

    @Override
    protected void addFilterParams() {
        addParam(pattern = new ParamString(PsitweaksSpellParams.STRING, SpellParam.YELLOW, false, false));
    }

    @Override
    protected ElementTest prepare(SpellContext context) throws SpellRuntimeException {
        WildcardStringMatcher matcher = WildcardStringMatcher.compile(getNotNullParamValue(context, pattern));
        return found -> {
            boolean matches = false;
            for (Tag tag : found) {
                if (matcher.matches(NbtPathHelper.text(tag))) {
                    matches = true;
                    break;
                }
            }
            return shouldKeep(matches);
        };
    }

    protected abstract boolean shouldKeep(boolean matches);
}
