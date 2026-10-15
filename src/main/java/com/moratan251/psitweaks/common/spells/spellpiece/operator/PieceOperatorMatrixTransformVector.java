package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import com.moratan251.psitweaksqol.api.PsitweaksValueKind;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * Transforms a vector, or every vector of a Vector List in order, with a 3x3 or 4x4 matrix. Both modes name the
 * input Target, so spells saved before the List mode existed load as the Vector mode with their wiring intact.
 */
public class PieceOperatorMatrixTransformVector extends PieceOperator implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";

    // Options of this piece only; they are not registered globally.
    public static final PsitweaksModeOption VECTOR = option("matrix_transform_vector", "V",
            "psi.datatype.vector3", "mode/vector");
    public static final PsitweaksModeOption VECTOR_LIST = option("matrix_transform_vector_list", "[V]",
            "psitweaks.datatype.vector_list", "mode/list");
    private static final List<PsitweaksModeOption> MODES = List.of(VECTOR, VECTOR_LIST);

    private PsitweaksModeOption mode = VECTOR;
    private SpellParam<MatrixValue> matrix;
    private SpellParam<Vector3> vector;
    private SpellParam<VectorListWrapper> vectors;

    public PieceOperatorMatrixTransformVector(Spell spell) {
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
        if (nextMode == getModeOption()) {
            return;
        }

        Map<String, SpellParam.Side> savedSides = new LinkedHashMap<>();
        paramSides.forEach((param, side) -> savedSides.put(param.name, side));
        mode = nextMode;
        rebuildParams(savedSides);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        String saved = tag.getString(TAG_MODE);
        mode = MODES.stream().filter(option -> option.serializedId().equals(saved)).findFirst().orElse(VECTOR);
        rebuildParams(null);
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    public boolean isListMode() {
        return getModeOption() == VECTOR_LIST;
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        addParam(matrix = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        vector = null;
        vectors = null;
        if (isListMode()) {
            addParam(vectors = new ParamVectorListWrapper(SpellParam.GENERIC_NAME_TARGET,
                    PsitweaksSpellParams.VECTOR_LIST_COLOR, false, false));
        } else {
            addParam(vector = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.GREEN, false, false));
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
        MatrixValue mat = getNotNullParamValue(context, matrix);
        if (isListMode()) {
            VectorListWrapper source = getNotNullParamValue(context, vectors);
            List<Vector3> result = new ArrayList<>(source.size());
            for (Vector3 element : source) {
                result.add(MatrixOperations.transformVector(mat, element));
            }
            return VectorListWrapper.make(result);
        }

        Vector3 vec = getNotNullParamValue(context, vector);
        return MatrixOperations.transformVector(mat, vec);
    }

    @Override
    public Class<?> getEvaluationType() {
        return isListMode() ? VectorListWrapper.class : Vector3.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(getModeOption().elementTranslationKey());
    }

    private static PsitweaksModeOption option(String id, String button, String typeKey, String overlay) {
        return new PsitweaksModeOption(location(id), id, button, typeKey, typeKey, 0, location(overlay),
                PsitweaksValueKind.PLAIN);
    }

    private static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(PsitweaksModeOptions.BUILTIN_NAMESPACE, path);
    }
}
