package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamBlockListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import com.moratan251.psitweaks.common.spells.util.RankedSelection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.SpatialHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * Sorts an Entity, Vector or Block List by the distance from a position, closest first. Entities are measured at
 * their feet with Psi's {@link SpatialHelper}, as Operator: Closest to Point does, so the closest element is the one
 * that piece picks. Blocks are measured at their center. Elements at the same distance keep their input order.
 */
public abstract class PieceOperatorListDistanceBase extends PieceOperator implements PsitweaksModeConfigurable {
    private static final String TAG_MODE = "psitweaksMode";
    private static final List<PsitweaksModeOption> MODES = List.of(
            PsitweaksModeOptions.ENTITY, PsitweaksModeOptions.VECTOR, PsitweaksModeOptions.BLOCK);

    private PsitweaksModeOption mode = PsitweaksModeOptions.ENTITY;
    private SpellParam<EntityListWrapper> entities;
    private SpellParam<VectorListWrapper> vectors;
    private SpellParam<BlockListWrapper> blocks;
    private SpellParam<Vector3> position;
    private SpellParam<Number> number;

    protected PieceOperatorListDistanceBase(Spell spell) {
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

    protected final boolean isMode(PsitweaksModeOption option) {
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
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.GREEN, false, false));
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.RED, false, false));

        if (savedSides != null) {
            for (SpellParam<?> param : params.values()) {
                SpellParam.Side side = savedSides.get(param.name);
                if (side != null) {
                    paramSides.put(param, side);
                }
            }
        }
    }

    protected final double numberValue(SpellContext context) throws SpellRuntimeException {
        return getNotNullParamValue(context, number).doubleValue();
    }

    /** The elements of the List input, closest to the position first. */
    protected final List<?> sortedByDistance(SpellContext context) throws SpellRuntimeException {
        Ranked ranked = byDistance(context);
        return RankedSelection.ascending(ranked.elements(), ranked.distances());
    }

    /** {@link RankedSelection#keep}: the {@code count} closest, or the -{@code count} farthest, to the position. */
    protected final List<?> keepByDistance(SpellContext context, double count) throws SpellRuntimeException {
        Ranked ranked = byDistance(context);
        return RankedSelection.keep(ranked.elements(), ranked.distances(), count);
    }

    /** The elements of the List input in its order, with their squared distances to the position. */
    private Ranked byDistance(SpellContext context) throws SpellRuntimeException {
        Vector3 origin = getNotNullParamValue(context, position);
        Level level = context.focalPoint == null ? null : context.focalPoint.level();
        List<Object> elements = new ArrayList<>();
        List<Vec3> points = new ArrayList<>();
        if (isMode(PsitweaksModeOptions.VECTOR)) {
            for (Vector3 vector : getNotNullParamValue(context, vectors)) {
                elements.add(vector);
                points.add(new Vec3(vector.x, vector.y, vector.z));
            }
        } else if (isMode(PsitweaksModeOptions.BLOCK)) {
            for (BlockValue block : getNotNullParamValue(context, blocks)) {
                elements.add(block);
                points.add(block.blockPos().getCenter());
            }
        } else {
            for (Entity entity : getNotNullParamValue(context, entities)) {
                if (entity != null) {
                    elements.add(entity);
                    points.add(entity.position());
                }
            }
        }

        double[] distances = new double[points.size()];
        for (int i = 0; i < distances.length; i++) {
            distances[i] = distanceSquared(level, origin, points.get(i));
        }
        return new Ranked(elements, distances);
    }

    private record Ranked(List<Object> elements, double[] distances) {
    }

    /** A List of the current mode with the given elements, in that order (an Entity List takes Psi's own order). */
    @SuppressWarnings("unchecked")
    protected final Object makeList(List<?> elements) {
        if (isMode(PsitweaksModeOptions.VECTOR)) {
            return VectorListWrapper.make((List<Vector3>) elements);
        }
        if (isMode(PsitweaksModeOptions.BLOCK)) {
            return BlockListWrapper.make((List<BlockValue>) elements);
        }
        return EntityListWrapper.make((List<Entity>) elements);
    }

    protected final Class<?> listType() {
        if (isMode(PsitweaksModeOptions.VECTOR)) {
            return VectorListWrapper.class;
        }
        return isMode(PsitweaksModeOptions.BLOCK) ? BlockListWrapper.class : EntityListWrapper.class;
    }

    protected final Class<?> elementType() {
        if (isMode(PsitweaksModeOptions.VECTOR)) {
            return Vector3.class;
        }
        return isMode(PsitweaksModeOptions.BLOCK) ? BlockValue.class : Entity.class;
    }

    private static double distanceSquared(Level level, Vector3 origin, Vec3 point) {
        if (level == null) {
            return point.distanceToSqr(origin.x, origin.y, origin.z);
        }
        return SpatialHelper.distanceSquared(level, origin.x, origin.y, origin.z, point);
    }
}
