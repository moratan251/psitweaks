package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import java.util.List;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Operator: Matrix Append Row. Each Value becomes a new row at the bottom; a Vector is a 3-column row. */
public class PieceOperatorMatrixAppendRow extends PieceOperatorMatrixAppendBase {
    public PieceOperatorMatrixAppendRow(Spell spell) {
        super(spell);
    }

    @Override
    protected MatrixValue append(MatrixValue base, List<double[]> lines) throws SpellRuntimeException {
        return MatrixOperations.appendRows(base, lines);
    }
}
