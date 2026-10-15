package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * A region matrix (3x4 or 4x4): columns 0-2 are the edge vectors and column 3 is the start point.
 * A point is inside when its coordinates along the three edges are all within [0, 1], boundary included.
 */
public final class RegionMatrix {
    private static final double EPSILON = 1e-12;

    private final Vector3 start;
    private final Vector3[] edges;
    private final MatrixValue inverse;

    private RegionMatrix(Vector3 start, Vector3[] edges, MatrixValue inverse) {
        this.start = start;
        this.edges = edges;
        this.inverse = inverse;
    }

    public static RegionMatrix parse(MatrixValue matrix) throws SpellRuntimeException {
        if ((matrix.rows() != 3 && matrix.rows() != 4) || matrix.cols() != 4) {
            throw new SpellRuntimeException(MassBlockBreakHelper.ERROR_INVALID_MATRIX);
        }

        Vector3[] edges = {column(matrix, 0), column(matrix, 1), column(matrix, 2)};
        MatrixValue edgeMatrix = MatrixOperations.checkedMatrix(3, 3, new double[]{
                edges[0].x, edges[1].x, edges[2].x,
                edges[0].y, edges[1].y, edges[2].y,
                edges[0].z, edges[1].z, edges[2].z
        });

        MatrixValue inverse;
        try {
            inverse = MatrixOperations.inverse(edgeMatrix);
        } catch (SpellRuntimeException e) {
            throw new SpellRuntimeException(MassBlockBreakHelper.ERROR_DEGENERATE_EDGES);
        }
        return new RegionMatrix(column(matrix, 3), edges, inverse);
    }

    public boolean contains(Vector3 position) {
        double dx = position.x - start.x;
        double dy = position.y - start.y;
        double dz = position.z - start.z;
        return withinEdge(0, dx, dy, dz) && withinEdge(1, dx, dy, dz) && withinEdge(2, dx, dy, dz);
    }

    /** The corner reached by adding the edges whose flag is set to the start point. */
    public Vector3 corner(boolean edge0, boolean edge1, boolean edge2) {
        Vector3 corner = start.copy();
        if (edge0) {
            corner.add(edges[0]);
        }
        if (edge1) {
            corner.add(edges[1]);
        }
        if (edge2) {
            corner.add(edges[2]);
        }
        return corner;
    }

    private boolean withinEdge(int row, double dx, double dy, double dz) {
        double coordinate = inverse.get(row, 0) * dx + inverse.get(row, 1) * dy + inverse.get(row, 2) * dz;
        return coordinate >= -EPSILON && coordinate <= 1.0 + EPSILON;
    }

    private static Vector3 column(MatrixValue matrix, int col) {
        return new Vector3(matrix.get(0, col), matrix.get(1, col), matrix.get(2, col));
    }
}
