package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamNumberListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Folds a Number List into a Number, or a Vector List into a Vector, component by component. */
public abstract class PieceOperatorListAggregateBase extends PieceOperatorFixedModeBase {
    public static final String ERROR_EMPTY_LIST = "psitweaks.spellerror.list_empty";

    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.NUMBER, PsitweaksModeOptions.VECTOR);

    private SpellParam<?> list;

    protected PieceOperatorListAggregateBase(Spell spell) {
        super(spell);
    }

    @Override
    protected final List<PsitweaksModeOption> modes() {
        return MODES;
    }

    @Override
    protected final void addModeParams() {
        if (isMode(PsitweaksModeOptions.VECTOR)) {
            addParam(list = new ParamVectorListWrapper(SpellParam.GENERIC_NAME_LIST,
                    PsitweaksSpellParams.VECTOR_LIST_COLOR, false, false));
        } else {
            addParam(list = new ParamNumberListWrapper(SpellParam.GENERIC_NAME_LIST,
                    PsitweaksSpellParams.NUMBER_LIST_COLOR, false, false));
        }
    }

    /** The result for the sum of {@code count} elements. */
    protected abstract double finish(double sum, int count) throws SpellRuntimeException;

    @Override
    public final Object execute(SpellContext context) throws SpellRuntimeException {
        if (isMode(PsitweaksModeOptions.VECTOR)) {
            VectorListWrapper vectors = getNotNullParamValue(context, typed(list));
            double x = 0;
            double y = 0;
            double z = 0;
            for (Vector3 vector : vectors) {
                x += vector.x;
                y += vector.y;
                z += vector.z;
            }
            int count = vectors.size();
            return new Vector3(checked(finish(x, count)), checked(finish(y, count)), checked(finish(z, count)));
        }
        NumberListWrapper numbers = getNotNullParamValue(context, typed(list));
        double sum = 0;
        for (double value : numbers) {
            sum += value;
        }
        return checked(finish(sum, numbers.size()));
    }

    private static double checked(double value) throws SpellRuntimeException {
        if (!Double.isFinite(value)) {
            throw new SpellRuntimeException(SpellRuntimeException.NAN);
        }
        return value;
    }

    @Override
    public final Class<?> getEvaluationType() {
        return isMode(PsitweaksModeOptions.VECTOR) ? Vector3.class : Double.class;
    }

    @Override
    public final Component getEvaluationTypeString() {
        return Component.translatable(getModeOption().elementTranslationKey());
    }
}
