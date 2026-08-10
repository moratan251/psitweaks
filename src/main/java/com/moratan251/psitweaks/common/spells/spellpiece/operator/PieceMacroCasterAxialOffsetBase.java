package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import com.moratan251.psitweaks.api.PsitweaksModeOptions;
import com.moratan251.psitweaks.client.spells.ModeOverlayRenderer;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

abstract class PieceMacroCasterAxialOffsetBase extends PieceOperator implements PsitweaksModeConfigurable {

    private static final String TAG_MODE = "psitweaksMode";
    private static final List<PsitweaksModeOption> MODES = List.of(PsitweaksModeOptions.NUMBER, PsitweaksModeOptions.VECTOR);

    private final boolean includePitch;
    private PsitweaksModeOption mode = PsitweaksModeOptions.NUMBER;

    private SpellParam<Vector3> position;
    private SpellParam<Number> leftRight;
    private SpellParam<Number> forwardBackward;
    private SpellParam<Number> upDown;
    private SpellParam<Vector3> offset;

    protected PieceMacroCasterAxialOffsetBase(Spell spell, boolean includePitch) {
        super(spell);
        this.includePitch = includePitch;
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(4));
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

        Map<String, SpellParam.Side> savedSides = saveParamSides();
        mode = nextMode;
        rebuildParams(savedSides);
    }

    @Override
    public void drawAdditional(PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        ModeOverlayRenderer.drawModeOverlay(poseStack, bufferSource, light, getModeOption());
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

    private boolean isVectorMode() {
        return getModeOption().id().equals(PsitweaksModeOptions.VECTOR.id());
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        if (isVectorMode()) {
            addParam(offset = new ParamVector(PsitweaksSpellParams.OFFSET, SpellParam.CYAN, true, false));
        } else {
            addParam(leftRight = new ParamNumber(PsitweaksSpellParams.LEFT_RIGHT, SpellParam.RED, true, false));
            addParam(forwardBackward = new ParamNumber(PsitweaksSpellParams.FORWARD_BACKWARD, SpellParam.GREEN, true, false));
            addParam(upDown = new ParamNumber(PsitweaksSpellParams.UP_DOWN, SpellParam.PURPLE, true, false));
        }
        restoreParamSides(savedSides);
    }

    private Map<String, SpellParam.Side> saveParamSides() {
        Map<String, SpellParam.Side> savedSides = new LinkedHashMap<>();
        paramSides.forEach((param, side) -> savedSides.put(param.name, side));
        return savedSides;
    }

    private void restoreParamSides(Map<String, SpellParam.Side> savedSides) {
        if (savedSides == null) {
            return;
        }

        for (SpellParam<?> param : params.values()) {
            SpellParam.Side side = savedSides.get(param.name);
            if (side != null) {
                paramSides.put(param, side);
            }
        }
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 4);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        CasterAxialBasis basis = getBasis(context);

        if (isVectorMode()) {
            Vector3 offsetVal = getParamValue(context, offset);
            Vector3 result = positionVal.copy();
            if (offsetVal != null) {
                addBasisOffset(result, basis, offsetVal.x, offsetVal.z, offsetVal.y);
            }
            return result;
        }

        double leftRightVal = numberOrZero(getParamValue(context, leftRight));
        double forwardBackwardVal = numberOrZero(getParamValue(context, forwardBackward));
        double upDownVal = numberOrZero(getParamValue(context, upDown));

        Vector3 result = positionVal.copy();
        addBasisOffset(result, basis, leftRightVal, forwardBackwardVal, upDownVal);
        return result;
    }

    protected CasterAxialBasis getBasis(SpellContext context) throws SpellRuntimeException {
        return includePitch
                ? CasterAxialBasis.of3D(context.caster)
                : CasterAxialBasis.of2D(context.caster);
    }

    private static void addBasisOffset(Vector3 result, CasterAxialBasis basis, double leftRight, double forwardBackward, double upDown) {
        if (leftRight != 0) {
            result.add(Vector3.fromDirection(basis.right).multiply(leftRight));
        }
        if (forwardBackward != 0) {
            result.add(Vector3.fromDirection(basis.forward).multiply(forwardBackward));
        }
        if (upDown != 0) {
            result.add(Vector3.fromDirection(basis.up).multiply(upDown));
        }
    }

    private static double numberOrZero(Number value) {
        return value == null ? 0.0D : value.doubleValue();
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.vector3");
    }
}
