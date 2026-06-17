package com.moratan251.psitweaks.api.value;

import net.minecraft.core.BlockPos;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;

public final class BlockValueHelper {
    private BlockValueHelper() {
    }

    public static boolean canAcceptBlockInput(SpellPiece piece, boolean requiresConstant) {
        if (piece == null) {
            return false;
        }
        if (requiresConstant && piece.getPieceType() != EnumPieceType.CONSTANT) {
            return false;
        }

        Class<?> evaluationType = piece.getEvaluationType();
        if (evaluationType == null) {
            return false;
        }
        return BlockValue.class.isAssignableFrom(evaluationType)
                || Vector3.class.isAssignableFrom(evaluationType);
    }

    public static BlockValue getBlockValue(SpellPiece piece, SpellContext context, SpellParam<?> param)
            throws SpellRuntimeException {
        Object value = piece.getParamValue(context, typed(param));
        return coerce(context, value);
    }

    public static BlockValue coerce(SpellContext context, Object value) throws SpellRuntimeException {
        if (value instanceof BlockValue block) {
            return block;
        }
        if (value instanceof Vector3 vector) {
            return snapshotVector(context, vector);
        }
        throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
    }

    public static BlockValue snapshotVector(SpellContext context, Vector3 vector) throws SpellRuntimeException {
        if (vector == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(vector)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        BlockPos pos = vector.toBlockPos();
        return BlockValue.snapshot(context.focalPoint.level(), pos);
    }

    @SuppressWarnings("unchecked")
    private static <T> SpellParam<T> typed(SpellParam<?> param) {
        return (SpellParam<T>) param;
    }
}
