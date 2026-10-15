package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamBlockListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * Filters a list by a region matrix. Entities are tested at their bounding box center, vectors as given,
 * and blocks at their block position, the same lattice points that Operator: Region Vector List returns.
 */
public abstract class PieceOperatorRegionFilterBase extends PsitweaksPieceOperator implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";
    // Entity first: spells saved before the mode menu existed load as Entity List filters.
    private static final List<PsitweaksModeOption> MODES = List.of(
            PsitweaksModeOptions.ENTITY, PsitweaksModeOptions.VECTOR, PsitweaksModeOptions.BLOCK);

    private PsitweaksModeOption mode = PsitweaksModeOptions.ENTITY;
    private SpellParam<EntityListWrapper> entities;
    private SpellParam<VectorListWrapper> vectors;
    private SpellParam<BlockListWrapper> blocks;
    private SpellParam<MatrixValue> region;

    public PieceOperatorRegionFilterBase(Spell spell) {
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

    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    @Override
    public void drawAdditional(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        com.moratan251.psitweaks.client.spells.ModeOverlayRenderer.drawModeOverlay(pose, buffers, light, getModeOption());
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

    private boolean isMode(PsitweaksModeOption option) {
        return getModeOption().id().equals(option.id());
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        // Every mode uses the same list param name so a mode change keeps the wire.
        if (isMode(PsitweaksModeOptions.VECTOR)) {
            addParam(vectors = new ParamVectorListWrapper(SpellParam.GENERIC_NAME_LIST,
                    PsitweaksSpellParams.VECTOR_LIST_COLOR, false, false));
        } else if (isMode(PsitweaksModeOptions.BLOCK)) {
            addParam(blocks = new ParamBlockListWrapper(SpellParam.GENERIC_NAME_LIST,
                    PsitweaksSpellParams.BLOCK_LIST_COLOR, false, false));
        } else {
            addParam(entities = new ParamEntityListWrapper(SpellParam.GENERIC_NAME_LIST, SpellParam.BLUE, false, false));
        }
        addParam(region = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, false, false));

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
        if (isMode(PsitweaksModeOptions.VECTOR)) {
            VectorListWrapper source = getNotNullParamValue(context, vectors);
            RegionMatrix area = RegionMatrix.parse(getNotNullParamValue(context, region));
            List<Vector3> result = new ArrayList<>();
            for (Vector3 vector : source) {
                if (area.contains(vector) == keepInside()) {
                    result.add(vector);
                }
            }
            return VectorListWrapper.make(result);
        }

        if (isMode(PsitweaksModeOptions.BLOCK)) {
            BlockListWrapper source = getNotNullParamValue(context, blocks);
            RegionMatrix area = RegionMatrix.parse(getNotNullParamValue(context, region));
            List<BlockValue> result = new ArrayList<>();
            for (BlockValue block : source) {
                if (area.contains(block.positionVector()) == keepInside()) {
                    result.add(block);
                }
            }
            return BlockListWrapper.make(result);
        }

        EntityListWrapper source = getNotNullParamValue(context, entities);
        RegionMatrix area = RegionMatrix.parse(getNotNullParamValue(context, region));
        List<Entity> result = new ArrayList<>();
        for (Entity entity : source) {
            if (entity == null) {
                continue;
            }
            Vec3 center = entity.getBoundingBox().getCenter();
            if (area.contains(new Vector3(center.x, center.y, center.z)) == keepInside()) {
                result.add(entity);
            }
        }
        return EntityListWrapper.make(result);
    }

    @Override
    public Class<?> getEvaluationType() {
        if (isMode(PsitweaksModeOptions.VECTOR)) {
            return VectorListWrapper.class;
        }
        return isMode(PsitweaksModeOptions.BLOCK) ? BlockListWrapper.class : EntityListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(getModeOption().listTranslationKey());
    }

    protected abstract boolean keepInside();
}
