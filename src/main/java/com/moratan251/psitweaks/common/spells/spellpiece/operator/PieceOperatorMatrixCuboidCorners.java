package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * The 4x4 region matrix of the axis-aligned cuboid with Vector A and Vector B as opposite corners, in the same form
 * as {@link PieceOperatorMatrixCuboidRegion}. The order of the corners does not matter.
 * <p>
 * Block mode (the default) turns each corner into the block that contains it, as Psi's tricks do, and takes in both
 * of those blocks, so corners at the same height make a layer one block thick. Vector mode uses the two points as
 * they are; a shared coordinate makes a flat box, which the pieces that read regions reject.
 */
public class PieceOperatorMatrixCuboidCorners extends PieceOperator implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";
    private static final double INTEGER_EPSILON = 1e-9;
    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.BLOCK, PsitweaksModeOptions.VECTOR);

    private PsitweaksModeOption mode = PsitweaksModeOptions.BLOCK;
    private SpellParam<Vector3> cornerA;
    private SpellParam<Vector3> cornerB;

    public PieceOperatorMatrixCuboidCorners(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(cornerA = new ParamVector(SpellParam.GENERIC_NAME_VECTOR1, SpellParam.GREEN, false, false));
        addParam(cornerB = new ParamVector(SpellParam.GENERIC_NAME_VECTOR2, SpellParam.GREEN, false, false));
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return MODES;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    /** The parameters are the same in both modes, so only the mode changes. */
    @Override
    public void setModeOption(PsitweaksModeOption modeOption) {
        mode = normalizeModeOption(modeOption);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        mode = normalizeModeOption(PsitweaksModeOptions.byId(tag.getString(TAG_MODE)).orElse(null));
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    public boolean isBlockMode() {
        return getModeOption().id().equals(PsitweaksModeOptions.BLOCK.id());
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 a = getNotNullParamValue(context, cornerA);
        Vector3 b = getNotNullParamValue(context, cornerB);
        boolean blocks = isBlockMode();

        AxisRegion x = axisRegion(a.x, b.x, blocks);
        AxisRegion y = axisRegion(a.y, b.y, blocks);
        AxisRegion z = axisRegion(a.z, b.z, blocks);

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

    private static AxisRegion axisRegion(double a, double b, boolean blocks) throws SpellRuntimeException {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            throw new SpellRuntimeException(MassBlockBreakHelper.ERROR_REGION_TOO_LARGE);
        }
        if (!blocks) {
            return new AxisRegion(Math.min(a, b), Math.abs(a - b));
        }

        // Blocks are the whole-number points of a region, so the box runs half a block past both corner blocks.
        double blockA = block(a);
        double blockB = block(b);
        return new AxisRegion(Math.min(blockA, blockB) - 0.5, Math.abs(blockA - blockB) + 1.0);
    }

    /** The block that contains the coordinate, letting a coordinate just under a whole number count as that number. */
    private static double block(double coordinate) throws SpellRuntimeException {
        double block = Math.floor(coordinate + INTEGER_EPSILON);
        if (block < Integer.MIN_VALUE || block > Integer.MAX_VALUE) {
            throw new SpellRuntimeException(MassBlockBreakHelper.ERROR_REGION_TOO_LARGE);
        }
        return block;
    }

    private record AxisRegion(double start, double edge) {
    }
}
