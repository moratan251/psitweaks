package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.param.ParamMatrixLineSource;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * Matrix Append Column / Row: adds Values 1 to 3 to the optional Matrix, in order. A Vector or a Number List is one
 * line and a Vector List gives one line per Vector.
 */
public abstract class PieceOperatorMatrixAppendBase extends PieceOperator {
    private SpellParam<MatrixValue> matrix;
    private SpellParam<Object> value1;
    private SpellParam<Object> value2;
    private SpellParam<Object> value3;

    protected PieceOperatorMatrixAppendBase(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(matrix = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, true, false));
        addParam(value1 = new ParamMatrixLineSource(PsitweaksSpellParams.VALUE1, PsitweaksSpellParams.NUMBER_LIST_COLOR, false));
        addParam(value2 = new ParamMatrixLineSource(PsitweaksSpellParams.VALUE2, PsitweaksSpellParams.NUMBER_LIST_COLOR, true));
        addParam(value3 = new ParamMatrixLineSource(PsitweaksSpellParams.VALUE3, PsitweaksSpellParams.NUMBER_LIST_COLOR, true));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        MatrixValue base = getParamValue(context, matrix);
        List<double[]> lines = new ArrayList<>();
        addLines(lines, getNotNullParamValue(context, value1));
        addLines(lines, getParamValue(context, value2));
        addLines(lines, getParamValue(context, value3));
        return append(base, lines);
    }

    /** {@code base} is null when the Matrix input is not connected. */
    protected abstract MatrixValue append(MatrixValue base, List<double[]> lines) throws SpellRuntimeException;

    private static void addLines(List<double[]> lines, Object value) {
        if (value instanceof Vector3 vector) {
            lines.add(new double[]{vector.x, vector.y, vector.z});
        } else if (value instanceof NumberListWrapper numbers) {
            double[] line = new double[numbers.size()];
            for (int i = 0; i < line.length; i++) {
                line[i] = numbers.get(i);
            }
            lines.add(line);
        } else if (value instanceof VectorListWrapper vectors) {
            for (Vector3 vector : vectors) {
                lines.add(new double[]{vector.x, vector.y, vector.z});
            }
        }
    }

    @Override
    public Class<?> getEvaluationType() {
        return MatrixValue.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.matrix");
    }
}
