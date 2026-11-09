package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * The 4x4 region matrix of an axis-aligned cuboid centred on the position. Vector mode (the default, and the mode of
 * spells saved before the modes existed) takes the size per axis; Number mode takes one size for all three axes, so
 * a cube needs only one number. Both modes name the input Size, so changing the mode keeps the wire.
 */
public class PieceOperatorMatrixCuboidRegion extends PieceOperator implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";
    private static final double INTEGER_EPSILON = 1e-9;
    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.VECTOR, PsitweaksModeOptions.NUMBER);

    private PsitweaksModeOption mode = PsitweaksModeOptions.VECTOR;
    private SpellParam<Vector3> position;
    private SpellParam<Vector3> size;
    private SpellParam<Number> cubeSize;

    public PieceOperatorMatrixCuboidRegion(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        rebuildParams(null);
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return MODES;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    @Override
    public void setModeOption(PsitweaksModeOption modeOption) {
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
        mode = normalizeModeOption(PsitweaksModeOptions.byId(tag.getString(TAG_MODE)).orElse(null));
        rebuildParams(null);
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    public boolean isNumberMode() {
        return getModeOption().id().equals(PsitweaksModeOptions.NUMBER.id());
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        size = null;
        cubeSize = null;
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        if (isNumberMode()) {
            addParam(cubeSize = new ParamNumber(PsitweaksSpellParams.SIZE, SpellParam.GREEN, false, false));
        } else {
            addParam(size = new ParamVector(PsitweaksSpellParams.SIZE, SpellParam.GREEN, false, false));
        }

        if (savedSides != null) {
            for (SpellParam<?> param : params.values()) {
                SpellParam.Side side = savedSides.get(param.name);
                if (side != null) {
                    paramSides.put(param, side);
                }
            }
        }
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 pos = getNotNullParamValue(context, position);
        Vector3 sz;
        if (isNumberMode()) {
            double edge = getNotNullParamValue(context, cubeSize).doubleValue();
            sz = new Vector3(edge, edge, edge);
        } else {
            sz = getNotNullParamValue(context, size);
        }

        AxisRegion x = axisRegion(pos.x, sz.x);
        AxisRegion y = axisRegion(pos.y, sz.y);
        AxisRegion z = axisRegion(pos.z, sz.z);

        double[] values = new double[]{
                x.edge, 0.0, 0.0, x.start,
                0.0, y.edge, 0.0, y.start,
                0.0, 0.0, z.edge, z.start,
                0.0, 0.0, 0.0, 1.0
        };
        return MatrixOperations.checkedMatrix(4, 4, values);
    }

    @Override
    public Class<?> getEvaluationType() {
        return MatrixValue.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.matrix");
    }

    private static AxisRegion axisRegion(double center, double size) {
        if (size == 0.0 || !isWholeNumber(size)) {
            return new AxisRegion(center - size * 0.5, size);
        }

        int blocks = (int) Math.abs(Math.rint(size));
        if (size > 0) {
            int minOffset = -((blocks - 1) / 2);
            return new AxisRegion(center + minOffset - 0.5, blocks);
        }

        int maxOffset = (blocks - 1) / 2;
        return new AxisRegion(center + maxOffset + 0.5, -blocks);
    }

    private static boolean isWholeNumber(double value) {
        return Double.isFinite(value)
                && Math.abs(value) <= Integer.MAX_VALUE
                && Math.abs(value - Math.rint(value)) < INTEGER_EPSILON;
    }

    private record AxisRegion(double start, double edge) {
    }
}
