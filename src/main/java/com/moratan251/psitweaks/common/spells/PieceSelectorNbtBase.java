package com.moratan251.psitweaks.common.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import com.moratan251.psitweaks.api.PsitweaksModeOptions;
import com.moratan251.psitweaks.api.PsitweaksValueKind;
import com.moratan251.psitweaks.client.spells.ModeOverlayRenderer;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

abstract class PieceSelectorNbtBase extends PieceSelector implements ModeConfigurableSpellPiece {
    private static final String TAG_MODE = "psitweaksMode";
    private static final String TARGET_PARAM = "psi.spellparam.target";

    private PsitweaksModeOption mode = PsitweaksModeOptions.ENTITY;
    private SpellParam<?> target;

    protected PieceSelectorNbtBase(Spell spell) {
        super(spell);
    }

    @Override
    public final void initParams() {
        rebuildParams(null);
    }

    @Override
    public final List<PsitweaksModeOption> getAvailableModeOptions() {
        return PsitweaksListAdapters.modeOptions(PsitweaksValueKind.CONTEXTUAL);
    }

    @Override
    public final PsitweaksModeOption getModeOption() {
        return currentMode();
    }

    @Override
    public final void setModeOption(PsitweaksModeOption mode) {
        PsitweaksModeOption nextMode = normalizeModeOption(mode);
        if (sameMode(nextMode, currentMode())) {
            return;
        }

        Map<String, SpellParam.Side> savedSides = saveParamSides();
        this.mode = nextMode;
        rebuildParams(savedSides);
    }

    @Override
    public void drawAdditional(PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        ModeOverlayRenderer.drawModeOverlay(poseStack, bufferSource, light, currentMode());
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
        tag.putString(TAG_MODE, currentMode().serializedId());
    }

    protected final NbtTargetHelper.TargetData getTargetData(SpellContext context) throws SpellRuntimeException {
        Object targetValue = getNotNullParamValue(context, typed(target));
        Object coercedTarget = currentAdapter().coerceElement(context, targetValue);
        return NbtTargetHelper.getNbt(context, coercedTarget).orElse(NbtTargetHelper.empty());
    }

    protected void addAdditionalParams() {
    }

    protected final PsitweaksModeOption currentMode() {
        return normalizeModeOption(mode);
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(target = currentAdapter().createElementParam(TARGET_PARAM, false));
        addAdditionalParams();
        restoreParamSides(savedSides);
    }

    private PsitweaksListAdapter<Object> currentAdapter() {
        return PsitweaksListAdapters.findModeAdapter(currentMode()).orElseThrow(
                () -> new IllegalStateException("No PsiTweaks list adapter registered for mode " + currentMode().id())
        );
    }

    private void clearModeParams() {
        params.clear();
        paramSides.clear();
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

    @SuppressWarnings("unchecked")
    private static <T> SpellParam<T> typed(SpellParam<?> param) {
        return (SpellParam<T>) param;
    }

    private Map<String, SpellParam.Side> saveParamSides() {
        Map<String, SpellParam.Side> savedSides = new LinkedHashMap<>();
        paramSides.forEach((param, side) -> savedSides.put(param.name, side));
        return savedSides;
    }

    private static boolean sameMode(PsitweaksModeOption left, PsitweaksModeOption right) {
        return left != null && right != null && left.id().equals(right.id());
    }
}
