package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public abstract class PieceOperatorRegionFilterBase extends PieceOperator {
    private static final double EPSILON = 1e-12;

    private SpellParam<EntityListWrapper> entities;
    private SpellParam<MatrixValue> region;

    public PieceOperatorRegionFilterBase(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(entities = new ParamEntityListWrapper(SpellParam.GENERIC_NAME_LIST, SpellParam.BLUE, false, false));
        addParam(region = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        EntityListWrapper source = getNotNullParamValue(context, entities);
        MatrixValue matrix = getNotNullParamValue(context, region);

        Region region = parseRegion(matrix);
        List<Entity> result = new ArrayList<>();
        for (Entity entity : source) {
            if (entity == null) {
                continue;
            }
            boolean inside = isInside(region, entityPosition(entity));
            if (inside == keepInside()) {
                result.add(entity);
            }
        }
        return EntityListWrapper.make(result);
    }

    @Override
    public Class<?> getEvaluationType() {
        return EntityListWrapper.class;
    }

    protected abstract boolean keepInside();

    private static Region parseRegion(MatrixValue matrix) throws SpellRuntimeException {
        if ((matrix.rows() != 3 && matrix.rows() != 4) || matrix.cols() != 4) {
            throw new SpellRuntimeException(MassBlockBreakHelper.ERROR_INVALID_MATRIX);
        }

        Vector3 edge0 = new Vector3(matrix.get(0, 0), matrix.get(1, 0), matrix.get(2, 0));
        Vector3 edge1 = new Vector3(matrix.get(0, 1), matrix.get(1, 1), matrix.get(2, 1));
        Vector3 edge2 = new Vector3(matrix.get(0, 2), matrix.get(1, 2), matrix.get(2, 2));
        Vector3 start = new Vector3(matrix.get(0, 3), matrix.get(1, 3), matrix.get(2, 3));

        MatrixValue edgeMatrix = MatrixOperations.checkedMatrix(3, 3, new double[]{
                edge0.x, edge1.x, edge2.x,
                edge0.y, edge1.y, edge2.y,
                edge0.z, edge1.z, edge2.z
        });

        MatrixValue inverseMatrix;
        try {
            inverseMatrix = MatrixOperations.inverse(edgeMatrix);
        } catch (SpellRuntimeException e) {
            throw new SpellRuntimeException(MassBlockBreakHelper.ERROR_DEGENERATE_EDGES);
        }

        return new Region(start, inverseMatrix);
    }

    private static Vector3 entityPosition(Entity entity) {
        AABB box = entity.getBoundingBox();
        Vec3 center = box.getCenter();
        return new Vector3(center.x, center.y, center.z);
    }

    private static boolean isInside(Region region, Vector3 position) throws SpellRuntimeException {
        double dx = position.x - region.start().x;
        double dy = position.y - region.start().y;
        double dz = position.z - region.start().z;

        MatrixValue inverse = region.inverseMatrix();
        double u = inverse.get(0, 0) * dx + inverse.get(0, 1) * dy + inverse.get(0, 2) * dz;
        double v = inverse.get(1, 0) * dx + inverse.get(1, 1) * dy + inverse.get(1, 2) * dz;
        double w = inverse.get(2, 0) * dx + inverse.get(2, 1) * dy + inverse.get(2, 2) * dz;

        return u >= -EPSILON && u <= 1.0 + EPSILON
                && v >= -EPSILON && v <= 1.0 + EPSILON
                && w >= -EPSILON && w <= 1.0 + EPSILON;
    }

    private record Region(Vector3 start, MatrixValue inverseMatrix) {
    }
}
