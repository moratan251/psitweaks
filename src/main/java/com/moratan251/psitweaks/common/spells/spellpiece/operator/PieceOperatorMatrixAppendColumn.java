package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import java.util.List;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Operator: Matrix Append Column. Each Value becomes a new column on the right; a Vector is a 3-row column. */
public class PieceOperatorMatrixAppendColumn extends PieceOperatorMatrixAppendBase {
    public PieceOperatorMatrixAppendColumn(Spell spell) {
        super(spell);
    }

    @Override
    protected MatrixValue append(MatrixValue base, List<double[]> lines) throws SpellRuntimeException {
        return MatrixOperations.appendColumns(base, lines);
    }
}
