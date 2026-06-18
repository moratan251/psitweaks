package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import com.moratan251.psitweaks.api.PsitweaksModeOptions;
import com.moratan251.psitweaks.api.PsitweaksPlainValues;
import com.moratan251.psitweaks.api.PsitweaksValueKind;
import com.moratan251.psitweaks.api.value.PlainValueType;
import com.moratan251.psitweaks.client.spells.ModeOverlayRenderer;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.piece.PieceOperator;

public abstract class PieceOperatorModeConversionBase extends PieceOperator implements ModeConfigurableSpellPiece {
    private static final String TAG_MODE = "psitweaksMode";

    private PsitweaksModeOption mode = PsitweaksModeOptions.STRING;

    protected PieceOperatorModeConversionBase(Spell spell) {
        super(spell);
    }

    @Override
    public final void initParams() {
        rebuildParams(null);
    }

    @Override
    public final List<PsitweaksModeOption> getAvailableModeOptions() {
        return availableModeOptions();
    }

    protected List<PsitweaksModeOption> availableModeOptions() {
        PsitweaksValueKind valueKind = modeOptionKindFilter();
        return valueKind == null ? PsitweaksListAdapters.modeOptions() : PsitweaksListAdapters.modeOptions(valueKind);
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

    protected final PsitweaksModeOption currentMode() {
        return normalizeModeOption(mode);
    }

    protected final PsitweaksListAdapter<Object> currentAdapter() {
        return PsitweaksListAdapters.findModeAdapter(currentMode()).orElseThrow(
                () -> new IllegalStateException("No PsiTweaks list adapter registered for mode " + currentMode().id())
        );
    }

    protected final Class<?> currentListType() {
        return PsitweaksListAdapters.listType(currentMode()).orElse(Object.class);
    }

    protected final PlainValueType<?> currentPlainType() {
        return PsitweaksPlainValues.byMode(currentMode()).orElseThrow(
                () -> new IllegalStateException("No PsiTweaks plain value type registered for mode " + currentMode().id())
        );
    }

    protected PsitweaksValueKind modeOptionKindFilter() {
        return null;
    }

    protected abstract void rebuildParams(Map<String, SpellParam.Side> savedSides);

    protected final void clearModeParams() {
        params.clear();
        paramSides.clear();
    }

    protected final void restoreParamSides(Map<String, SpellParam.Side> savedSides) {
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
    protected final <T> SpellParam<T> typed(SpellParam<?> param) {
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
