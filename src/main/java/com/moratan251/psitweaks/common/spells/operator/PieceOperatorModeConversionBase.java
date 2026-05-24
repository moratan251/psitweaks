package com.moratan251.psitweaks.common.spells.operator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.client.spells.ModeOverlayRenderer;
import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.piece.PieceOperator;

public abstract class PieceOperatorModeConversionBase extends PieceOperator implements ModeConfigurableSpellPiece {
    private static final String TAG_MODE = "psitweaksMode";
    private static final ListElementMode[] CONVERSION_MODES = {
            ListElementMode.ENTITY,
            ListElementMode.ITEM,
            ListElementMode.VECTOR,
            ListElementMode.NUMBER
    };

    private ListElementMode mode = ListElementMode.ENTITY;

    protected PieceOperatorModeConversionBase(Spell spell) {
        super(spell);
    }

    @Override
    public final void initParams() {
        rebuildParams(null);
    }

    @Override
    public final ListElementMode[] getAvailableElementModes() {
        return availableElementModes().clone();
    }

    @Override
    public final ListElementMode getElementMode() {
        return currentMode();
    }

    @Override
    public final void setElementMode(ListElementMode mode) {
        ListElementMode nextMode = normalizeElementMode(mode);
        if (nextMode == currentMode()) {
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
        mode = normalizeElementMode(ListElementMode.byId(tag.getString(TAG_MODE)));
        rebuildParams(null);
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, currentMode().id());
    }

    protected final ListElementMode currentMode() {
        return normalizeElementMode(mode);
    }

    protected ListElementMode[] availableElementModes() {
        return CONVERSION_MODES;
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
}
