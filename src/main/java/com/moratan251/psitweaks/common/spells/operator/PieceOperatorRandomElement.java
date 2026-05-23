package com.moratan251.psitweaks.common.spells.operator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.client.spells.ModeOverlayRenderer;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamStringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public class PieceOperatorRandomElement extends PieceOperator implements ModeConfigurableSpellPiece {
    private static final String TAG_MODE = "psitweaksMode";

    private ListElementMode mode = ListElementMode.STRING;
    private SpellParam<?> list;

    public PieceOperatorRandomElement(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        rebuildParams(null);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        return switch (currentMode()) {
            case STRING -> randomString(context);
            case ENTITY -> randomEntity(context);
            case ITEM -> randomItem(context);
        };
    }

    @Override
    public Class<?> getEvaluationType() {
        return switch (currentMode()) {
            case STRING -> String.class;
            case ENTITY -> Entity.class;
            case ITEM -> SpellItemValue.class;
        };
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().elementTranslationKey());
    }

    @Override
    public ListElementMode getElementMode() {
        return currentMode();
    }

    @Override
    public void setElementMode(ListElementMode mode) {
        ListElementMode nextMode = mode == null ? ListElementMode.STRING : mode;
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
        mode = ListElementMode.byId(tag.getString(TAG_MODE));
        rebuildParams(null);
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, currentMode().id());
    }

    private Object randomString(SpellContext context) throws SpellRuntimeException {
        StringListWrapper source = getNotNullParamValue(context, typedList());
        if (source.size() == 0) {
            return "";
        }

        return source.get(context.caster.getRandom().nextInt(source.size()));
    }

    private Object randomEntity(SpellContext context) throws SpellRuntimeException {
        EntityListWrapper source = getNotNullParamValue(context, typedList());
        if (source.size() == 0) {
            return null;
        }

        return source.get(context.caster.getRandom().nextInt(source.size()));
    }

    private Object randomItem(SpellContext context) throws SpellRuntimeException {
        SpellItemListWrapper source = getNotNullParamValue(context, typedList());
        if (source.size() == 0) {
            return SpellItemValue.EMPTY;
        }

        return source.get(context.caster.getRandom().nextInt(source.size()));
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();

        list = switch (currentMode()) {
            case STRING -> new ParamStringListWrapper(SpellParam.GENERIC_NAME_LIST,
                    PsitweaksSpellParams.STRING_LIST_COLOR, false, false);
            case ENTITY -> new ParamEntityListWrapper(SpellParam.GENERIC_NAME_LIST, SpellParam.BLUE, false, false);
            case ITEM -> new ParamSpellItemListWrapper(SpellParam.GENERIC_NAME_LIST,
                    PsitweaksSpellParams.ITEM_LIST_COLOR, false, false);
        };
        addParam(list);

        if (savedSides != null) {
            SpellParam.Side side = savedSides.get(list.name);
            if (side != null) {
                paramSides.put(list, side);
            }
        }
    }

    private Map<String, SpellParam.Side> saveParamSides() {
        Map<String, SpellParam.Side> savedSides = new LinkedHashMap<>();
        paramSides.forEach((param, side) -> savedSides.put(param.name, side));
        return savedSides;
    }

    private ListElementMode currentMode() {
        return mode == null ? ListElementMode.STRING : mode;
    }

    @SuppressWarnings("unchecked")
    private <T> SpellParam<T> typedList() {
        return (SpellParam<T>) list;
    }
}
