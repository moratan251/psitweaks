package com.moratan251.psitweaks.api.value;

import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * Immutable matrix value used by PsiTweaks spell pieces.
 *
 * <p>Matrices are stored in row-major order. Rows and columns are limited to
 * 1-4. NaN and infinite values are rejected.</p>
 */
public final class MatrixValue {
    public static final int MAX_SIZE = 4;

    private final int rows;
    private final int cols;
    private final double[] values;

    public MatrixValue(int rows, int cols, double[] values) {
        this.rows = validateDimension(rows);
        this.cols = validateDimension(cols);
        this.values = validateValues(values, this.rows * this.cols);
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public double get(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Matrix index out of bounds: (" + row + ", " + col + ")");
        }
        return values[row * cols + col];
    }

    public double[] valuesCopy() {
        return Arrays.copyOf(values, values.length);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("rows", rows);
        tag.putInt("cols", cols);
        ListTag list = new ListTag();
        for (double value : values) {
            list.add(DoubleTag.valueOf(value));
        }
        tag.put("values", list);
        return tag;
    }

    public static Optional<MatrixValue> deserialize(CompoundTag tag) {
        if (tag == null
                || !tag.contains("rows", Tag.TAG_INT)
                || !tag.contains("cols", Tag.TAG_INT)
                || !tag.contains("values", Tag.TAG_LIST)) {
            return Optional.empty();
        }

        int rows = tag.getInt("rows");
        int cols = tag.getInt("cols");
        ListTag list = tag.getList("values", Tag.TAG_DOUBLE);
        if (list.size() != rows * cols) {
            return Optional.empty();
        }

        double[] values = new double[list.size()];
        for (int i = 0; i < values.length; i++) {
            double value = list.getDouble(i);
            if (!Double.isFinite(value)) {
                return Optional.empty();
            }
            values[i] = value;
        }

        try {
            return Optional.of(new MatrixValue(rows, cols, values));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public static Optional<MatrixValue> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        String normalized = input.replaceAll("\\s+", "");
        if (normalized.startsWith("Matrix")) {
            normalized = normalized.substring("Matrix".length());
        }

        if (!normalized.startsWith("[") || !normalized.endsWith("]")) {
            return Optional.empty();
        }

        List<String> rowStrings = splitTopLevel(normalized.substring(1, normalized.length() - 1));
        if (rowStrings.isEmpty() || rowStrings.size() > MAX_SIZE) {
            return Optional.empty();
        }

        int rows = rowStrings.size();
        int cols = -1;
        List<double[]> rowValues = new ArrayList<>(rows);
        for (String rowString : rowStrings) {
            if (!rowString.startsWith("[") || !rowString.endsWith("]")) {
                return Optional.empty();
            }

            List<String> numberStrings = splitTopLevel(rowString.substring(1, rowString.length() - 1));
            if (numberStrings.isEmpty() || numberStrings.size() > MAX_SIZE) {
                return Optional.empty();
            }

            if (cols == -1) {
                cols = numberStrings.size();
            } else if (cols != numberStrings.size()) {
                return Optional.empty();
            }

            double[] row = new double[numberStrings.size()];
            for (int i = 0; i < numberStrings.size(); i++) {
                OptionalDouble parsed = StringSpellHelper.parseFiniteDouble(numberStrings.get(i));
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                row[i] = parsed.getAsDouble();
            }
            rowValues.add(row);
        }

        double[] values = new double[rows * cols];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(rowValues.get(r), 0, values, r * cols, cols);
        }
        return Optional.of(new MatrixValue(rows, cols, values));
    }

    public String toSpellString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Matrix[");
        for (int r = 0; r < rows; r++) {
            if (r > 0) {
                builder.append(",");
            }
            builder.append("[");
            for (int c = 0; c < cols; c++) {
                if (c > 0) {
                    builder.append(",");
                }
                builder.append(values[r * cols + c]);
            }
            builder.append("]");
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MatrixValue other)) {
            return false;
        }
        return rows == other.rows && cols == other.cols && Arrays.equals(values, other.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows, cols, Arrays.hashCode(values));
    }

    @Override
    public String toString() {
        return toSpellString();
    }

    private static int validateDimension(int dimension) {
        if (dimension < 1 || dimension > MAX_SIZE) {
            throw new IllegalArgumentException("Matrix dimension must be between 1 and " + MAX_SIZE + ": " + dimension);
        }
        return dimension;
    }

    private static double[] validateValues(double[] values, int expectedCount) {
        if (values == null) {
            throw new IllegalArgumentException("Matrix values must not be null");
        }
        if (values.length != expectedCount) {
            throw new IllegalArgumentException("Expected " + expectedCount + " values but got " + values.length);
        }
        double[] copy = Arrays.copyOf(values, values.length);
        for (double value : copy) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Matrix values must be finite");
            }
        }
        return copy;
    }

    private static List<String> splitTopLevel(String input) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
            } else if (c == ',' && depth == 0) {
                result.add(input.substring(start, i));
                start = i + 1;
            }
        }
        result.add(input.substring(start));
        return result;
    }
}
