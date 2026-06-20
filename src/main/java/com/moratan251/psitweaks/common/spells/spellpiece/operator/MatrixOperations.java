package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import java.util.ArrayList;
import java.util.List;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellRuntimeException;

public final class MatrixOperations {
    public static final String ERROR_INCOMPATIBLE_SIZES = "psitweaks.spellerror.matrix_incompatible_sizes";
    public static final String ERROR_NOT_SQUARE = "psitweaks.spellerror.matrix_not_square";
    public static final String ERROR_SINGULAR = "psitweaks.spellerror.matrix_singular";
    public static final String ERROR_OUT_OF_BOUNDS = "psitweaks.spellerror.matrix_out_of_bounds";
    public static final String ERROR_TOO_LARGE = "psitweaks.spellerror.matrix_too_large";
    public static final String ERROR_INVALID_DIMENSION = "psitweaks.spellerror.matrix_invalid_dimension";
    public static final String ERROR_INVALID_TRANSFORM = "psitweaks.spellerror.matrix_invalid_transform";
    public static final String ERROR_ZERO_W = "psitweaks.spellerror.matrix_zero_w";

    private static final double EPSILON = 1e-12;

    private MatrixOperations() {
    }

    public static NumberListWrapper asNumberList(Object value) {
        if (value instanceof Vector3 vector) {
            return NumberListWrapper.make(List.of(vector.x, vector.y, vector.z));
        }
        return (NumberListWrapper) value;
    }

    public static MatrixValue add(MatrixValue a, MatrixValue b) throws SpellRuntimeException {
        return add(a, b, null);
    }

    public static MatrixValue add(MatrixValue a, MatrixValue b, MatrixValue c) throws SpellRuntimeException {
        requireSameSize(a, b);
        if (c != null) {
            requireSameSize(a, c);
        }
        int rows = a.rows();
        int cols = a.cols();
        double[] values = new double[rows * cols];
        for (int r = 0; r < rows; r++) {
            for (int col = 0; col < cols; col++) {
                int index = r * cols + col;
                values[index] = a.get(r, col) + b.get(r, col);
                if (c != null) {
                    values[index] += c.get(r, col);
                }
            }
        }
        return new MatrixValue(rows, cols, values);
    }

    public static MatrixValue subtract(MatrixValue a, MatrixValue b) throws SpellRuntimeException {
        return subtract(a, b, null);
    }

    public static MatrixValue subtract(MatrixValue a, MatrixValue b, MatrixValue c) throws SpellRuntimeException {
        requireSameSize(a, b);
        if (c != null) {
            requireSameSize(a, c);
        }
        int rows = a.rows();
        int cols = a.cols();
        double[] values = new double[rows * cols];
        for (int r = 0; r < rows; r++) {
            for (int col = 0; col < cols; col++) {
                int index = r * cols + col;
                values[index] = a.get(r, col) - b.get(r, col);
                if (c != null) {
                    values[index] -= c.get(r, col);
                }
            }
        }
        return new MatrixValue(rows, cols, values);
    }

    public static MatrixValue multiply(MatrixValue a, MatrixValue b) throws SpellRuntimeException {
        if (a.cols() != b.rows()) {
            throw new SpellRuntimeException(ERROR_INCOMPATIBLE_SIZES);
        }
        int rows = a.rows();
        int cols = b.cols();
        int inner = a.cols();
        double[] values = new double[rows * cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double sum = 0;
                for (int k = 0; k < inner; k++) {
                    sum += a.get(r, k) * b.get(k, c);
                }
                values[r * cols + c] = sum;
            }
        }
        return new MatrixValue(rows, cols, values);
    }

    public static MatrixValue multiply(MatrixValue a, MatrixValue b, MatrixValue c) throws SpellRuntimeException {
        MatrixValue result = multiply(a, b);
        if (c != null) {
            result = multiply(result, c);
        }
        return result;
    }

    public static MatrixValue scalarMultiply(double scalar, MatrixValue matrix) {
        int rows = matrix.rows();
        int cols = matrix.cols();
        double[] values = matrix.valuesCopy();
        for (int i = 0; i < values.length; i++) {
            values[i] *= scalar;
        }
        return new MatrixValue(rows, cols, values);
    }

    public static MatrixValue transpose(MatrixValue matrix) {
        int rows = matrix.rows();
        int cols = matrix.cols();
        double[] values = new double[rows * cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                values[c * rows + r] = matrix.get(r, c);
            }
        }
        return new MatrixValue(cols, rows, values);
    }

    public static double determinant(MatrixValue matrix) throws SpellRuntimeException {
        requireSquare(matrix);
        int n = matrix.rows();
        if (n == 1) {
            return matrix.get(0, 0);
        }
        if (n == 2) {
            return matrix.get(0, 0) * matrix.get(1, 1) - matrix.get(0, 1) * matrix.get(1, 0);
        }
        double det = 0;
        for (int c = 0; c < n; c++) {
            double sign = (c % 2 == 0) ? 1 : -1;
            det += sign * matrix.get(0, c) * determinant(subMatrix(matrix, 0, c));
        }
        return det;
    }

    public static MatrixValue inverse(MatrixValue matrix) throws SpellRuntimeException {
        requireSquare(matrix);
        int n = matrix.rows();

        double[][] augmented = new double[n][2 * n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                augmented[r][c] = matrix.get(r, c);
            }
            augmented[r][n + r] = 1;
        }

        for (int r = 0; r < n; r++) {
            int pivot = r;
            double max = Math.abs(augmented[r][r]);
            for (int row = r + 1; row < n; row++) {
                double value = Math.abs(augmented[row][r]);
                if (value > max) {
                    max = value;
                    pivot = row;
                }
            }
            if (max < EPSILON) {
                throw new SpellRuntimeException(ERROR_SINGULAR);
            }
            if (pivot != r) {
                double[] temp = augmented[r];
                augmented[r] = augmented[pivot];
                augmented[pivot] = temp;
            }

            double pivotValue = augmented[r][r];
            for (int c = 0; c < 2 * n; c++) {
                augmented[r][c] /= pivotValue;
            }

            for (int row = 0; row < n; row++) {
                if (row == r) {
                    continue;
                }
                double factor = augmented[row][r];
                if (Math.abs(factor) > EPSILON) {
                    for (int c = 0; c < 2 * n; c++) {
                        augmented[row][c] -= factor * augmented[r][c];
                    }
                }
            }
        }

        double[] values = new double[n * n];
        for (int r = 0; r < n; r++) {
            System.arraycopy(augmented[r], n, values, r * n, n);
        }
        return new MatrixValue(n, n, values);
    }

    public static NumberListWrapper extractRow(MatrixValue matrix, int row) throws SpellRuntimeException {
        if (row < 0 || row >= matrix.rows()) {
            throw new SpellRuntimeException(ERROR_OUT_OF_BOUNDS);
        }
        List<Double> values = new ArrayList<>(matrix.cols());
        for (int c = 0; c < matrix.cols(); c++) {
            values.add(matrix.get(row, c));
        }
        return NumberListWrapper.make(values);
    }

    public static NumberListWrapper extractColumn(MatrixValue matrix, int col) throws SpellRuntimeException {
        if (col < 0 || col >= matrix.cols()) {
            throw new SpellRuntimeException(ERROR_OUT_OF_BOUNDS);
        }
        List<Double> values = new ArrayList<>(matrix.rows());
        for (int r = 0; r < matrix.rows(); r++) {
            values.add(matrix.get(r, col));
        }
        return NumberListWrapper.make(values);
    }

    public static double element(MatrixValue matrix, int row, int col) throws SpellRuntimeException {
        if (row < 0 || row >= matrix.rows() || col < 0 || col >= matrix.cols()) {
            throw new SpellRuntimeException(ERROR_OUT_OF_BOUNDS);
        }
        return matrix.get(row, col);
    }

    public static NumberListWrapper multiplyVector(MatrixValue matrix, NumberListWrapper vector) throws SpellRuntimeException {
        if (matrix.cols() != vector.size()) {
            throw new SpellRuntimeException(ERROR_INCOMPATIBLE_SIZES);
        }
        List<Double> values = new ArrayList<>(matrix.rows());
        for (int r = 0; r < matrix.rows(); r++) {
            double sum = 0;
            for (int c = 0; c < matrix.cols(); c++) {
                sum += matrix.get(r, c) * vector.get(c);
            }
            values.add(sum);
        }
        return NumberListWrapper.make(values);
    }

    public static MatrixValue columnFromList(NumberListWrapper list) throws SpellRuntimeException {
        if (list.size() < 1 || list.size() > MatrixValue.MAX_SIZE) {
            throw new SpellRuntimeException(ERROR_TOO_LARGE);
        }
        double[] values = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = list.get(i);
        }
        return new MatrixValue(list.size(), 1, values);
    }

    public static NumberListWrapper flatten(MatrixValue matrix) {
        List<Double> values = new ArrayList<>(matrix.rows() * matrix.cols());
        for (int r = 0; r < matrix.rows(); r++) {
            for (int c = 0; c < matrix.cols(); c++) {
                values.add(matrix.get(r, c));
            }
        }
        return NumberListWrapper.make(values);
    }

    public static MatrixValue identity(int size) throws SpellRuntimeException {
        validateDimension(size);
        double[] values = new double[size * size];
        for (int i = 0; i < size; i++) {
            values[i * size + i] = 1;
        }
        return new MatrixValue(size, size, values);
    }

    public static MatrixValue zero(int rows, int cols) throws SpellRuntimeException {
        validateDimension(rows);
        validateDimension(cols);
        return new MatrixValue(rows, cols, new double[rows * cols]);
    }

    public static MatrixValue diagonal(NumberListWrapper values) throws SpellRuntimeException {
        if (values.size() < 1 || values.size() > MatrixValue.MAX_SIZE) {
            throw new SpellRuntimeException(ERROR_TOO_LARGE);
        }
        int n = values.size();
        double[] result = new double[n * n];
        for (int i = 0; i < n; i++) {
            result[i * n + i] = values.get(i);
        }
        return new MatrixValue(n, n, result);
    }

    public static MatrixValue replaceColumn(MatrixValue matrix, int col, NumberListWrapper values) throws SpellRuntimeException {
        if (col < 0) {
            throw new SpellRuntimeException(ERROR_OUT_OF_BOUNDS);
        }
        if (values.size() > MatrixValue.MAX_SIZE) {
            throw new SpellRuntimeException(ERROR_TOO_LARGE);
        }
        int newRows = Math.max(matrix.rows(), values.size());
        int newCols = Math.max(matrix.cols(), col + 1);
        validateDimension(newRows);
        validateDimension(newCols);

        double[] result = new double[newRows * newCols];
        for (int r = 0; r < matrix.rows(); r++) {
            System.arraycopy(matrix.valuesCopy(), r * matrix.cols(), result, r * newCols, matrix.cols());
        }
        for (int r = 0; r < newRows; r++) {
            result[r * newCols + col] = 0;
        }
        for (int r = 0; r < values.size(); r++) {
            result[r * newCols + col] = values.get(r);
        }
        return new MatrixValue(newRows, newCols, result);
    }

    public static MatrixValue replaceRow(MatrixValue matrix, int row, NumberListWrapper values) throws SpellRuntimeException {
        if (row < 0) {
            throw new SpellRuntimeException(ERROR_OUT_OF_BOUNDS);
        }
        if (values.size() > MatrixValue.MAX_SIZE) {
            throw new SpellRuntimeException(ERROR_TOO_LARGE);
        }
        int newRows = Math.max(matrix.rows(), row + 1);
        int newCols = Math.max(matrix.cols(), values.size());
        validateDimension(newRows);
        validateDimension(newCols);

        double[] result = new double[newRows * newCols];
        for (int r = 0; r < matrix.rows(); r++) {
            for (int c = 0; c < matrix.cols(); c++) {
                result[r * newCols + c] = matrix.get(r, c);
            }
        }
        for (int c = 0; c < newCols; c++) {
            result[row * newCols + c] = 0;
        }
        for (int c = 0; c < values.size(); c++) {
            result[row * newCols + c] = values.get(c);
        }
        return new MatrixValue(newRows, newCols, result);
    }

    public static Vector3 transformPosition(MatrixValue matrix, Vector3 vector) throws SpellRuntimeException {
        requireTransformSize(matrix);
        if (matrix.rows() == 3) {
            double[] result = multiplyColumnVector(matrix, new double[]{vector.x, vector.y, vector.z});
            return new Vector3(result[0], result[1], result[2]);
        }
        double[] result = multiplyColumnVector(matrix, new double[]{vector.x, vector.y, vector.z, 1});
        double w = result[3];
        if (Math.abs(w) < EPSILON) {
            throw new SpellRuntimeException(ERROR_ZERO_W);
        }
        return new Vector3(result[0] / w, result[1] / w, result[2] / w);
    }

    public static Vector3 transformDirection(MatrixValue matrix, Vector3 vector) throws SpellRuntimeException {
        requireTransformSize(matrix);
        if (matrix.rows() == 3) {
            double[] result = multiplyColumnVector(matrix, new double[]{vector.x, vector.y, vector.z});
            return new Vector3(result[0], result[1], result[2]);
        }
        double[] result = multiplyColumnVector(matrix, new double[]{vector.x, vector.y, vector.z, 0});
        return new Vector3(result[0], result[1], result[2]);
    }

    private static double[] multiplyColumnVector(MatrixValue matrix, double[] vector) {
        int rows = matrix.rows();
        int cols = matrix.cols();
        double[] result = new double[rows];
        for (int r = 0; r < rows; r++) {
            double sum = 0;
            for (int c = 0; c < cols; c++) {
                sum += matrix.get(r, c) * vector[c];
            }
            result[r] = sum;
        }
        return result;
    }

    private static MatrixValue subMatrix(MatrixValue matrix, int removeRow, int removeCol) {
        int n = matrix.rows();
        double[] values = new double[(n - 1) * (n - 1)];
        int index = 0;
        for (int r = 0; r < n; r++) {
            if (r == removeRow) {
                continue;
            }
            for (int c = 0; c < n; c++) {
                if (c == removeCol) {
                    continue;
                }
                values[index++] = matrix.get(r, c);
            }
        }
        return new MatrixValue(n - 1, n - 1, values);
    }

    private static void requireSameSize(MatrixValue a, MatrixValue b) throws SpellRuntimeException {
        if (a.rows() != b.rows() || a.cols() != b.cols()) {
            throw new SpellRuntimeException(ERROR_INCOMPATIBLE_SIZES);
        }
    }

    private static void requireSquare(MatrixValue matrix) throws SpellRuntimeException {
        if (matrix.rows() != matrix.cols()) {
            throw new SpellRuntimeException(ERROR_NOT_SQUARE);
        }
    }

    private static void requireTransformSize(MatrixValue matrix) throws SpellRuntimeException {
        if (matrix.rows() != matrix.cols() || (matrix.rows() != 3 && matrix.rows() != 4)) {
            throw new SpellRuntimeException(ERROR_INVALID_TRANSFORM);
        }
    }

    private static void validateDimension(int dimension) throws SpellRuntimeException {
        if (dimension < 1 || dimension > MatrixValue.MAX_SIZE) {
            throw new SpellRuntimeException(ERROR_INVALID_DIMENSION);
        }
    }
}
