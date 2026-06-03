package com.moratan251.psitweaks.common.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import com.moratan251.psitweaks.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaks.api.PsitweaksModeOptions;
import com.moratan251.psitweaks.api.PsitweaksPlainValues;
import com.moratan251.psitweaks.api.value.PlainValueType;
import com.moratan251.psitweaks.client.spells.ModeOverlayRenderer;
import com.moratan251.psitweaks.common.spells.memory.CadPlainMemory;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorStoredValue extends PieceSelector implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";

    private SpellParam<Number> number;
    private PlainValueType<?> type = PsitweaksPlainValues.STRING;

    public PieceSelectorStoredValue(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.GREEN, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double numberVal = (Double) getParamEvaluation(number);
        if (numberVal != null && numberVal > 0D && numberVal == numberVal.intValue()) {
            meta.addStat(EnumSpellStat.POTENCY, numberVal.intValue() * 6);
        } else {
            throw new SpellCompilationException("psi.spellerror.nonpositiveinteger", x, y);
        }
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int internalSlot = CadPlainMemory.internalSlot(getParamValue(context, number));
        if (CadPlainMemory.isSlotLocked(context, internalSlot)) {
            throw new SpellRuntimeException("psi.spellerror.lockedmemory");
        }
        return CadPlainMemory.read(context, internalSlot, currentType());
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentType().valueClass();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentType().modeOption().elementTranslationKey());
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return currentType().modeOption();
    }

    @Override
    public void setModeOption(PsitweaksModeOption mode) {
        type = normalizeType(PsitweaksPlainValues.byMode(mode).orElse(null));
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return PsitweaksPlainValues.snapshot().stream()
                .map(PlainValueType::modeOption)
                .toList();
    }

    @Override
    public void drawAdditional(PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        ModeOverlayRenderer.drawModeOverlay(poseStack, bufferSource, light, getModeOption());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        type = normalizeType(PsitweaksPlainValues.byMode(PsitweaksModeOptions.byId(tag.getString(TAG_MODE))
                        .orElse(null))
                .orElse(null));
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, currentType().modeOption().serializedId());
    }

    private PlainValueType<?> currentType() {
        type = normalizeType(type);
        return type;
    }

    private PlainValueType<?> normalizeType(PlainValueType<?> candidate) {
        if (candidate != null && PsitweaksPlainValues.snapshot().contains(candidate)) {
            return candidate;
        }
        List<PlainValueType<?>> types = PsitweaksPlainValues.snapshot();
        return types.isEmpty() ? PsitweaksPlainValues.STRING : types.get(0);
    }

}
