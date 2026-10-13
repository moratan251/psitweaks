package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import com.moratan251.psitweaksqol.api.PsitweaksValueKind;
import java.util.List;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * The 4x4 matrix of the caster's axis-rounded frame: the columns are right, up, forward and the origin. It maps a
 * local point (X = right, Y = up, Z = forward) to world space, so multiplying it with a region built around the
 * local origin turns that region to follow the caster.
 */
public class PieceOperatorMatrixCasterBasis extends PieceOperator implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";

    // Options of this piece only; they are not registered globally.
    public static final PsitweaksModeOption HORIZONTAL = option("caster_basis_2d", "2D", "basis_2d");
    public static final PsitweaksModeOption FULL = option("caster_basis_3d", "3D", "basis_3d");
    public static final PsitweaksModeOption FACE = option("caster_basis_face", "F", "basis_face");
    private static final List<PsitweaksModeOption> MODES = List.of(HORIZONTAL, FULL, FACE);

    private PsitweaksModeOption mode = HORIZONTAL;
    private SpellParam<Vector3> position;

    public PieceOperatorMatrixCasterBasis(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, true, false));
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return MODES;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    /** The parameters are the same in every mode, so only the mode changes. */
    @Override
    public void setModeOption(PsitweaksModeOption modeOption) {
        mode = normalizeModeOption(modeOption);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        String saved = tag.getString(TAG_MODE);
        mode = MODES.stream().filter(option -> option.serializedId().equals(saved)).findFirst().orElse(HORIZONTAL);
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 origin = getParamValue(context, position);
        CasterAxialBasis basis;
        if (getModeOption() == FACE) {
            CasterAxialBasis.TargetFace targetFace = CasterAxialBasis.targetFace(context);
            basis = targetFace.basis();
            if (origin == null) {
                origin = targetFace.blockPosition();
            }
        } else {
            basis = getModeOption() == FULL ? CasterAxialBasis.of3D(context.caster) : CasterAxialBasis.of2D(context.caster);
            if (origin == null) {
                origin = Vector3.fromBlockPos(context.caster.blockPosition());
            }
        }
        return basisMatrix(basis, origin);
    }

    static MatrixValue basisMatrix(CasterAxialBasis basis, Vector3 origin) throws SpellRuntimeException {
        Vec3i right = basis.right.getNormal();
        Vec3i up = basis.up.getNormal();
        Vec3i forward = basis.forward.getNormal();
        return MatrixOperations.checkedMatrix(4, 4, new double[]{
                right.getX(), up.getX(), forward.getX(), origin.x,
                right.getY(), up.getY(), forward.getY(), origin.y,
                right.getZ(), up.getZ(), forward.getZ(), origin.z,
                0.0, 0.0, 0.0, 1.0
        });
    }

    @Override
    public Class<?> getEvaluationType() {
        return MatrixValue.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.matrix");
    }

    private static PsitweaksModeOption option(String id, String button, String overlay) {
        return new PsitweaksModeOption(location(id), id, button, "psitweaks.mode." + id, "psitweaks.datatype.matrix",
                0, location("mode/" + overlay), PsitweaksValueKind.PLAIN);
    }

    private static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(PsitweaksModeOptions.BUILTIN_NAMESPACE, path);
    }
}
